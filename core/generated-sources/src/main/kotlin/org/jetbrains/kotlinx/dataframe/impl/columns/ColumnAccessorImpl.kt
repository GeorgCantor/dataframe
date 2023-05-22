package org.jetbrains.kotlinx.dataframe.impl.columns

import org.jetbrains.kotlinx.dataframe.AnyCol
import org.jetbrains.kotlinx.dataframe.AnyRow
import org.jetbrains.kotlinx.dataframe.api.asColumnGroup
import org.jetbrains.kotlinx.dataframe.api.cast
import org.jetbrains.kotlinx.dataframe.api.isColumnGroup
import org.jetbrains.kotlinx.dataframe.api.toPath
import org.jetbrains.kotlinx.dataframe.columns.*

internal class ColumnAccessorImpl<T>(val path: ColumnPath) : ColumnAccessor<T> {

    override fun name() = path.last()

    override fun path() = path

    constructor(vararg path: String) : this(path.toPath())

    override fun resolveSingle(context: ColumnResolutionContext): ColumnWithPath<T>? {
        var df = context.df
        var col: AnyCol? = null
        for (colName in path) {
            col = df.getColumn<Any?>(colName, context.unresolvedColumnsPolicy) ?: return null
            if (col.isColumnGroup()) {
                df = col.asColumnGroup()
            }
        }
        return col?.cast<T>()?.addPath(path)
    }

    override fun rename(newName: String) = ColumnAccessorImpl<T>(path.dropLast(1) + newName)

    override fun <C> get(column: ColumnReference<C>) = ColumnAccessorImpl<C>(path + column.path())

    override fun getValue(row: AnyRow) = path.getValue(row) as T

    override fun getValueOrNull(row: AnyRow) = path.getValueOrNull(row) as T
}
