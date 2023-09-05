package org.jetbrains.kotlinx.dataframe.api

import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.Predicate
import org.jetbrains.kotlinx.dataframe.api.ValueColsColumnsSelectionDsl.Usage.ColumnGroupName
import org.jetbrains.kotlinx.dataframe.api.ValueColsColumnsSelectionDsl.Usage.ColumnSetName
import org.jetbrains.kotlinx.dataframe.api.ValueColsColumnsSelectionDsl.Usage.PlainDslName
import org.jetbrains.kotlinx.dataframe.columns.ColumnGroup
import org.jetbrains.kotlinx.dataframe.columns.ColumnPath
import org.jetbrains.kotlinx.dataframe.columns.ColumnReference
import org.jetbrains.kotlinx.dataframe.columns.ColumnSet
import org.jetbrains.kotlinx.dataframe.columns.ColumnWithPath
import org.jetbrains.kotlinx.dataframe.columns.ColumnsResolver
import org.jetbrains.kotlinx.dataframe.columns.SingleColumn
import org.jetbrains.kotlinx.dataframe.columns.ValueColumn
import org.jetbrains.kotlinx.dataframe.documentation.AccessApi
import org.jetbrains.kotlinx.dataframe.documentation.Indent
import org.jetbrains.kotlinx.dataframe.documentation.LineBreak
import org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate
import org.jetbrains.kotlinx.dataframe.impl.columns.TransformableColumnSet
import kotlin.reflect.KProperty

// region ColumnsSelectionDsl

public interface ValueColsColumnsSelectionDsl<out T> : ColumnsSelectionDslExtension<T> {

    /**
     * ## Value Columns Usage
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
     *  {@include [PlainDslName]}` [` **`{ `**{@include [UsageTemplate.ConditionRef]}**` \\}`** `]` `[ `{@include [AtAnyDepthColumnsSelectionDsl.Usage.Name]} ` ]`
     * }
     *
     * {@setArg [UsageTemplate.ColumnSetFunctionsArg]
     *  {@include [Indent]}{@include [ColumnSetName]}` [` **`{ `**{@include [UsageTemplate.ConditionRef]}**` \\}`** `]` `[ `{@include [AtAnyDepthColumnsSelectionDsl.Usage.Name]} ` ]`
     * }
     *
     * {@setArg [UsageTemplate.ColumnGroupFunctionsArg]
     *  {@include [Indent]}{@include [ColumnGroupName]}` [` **`{ `**{@include [UsageTemplate.ConditionRef]}**` \\}`** `]` `[ `{@include [AtAnyDepthColumnsSelectionDsl.Usage.Name]} ` ]`
     * }
     */
    public interface Usage {

        /** [**valueCols**][ColumnsSelectionDsl.valueCols] */
        public interface PlainDslName

        /** .[**valueCols**][ColumnsSelectionDsl.valueCols] */
        public interface ColumnSetName

        /** .[**valueCols**][ColumnsSelectionDsl.valueCols] */
        public interface ColumnGroupName
    }

    /**
     * ## Value Columns
     * Creates a subset of columns that are [ValueColumns][ValueColumn] from the current [ColumnSet].
     *
     * If the current [ColumnsResolver] is a [SingleColumn]
     * (and thus consists of only one column (or [column group][ColumnGroup])),
     * then [valueCols] will create a subset of its children.
     *
     * You can optionally use a [filter\] to only include certain columns.
     * [valueCols] can be called using any of the supported [APIs][AccessApi] (+ [ColumnPath]).
     *
     * Check out [Usage] for how to use [valueCols].
     *
     * #### For example:
     *
     * `df.`[select][DataFrame.select]` { `[valueCols][ColumnsSelectionDsl.valueCols]` { it.`[name][ColumnReference.name]`.`[startsWith][String.startsWith]`("my") } }`
     *
     * `df.`[select][DataFrame.select]` { `[valueCols][ColumnsSelectionDsl.valueCols]`().`[atAnyDepth][ColumnsSelectionDsl.atAnyDepth]`() }`
     *
     * `df.`[select][DataFrame.select]` { "myColGroup".`[valueCols][String.valueCols]`() }`
     *
     * #### Examples for this overload:
     *
     * {@getArg [CommonValueColsDocs.ExampleArg]}
     *
     * @param [filter\] An optional [predicate][Predicate] to filter the value columns by.
     * @return A ([transformable][TransformableColumnSet]) [ColumnSet] of [ValueColumns][ValueColumn].
     * @see [ColumnsSelectionDsl.colsOfKind\]
     * @see [ColumnsSelectionDsl.frameCols\]
     * @see [ColumnsSelectionDsl.colGroups\]
     * @see [ColumnsSelectionDsl.cols\]
     */
    private interface CommonValueColsDocs {

        /** Example argument */
        interface ExampleArg
    }

