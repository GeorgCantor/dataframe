package org.jetbrains.kotlinx.dataframe.api

import org.jetbrains.kotlinx.dataframe.AnyRow
import org.jetbrains.kotlinx.dataframe.ColumnsSelector
import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataFrameExpression
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.RowColumnExpression
import org.jetbrains.kotlinx.dataframe.RowValueExpression
import org.jetbrains.kotlinx.dataframe.RowValueFilter
import org.jetbrains.kotlinx.dataframe.Selector
import org.jetbrains.kotlinx.dataframe.api.Update.Usage
import org.jetbrains.kotlinx.dataframe.columns.ColumnReference
import org.jetbrains.kotlinx.dataframe.documentation.*
import org.jetbrains.kotlinx.dataframe.impl.api.updateImpl
import org.jetbrains.kotlinx.dataframe.impl.api.updateWithValuePerColumnImpl
import org.jetbrains.kotlinx.dataframe.impl.columns.toColumnSet
import org.jetbrains.kotlinx.dataframe.impl.columns.toColumns
import org.jetbrains.kotlinx.dataframe.impl.headPlusArray
import org.jetbrains.kotlinx.dataframe.index
import kotlin.reflect.KProperty

/**
 * ## The Update Operation
 *
 * Returns the [DataFrame] with changed values in some cells
 * (column types can not be changed).
 *
 * Check out the [Update Operation Usage][Usage].
 * For more information: {@include [DocumentationUrls.Update]}
 */
public data class Update<T, C>(
    val df: DataFrame<T>,
    val filter: RowValueFilter<T, C>?,
    val columns: ColumnsSelector<T, C>,
) {
    public fun <R : C> cast(): Update<T, R> =
        Update(df, filter as RowValueFilter<T, R>?, columns as ColumnsSelector<T, R>)

    /** This argument providing the (clickable) name of the update-like function.
     * Note: If clickable, make sure to [alias][your type].
     */
    internal interface UpdateOperationArg

    /**
     * ## {@includeArg [UpdateOperationArg]} Operation Usage
     *
     * {@includeArg [UpdateOperationArg]} `{ `[columns][Columns]` }`
     *
     * - `[.`[where][org.jetbrains.kotlinx.dataframe.api.Update.where]` { `[rowValueCondition][RowCondition.RowValueCondition]` } ]`
     *
     * - `[.`[at][org.jetbrains.kotlinx.dataframe.api.Update.at]` (`[rowIndices][At.RowIndices]`) ]`
     *
     * - `.`[with][org.jetbrains.kotlinx.dataframe.api.Update.with]` { `[rowExpression][RowExpressions.RowValueExpression]` }
     *   | .`[notNull][org.jetbrains.kotlinx.dataframe.api.Update.notNull]` { `[rowExpression][RowExpressions.RowValueExpression]` }
     *   | .`[perCol][org.jetbrains.kotlinx.dataframe.api.Update.perCol]` { colExpression }
     *   | .`[perRowCol][org.jetbrains.kotlinx.dataframe.api.Update.perRowCol]` { rowColExpression }
     *   | .`[withValue][org.jetbrains.kotlinx.dataframe.api.Update.withValue]`(value)
     *   | .`[withNull][org.jetbrains.kotlinx.dataframe.api.Update.withNull]`()
     *   | .`[withZero][org.jetbrains.kotlinx.dataframe.api.Update.withZero]`()
     *   | .`[asFrame][org.jetbrains.kotlinx.dataframe.api.Update.asFrame]` { frameExpression }`
     *
     * {@comment TODO
     * colExpression: DataColumn.(DataColumn) -> NewValue
     * rowColExpression: DataRow.(DataColumn) -> NewValue
     * frameExpression: DataFrame.(DataFrame) -> DataFrame}
     * {@arg [UpdateOperationArg] [update][update]}
     */
    internal interface Usage

    /** Select the columns to update. See {@include [SelectingColumnsLink]} for all the options. */
    internal interface Columns

    /** @param columns The [ColumnsSelector] used to select the columns of this [DataFrame] to update. */
    internal interface DslParam

    /** @param columns The [ColumnReference]s of this [DataFrame] to update. */
    internal interface ColumnAccessorsParam

    /** @param columns The [KProperty] values corresponding to columns of this [DataFrame] to update. */
    internal interface KPropertiesParam

    /** @param columns The column names belonging to this [DataFrame] to update. */
    internal interface ColumnNamesParam
}

// region update

/** {@arg [SelectingColumns.OperationArg] [update][update]} */
private interface SetSelectingColumnsOperationArg

/**
 * @include [Update] {@comment Description of the update operation.}
 *
 * @include [Update.Columns] {@comment Description of what this function expects the user to do.}
 * ## This Update Overload
 */
