package org.jetbrains.kotlinx.dataframe.api

import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.columns.ColumnPath
import org.jetbrains.kotlinx.dataframe.columns.ColumnSet
import org.jetbrains.kotlinx.dataframe.columns.SingleColumn
import org.jetbrains.kotlinx.dataframe.columns.size
import org.jetbrains.kotlinx.dataframe.documentation.LineBreak
import org.jetbrains.kotlinx.dataframe.impl.columns.TransformableColumnSet
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

// region ColumnsSelectionDsl
public interface ColsOfColumnsSelectionDsl<out T> : ColumnsSelectionDslExtension<T> {

    /**
     * @include [CommonColsOfDocs]
     * Get sub-columns of the column with this name by [type] with optional [filter].
     *
     * #### For example:
     *
     * `df.`[select][DataFrame.select]` { "myColumnGroup".`[colsOf][String.colsOf]`(`[typeOf][typeOf]`<`[Int][Int]`>()) }`
     *
     * `df.`[select][DataFrame.select]` { "myColumnGroup".`[colsOf][String.colsOf]`(`[typeOf][typeOf]`<`[Int][Int]`>()) { it: `[DataColumn][DataColumn]`<`[Int][Int]`> -> it.`[size][DataColumn.size]` > 10 } }`
     *
     * @include [CommonColsOfDocs.FilterParam]
     * @include [CommonColsOfDocs.Return]
     */
    public fun <C> String.colsOf(
        type: KType,
        filter: (DataColumn<C>) -> Boolean = { true },
    ): ColumnSet<*> = columnGroup(this).colsOf(type, filter)

    /**
     * @include [CommonColsOfDocs]
     * Get sub-columns of the column this [KProperty Accessor][KProperty] points to by [type] with optional [filter].
     *
     * #### For example:
     *
     * `df.`[select][DataFrame.select]` { DataSchemaType::myColumnGroup.`[colsOf][KProperty.colsOf]`(`[typeOf][typeOf]`<`[Int][Int]`>()) }`
     *
     * `df.`[select][DataFrame.select]` { `[colGroup][ColumnsSelectionDsl.colGroup]`(Type::myColumnGroup).`[colsOf][SingleColumn.colsOf]`(`[typeOf][typeOf]`<`[Int][Int]`>()) { it: `[DataColumn][DataColumn]`<`[Int][Int]`> -> it.`[size][DataColumn.size]` > 10 } }`
     *
     * @include [CommonColsOfDocs.FilterParam]
     * @include [CommonColsOfDocs.Return]
     */
    public fun <C> KProperty<DataRow<*>>.colsOf(
        type: KType,
        filter: (DataColumn<C>) -> Boolean = { true },
    ): ColumnSet<*> = columnGroup(this).colsOf(type, filter)

    /**
     * @include [CommonColsOfDocs]
     * Get sub-columns of the column this [ColumnPath] points to by [type] with optional [filter].
     *
     * #### For example:
     *
     * `df.`[select][DataFrame.select]` { "pathTo"["myColumnGroup"].`[colsOf][ColumnPath.colsOf]`(`[typeOf][typeOf]`<`[Int][Int]`>()) }`
     *
     * `df.`[select][DataFrame.select]` { "pathTo"["myColumnGroup"].`[colsOf][ColumnPath.colsOf]`(`[typeOf][typeOf]`<`[Int][Int]`>()) { it: `[DataColumn][DataColumn]`<`[Int][Int]`> -> it.`[size][DataColumn.size]` > 10 } }`
     *
     * @include [CommonColsOfDocs.FilterParam]
     * @include [CommonColsOfDocs.Return]
     */
    public fun <C> ColumnPath.colsOf(
        type: KType,
        filter: (DataColumn<C>) -> Boolean = { true },
    ): ColumnSet<*> = columnGroup(this).colsOf(type, filter)
}