    /**
     * @include [CommonValueColsDocs]
     * @setArg [CommonValueColsDocs.ExampleArg]
     *
     * `df.`[select][DataFrame.select]` { `[cols][ColumnsSelectionDsl.cols]` { it.`[name][ColumnReference.name]`.`[startsWith][String.startsWith]`("my") }.`[valueCols][ColumnSet.valueCols]`() }`
     *
     * `// NOTE: This can be shortened to just:`
     *
     * `df.`[select][DataFrame.select]` { `[valueCols][ColumnsSelectionDsl.valueCols]` { it.`[name][ColumnReference.name]`.`[startsWith][String.startsWith]`("my") } }`
     */
    public fun ColumnSet<*>.valueCols(filter: Predicate<ValueColumn<*>> = { true }): TransformableColumnSet<*> =
        valueColumnsInternal(filter)

    /**
     * @include [CommonValueColsDocs]
     * @setArg [CommonValueColsDocs.ExampleArg]
     *
     * `df.`[select][DataFrame.select]` { `[valueCols][ColumnsSelectionDsl.valueCols]`() }`
     *
     * `df.`[select][DataFrame.select]` { `[valueCols][ColumnsSelectionDsl.valueCols]` { it.`[any][ColumnWithPath.any]` { it == "Alice" } } }`
     */
    public fun ColumnsSelectionDsl<*>.valueCols(filter: Predicate<ValueColumn<*>> = { true }): TransformableColumnSet<*> =
        asSingleColumn().valueColumnsInternal(filter)

    /**
     * @include [CommonValueColsDocs]
     * @setArg [CommonValueColsDocs.ExampleArg]
     *
     * `df.`[select][DataFrame.select]` { myColGroup.`[valueCols][SingleColumn.valueCols]`() }`
     *
     * `df.`[select][DataFrame.select]` { myColGroup.`[valueCols][SingleColumn.valueCols]` { it.`[any][ColumnWithPath.any]` { it == "Alice" } } }`
     */
    public fun SingleColumn<DataRow<*>>.valueCols(filter: Predicate<ValueColumn<*>> = { true }): TransformableColumnSet<*> =
        this.ensureIsColumnGroup().valueColumnsInternal(filter)

    /**
     * @include [CommonValueColsDocs]
     * @setArg [CommonValueColsDocs.ExampleArg]
     *
     * `df.`[select][DataFrame.select]` { "myColGroup".`[valueCols][String.valueCols]` { it.`[any][ColumnWithPath.any]` { it == "Alice" } } }`
     *
     * `df.`[select][DataFrame.select]` { "myColGroup".`[valueCols][String.valueCols]`() }`
     */
    public fun String.valueCols(filter: Predicate<ValueColumn<*>> = { true }): TransformableColumnSet<*> =
        columnGroup(this).valueCols(filter)

    /**
     * @include [CommonValueColsDocs]
     * @setArg [CommonValueColsDocs.ExampleArg]
     *
     * `df.`[select][DataFrame.select]` { Type::myColumnGroup.`[valueCols][KProperty.valueCols]` { it.`[any][ColumnWithPath.any]` { it == "Alice" } } }`
     *
     * `df.`[select][DataFrame.select]` { Type::myColumnGroup.`[valueCols][KProperty.valueCols]`() }`
     *
     * `df.`[select][DataFrame.select]` { DataSchemaType::myColumnGroup.`[valueCols][KProperty.valueCols]`().`[atAnyDepth][ColumnsSelectionDsl.atAnyDepth]`() }`
     */
    public fun KProperty<*>.valueCols(filter: Predicate<ValueColumn<*>> = { true }): TransformableColumnSet<*> =
        columnGroup(this).valueCols(filter)

    /**
     * @include [CommonValueColsDocs]
     * @setArg [CommonValueColsDocs.ExampleArg]
     *
     * `df.`[select][DataFrame.select]` { "pathTo"["myGroupCol"].`[valueCols][ColumnPath.valueCols]`().`[atAnyDepth][ColumnsSelectionDsl.atAnyDepth]`() }`
     */
    public fun ColumnPath.valueCols(filter: Predicate<ValueColumn<*>> = { true }): TransformableColumnSet<*> =
        columnGroup(this).valueCols(filter)
}

/**
 * Returns a TransformableColumnSet containing the value columns that satisfy the given filter.
 *
 * @param filter The filter function to apply on each value column. Must accept a ValueColumn object and return a Boolean.
 * @return A [TransformableColumnSet] containing the value columns that satisfy the filter.
 */
internal fun ColumnsResolver<*>.valueColumnsInternal(filter: (ValueColumn<*>) -> Boolean): TransformableColumnSet<*> =
    colsInternal { it.isValueColumn() && filter(it.asValueColumn()) }

// endregion