internal interface CommonUpdateFunctionDoc

/**
 * @include [CommonUpdateFunctionDoc]
 * @include [SelectingColumns.Dsl] {@include [SetSelectingColumnsOperationArg]}
 * @include [Update.DslParam]
 */
public fun <T, C> DataFrame<T>.update(columns: ColumnsSelector<T, C>): Update<T, C> =
    Update(this, null, columns)

/**
 * @include [CommonUpdateFunctionDoc]
 * @include [SelectingColumns.ColumnNames] {@include [SetSelectingColumnsOperationArg]}
 * @include [Update.ColumnNamesParam]
 */
public fun <T> DataFrame<T>.update(vararg columns: String): Update<T, Any?> = update { columns.toColumns() }

/**
 * @include [CommonUpdateFunctionDoc]
 * @include [SelectingColumns.KProperties] {@include [SetSelectingColumnsOperationArg]}
 * @include [Update.KPropertiesParam]
 */
public fun <T, C> DataFrame<T>.update(vararg columns: KProperty<C>): Update<T, C> = update { columns.toColumns() }

/**
 * @include [CommonUpdateFunctionDoc]
 * @include [SelectingColumns.ColumnAccessors] {@include [SetSelectingColumnsOperationArg]}
 * @include [Update.ColumnAccessorsParam]
 */
public fun <T, C> DataFrame<T>.update(vararg columns: ColumnReference<C>): Update<T, C> =
    update { columns.toColumns() }

/**
 * @include [CommonUpdateFunctionDoc]
 * @include [SelectingColumns.ColumnAccessors] {@include [SetSelectingColumnsOperationArg]}
 * @include [Update.ColumnAccessorsParam]
 */
public fun <T, C> DataFrame<T>.update(columns: Iterable<ColumnReference<C>>): Update<T, C> =
    update { columns.toColumnSet() }

// endregion

/** ## Where
 * @include [RowCondition.RowValueCondition]
 * {@arg [RowCondition.FirstOperationArg] [update][update]}
 * {@arg [RowCondition.SecondOperationArg] [where][where]}
 *
 * @param predicate The [row value filter][RowValueFilter] to select the rows to update.
 */
public fun <T, C> Update<T, C>.where(predicate: RowValueFilter<T, C>): Update<T, C> =
    copy(filter = filter and predicate)

/** ## At
 * Only update the columns at certain given [row indices][CommonUpdateAtFunctionDoc.RowIndicesParam]:
 *
 * Either a [Collection]<[Int]>, an [IntRange], or just `vararg` indices.
 *
 * For example:
 *
 * `df.`[update][update]` { city }.`[at][at]`(5..10).`[with][with]` { "Paris" }`
 *
 * `df.`[update][update]` { name }.`[at][at]`(1, 2, 3, 4).`[with][with]` { "Empty" }`
 *
 * ## This At Overload
 */
private interface CommonUpdateAtFunctionDoc {

    /** The indices of the rows to update. */
    interface RowIndicesParam
}

/**
 * @include [CommonUpdateAtFunctionDoc]
 *
 * Provide a [Collection]<[Int]> of row indices to update.
 *
 * @param rowIndices {@include [CommonUpdateAtFunctionDoc.RowIndicesParam]}
 */
public fun <T, C> Update<T, C>.at(rowIndices: Collection<Int>): Update<T, C> = where { index in rowIndices }

/**
 * @include [CommonUpdateAtFunctionDoc]
 *
 * Provide a `vararg` of [Ints][Int] of row indices to update.
 *
 * @param rowIndices {@include [CommonUpdateAtFunctionDoc.RowIndicesParam]}
 */
public fun <T, C> Update<T, C>.at(vararg rowIndices: Int): Update<T, C> = at(rowIndices.toSet())

/**
 * @include [CommonUpdateAtFunctionDoc]
 *
 * Provide an [IntRange] of row indices to update.
 *
 * @param rowRange {@include [CommonUpdateAtFunctionDoc.RowIndicesParam]}
 */
public fun <T, C> Update<T, C>.at(rowRange: IntRange): Update<T, C> = where { index in rowRange }

/** TODO */
public infix fun <T, C> Update<T, C>.perRowCol(expression: RowColumnExpression<T, C, C>): DataFrame<T> =
    updateImpl { row, column, _ -> expression(row, column) }

public typealias UpdateExpression<T, C, R> = AddDataRow<T>.(C) -> R

/** ## With
 * {@include [RowExpressions.RowValueExpression]}
 * {@arg [RowExpressions.OperationArg] [update][update]` { city \}.`[with][with]}
 *
 * ## Note
 * @include [RowExpressions.AddDataRowNote]
 * @param expression The {@include [RowExpressions.RowValueExpressionLink]} to update the rows with.
 */
