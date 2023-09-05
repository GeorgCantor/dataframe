package org.jetbrains.kotlinx.dataframe.api

import org.jetbrains.kotlinx.dataframe.ColumnFilter
import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.RowExpression
import org.jetbrains.kotlinx.dataframe.api.SingleColumnsSelectionDsl.CommonSingleDocs.Examples
import org.jetbrains.kotlinx.dataframe.api.SingleColumnsSelectionDsl.Usage
import org.jetbrains.kotlinx.dataframe.api.SingleColumnsSelectionDsl.Usage.ColumnGroupName
import org.jetbrains.kotlinx.dataframe.api.SingleColumnsSelectionDsl.Usage.ColumnSetName
import org.jetbrains.kotlinx.dataframe.api.SingleColumnsSelectionDsl.Usage.PlainDslName
import org.jetbrains.kotlinx.dataframe.columns.ColumnGroup
import org.jetbrains.kotlinx.dataframe.columns.ColumnPath
import org.jetbrains.kotlinx.dataframe.columns.ColumnReference
import org.jetbrains.kotlinx.dataframe.columns.ColumnSet
import org.jetbrains.kotlinx.dataframe.columns.SingleColumn
import org.jetbrains.kotlinx.dataframe.columns.asColumnSet
import org.jetbrains.kotlinx.dataframe.columns.values
import org.jetbrains.kotlinx.dataframe.documentation.Indent
import org.jetbrains.kotlinx.dataframe.documentation.LineBreak
import org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate
import org.jetbrains.kotlinx.dataframe.impl.columns.TransformableColumnSet
import org.jetbrains.kotlinx.dataframe.impl.columns.TransformableSingleColumn
import org.jetbrains.kotlinx.dataframe.impl.columns.singleOrNullWithTransformerImpl
import org.jetbrains.kotlinx.dataframe.impl.columns.transform
import org.jetbrains.kotlinx.dataframe.nrow
import kotlin.reflect.KProperty

// region DataColumn

public fun <C> DataColumn<C>.single(): C = values.single()

// endregion

// region DataFrame

public fun <T> DataFrame<T>.single(): DataRow<T> =
    when (nrow) {
        0 -> throw NoSuchElementException("DataFrame has no rows. Use `singleOrNull`.")
        1 -> get(0)
        else -> throw IllegalArgumentException("DataFrame has more than one row.")
    }

public fun <T> DataFrame<T>.singleOrNull(): DataRow<T>? = rows().singleOrNull()

public fun <T> DataFrame<T>.single(predicate: RowExpression<T, Boolean>): DataRow<T> =
    rows().single { predicate(it, it) }

public fun <T> DataFrame<T>.singleOrNull(predicate: RowExpression<T, Boolean>): DataRow<T>? =
    rows().singleOrNull { predicate(it, it) }

// endregion

// region ColumnsSelectionDsl

/**
 * See [Usage].
 */
public interface SingleColumnsSelectionDsl<out T> : ColumnsSelectionDslExtension<T> {

    /**
     * ## Single (Col) Usage
     *
     * @include [UsageTemplate]
     * {@setArg [UsageTemplate.DefinitionsArg]
     *  {@include [UsageTemplate.ColumnSetDef]}
     *  {@include [LineBreak]}
     *  {@include [UsageTemplate.ColumnGroupDef]}
     *  {@include [LineBreak]}
     *  {@include [UsageTemplate.ConditionDef]}
     * }
     *
     * {@setArg [UsageTemplate.PlainDslFunctionsArg]
     *  {@include [PlainDslName]}` [` **`{ `**{@include [UsageTemplate.ConditionRef]}**` \\}`** `] [ `{@include [AtAnyDepthColumnsSelectionDsl.Usage.Name]} ` ]`
     * }
     *
     * {@setArg [UsageTemplate.ColumnSetFunctionsArg]
     *  {@include [Indent]}{@include [ColumnSetName]}` [` **`{ `**{@include [UsageTemplate.ConditionRef]}**` \\}`** `] [ `{@include [AtAnyDepthColumnsSelectionDsl.Usage.Name]} ` ]`
     * }
     *
     * {@setArg [UsageTemplate.ColumnGroupFunctionsArg]
     *  {@include [Indent]}{@include [ColumnGroupName]}` [` **`{ `**{@include [UsageTemplate.ConditionRef]}**` \\}`** `] [ `{@include [AtAnyDepthColumnsSelectionDsl.Usage.Name]} ` ]`
     * }
     */
    public interface Usage {

        /** [**single**][ColumnsSelectionDsl.single] */
        public interface PlainDslName

        /** .[**single**][ColumnsSelectionDsl.single] */
        public interface ColumnSetName

        /** .[**singleCol**][ColumnsSelectionDsl.singleCol] */
        public interface ColumnGroupName
    }

