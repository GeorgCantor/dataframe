package org.jetbrains.kotlinx.dataframe.api

import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.ColumnResolutionContext
import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.columns.ColumnReference
import org.jetbrains.kotlinx.dataframe.columns.ColumnWithPath
import org.jetbrains.kotlinx.dataframe.columns.Columns
import org.jetbrains.kotlinx.dataframe.columns.name
import org.jetbrains.kotlinx.dataframe.getColumnsWithPaths
import org.jetbrains.kotlinx.dataframe.impl.DataFrameReceiver
import org.jetbrains.kotlinx.dataframe.impl.columns.ColumnsList
import org.jetbrains.kotlinx.dataframe.impl.columns.toColumnSet
import org.jetbrains.kotlinx.dataframe.impl.prepareForReceiver
import org.jetbrains.kotlinx.dataframe.indices
import org.jetbrains.kotlinx.dataframe.isGroup
import org.jetbrains.kotlinx.dataframe.kind
import org.jetbrains.kotlinx.dataframe.nrow
import org.jetbrains.kotlinx.dataframe.toColumnAccessor
import org.jetbrains.kotlinx.dataframe.toDataFrame
import org.jetbrains.kotlinx.dataframe.type
import kotlin.reflect.full.withNullability

public interface JoinReceiver<out A, out B> : SelectReceiver<A> {

    public val right: DataFrame<B>

    public infix fun <C> ColumnReference<C>.match(other: ColumnReference<C>): ColumnMatch<C> = ColumnMatch(this, other)
}

public class ColumnMatch<C>(public val left: ColumnReference<C>, public val right: ColumnReference<C>) : Columns<C> {

    override fun resolve(context: ColumnResolutionContext): List<ColumnWithPath<C>> {
        throw UnsupportedOperationException()
    }
}

internal class JoinReceiverImpl<A, B>(left: DataFrame<A>, val other: DataFrame<B>) :
    DataFrameReceiver<A>(left, false), JoinReceiver<A, B> {

    override val right: DataFrame<B> = prepareForReceiver(other)
}

public typealias JoinColumnSelector<A, B> = JoinReceiver<A, B>.(JoinReceiver<A, B>) -> Columns<*>

internal fun <C> Columns<C>.extractJoinColumns(other: AnyFrame): List<ColumnMatch<C>> = when (this) {
    is ColumnsList -> columns.flatMap { it.extractJoinColumns(other) }
    is ColumnReference<C> -> listOf(ColumnMatch(this, path().toColumnAccessor() as ColumnReference<C>))
    is ColumnMatch -> listOf(this)
    else -> throw Exception()
}

internal fun <A, B> DataFrame<A>.getColumns(other: DataFrame<B>, selector: JoinColumnSelector<A, B>) =
    JoinReceiverImpl(this, other).let { selector(it, it).extractJoinColumns(other) }

public enum class JoinType {
    LEFT, // all data from left data frame, nulls for mismatches in right data frame
    RIGHT, // all data from right data frame, nulls for mismatches in left data frame
    INNER, // only matched data from right and left data frame
    OUTER, // all data from left and from right data frame, nulls for any mismatches
    EXCLUDE // mismatched rows from left data frame
}

public val JoinType.allowLeftNulls: Boolean get() = this == JoinType.RIGHT || this == JoinType.OUTER
public val JoinType.allowRightNulls: Boolean get() = this == JoinType.LEFT || this == JoinType.OUTER || this == JoinType.EXCLUDE

internal fun <A, B> defaultJoinColumns(left: DataFrame<A>, right: DataFrame<B>): JoinColumnSelector<A, B> =
    { left.columnNames().intersect(right.columnNames()).map { it.toColumnAccessor() }.let { ColumnsList(it) } }

internal fun <T> defaultJoinColumns(dataFrames: Iterable<DataFrame<T>>): JoinColumnSelector<T, T> =
    {
        dataFrames.map { it.columnNames() }.fold<List<String>, Set<String>?>(null) { set, names ->
            set?.intersect(names) ?: names.toSet()
        }.orEmpty().map { it.toColumnAccessor() }.let { ColumnsList(it) }
    }

public fun <A, B> DataFrame<A>.innerJoin(
    other: DataFrame<B>,
    selector: JoinColumnSelector<A, B> = defaultJoinColumns(this, other)
): DataFrame<A> = join(other, JoinType.INNER, selector = selector)

