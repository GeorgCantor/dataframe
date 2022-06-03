package org.jetbrains.kotlinx.dataframe.impl.aggregation

import org.jetbrains.kotlinx.dataframe.api.name
import org.jetbrains.kotlinx.dataframe.columns.ColumnPath
import org.jetbrains.kotlinx.dataframe.columns.ColumnResolutionContext
import org.jetbrains.kotlinx.dataframe.columns.ColumnSet
import org.jetbrains.kotlinx.dataframe.columns.ColumnWithPath
import org.jetbrains.kotlinx.dataframe.columns.shortPath

internal class ConfiguredAggregateColumn<C> private constructor(
    val columns: ColumnSet<C>,
    private val default: C? = null,
    private val newPath: ColumnPath? = null
) : ColumnSet<C> {

    private fun ColumnWithPath<C>.toDescriptor(keepName: Boolean) = when (val col = this) {
        is AggregateColumnDescriptor<C> -> {
            val path = if (keepName) newPath?.plus(col.newPath ?: col.column.shortPath()) ?: col.newPath
            else newPath ?: col.newPath
            AggregateColumnDescriptor(col.column, default ?: col.default, path)
        }
        else -> AggregateColumnDescriptor(col, default, if (keepName) newPath?.plus(col.name) else newPath)
    }

    override fun resolve(context: ColumnResolutionContext): List<ColumnWithPath<C>> {
        val resolved = columns.resolve(context)
        if (resolved.size == 1) return listOf(resolved[0].toDescriptor(false))
        else return resolved.map {
            it.toDescriptor(true)
        }
    }

    companion object {

        fun <C> withDefault(src: ColumnSet<C>, default: C?): ColumnSet<C> = when (src) {
            is ConfiguredAggregateColumn<C> -> ConfiguredAggregateColumn(src.columns, default, src.newPath)
            else -> ConfiguredAggregateColumn(src, default, null)
        }

        fun <C> withPath(src: ColumnSet<C>, newPath: ColumnPath): ColumnSet<C> = when (src) {
            is ConfiguredAggregateColumn<C> -> ConfiguredAggregateColumn(src.columns, src.default, newPath)
            else -> ConfiguredAggregateColumn(src, null, newPath)
        }
    }
}
