package org.jetbrains.kotlinx.dataframe.impl.api

import org.jetbrains.kotlinx.dataframe.ColumnSelector
import org.jetbrains.kotlinx.dataframe.ColumnsSelector
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.GroupBy
import org.jetbrains.kotlinx.dataframe.api.asGroupBy
import org.jetbrains.kotlinx.dataframe.api.filter
import org.jetbrains.kotlinx.dataframe.columns.UnresolvedColumnsPolicy
import org.jetbrains.kotlinx.dataframe.impl.columns.isMissingColumn
import org.jetbrains.kotlinx.dataframe.impl.getColumnsImpl

internal fun <T, C> DataFrame<T>.xsImpl(
    keyColumns: ColumnsSelector<T, C>,
    allowMissingColumns: Boolean,
    vararg keyValues: C
): DataFrame<T> {
    val cols = getColumnsImpl(if (allowMissingColumns) UnresolvedColumnsPolicy.Create else UnresolvedColumnsPolicy.Fail, keyColumns)
    val n = keyValues.count()
    require(cols.size == n) { "Number of key values $n doesn't equal to number of key columns ${cols.size}" }
    val pairs = cols.zip(keyValues).filter { !it.first.isMissingColumn() }
    return filter {
        val rowIndex = index()
        var include = true
        for ((col, value) in pairs) {
            if (col[rowIndex] != value) {
                include = false
                break
            }
        }
        include
    }.removeImpl(keyColumns, allowMissingColumns).df
}

internal fun <T, G, C> GroupBy<T, G>.xsImpl(vararg keyValues: C, keyColumns: ColumnsSelector<T, C>): GroupBy<T, G> {
    return toDataFrame().xsImpl(keyColumns, true, *keyValues).asGroupBy(groups)
        .mapGroups { it.xsImpl(keyColumns as ColumnSelector<G, C>, true, *keyValues) }
}