public fun <A, B> DataFrame<A>.leftJoin(
    other: DataFrame<B>,
    selector: JoinColumnSelector<A, B> = defaultJoinColumns(this, other)
): DataFrame<A> = join(other, JoinType.LEFT, selector = selector)

public fun <A, B> DataFrame<A>.rightJoin(
    other: DataFrame<B>,
    selector: JoinColumnSelector<A, B> = defaultJoinColumns(this, other)
): DataFrame<A> = join(other, JoinType.RIGHT, selector = selector)

public fun <A, B> DataFrame<A>.outerJoin(
    other: DataFrame<B>,
    selector: JoinColumnSelector<A, B> = defaultJoinColumns(this, other)
): DataFrame<A> = join(other, JoinType.OUTER, selector = selector)

public fun <A, B> DataFrame<A>.filterJoin(
    other: DataFrame<B>,
    selector: JoinColumnSelector<A, B> = defaultJoinColumns(this, other)
): DataFrame<A> = join(other, JoinType.INNER, addNewColumns = false, selector = selector)

public fun <A, B> DataFrame<A>.excludeJoin(
    other: DataFrame<B>,
    selector: JoinColumnSelector<A, B> = defaultJoinColumns(this, other)
): DataFrame<A> = join(other, JoinType.EXCLUDE, addNewColumns = false, selector = selector)

public fun <T> Iterable<DataFrame<T>>.joinOrNull(
    joinType: JoinType = JoinType.INNER,
    selector: JoinColumnSelector<T, T> = defaultJoinColumns(this)
): DataFrame<T>? =
    fold<DataFrame<T>, DataFrame<T>?>(null) { joined, new -> joined?.join(new, joinType, selector = selector) ?: new }

