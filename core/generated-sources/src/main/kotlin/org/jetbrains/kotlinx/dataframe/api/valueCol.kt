package org.jetbrains.kotlinx.dataframe.api

import org.jetbrains.kotlinx.dataframe.AnyColumnGroupAccessor
import org.jetbrains.kotlinx.dataframe.ColumnGroupReference
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage.ColumnGroupName
import org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage.ColumnSetName
import org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage.PlainDslName
import org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor
import org.jetbrains.kotlinx.dataframe.columns.ColumnGroup
import org.jetbrains.kotlinx.dataframe.columns.ColumnPath
import org.jetbrains.kotlinx.dataframe.columns.ColumnSet
import org.jetbrains.kotlinx.dataframe.columns.ColumnWithPath
import org.jetbrains.kotlinx.dataframe.columns.SingleColumn
import org.jetbrains.kotlinx.dataframe.columns.ValueColumn
import org.jetbrains.kotlinx.dataframe.documentation.Indent
import org.jetbrains.kotlinx.dataframe.documentation.LineBreak
import org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate
import org.jetbrains.kotlinx.dataframe.impl.columns.getAt
import org.jetbrains.kotlinx.dataframe.impl.columns.onResolve
import org.jetbrains.kotlinx.dataframe.impl.columns.singleImpl
import org.jetbrains.kotlinx.dataframe.impl.columns.transformSingle
import kotlin.reflect.KProperty

// region ColumnsSelectionDsl

public interface ValueColColumnsSelectionDsl<out T> : ColumnsSelectionDslExtension<T> {

    /**
     * ## Value Col Usage
     *
     *
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     * `columnSet: `[ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet]`<*>`
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  `columnGroup: `[SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]`<`[DataRow][org.jetbrains.kotlinx.dataframe.DataRow]`<*>> | `[String][String]
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * `| `[KProperty][KProperty]`<*>` | `[ColumnPath][ColumnPath]
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  `column: `[ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]` | `[String][String]
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * `| `[KProperty][KProperty]`<*> | `[ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath]
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  `index: `[Int][Int]
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  `T: Column type`
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  ### In the plain DSL:
     *
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  [**valueCol**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.valueCol]`[`**`<`**[T][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnTypeDef]**`>`**`]`**`(`**[column][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnDef]` | `[index][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.IndexDef]**`)`**
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  ### On a [ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet]:
     *
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  [columnSet][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnSetDef]
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;.[**valueCol**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.valueCol]**`(`**[index][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.IndexDef]**`)`**
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  ### On a column group reference:
     *
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  [columnGroup][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnGroupDef]
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;.[**valueCol**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.valueCol]`[`**`<`**[T][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnTypeDef]**`>`**`]`**`(`**[column][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnDef]` | `[index][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.IndexDef]**`)`**
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     */
    public interface Usage {

        /** [**valueCol**][ColumnsSelectionDsl.valueCol] */
        public interface PlainDslName

        /** .[**valueCol**][ColumnsSelectionDsl.valueCol] */
        public interface ColumnSetName

