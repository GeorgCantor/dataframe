package org.jetbrains.kotlinx.dataframe.impl

import org.jetbrains.kotlinx.dataframe.ColumnSelector
import org.jetbrains.kotlinx.dataframe.ColumnsSelector
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.Selector
import org.jetbrains.kotlinx.dataframe.aggregation.AggregateGroupedBody
import org.jetbrains.kotlinx.dataframe.aggregation.NamedValue
import org.jetbrains.kotlinx.dataframe.api.GroupKey
import org.jetbrains.kotlinx.dataframe.api.GroupedDataFrame
import org.jetbrains.kotlinx.dataframe.api.GroupedRowFilter
import org.jetbrains.kotlinx.dataframe.api.asGroupedDataFrame
import org.jetbrains.kotlinx.dataframe.api.cast
import org.jetbrains.kotlinx.dataframe.api.concat
import org.jetbrains.kotlinx.dataframe.api.convert
import org.jetbrains.kotlinx.dataframe.api.filter
import org.jetbrains.kotlinx.dataframe.api.frameColumn
import org.jetbrains.kotlinx.dataframe.api.getColumn
import org.jetbrains.kotlinx.dataframe.api.getColumnsWithPaths
import org.jetbrains.kotlinx.dataframe.api.into
import org.jetbrains.kotlinx.dataframe.api.minus
import org.jetbrains.kotlinx.dataframe.api.name
import org.jetbrains.kotlinx.dataframe.api.rename
import org.jetbrains.kotlinx.dataframe.columns.FrameColumn
import org.jetbrains.kotlinx.dataframe.columns.values
import org.jetbrains.kotlinx.dataframe.frameColumn
import org.jetbrains.kotlinx.dataframe.impl.aggregation.AggregatableInternal
import org.jetbrains.kotlinx.dataframe.impl.aggregation.GroupByReceiverImpl
import org.jetbrains.kotlinx.dataframe.impl.aggregation.receivers.AggregateBodyInternal
import org.jetbrains.kotlinx.dataframe.impl.api.AggregatedPivot
import org.jetbrains.kotlinx.dataframe.impl.api.ColumnToInsert
import org.jetbrains.kotlinx.dataframe.impl.api.GroupedDataRowImpl
import org.jetbrains.kotlinx.dataframe.impl.api.insertImpl
import org.jetbrains.kotlinx.dataframe.impl.api.removeImpl
import org.jetbrains.kotlinx.dataframe.impl.columns.toColumns
import org.jetbrains.kotlinx.dataframe.pathOf
import org.jetbrains.kotlinx.dataframe.values

internal class GroupedDataFrameImpl<T, G>(
    val df: DataFrame<T>,
    override val groups: FrameColumn<G>,
    private val keyColumnsInGroups: ColumnsSelector<G, *>
) :
    GroupedDataFrame<T, G>,
    AggregatableInternal<G> {

    override val keys by lazy { df - groups }

    override operator fun get(key: GroupKey): DataFrame<T> {
        require(key.size < df.ncol()) { "Invalid size of the key" }

        val keySize = key.size
        val filtered = df.filter { it.values.subList(0, keySize) == key }
        return filtered.frameColumn(groups.name()).values.concat().cast<T>()
    }

    override fun <R> mapGroups(transform: Selector<DataFrame<G>, DataFrame<R>>) =
        df.convert(groups) { transform(it, it) }.asGroupedDataFrame(groups.name()) as GroupedDataFrame<T, R>

    override fun toDataFrame(groupedColumnName: String?) = if (groupedColumnName == null || groupedColumnName == groups.name()) df else df.rename(groups).into(groupedColumnName)

    override fun toString() = df.toString()

    override fun remainingColumnsSelector(): ColumnsSelector<*, *> = { all().except(keyColumnsInGroups.toColumns()) }

    override fun <R> aggregate(body: AggregateGroupedBody<G, R>) = aggregateGroupBy(toDataFrame(), { groups }, removeColumns = true, body).cast<G>()

    override fun <R> aggregateInternal(body: AggregateBodyInternal<G, R>) = aggregate(body as AggregateGroupedBody<G, R>)

    override fun filter(predicate: GroupedRowFilter<T, G>): GroupedDataFrame<T, G> {
        val indices = (0 until df.nrow()).filter {
            val row = GroupedDataRowImpl(df.get(it), groups)
            predicate(row, row)
        }
        return df[indices].asGroupedDataFrame(groups)
    }
}

internal fun <T, G, R> aggregateGroupBy(
    df: DataFrame<T>,
    selector: ColumnSelector<T, DataFrame<G>?>,
    removeColumns: Boolean,
    body: AggregateGroupedBody<G, R>
): DataFrame<T> {
    val defaultAggregateName = "aggregated"

    val column = df.getColumn(selector)

    val removed = df.removeImpl(selector)

    val groupedFrame = column.values.map {
        if (it == null) null
        else {
            val builder = GroupByReceiverImpl(it)
            val result = body(builder, builder)
            if (result != Unit && result !is NamedValue && result !is AggregatedPivot<*>) builder.yield(
                NamedValue.create(
                    pathOf(defaultAggregateName), result, null, null, true
                )
            )
            builder.compute()
        }
    }.concat()

    val removedNode = removed.removedColumns.single()
    val insertPath = removedNode.pathFromRoot().dropLast(1)

    if (!removeColumns) removedNode.data.wasRemoved = false

    val columnsToInsert = groupedFrame.getColumnsWithPaths { dfs() }.map {
        ColumnToInsert(insertPath + it.path, it, removedNode)
    }
    val src = if (removeColumns) removed.df else df
    return src.insertImpl(columnsToInsert)
}