public fun <A, B> DataFrame<A>.join(
    other: DataFrame<B>,
    joinType: JoinType = JoinType.INNER,
    addNewColumns: Boolean = true,
    selector: JoinColumnSelector<A, B> = defaultJoinColumns(this, other)
): DataFrame<A> {
    val joinColumns = getColumns(other, selector)

    val leftJoinColumns = getColumnsWithPaths { joinColumns.map { it.left }.toColumnSet() }
    val rightJoinColumns = other.getColumnsWithPaths { joinColumns.map { it.right }.toColumnSet() }

    require(leftJoinColumns.size == rightJoinColumns.size)

    // replace all MapColumns in join with nested columns, matching by column path
    val allLeftJoinColumns = mutableListOf<ColumnWithPath<*>>()
    val allRightJoinColumns = mutableListOf<ColumnWithPath<*>>()

    leftJoinColumns.indices.forEach { i ->
        val leftCol = leftJoinColumns[i]
        val rightCol = rightJoinColumns[i]
        if (leftCol.isGroup() && rightCol.isGroup()) {
            val allLeftChildren = getColumnsWithPaths { leftCol.dfs() }
            val allRightChildren = other.getColumnsWithPaths { rightCol.dfs() }
            val matchedPaths = allLeftChildren.map { it.path }.intersect(allRightChildren.map { it.path })
            val matchedLeftColumns = allLeftChildren.filter { matchedPaths.contains(it.path) }
            val matchedRightColumns = allRightChildren.filter { matchedPaths.contains(it.path) }
            require(matchedLeftColumns.size == matchedRightColumns.size)

            allLeftJoinColumns.addAll(matchedLeftColumns)
            allRightJoinColumns.addAll(matchedRightColumns)
        } else {
            allLeftJoinColumns.add(leftCol)
            allRightJoinColumns.add(rightCol)
        }
    }

    // compute left to right column path mappings
    val pathMapping = allLeftJoinColumns.mapIndexed { colNumber, leftCol ->
        leftCol.path to allRightJoinColumns[colNumber].path
    }.toMap()

    // compute pairs of join key to row index from right data frame
    val rightJoinKeyToIndex = other.indices()
        .map { index -> allRightJoinColumns.map { it.data[index] } to index }

    // group row indices by key from right data frame
    val groupedRight = when (joinType) {
        JoinType.EXCLUDE -> rightJoinKeyToIndex.map { it.first to emptyList<Int>() }.toMap()
        else -> rightJoinKeyToIndex.groupBy({ it.first }) { it.second }
    }

    var outputRowsCount = 0

    // for every row index from left data frame compute a list of matched indices from right data frame
    val leftToRightMapping = indices.map { leftIndex ->
        val leftKey = allLeftJoinColumns.map { it.data[leftIndex] }
        val rightIndices = groupedRight[leftKey]
        outputRowsCount += rightIndices?.size ?: if (joinType.allowRightNulls) 1 else 0
        rightIndices
    }

    // for every row index in right data frame store a flag indicating whether this row was matched by some row in left data frame
    val rightMatched = Array(other.nrow) { false }

    // number of rows in right data frame that were not matched by any row in left data frame. Used for correct allocation of an output array
    var rightUnmatchedCount = other.nrow

    // compute matched indices from right data frame and number of rows in output data frame
    if (joinType.allowLeftNulls) {
        leftToRightMapping.forEach { rightIndices ->
            rightIndices?.forEach { i ->
                if (!rightMatched[i]) {
                    rightUnmatchedCount--
                    rightMatched[i] = true
                }
            }
        }
        outputRowsCount += rightUnmatchedCount
    }

    val leftColumns = getColumnsWithPaths { dfs() }

    val rightJoinColumnPaths = allRightJoinColumns.map { it.path to it.data }.toMap()

    val newRightColumns =
        if (addNewColumns) other.getColumnsWithPaths { dfs { !it.isGroup() && !rightJoinColumnPaths.contains(it.path) } } else emptyList()

    // for every column index from left data frame stores matching column from right data frame
    val leftToRightColumns = leftColumns.map { rightJoinColumnPaths[pathMapping[it.path()]] }

    val leftColumnsCount = leftColumns.size
    val newRightColumnsCount = newRightColumns.size
    val outputColumnsCount = leftColumnsCount + newRightColumnsCount

    val outputData = Array<Array<Any?>>(outputColumnsCount) { arrayOfNulls(outputRowsCount) }
    val hasNulls = Array(outputColumnsCount) { false }

    var row = 0

    leftToRightMapping.forEachIndexed { leftRow, rightRows ->
        if (rightRows == null) {
            if (joinType.allowRightNulls) {
                for (col in 0 until leftColumnsCount) {
                    val leftColumn = leftColumns[col].data
                    outputData[col][row] = leftColumn[leftRow].also { if (it == null) hasNulls[col] = true }
                }
                for (col in 0 until newRightColumnsCount) {
                    outputData[leftColumnsCount + col][row] = null
                    hasNulls[leftColumnsCount + col] = true
                }
                row++
            }
        } else {
            for (rightRow in rightRows) {
                for (col in 0 until leftColumnsCount) {
                    val leftColumn = leftColumns[col].data
                    outputData[col][row] = leftColumn[leftRow].also { if (it == null) hasNulls[col] = true }
                }
                for (col in 0 until newRightColumnsCount) {
                    val rightColumn = newRightColumns[col].data
                    outputData[leftColumnsCount + col][row] =
                        rightColumn[rightRow].also { if (it == null) hasNulls[leftColumnsCount + col] = true }
                }
                row++
            }
        }
    }

    if (joinType.allowLeftNulls) {
        for (rightRow in rightMatched.indices) {
            if (!rightMatched[rightRow]) {
                for (col in 0 until leftColumnsCount) {
                    val rightColumn = leftToRightColumns[col]
                    outputData[col][row] = rightColumn?.get(rightRow).also { if (it == null) hasNulls[col] = true }
                }
                for (col in 0 until newRightColumnsCount) {
                    val rightColumn = newRightColumns[col].data
                    outputData[leftColumnsCount + col][row] =
                        rightColumn[rightRow].also { if (it == null) hasNulls[leftColumnsCount + col] = true }
                }
                row++
            }
        }
    }

    val columns = outputData.mapIndexed { columnIndex, columnValues ->
        val srcColumn =
            if (columnIndex < leftColumnsCount) leftColumns[columnIndex] else newRightColumns[columnIndex - leftColumnsCount]
        val hasNulls = hasNulls[columnIndex]
        val newColumn = when (srcColumn.kind) {
            org.jetbrains.kotlinx.dataframe.ColumnKind.Value -> DataColumn.create(srcColumn.name, columnValues.asList(), srcColumn.type.withNullability(hasNulls))
            org.jetbrains.kotlinx.dataframe.ColumnKind.Frame -> DataColumn.create(srcColumn.name, columnValues.asList() as List<AnyFrame?>)
            org.jetbrains.kotlinx.dataframe.ColumnKind.Group -> error("Unexpected MapColumn at path ${srcColumn.path}")
        }
        srcColumn.path to newColumn
    }

    return columns.toDataFrame()
}