    /**
     * ## Single (Col)
     * Returns the single ([transformable][TransformableSingleColumn]) column in this [ColumnSet] or [ColumnGroup]
     * that adheres to the given [condition\].
     * If no column adheres to the given [condition\], [NoSuchElementException] is thrown.
     * If multiple columns adhere to it, [IllegalArgumentException] is thrown.
     *
     * NOTE: For [column groups][ColumnsSelectionDsl], `single` is named `singleCol` instead to avoid confusion.
     *
     * Check out [Usage] for how to use [single]/[singleCol].
     *
     * #### Examples:
     *
     * `df.`[select][DataFrame.select]` { `[single][ColumnsSelectionDsl.single]` { it.`[name][ColumnReference.name]`().`[startsWith][String.startsWith]`("order") } }`
     *
     * `df.`[select][DataFrame.select]` { "myColumnGroup".`[singleCol][String.singleCol]` { it.`[name][ColumnReference.name]`().`[startsWith][String.startsWith]`("order") }.`[atAnyDepth][ColumnsSelectionDsl.atAnyDepth]`() }`
     *
     * #### Examples for this overload:
     *
     * {@getArg [Examples]}
     *
     * @param [condition\] The optional [ColumnFilter] condition that the column must adhere to.
     * @return A ([transformable][TransformableSingleColumn]) [SingleColumn] containing the single column that adheres to the given [condition\].
     * @throws [NoSuchElementException\] if no column adheres to the given [condition\].
     * @throws [IllegalArgumentException\] if more than one column adheres to the given [condition\].
     */
    private interface CommonSingleDocs {

        /** Examples key */
        interface Examples
    }

    /**
     * @include [CommonSingleDocs]
     * @setArg [CommonSingleDocs.Examples]
     * `df.`[select][DataFrame.select]` { `[colsOf][SingleColumn.colsOf]`<`[String][String]`>().`[single][ColumnSet.single]` { it.`[name][ColumnReference.name]`().`[startsWith][String.startsWith]`("year") } }`
     *
     * `df.`[select][DataFrame.select]` { `[colsOf][SingleColumn.colsOf]`<`[Int][Int]`>().`[single][ColumnSet.single]`() }`
     */
    public fun <C> ColumnSet<C>.single(condition: ColumnFilter<C> = { true }): TransformableSingleColumn<C> =
        singleInternal(condition)

    /**
     * @include [CommonSingleDocs]
     * @setArg [CommonSingleDocs.Examples]
     *
     * `df.`[select][DataFrame.select]` { `[single][ColumnsSelectionDsl.single]` { it.`[name][ColumnReference.name]`().`[startsWith][String.startsWith]`("year") } }`
     */
    public fun ColumnsSelectionDsl<*>.single(condition: ColumnFilter<*> = { true }): TransformableSingleColumn<*> =
        asSingleColumn().singleCol(condition)

    /**
     * @include [CommonSingleDocs]
     * @setArg [CommonSingleDocs.Examples]
     *
     * `df.`[select][DataFrame.select]` { myColumnGroup.`[singleCol][SingleColumn.singleCol]`() }`
     */
    public fun SingleColumn<DataRow<*>>.singleCol(condition: ColumnFilter<*> = { true }): TransformableSingleColumn<*> =
        this.ensureIsColumnGroup().asColumnSet().single(condition)

    /**
     * @include [CommonSingleDocs]
     * @setArg [CommonSingleDocs.Examples]
     * `df.`[select][DataFrame.select]` { "myColumnGroup".`[singleCol][String.singleCol]` { it.`[name][ColumnReference.name]`().`[startsWith][String.startsWith]`("year") } }`
     */
    public fun String.singleCol(condition: ColumnFilter<*> = { true }): TransformableSingleColumn<*> =
        columnGroup(this).singleCol(condition)

    /**
     * @include [CommonSingleDocs]
     * @setArg [CommonSingleDocs.Examples]
     * `df.`[select][DataFrame.select]` { Type::myColumnGroup.`[singleCol][SingleColumn.singleCol]` { it.`[name][ColumnReference.name]`().`[startsWith][String.startsWith]`("year") } }`
     *
     * `df.`[select][DataFrame.select]` { DataSchemaType::myColumnGroup.`[singleCol][KProperty.singleCol]`() }`
     */
    public fun KProperty<*>.singleCol(condition: ColumnFilter<*> = { true }): TransformableSingleColumn<*> =
        columnGroup(this).singleCol(condition)

    /**
     * @include [CommonSingleDocs]
     * @setArg [CommonSingleDocs.Examples]
     * `df.`[select][DataFrame.select]` { "pathTo"["myColumnGroup"].`[singleCol][ColumnPath.singleCol]` { it.`[name][ColumnReference.name]`().`[startsWith][String.startsWith]`("year") } }`
     */
    public fun ColumnPath.singleCol(condition: ColumnFilter<*> = { true }): TransformableSingleColumn<*> =
        columnGroup(this).singleCol(condition)
}

@Suppress("UNCHECKED_CAST")
internal fun <C> ColumnSet<C>.singleInternal(condition: ColumnFilter<C> = { true }) =
    (allColumnsInternal() as TransformableColumnSet<C>)
        .transform { listOf(it.single(condition)) }
        .singleOrNullWithTransformerImpl()

// endregion