/**
 * ## (Children) Cols Of
 * Get columns by a given type and an optional filter.
 *
 * #### For example:
 *
 * `df.`[select][DataFrame.select]` { `[colsOf][SingleColumn.colsOf]`<`[Int][Int]`>() }`
 *
 * `df.`[select][DataFrame.select]` { `[colsOf][SingleColumn.colsOf]`<`[Int][Int]`> { it.`[size][DataColumn.size]` > 10 } }`
 * {@include [LineBreak]}
 * Alternatively, [colsOf] can also be called on existing columns:
 *
 * `df.`[select][DataFrame.select]` { myColumnGroup.`[colsOf][SingleColumn.colsOf]`<`[Int][Int]`>() }`
 *
 * `df.`[select][DataFrame.select]` { "myColumnGroup"<Type>().`[colsOf][SingleColumn.colsOf]`<`[Int][Int]`> { it.`[size][DataColumn.size]` > 10 } }`
 *
 * `df.`[select][DataFrame.select]` { `[columnGroup][columnGroup]`(Type::myColumnGroup).`[colsOf][SingleColumn.colsOf]`<`[Double][Double]`>() }`
 * {@include [LineBreak]}
 * Finally, [colsOf] can also take a [KType] argument instead of a reified type.
 * This is useful when the type is not known at compile time or when the API function cannot be inlined.
 * {@comment TODO: [Issue: #325, context receiver support](https://github.com/Kotlin/dataframe/issues/325) }
 *
 * `df.`[select][DataFrame.select]` { `[colsOf][SingleColumn.colsOf]`(`[typeOf][typeOf]`<`[Int][Int]`>()) }`
 *
 * `df.`[select][DataFrame.select]` { "myColumnGroup".`[colsOf][ColumnsSelectionDsl.colsOf]`(`[typeOf][typeOf]`<`[Int][Int]`>()) { it: `[DataColumn][DataColumn]`<`[Int][Int]`> -> it.`[size][DataColumn.size]` > 10 } }`
 *
 * ### This Cols Of Overload
 */
private interface CommonColsOfDocs {

    /** @return A [ColumnSet] containing the columns of given type that were included by [filter\]. */
    interface Return

    /** @param [filter\] an optional filter function that takes a column of type [C\] and returns `true` if the column should be included. */
    interface FilterParam
}

/**
 * @include [CommonColsOfDocs]
 * Get (sub-)columns by [type] with optional [filter].
 * #### For example:
 *
 * `df.`[select][DataFrame.select]` { `[cols][ColumnsSelectionDsl.cols]` { .. }.`[colsOf][ColumnSet.colsOf]`(`[typeOf][typeOf]`<`[Int][Int]`>()) }`
 *
 * `df.`[select][DataFrame.select]` { `[cols][ColumnsSelectionDsl.cols]` { .. }.`[colsOf][ColumnSet.colsOf]`(`[typeOf][typeOf]`<`[Int][Int]`>()) { it: `[DataColumn][DataColumn]`<`[Int][Int]`> -> it.`[size][DataColumn.size]` > 10 } }`
 *
 * @include [CommonColsOfDocs.FilterParam]
 * @include [CommonColsOfDocs.Return]
 */
@Suppress("UNCHECKED_CAST")
public fun <C> ColumnSet<*>.colsOf(
    type: KType,
    filter: (DataColumn<C>) -> Boolean = { true },
): TransformableColumnSet<C> =
    colsInternal { it.isSubtypeOf(type) && filter(it.cast()) } as TransformableColumnSet<C>

/**
 * @include [CommonColsOfDocs]
 * Get (sub-)columns by a given type with optional [filter].
 * #### For example:
 *
 * `df.`[select][DataFrame.select]` { `[cols][ColumnsSelectionDsl.cols]` { .. }.`[colsOf][ColumnSet.colsOf]`<`[Int][Int]`>() }`
 *
 * `df.`[select][DataFrame.select]` { `[cols][ColumnsSelectionDsl.cols]` { .. }.`[colsOf][ColumnSet.colsOf]`<`[Int][Int]`> { it.`[size][DataColumn.size]` > 10 } }`
 *
 * @include [CommonColsOfDocs.FilterParam]
 * @include [CommonColsOfDocs.Return]
 */
public inline fun <reified C> ColumnSet<*>.colsOf(
    noinline filter: (DataColumn<C>) -> Boolean = { true },
): TransformableColumnSet<C> =
    colsOf(typeOf<C>(), filter)