public infix fun <T, C> Update<T, C>.with(expression: UpdateExpression<T, C, C?>): DataFrame<T> =
    updateImpl { row, _, value ->
        expression(row, value)
    }

/** TODO */
public infix fun <T, C, R> Update<T, DataRow<C>>.asFrame(expression: DataFrameExpression<C, DataFrame<R>>): DataFrame<T> =
    df.replace(columns).with { it.asColumnGroup().let { expression(it, it) }.asColumnGroup(it.name()) }

@Deprecated(
    "Useless unless in combination with `withValue(null)`, but then users can just use `with { null }`...",
    ReplaceWith("this as Update<T, C?>")
)
public fun <T, C> Update<T, C>.asNullable(): Update<T, C?> = this as Update<T, C?>

/** TODO */
public fun <T, C> Update<T, C>.perCol(values: Map<String, C>): DataFrame<T> = updateWithValuePerColumnImpl {
    values[it.name()] ?: throw IllegalArgumentException("Update value for column ${it.name()} is not defined")
}

/** TODO */
public fun <T, C> Update<T, C>.perCol(values: AnyRow): DataFrame<T> = perCol(values.toMap() as Map<String, C>)

/** TODO */
public fun <T, C> Update<T, C>.perCol(valueSelector: Selector<DataColumn<C>, C>): DataFrame<T> =
    updateWithValuePerColumnImpl(valueSelector)

/** TODO */
internal infix fun <T, C> RowValueFilter<T, C>?.and(other: RowValueFilter<T, C>): RowValueFilter<T, C> {
    if (this == null) return other
    val thisExp = this
    return { thisExp(this, it) && other(this, it) }
}

/** TODO */
public fun <T, C> Update<T, C?>.notNull(): Update<T, C> =
    copy(filter = filter and { it != null }) as Update<T, C>

/** TODO */
public fun <T, C> Update<T, C?>.notNull(expression: RowValueExpression<T, C, C>): DataFrame<T> =
    notNull().updateImpl { row, column, value ->
        expression(row, value)
    }

/** TODO */
public fun <T, C> DataFrame<T>.update(
    firstCol: ColumnReference<C>,
    vararg cols: ColumnReference<C>,
    expression: RowValueExpression<T, C, C>
): DataFrame<T> =
    update(*headPlusArray(firstCol, cols)).with(expression)

/** TODO */
public fun <T, C> DataFrame<T>.update(
    firstCol: KProperty<C>,
    vararg cols: KProperty<C>,
    expression: RowValueExpression<T, C, C>
): DataFrame<T> =
    update(*headPlusArray(firstCol, cols)).with(expression)

/** TODO */
public fun <T> DataFrame<T>.update(
    firstCol: String,
    vararg cols: String,
    expression: RowValueExpression<T, Any?, Any?>
): DataFrame<T> =
    update(*headPlusArray(firstCol, cols)).with(expression)


/**
 * Specific version of [with] that simply sets the value of each selected row to {@includeArg [CommonSpecificWithDocFirstArg]}.
 *
 * For example:
 *
 * `df.`[update][update]` { id }.`[where][Update.where]` { it < 0 }.`{@includeArg [CommonSpecificWithDocSecondArg]}`
 */
private interface CommonSpecificWithDoc

/** Arg for the resulting value */
private interface CommonSpecificWithDocFirstArg

/** Arg for the function call */
private interface CommonSpecificWithDocSecondArg

/**
 * ## With Null
 * @include [CommonSpecificWithDoc]
 * {@arg [CommonSpecificWithDocFirstArg] `null`}
 * {@arg [CommonSpecificWithDocSecondArg] [withNull][withNull]`()}
 */
public fun <T, C> Update<T, C>.withNull(): DataFrame<T> = with { null }

/**
 * ## With Zero
 * @include [CommonSpecificWithDoc]
 * {@arg [CommonSpecificWithDocFirstArg] `0`}
 * {@arg [CommonSpecificWithDocSecondArg] [withZero][withZero]`()}
 */
public fun <T, C : Number?> Update<T, C>.withZero(): DataFrame<T> = updateWithValuePerColumnImpl { 0 as C }

/**
 * ## With Value
 * @include [CommonSpecificWithDoc]
 * {@arg [CommonSpecificWithDocFirstArg] [value]}
 * {@arg [CommonSpecificWithDocSecondArg] [withValue][withValue]`(-1)}
 *
 * @param value The value to set the selected rows to. In contrast to [with][Update.with], this must be the same exact type.
 */
public infix fun <T, C> Update<T, C>.withValue(value: C): DataFrame<T> = with { value }