        /** .[**valueCol**][ColumnsSelectionDsl.valueCol] */
        public interface ColumnGroupName
    }

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor] (or [SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath], [KProperty], or [ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage] for how to use [valueCol].
     *
     * #### For example:
     *
     * `df.`[select][DataFrame.select]` { `[valueCol][valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][DataFrame.select]` { `[valueCol][valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][DataFrame.select]` { myColumnGroup.`[valueCol][valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * {@getArg [CommonValueColDocs.ExampleArg]}
     *
     * To create a [ColumnAccessor] for another kind of column, take a look at the functions
     * [col][ColumnsSelectionDsl.col],
     * [colGroup][ColumnsSelectionDsl.colGroup],
     * and [frameCol][ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn].
     * @throws [IllegalStateException\] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException\] if the column with the given argument is not a value column.
     *
     * @see [valueColumn\]
     * @see [ColumnsSelectionDsl.colGroup\]
     * @see [ColumnsSelectionDsl.frameCol\]
     * @see [ColumnsSelectionDsl.col\]
     *
     */
    private interface CommonValueColDocs {

        /* Example argument, can be either {@include [SingleExample]} or {@include [DoubleExample]} */
        interface ExampleArg

        /**
         * `df.`[select][DataFrame.select]` { {@getArg [CommonValueColDocs.ReceiverArg]}`[valueCol][valueCol]`({@getArg [CommonValueColDocs.Arg]}) \\\\}`
         */
        interface SingleExample

        /**
         * `df.`[select][DataFrame.select]` { {@getArg [CommonValueColDocs.ReceiverArg]}`[valueCol][valueCol]`({@getArg [CommonValueColDocs.Arg]}) \\\\}`
         *
         * `df.`[select][DataFrame.select]` { {@getArg [CommonValueColDocs.ReceiverArg]}`[valueCol][valueCol]`<`[String][String]`>({@getArg [CommonValueColDocs.Arg]}) \\\\}`
         */
        interface DoubleExample

        /* Receiver argument for the example(s) */
        interface ReceiverArg

        /* Argument for the example(s) */
        interface Arg

        /* Optional note */
        interface Note

        /** @param [C\] The type of the value column. */
        interface ValueColumnTypeParam
    }

    // region reference

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { {@getArg [CommonValueColDocs.ReceiverArg]}`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(valueColumnA) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [col\] The [ColumnAccessor] pointing to the value column.
     * @include [CommonValueColDocs.ValueColumnTypeParam]
     */
    private interface ValueColReferenceDocs

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(valueColumnA) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [col] The [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] pointing to the value column.
     * @param [C] The type of the value column. 
     */
    public fun <C> valueCol(valueCol: ColumnAccessor<C>): ColumnAccessor<C> = valueCol.ensureIsValueColumn()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(valueColumnA) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [col] The [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] pointing to the value column.
     * @param [C] The type of the value column. 
     */
    public fun <C> SingleColumn<DataRow<*>>.valueCol(valueCol: ColumnAccessor<C>): SingleColumn<C> =
        this.ensureIsColumnGroup().transformSingle {
            val child = it.getChild(valueCol)
                ?: throw IllegalStateException("ValueColumn '${valueCol.path()}' not found in column group '${it.path}'")
            child.data.ensureIsValueColumn()
            listOf(child)
        }.singleImpl()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(valueColumnA) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [col] The [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] pointing to the value column.
     * @param [C] The type of the value column. 
     */
    public fun <C> AnyColumnGroupAccessor.valueCol(valueCol: ColumnAccessor<C>): ColumnAccessor<C> =
        this.ensureIsColumnGroup().valueColumn<C>(valueCol.path()).ensureIsValueColumn()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myColumnGroup".`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(valueColumnA) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [col] The [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] pointing to the value column.
     * @param [C] The type of the value column. 
     */
    public fun <C> String.valueCol(valueCol: ColumnAccessor<C>): ColumnAccessor<C> =
        columnGroup(this).ensureIsColumnGroup().valueColumn<C>(valueCol.path()).ensureIsValueColumn()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { Type::myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(valueColumnA) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [col] The [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] pointing to the value column.
     * @param [C] The type of the value column. 
     */
    public fun <C> KProperty<*>.valueCol(valueCol: ColumnAccessor<C>): ColumnAccessor<C> =
        columnGroup(this).ensureIsColumnGroup().valueColumn<C>(valueCol.path()).ensureIsValueColumn()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myColumnGroup"].`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(valueColumnA) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [col] The [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] pointing to the value column.
     * @param [C] The type of the value column. 
     */
    public fun <C> ColumnPath.valueCol(valueCol: ColumnAccessor<C>): ColumnAccessor<C> =
        columnGroup(this).ensureIsColumnGroup().valueColumn<C>(valueCol.path()).ensureIsValueColumn()

    // endregion

    // region name

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { {@getArg [CommonValueColDocs.ReceiverArg]}`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("valueColumnName") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { {@getArg [CommonValueColDocs.ReceiverArg]}`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColumnName") }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [name\] The name of the value column.
     */
    private interface ValueColNameDocs

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("valueColumnName") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColumnName") }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [name] The name of the value column. 
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("valueColUnTyped")
    public fun valueCol(name: String): ColumnAccessor<*> = valueColumn<Any?>(name).ensureIsValueColumn()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("valueColumnName") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColumnName") }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [name] The name of the value column. 
     * @param [C] The type of the value column.
     */
    public fun <C> valueCol(name: String): ColumnAccessor<C> = valueColumn<C>(name).ensureIsValueColumn()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("valueColumnName") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColumnName") }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [name] The name of the value column. 
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("valueColUnTyped")
    public fun SingleColumn<DataRow<*>>.valueCol(name: String): SingleColumn<*> =
        valueCol<Any?>(name)

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("valueColumnName") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColumnName") }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [name] The name of the value column. 
     * @param [C] The type of the value column.
     */
    public fun <C> SingleColumn<DataRow<*>>.valueCol(name: String): SingleColumn<C> =
        this.ensureIsColumnGroup().transformSingle {
            val child = it.getChild(name)?.cast<C>()
                ?: throw IllegalStateException("Value column '$name' not found in column group '${it.path}'")
            child.data.ensureIsValueColumn()
            listOf(child)
        }.singleImpl()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("valueColumnName") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColumnName") }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [name] The name of the value column. 
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("valueColUnTyped")
    public fun AnyColumnGroupAccessor.valueCol(name: String): ColumnAccessor<*> =
        valueCol<Any?>(name)

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("valueColumnName") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColumnName") }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [name] The name of the value column. 
     * @param [C] The type of the value column.
     */
    public fun <C> AnyColumnGroupAccessor.valueCol(name: String): ColumnAccessor<C> =
        this.ensureIsColumnGroup().valueColumn<C>(name).ensureIsValueColumn()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myColumnGroup".`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("valueColumnName") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myColumnGroup".`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColumnName") }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [name] The name of the value column. 
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("valueColUnTyped")
    public fun String.valueCol(name: String): ColumnAccessor<*> =
        valueCol<Any?>(name)

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myColumnGroup".`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("valueColumnName") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myColumnGroup".`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColumnName") }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [name] The name of the value column. 
     * @param [C] The type of the value column.
     */
    public fun <C> String.valueCol(name: String): ColumnAccessor<C> =
        columnGroup(this).ensureIsColumnGroup().valueColumn<C>(name).ensureIsValueColumn()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { Type::myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("valueColumnName") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { Type::myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColumnName") }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [name] The name of the value column. 
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("valueColUnTyped")
    public fun KProperty<*>.valueCol(name: String): ColumnAccessor<*> =
        valueCol<Any?>(name)

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { Type::myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("valueColumnName") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { Type::myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColumnName") }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [name] The name of the value column. 
     * @param [C] The type of the value column.
     */
    public fun <C> KProperty<*>.valueCol(name: String): ColumnAccessor<C> =
        columnGroup(this).ensureIsColumnGroup().valueColumn<C>(name).ensureIsValueColumn()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myColumnGroup"].`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("valueColumnName") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myColumnGroup"].`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColumnName") }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [name] The name of the value column. 
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("valueColUnTyped")
    public fun ColumnPath.valueCol(name: String): ColumnAccessor<*> =
        valueCol<Any?>(name)

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myColumnGroup"].`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("valueColumnName") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myColumnGroup"].`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColumnName") }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [name] The name of the value column. 
     * @param [C] The type of the value column.
     */
    public fun <C> ColumnPath.valueCol(name: String): ColumnAccessor<C> =
        columnGroup(this).ensureIsColumnGroup().valueColumn<C>(name).ensureIsValueColumn()

    // endregion

    // region path

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { {@getArg [CommonValueColDocs.ReceiverArg]}`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("pathTo"["valueColumnName"] ) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { {@getArg [CommonValueColDocs.ReceiverArg]}`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("pathTo"["valueColumnName"] ) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [path\] The path to the value column.
     */
    private interface ValueColPathDocs

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("pathTo"["valueColumnName"] ) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("pathTo"["valueColumnName"] ) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [path] The path to the value column. 
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("valueColUnTyped")
    public fun valueCol(path: ColumnPath): ColumnAccessor<*> = valueColumn<Any?>(path).ensureIsValueColumn()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("pathTo"["valueColumnName"] ) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("pathTo"["valueColumnName"] ) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [path] The path to the value column. 
     * @param [C] The type of the value column.
     */
    public fun <C> valueCol(path: ColumnPath): ColumnAccessor<C> = valueColumn<C>(path).ensureIsValueColumn()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("pathTo"["valueColumnName"] ) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("pathTo"["valueColumnName"] ) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [path] The path to the value column. 
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("valueColUnTyped")
    public fun SingleColumn<DataRow<*>>.valueCol(path: ColumnPath): SingleColumn<*> =
        valueCol<Any?>(path)

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("pathTo"["valueColumnName"] ) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("pathTo"["valueColumnName"] ) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [path] The path to the value column. 
     * @param [C] The type of the value column.
     */
    public fun <C> SingleColumn<DataRow<*>>.valueCol(path: ColumnPath): SingleColumn<C> =
        this.ensureIsColumnGroup().transformSingle {
            val child = it.getChild(path)?.cast<C>()
                ?: throw IllegalStateException("Value column '$path' not found in column group '${it.path}'")
            child.data.ensureIsValueColumn()
            listOf(child)
        }.singleImpl()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("pathTo"["valueColumnName"] ) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("pathTo"["valueColumnName"] ) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [path] The path to the value column. 
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("valueColUnTyped")
    public fun AnyColumnGroupAccessor.valueCol(path: ColumnPath): ColumnAccessor<*> =
        valueCol<Any?>(path)

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("pathTo"["valueColumnName"] ) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("pathTo"["valueColumnName"] ) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [path] The path to the value column. 
     * @param [C] The type of the value column.
     */
    public fun <C> AnyColumnGroupAccessor.valueCol(path: ColumnPath): ColumnAccessor<C> =
        this.ensureIsColumnGroup().valueColumn<C>(path).ensureIsValueColumn()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myColumnGroup".`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("pathTo"["valueColumnName"] ) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myColumnGroup".`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("pathTo"["valueColumnName"] ) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [path] The path to the value column. 
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("valueColUnTyped")
    public fun String.valueCol(path: ColumnPath): ColumnAccessor<*> =
        valueCol<Any?>(path)

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myColumnGroup".`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("pathTo"["valueColumnName"] ) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myColumnGroup".`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("pathTo"["valueColumnName"] ) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [path] The path to the value column. 
     * @param [C] The type of the value column.
     */
    public fun <C> String.valueCol(path: ColumnPath): ColumnAccessor<C> =
        columnGroup(this).ensureIsColumnGroup().valueColumn<C>(path).ensureIsValueColumn()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { Type::myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("pathTo"["valueColumnName"] ) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { Type::myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("pathTo"["valueColumnName"] ) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [path] The path to the value column. 
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("valueColUnTyped")
    public fun KProperty<*>.valueCol(path: ColumnPath): ColumnAccessor<*> =
        valueCol<Any?>(path)

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { Type::myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("pathTo"["valueColumnName"] ) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { Type::myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("pathTo"["valueColumnName"] ) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [path] The path to the value column. 
     * @param [C] The type of the value column.
     */
    public fun <C> KProperty<*>.valueCol(path: ColumnPath): ColumnAccessor<C> =
        columnGroup(this).ensureIsColumnGroup().valueColumn<C>(path).ensureIsValueColumn()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myColumnGroup"].`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("pathTo"["valueColumnName"] ) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myColumnGroup"].`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("pathTo"["valueColumnName"] ) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [path] The path to the value column. 
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("valueColUnTyped")
    public fun ColumnPath.valueCol(path: ColumnPath): ColumnAccessor<*> =
        valueCol<Any?>(path)

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myColumnGroup"].`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`("pathTo"["valueColumnName"] ) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myColumnGroup"].`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("pathTo"["valueColumnName"] ) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [path] The path to the value column. 
     * @param [C] The type of the value column.
     */
    public fun <C> ColumnPath.valueCol(path: ColumnPath): ColumnAccessor<C> =
        columnGroup(this).ensureIsColumnGroup().valueColumn<C>(path).ensureIsValueColumn()

    // endregion

    // region property

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { {@getArg [CommonValueColDocs.ReceiverArg]}`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(Type::valueColumnA) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [property\] The [KProperty] reference to the value column.
     * @include [CommonValueColDocs.ValueColumnTypeParam]
     */
    private interface ValueColKPropertyDocs

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(Type::valueColumnA) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [property] The [KProperty] reference to the value column.
     * @param [C] The type of the value column. 
     */
    public fun <C> valueCol(property: KProperty<C>): SingleColumn<C> = valueColumn(property).ensureIsValueColumn()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(Type::valueColumnA) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [property] The [KProperty] reference to the value column.
     * @param [C] The type of the value column. 
     */
    public fun <C> SingleColumn<DataRow<*>>.valueCol(property: KProperty<C>): SingleColumn<C> =
        valueCol<C>(property.name)

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(Type::valueColumnA) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [property] The [KProperty] reference to the value column.
     * @param [C] The type of the value column. 
     */
    public fun <C> AnyColumnGroupAccessor.valueCol(property: KProperty<C>): ColumnAccessor<C> =
        this.ensureIsColumnGroup().valueColumn(property).ensureIsValueColumn()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myColumnGroup".`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(Type::valueColumnA) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [property] The [KProperty] reference to the value column.
     * @param [C] The type of the value column. 
     */
    public fun <C> String.valueCol(property: KProperty<C>): ColumnAccessor<C> =
        columnGroup(this).ensureIsColumnGroup().valueColumn(property).ensureIsValueColumn()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { Type::myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(Type::valueColumnA) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [property] The [KProperty] reference to the value column.
     * @param [C] The type of the value column. 
     */
    public fun <C> KProperty<*>.valueCol(property: KProperty<C>): ColumnAccessor<C> =
        columnGroup(this).ensureIsColumnGroup().valueColumn(property).ensureIsValueColumn()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myColumnGroup"].`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(Type::valueColumnA) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [property] The [KProperty] reference to the value column.
     * @param [C] The type of the value column. 
     */
    public fun <C> ColumnPath.valueCol(property: KProperty<C>): ColumnAccessor<C> =
        columnGroup(this).ensureIsColumnGroup().valueColumn(property).ensureIsValueColumn()

    // endregion

    // region index

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { {@getArg [CommonValueColDocs.ReceiverArg]}`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(0) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { {@getArg [CommonValueColDocs.ReceiverArg]}`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>(0) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [index\] The index of the value column.
     * @throws [IndexOutOfBoundsException\] if the index is out of bounds.
     */
    private interface ValueColIndexDocs

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[colsOf][ColumnsSelectionDsl.colsOf]`<`[Int][Int]`>().`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(0) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [index] The index of the value column.
     * @throws [IndexOutOfBoundsException] if the index is out of bounds. 
     * @param [C] The type of the value column.
     *
     */
    public fun <C> ColumnSet<C>.valueCol(index: Int): SingleColumn<C> = getAt(index).ensureIsValueColumn()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(0) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>(0) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [index] The index of the value column.
     * @throws [IndexOutOfBoundsException] if the index is out of bounds. 
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("valueColUnTyped")
    public fun ColumnsSelectionDsl<*>.valueCol(index: Int): SingleColumn<*> =
        valueCol<Any?>(index)

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(0) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>(0) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [index] The index of the value column.
     * @throws [IndexOutOfBoundsException] if the index is out of bounds. 
     * @param [C] The type of the value column.
     */
    public fun <C> ColumnsSelectionDsl<*>.valueCol(index: Int): SingleColumn<C> =
        asSingleColumn().valueCol<C>(index)

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(0) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>(0) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [index] The index of the value column.
     * @throws [IndexOutOfBoundsException] if the index is out of bounds. 
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("valueColUnTyped")
    public fun SingleColumn<DataRow<*>>.valueCol(index: Int): SingleColumn<*> =
        valueCol<Any?>(index)

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(0) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>(0) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [index] The index of the value column.
     * @throws [IndexOutOfBoundsException] if the index is out of bounds. 
     * @param [C] The type of the value column.
     */
    public fun <C> SingleColumn<DataRow<*>>.valueCol(index: Int): SingleColumn<C> =
        this.ensureIsColumnGroup()
            .allColumnsInternal()
            .getAt(index)
            .ensureIsValueColumn()
            .cast()

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myColumnGroup".`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(0) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myColumnGroup".`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>(0) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [index] The index of the value column.
     * @throws [IndexOutOfBoundsException] if the index is out of bounds. 
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("valueColUnTyped")
    public fun String.valueCol(index: Int): SingleColumn<*> =
        valueCol<Any?>(index)

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myColumnGroup".`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(0) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myColumnGroup".`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>(0) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [index] The index of the value column.
     * @throws [IndexOutOfBoundsException] if the index is out of bounds. 
     * @param [C] The type of the value column.
     */
    public fun <C> String.valueCol(index: Int): SingleColumn<C> =
        columnGroup(this).valueCol<C>(index)

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { Type::myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(0) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { Type::myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>(0) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [index] The index of the value column.
     * @throws [IndexOutOfBoundsException] if the index is out of bounds. 
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("valueColUnTyped")
    public fun KProperty<*>.valueCol(index: Int): SingleColumn<*> =
        valueCol<Any?>(index)

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { Type::myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(0) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { Type::myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>(0) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [index] The index of the value column.
     * @throws [IndexOutOfBoundsException] if the index is out of bounds. 
     * @param [C] The type of the value column.
     */
    public fun <C> KProperty<*>.valueCol(index: Int): SingleColumn<C> =
        columnGroup(this).valueCol<C>(index)

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myColumnGroup"].`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(0) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myColumnGroup"].`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>(0) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [index] The index of the value column.
     * @throws [IndexOutOfBoundsException] if the index is out of bounds. 
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("valueColUnTyped")
    public fun ColumnPath.valueCol(index: Int): SingleColumn<*> =
        valueCol<Any?>(index)

    /**
     * ## Value Col
     *
     * Creates a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] (or [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]) for a value column with the given argument which can be either
     * an index ([Int]) or a reference to a column ([String], [ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath], [KProperty], or [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]).
     *
     * This is a DSL-shorthand for [valueColumn][org.jetbrains.kotlinx.dataframe.api.valueColumn] and can be both typed and untyped (in case you're supplying
     * a column name, -path, or index). In addition, extra runtime checks are in place to ensure that the column
     * you specify is actually a value column.
     * The function can also be called on [ColumnGroups][org.jetbrains.kotlinx.dataframe.ColumnGroupReference] to create
     * an accessor for a value column inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *
     * Check out [Usage][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.Usage] for how to use [valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol].
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>("valueColA") }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(SomeType::valueColB) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColumnGroup.`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(1) }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myColumnGroup"].`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`(0) }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myColumnGroup"].`[valueCol][org.jetbrains.kotlinx.dataframe.api.ValueColColumnsSelectionDsl.valueCol]`<`[String][String]`>(0) }`
     *
     * To create a [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for another kind of column, take a look at the functions
     * [col][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col],
     * [colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup],
     * and [frameCol][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol].
     *
     * @return A [ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor] for the value column with the given argument if possible, else a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn].
     * @throws [IllegalStateException] if the column with the given argument does not exist.
     * @throws [IllegalArgumentException] if the column with the given argument is not a value column.
     *
     * @see [valueColumn]
     * @see [ColumnsSelectionDsl.colGroup]
     * @see [ColumnsSelectionDsl.frameCol]
     * @see [ColumnsSelectionDsl.col]
     *
     *
     *
     * @param [index] The index of the value column.
     * @throws [IndexOutOfBoundsException] if the index is out of bounds. 
     * @param [C] The type of the value column.
     */
    public fun <C> ColumnPath.valueCol(index: Int): SingleColumn<C> =
        columnGroup(this).valueCol<C>(index)

    // endregion
}

/**
 * Checks the validity of this [SingleColumn],
 * by adding a check to see it's a [ValueColumn] (so, a [SingleColumn]<*>)
 * and throwing an [IllegalArgumentException] if it's not.
 */
internal fun <C> SingleColumn<C>.ensureIsValueColumn(): SingleColumn<C> =
    onResolve { col: ColumnWithPath<*>? ->
        require(col?.isValueColumn() != false) {
            "Column at ${col?.path} is not a ValueColumn, but a ${col?.kind()}."
        }
    }

/** Checks the validity of this [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn],
 * by adding a check to see it's a [ValueColumn][org.jetbrains.kotlinx.dataframe.columns.ValueColumn] (so, a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]<*>)
 * and throwing an [IllegalArgumentException] if it's not. */
internal fun <C> ColumnAccessor<C>.ensureIsValueColumn(): ColumnAccessor<C> =
    onResolve { col: ColumnWithPath<*>? ->
        require(col?.isValueColumn() != false) {
            "Column at ${col?.path} is not a ValueColumn, but a ${col?.kind()}."
        }
    }

// endregion
