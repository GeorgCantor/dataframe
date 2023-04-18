package org.jetbrains.kotlinx.dataframe.impl.columns

import org.jetbrains.kotlinx.dataframe.columns.ColumnResolutionContext
import org.jetbrains.kotlinx.dataframe.columns.ColumnSet
import org.jetbrains.kotlinx.dataframe.columns.ColumnSetTransformer
import org.jetbrains.kotlinx.dataframe.columns.ColumnWithPath

internal class ColumnsList<C>(val columns: List<ColumnSet<C>>) : ColumnSet<C> {
    constructor(vararg columns: ColumnSet<C>) : this(columns.toList())

    override fun resolve(context: ColumnResolutionContext) =
        columns.flatMap { it.resolve(context) }

    override fun resolveAfterTransform(
        context: ColumnResolutionContext,
        transformer: ColumnSetTransformer,
    ): List<ColumnWithPath<C>> =
        columns.flatMap { it.resolveAfterTransform(context, transformer) }
}