/**
 * @include [CommonColsOfDocs]
 * Get (sub-)columns by [type] with optional [filter].
 *
 * #### For example:
 *
 * `df.`[select][DataFrame.select]` { `[colsOf][SingleColumn.colsOf]`(`[typeOf][typeOf]`<`[Int][Int]`>()) }`
 *
 * @include [CommonColsOfDocs.FilterParam]
 * @include [CommonColsOfDocs.Return]
 */
public fun <C> ColumnsSelectionDsl<*>.colsOf(
    type: KType,
    filter: (DataColumn<C>) -> Boolean = { true },
): TransformableColumnSet<C> =
    asSingleColumn().colsOf(type, filter)

/**
 * @include [CommonColsOfDocs]
 * Get (sub-)columns by a given type with optional [filter].
 * #### For example:
 *
 * `df.`[select][DataFrame.select]` { `[colsOf][SingleColumn.colsOf]`<`[Int][Int]`>() }`
 *
 * @include [CommonColsOfDocs.FilterParam]
 * @include [CommonColsOfDocs.Return]
 */
public inline fun <reified C> ColumnsSelectionDsl<*>.colsOf(
    noinline filter: (DataColumn<C>) -> Boolean = { true },
): TransformableColumnSet<C> =
    asSingleColumn().colsOf(typeOf<C>(), filter)

// TODO
public inline fun <reified C> AtAnyDepthDsl<*>.colsOf(
    noinline filter: (DataColumn<C>) -> Boolean = { true },
): TransformableColumnSet<C> =
    context.asSingleColumn().colsOf(typeOf<C>(), filter)

/**
 * @include [CommonColsOfDocs]
 * Get (sub-)columns by [type] with optional [filter].
 *
 * #### For example:
 *
 * `df.`[select][DataFrame.select]` { myColumnGroup.`[colsOf][SingleColumn.colsOf]`(`[typeOf][typeOf]`<`[Int][Int]`>()) { it: `[DataColumn][DataColumn]`<`[Int][Int]`> -> it.`[size][DataColumn.size]` > 10 } }`
 *
 * `df.`[select][DataFrame.select]` { myColumnGroup.`[colsOf][SingleColumn.colsOf]`(`[typeOf][typeOf]`<`[Int][Int]`>()) }`
 *
 * @include [CommonColsOfDocs.FilterParam]
 * @include [CommonColsOfDocs.Return]
 */
@Suppress("UNCHECKED_CAST")
public fun <C> SingleColumn<DataRow<*>>.colsOf(
    type: KType,
    filter: (DataColumn<C>) -> Boolean = { true },
): TransformableColumnSet<C> =
    this.ensureIsColumnGroup().colsInternal { it.isSubtypeOf(type) && filter(it.cast()) } as TransformableColumnSet<C>

/**
 * @include [CommonColsOfDocs]
 * Get (sub-)columns by a given type with optional [filter].
 * #### For example:
 *
 * `df.`[select][DataFrame.select]` { myColumnGroup.`[colsOf][SingleColumn.colsOf]`<`[Int][Int]`> { it.`[size][DataColumn.size]` > 10 } }`
 *
 * `df.`[select][DataFrame.select]` { myColumnGroup.`[colsOf][SingleColumn.colsOf]`<`[Int][Int]`>() }`
 *
 * @include [CommonColsOfDocs.FilterParam]
 * @include [CommonColsOfDocs.Return]
 */
public inline fun <reified C> SingleColumn<DataRow<*>>.colsOf(
    noinline filter: (DataColumn<C>) -> Boolean = { true },
): TransformableColumnSet<C> =
    colsOf(typeOf<C>(), filter)

/* TODO: [Issue: #325, context receiver support](https://github.com/Kotlin/dataframe/issues/325)
context(ColumnsSelectionDsl)
public inline fun <reified C> KProperty<DataRow<*>>.colsOf(noinline filter: (DataColumn<C>) -> Boolean = { true }): ColumnSet<*> =
    colsOf(typeOf<C>(), filter)

context(ColumnsSelectionDsl)
public inline fun <reified C> String.colsOf(noinline filter: (DataColumn<C>) -> Boolean = { true }): ColumnSet<*> =
    colsOf(typeOf<C>(), filter)

 */

// endregion
