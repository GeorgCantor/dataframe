package org.jetbrains.kotlinx.dataframe.api

import org.jetbrains.kotlinx.dataframe.ColumnsSelector
import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.Usage
import org.jetbrains.kotlinx.dataframe.columns.ColumnGroup
import org.jetbrains.kotlinx.dataframe.columns.ColumnPath
import org.jetbrains.kotlinx.dataframe.columns.ColumnReference
import org.jetbrains.kotlinx.dataframe.columns.ColumnSet
import org.jetbrains.kotlinx.dataframe.columns.ColumnsResolver
import org.jetbrains.kotlinx.dataframe.columns.SingleColumn
import org.jetbrains.kotlinx.dataframe.columns.toColumnSet
import org.jetbrains.kotlinx.dataframe.documentation.AccessApi
import org.jetbrains.kotlinx.dataframe.documentation.ColumnExpression
import org.jetbrains.kotlinx.dataframe.documentation.DocumentationUrls
import org.jetbrains.kotlinx.dataframe.documentation.Indent
import org.jetbrains.kotlinx.dataframe.documentation.LineBreak
import org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl
import org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate
import org.jetbrains.kotlinx.dataframe.impl.columns.ColumnsList
import org.jetbrains.kotlinx.dataframe.impl.columns.changePath
import org.jetbrains.kotlinx.dataframe.impl.columns.createColumnSet
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Referring to or expressing column(s) in the selection DSL can be done in several ways corresponding to all
 * [Access APIs][AccessApi]:
 * TODO: [Issue #286](https://github.com/Kotlin/dataframe/issues/286)
 *
 * [See Column Selectors on the documentation website.](https://kotlin.github.io/dataframe/columnselectors.html)
 */
private interface CommonColumnSelectionDocs

/**
 *
 */
private interface CommonColumnSelectionExamples

/** [Columns Selection DSL][ColumnsSelectionDsl] */
internal interface ColumnsSelectionDslLink

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal fun <T> ColumnsSelectionDsl<T>.asSingleColumn(): SingleColumn<DataRow<T>> =
    this as SingleColumn<DataRow<T>>

/** Interface reserved for all extensions of [ColumnsSelectionDslExtension] (and [AtAnyDepthDsl]). */
public sealed interface ColumnsSelectionDslExtension<out T> {
    public val scope: Scope
}

public enum class Scope(public val kClass: KClass<*>) {
    COLUMNS_SELECTION_DSL(ColumnSelectionDsl::class),
    AT_ANY_DEPTH_DSL(AtAnyDepthDsl::class)
}

@PublishedApi
internal val <T> ColumnsSelectionDslExtension<T>.context: ColumnsSelectionDsl<T>
    get() = when (scope) {
        Scope.COLUMNS_SELECTION_DSL -> this as ColumnsSelectionDsl<T>
        Scope.AT_ANY_DEPTH_DSL -> (this as AtAnyDepthDsl<T>).context
    }

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
public annotation class ColumnsSelectionDslMarker

/**
 * Referring to or expressing column(s) in the selection DSL can be done in several ways corresponding to all
 * [Access APIs][org.jetbrains.kotlinx.dataframe.documentation.AccessApi]:
 * TODO: [Issue #286](https://github.com/Kotlin/dataframe/issues/286)
 *
 * [See Column Selectors on the documentation website.](https://kotlin.github.io/dataframe/columnselectors.html)
 *
 * Can be safely cast to [SingleColumn] across the library. It does not directly
 * implement it for DSL purposes.
 *
 * See [Usage] for the DSL Grammar of the ColumnsSelectionDsl.
 */
@ColumnsSelectionDslMarker
public interface ColumnsSelectionDsl<out T> : /* SingleColumn<DataRow<T>> */
    ColumnSelectionDsl<T>,

    // first {}, firstCol()
    FirstColumnsSelectionDsl<T>,
    // last {}, lastCol()
    LastColumnsSelectionDsl<T>,
    // single {}, singleCol()
    SingleColumnsSelectionDsl<T>,

    // col(name), col(5), [5]
    ColColumnsSelectionDsl<T>,
    // valueCol(name), valueCol(5)
    ValueColColumnsSelectionDsl<T>,
    // frameCol(name), frameCol(5)
    FrameColColumnsSelectionDsl<T>,
    // colGroup(name), colGroup(5)
    ColGroupColumnsSelectionDsl<T>,

    // cols {}, cols(), cols(colA, colB), cols(1, 5), cols(1..5), [{}]
    ColsColumnsSelectionDsl<T>,

    // colA.."colB"
    ColumnRangeColumnsSelectionDsl<T>,

    // valueCols {}, valueCols()
    ValueColsColumnsSelectionDsl<T>,
    // frameCols {}, frameCols()
    FrameColsColumnsSelectionDsl<T>,
    // colGroups {}, colGroups()
    ColGroupsColumnsSelectionDsl<T>,
    // colsOfKind(Value, Frame) {}, colsOfKind(Value, Frame)
    ColsOfKindColumnsSelectionDsl<T>,

    // all(), allAfter(colA), allBefore(colA), allFrom(colA), allUpTo(colA)
    AllColumnsSelectionDsl<T>,
    // .atAnyDepth()
    AtAnyDepthColumnsSelectionDsl<T>,
    // TODO
    AtAnyDepth2ColumnsSelectionDsl<T>,
    // children {}, children()
    ChildrenColumnsSelectionDsl<T>,
    // take(5), takeLastChildren(2), takeLastWhile {}, takeChildrenWhile {}
    TakeColumnsSelectionDsl<T>,
    // drop(5), dropLastChildren(2), dropLastWhile {}, dropChildrenWhile {}
    DropColumnsSelectionDsl<T>,
    // except(), allExcept {}
    ExceptColumnsSelectionDsl<T>,
    // nameContains(""), childrenNameContains(""), nameStartsWith(""), childrenNameEndsWith("")
    ColumnNameFiltersColumnsSelectionDsl<T>,
    // withoutNulls(), childrenWithoutNulls()
    WithoutNullsColumnsSelectionDsl<T>,
    // distinct()
    DistinctColumnsSelectionDsl<T>,
    // none()
    NoneColumnsSelectionDsl<T>,
    // colsOf<>(), colsOf<> {}
    ColsOfColumnsSelectionDsl<T>,
    // roots()
    RootsColumnsSelectionDsl<T>,
    // filter {}, filterChildren {}
    FilterColumnsSelectionDsl<T>,
    // colSet and colB
    AndColumnsSelectionDsl<T>,
    // colA named "colB", colA into "colB"
    RenameColumnsSelectionDsl<T> {

    override val scope: Scope
        get() = Scope.COLUMNS_SELECTION_DSL

    /**
     * ## [ColumnsSelectionDsl] Usage
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
     *  `condition: `[ColumnFilter][org.jetbrains.kotlinx.dataframe.ColumnFilter]
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
     *  `indexRange: `[IntRange][IntRange]
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  `kind: `[ColumnKind][org.jetbrains.kotlinx.dataframe.columns.ColumnKind]
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  `colSelector: `[ColumnSelector][org.jetbrains.kotlinx.dataframe.ColumnSelector]
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  ### In the plain DSL:
     *
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  `(`
     *  [**first**][org.jetbrains.kotlinx.dataframe.api.FirstColumnsSelectionDsl.first]
     *  `|` [**last**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.last]
     *  `|` [**single**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.single]
     *  `) [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ConditionDef]**` }`** `] [ `.[**atAnyDepth**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.atAnyDepth]`()` ` ]`
     *
     *  `|` `(`
     *  [**col**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col]
     *  `|` [**valueCol**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.valueCol]
     *  `|` [**frameCol**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol]
     *  `|` [**colGroup**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup]
     *  `)[`**`<`**[T][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnTypeDef]**`>`**`]`**`(`**[column][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnDef]` | `[index][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.IndexDef]**`)`**
     *
     *  `|` [**cols**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols]`[`**`<`**[T][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnTypeDef]**`>`**`]`**`(`**[column][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnDef]`, .. | `[index][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.IndexDef]`, .. | `[indexRange][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.IndexRangeDef]**`)`**
     *
     *  `|` **`this`**`/`**`it`** [**`[`**][cols][column][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnDef]`, ..`[**`]`**][cols]
     *
     *  `|` `(` [**cols**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols]` [ `**` { `**[condition][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ConditionDef]**` } `**`] |  `**`this`**`/`**`it`** [**`[`**][cols]**`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ConditionDef]**` }`**[**`]`**][cols]` )` `[ `.[**atAnyDepth**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.atAnyDepth]`()` ` ]`
     *
     *  `|` `(`
     *  [**valueCols**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.valueCols]
     *  `|` [**frameCols**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroups]
     *  `|` [**colGroups**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroups]
     *  `) [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ConditionDef]**` }`** `] [ `.[**atAnyDepth**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.atAnyDepth]`()` ` ]`
     *
     *  `|` [**colsOfKind**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroups]**`(`**[kind][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnKindDef]`, ..`**`)`**` [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ConditionDef]**` }`** `]` `[ `.[**atAnyDepth**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.atAnyDepth]`()` ` ]`
     *
     *  `|` [column][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnDef] [**..**][org.jetbrains.kotlinx.dataframe.api.ColumnRangeColumnsSelectionDsl.rangeTo] [column][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnDef]
     *
     *  `|` [**all**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.all]**`()`**` [ `.[**atAnyDepth**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.atAnyDepth]`()` ` ]`
     *
     *  `|` **`all`**`(`[**Before**][org.jetbrains.kotlinx.dataframe.api.AllColumnsSelectionDsl.allColsBefore]`|`[**After**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.allAfter]`|`[**From**][org.jetbrains.kotlinx.dataframe.api.AllColumnsSelectionDsl.allColsFrom]`|`[**UpTo**][org.jetbrains.kotlinx.dataframe.api.AllColumnsSelectionDsl.allColsUpTo]`)` `(` **`(`**[column][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnDef]**`)`** `|` **`{`** [colSelector][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnSelectorDef] **`}`** `)`
     *
     *  `|` TODO
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
     *  &nbsp;&nbsp;&nbsp;&nbsp;`(`
     *  .[**first**][org.jetbrains.kotlinx.dataframe.api.FirstColumnsSelectionDsl.first]
     *  `|` .[**last**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.last]
     *  `|` .[**single**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.single]
     *  `) [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ConditionDef]**` }`** `] [ `.[**atAnyDepth**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.atAnyDepth]`()` ` ]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` `(`
     *  .[**col**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col]
     *  `|` .[**valueCol**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.valueCol]
     *  `|` .[**frameCol**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol]
     *  `|` .[**colGroup**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup]
     *  `)`**`(`**[index][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.IndexDef]**`)`**
     *  `|` [**`[`**][ColumnsSelectionDsl.col][index][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.IndexDef][**`]`**][ColumnsSelectionDsl.col]
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**cols**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols]**`(`**[index][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.IndexDef]`, .. | `[indexRange][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.IndexRangeDef]**`)`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` [**`[`**][cols][index][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.IndexDef]`, .. | `[indexRange][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.IndexRangeDef][**`]`**][cols]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` `(` .[**cols**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols]` [ `**` { `**[condition][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ConditionDef]**` } `**`] | `[**`[`**][cols]**`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ConditionDef]**` }`**[**`]`**][cols]` )` `[ `.[**atAnyDepth**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.atAnyDepth]`()` ` ]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` `(`
     *  .[**valueCol**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.valueCol]
     *  `|` .[**frameCol**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol]
     *  `|` .[**colGroup**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup]
     *  `) [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ConditionDef]**` }`** `] [ `.[**atAnyDepth**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.atAnyDepth]`()` ` ]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**colsOfKind**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroups]**`(`**[kind][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnKindDef]`, ..`**`)`**` [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ConditionDef]**` }`** `]` `[ `.[**atAnyDepth**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.atAnyDepth]`()` ` ]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**all**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.all]**`()`**` [ `.[**atAnyDepth**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.atAnyDepth]`()` ` ]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .**`all`**`(`[**Before**][org.jetbrains.kotlinx.dataframe.api.AllColumnsSelectionDsl.allColsBefore]`|`[**After**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.allAfter]`|`[**From**][org.jetbrains.kotlinx.dataframe.api.AllColumnsSelectionDsl.allColsFrom]`|`[**UpTo**][org.jetbrains.kotlinx.dataframe.api.AllColumnsSelectionDsl.allColsUpTo]`)` `(` **`(`**[column][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnDef]**`)`** `|` **`{`** [colSelector][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnSelectorDef] **`}`** `)`
     *  TODO debate whether these overloads make sense. They didn't exist in 0.9.0
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` TODO
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
     *  &nbsp;&nbsp;&nbsp;&nbsp;`(`
     *  .[**firstCol**][org.jetbrains.kotlinx.dataframe.api.FirstColumnsSelectionDsl.firstCol]
     *  `|` .[**lastCol**][org.jetbrains.kotlinx.dataframe.api.LastColumnsSelectionDsl.lastCol]
     *  `|` .[**singleCol**][org.jetbrains.kotlinx.dataframe.api.SingleColumnsSelectionDsl.singleCol]
     *  `) [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ConditionDef]**` }`** `] [ `.[**atAnyDepth**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.atAnyDepth]`()` ` ]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`| (`
     *  .[**col**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col]
     *  `|` .[**valueCol**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.valueCol]
     *  `|` .[**frameCol**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol]
     *  `|` .[**colGroup**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup]
     *  `)[`**`<`**[T][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnTypeDef]**`>`**`]`**`(`**[column][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnDef]` | `[index][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.IndexDef]**`)`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**cols**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols]`[`**`<`**[T][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnTypeDef]**`>`**`]`**`(`**[column][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnDef]`, .. | `[index][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.IndexDef]`, .. | `[indexRange][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.IndexRangeDef]**`)`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` [**`[`**][cols][column][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnDef]`, ..`[**`]`**][cols]
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` `(` .[**cols**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols]` [ `**` { `**[condition][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ConditionDef]**` } `**`] | `[**`[`**][cols]**`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ConditionDef]**` }`**[**`]`**][cols]` )` `[ `.[**atAnyDepth**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.atAnyDepth]`()` ` ]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` `(`
     *   .[**valueCol**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.valueCol]
     *   `|` .[**frameCol**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol]
     *   `|` .[**colGroup**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup]
     *   `) [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ConditionDef]**` }`** `] [ `.[**atAnyDepth**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.atAnyDepth]`()` ` ]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**colsOfKind**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroups]**`(`**[kind][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnKindDef]`, ..`**`)`**` [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ConditionDef]**` }`** `]` `[ `.[**atAnyDepth**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.atAnyDepth]`()` ` ]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**allCols**][org.jetbrains.kotlinx.dataframe.api.AllColumnsSelectionDsl.allCols]**`()`**` [ `.[**atAnyDepth**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.atAnyDepth]`()` ` ]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .**`allCols`**`(`[**Before**][org.jetbrains.kotlinx.dataframe.api.AllColumnsSelectionDsl.allColsBefore]`|`[**After**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.allAfter]`|`[**From**][org.jetbrains.kotlinx.dataframe.api.AllColumnsSelectionDsl.allColsFrom]`|`[**UpTo**][org.jetbrains.kotlinx.dataframe.api.AllColumnsSelectionDsl.allColsUpTo]`)` `(` **`(`**[column][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnDef]**`)`** `|` **`{`** [colSelector][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnSelectorDef] **`}`** `)`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` TODO
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
     *
     */
    public interface Usage

    /**
     * Invokes the given [ColumnsSelector] using this [ColumnsSelectionDsl].
     */
    public operator fun <C> ColumnsSelector<T, C>.invoke(): ColumnsResolver<C> =
        this(this@ColumnsSelectionDsl, this@ColumnsSelectionDsl)

    // TODO add docs `this { age } / it { age }`
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("invokeColumnsSelector")
    public operator fun <C> invoke(selection: ColumnsSelector<T, C>): ColumnsResolver<C> = selection()

    /**
     * ## Columns by Index Range from List of Columns
     * Helper function to create a [ColumnSet] from a list of columns by specifying a range of indices.
     *
     *
     */
    public operator fun <C> List<DataColumn<C>>.get(range: IntRange): ColumnSet<C> =
        ColumnsList(subList(range.first, range.last + 1))

    // region select
    // TODO due to String.invoke conflict this cannot be moved out

    /**
     * ## Select from [ColumnGroup]
     *
     * Perform a selection of columns using the [Columns Selection DSL][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl] on
     * any [ColumnGroup]. This is more powerful than [ColumnsSelectionDsl.cols], because all operations of
     * the DSL are at your disposal.
     *
     * The [invoke][SingleColumn.invoke] operator is overloaded to work as a shortcut for this method.
     *
     * #### For example:
     *
     * `df.`[select][DataFrame.select]` { myColGroup.`[select][SingleColumn.select]` { someCol `[and][ColumnsSelectionDsl.and]` `[colsOf][SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][DataFrame.select]` { "myGroupCol" `[{][String.select]` "colA" and `[expr][ColumnsSelectionDsl.expr]` { 0 } `[}][String.select]` }`
     *
     * `df.`[select][DataFrame.select]` { "pathTo"["myGroupCol"].`[select][ColumnPath.select]` { "colA" and "colB" } }`
     *
     * `df.`[select][DataFrame.select]` { it["myGroupCol"].`[asColumnGroup][DataColumn.asColumnGroup]`()`[() {][SingleColumn.select]` "colA" and "colB" `[}][SingleColumn.select]` }`
     *
     * #### Examples for this overload:
     *
     * {@getArg [CommonSelectDocs.ExampleArg]}
     *
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     * See also [except][ColumnsSelectionDsl.except]/[allExcept][ColumnsSelectionDsl.allExcept] for the inverted operation of this function.
     *
     * @param [selector\] The [ColumnsSelector] to use for the selection.
     * @receiver The [ColumnGroup] to select from.
     * @throws [IllegalArgumentException\] If [this\] is not a [ColumnGroup].
     * @return A [ColumnSet] containing the columns selected by [selector\].
     * @see [SingleColumn.except\]
     */
    private interface CommonSelectDocs {

        interface ExampleArg
    }

    /**
     * ## Select from [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]
     *
     * Perform a selection of columns using the [Columns Selection DSL][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl] on
     * any [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]. This is more powerful than [ColumnsSelectionDsl.cols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols], because all operations of
     * the DSL are at your disposal.
     *
     * The [invoke][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.invoke] operator is overloaded to work as a shortcut for this method.
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColGroup.`[select][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` { someCol `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myGroupCol" `[{][kotlin.String.select]` "colA" and `[expr][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.expr]` { 0 } `[}][kotlin.String.select]` }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myGroupCol"].`[select][org.jetbrains.kotlinx.dataframe.columns.ColumnPath.select]` { "colA" and "colB" } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { it["myGroupCol"].`[asColumnGroup][org.jetbrains.kotlinx.dataframe.DataColumn.asColumnGroup]`()`[() {][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` "colA" and "colB" `[}][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][DataFrame.select]` { myColGroup.`[select][SingleColumn.select]` { someCol `[and][ColumnsSelectionDsl.and]` `[colsOf][SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][DataFrame.select]` { myColGroup `[{][SingleColumn.select]` colA `[and][ColumnsSelectionDsl.and]` colB `[}][SingleColumn.select]` }`
     *
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     * See also [except][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.except]/[allExcept][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.allExcept] for the inverted operation of this function.
     *
     * @param [selector] The [ColumnsSelector][org.jetbrains.kotlinx.dataframe.ColumnsSelector] to use for the selection.
     * @receiver The [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup] to select from.
     * @throws [IllegalArgumentException] If [this] is not a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     * @return A [ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet] containing the columns selected by [selector].
     * @see [SingleColumn.except]
     */
    @Suppress("UNCHECKED_CAST")
    public fun <C, R> SingleColumn<DataRow<C>>.select(selector: ColumnsSelector<C, R>): ColumnSet<R> =
        createColumnSet { context ->
            this.ensureIsColumnGroup().resolveSingle(context)?.let { col ->
                require(col.isColumnGroup()) {
                    "Column ${col.path} is not a ColumnGroup and can thus not be selected from."
                }

                col.asColumnGroup()
                    .getColumnsWithPaths(selector as ColumnsSelector<*, R>)
                    .map { it.changePath(col.path + it.path) }
            } ?: emptyList()
        }

    /** ## Select from [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]
     *
     * Perform a selection of columns using the [Columns Selection DSL][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl] on
     * any [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]. This is more powerful than [ColumnsSelectionDsl.cols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols], because all operations of
     * the DSL are at your disposal.
     *
     * The [invoke][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.invoke] operator is overloaded to work as a shortcut for this method.
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColGroup.`[select][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` { someCol `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myGroupCol" `[{][kotlin.String.select]` "colA" and `[expr][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.expr]` { 0 } `[}][kotlin.String.select]` }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myGroupCol"].`[select][org.jetbrains.kotlinx.dataframe.columns.ColumnPath.select]` { "colA" and "colB" } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { it["myGroupCol"].`[asColumnGroup][org.jetbrains.kotlinx.dataframe.DataColumn.asColumnGroup]`()`[() {][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` "colA" and "colB" `[}][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColGroup.`[select][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` { someCol `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColGroup `[{][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` colA `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` colB `[}][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` }`
     *
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     * See also [except][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.except]/[allExcept][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.allExcept] for the inverted operation of this function.
     *
     * @param [selector] The [ColumnsSelector][org.jetbrains.kotlinx.dataframe.ColumnsSelector] to use for the selection.
     * @receiver The [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup] to select from.
     * @throws [IllegalArgumentException] If [this] is not a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     * @return A [ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet] containing the columns selected by [selector].
     * @see [SingleColumn.except]
     */
    public operator fun <C, R> SingleColumn<DataRow<C>>.invoke(selector: ColumnsSelector<C, R>): ColumnSet<R> =
        select(selector)

    /**
     * ## Select from [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]
     *
     * Perform a selection of columns using the [Columns Selection DSL][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl] on
     * any [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]. This is more powerful than [ColumnsSelectionDsl.cols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols], because all operations of
     * the DSL are at your disposal.
     *
     * The [invoke][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.invoke] operator is overloaded to work as a shortcut for this method.
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColGroup.`[select][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` { someCol `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myGroupCol" `[{][kotlin.String.select]` "colA" and `[expr][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.expr]` { 0 } `[}][kotlin.String.select]` }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myGroupCol"].`[select][org.jetbrains.kotlinx.dataframe.columns.ColumnPath.select]` { "colA" and "colB" } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { it["myGroupCol"].`[asColumnGroup][org.jetbrains.kotlinx.dataframe.DataColumn.asColumnGroup]`()`[() {][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` "colA" and "colB" `[}][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][DataFrame.select]` { `[colGroup][ColumnsSelectionDsl.colGroup]`(Type::myColGroup).`[select][SingleColumn.select]` { someCol `[and][ColumnsSelectionDsl.and]` `[colsOf][SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][DataFrame.select]` { `[colGroup][ColumnsSelectionDsl.colGroup]`(Type::myColGroup)`[() `{`][SingleColumn.select]` colA `[and][ColumnsSelectionDsl.and]` colB `[`}`][SingleColumn.select]` }`
     *
     * `df.`[select][DataFrame.select]` { Type::myColGroup.`[select][SingleColumn.select]` { colA `[and][ColumnsSelectionDsl.and]` colB } }`
     *
     * `df.`[select][DataFrame.select]` { DataSchemaType::myColGroup.`[select][KProperty.select]` { colA `[and][ColumnsSelectionDsl.and]` colB } }`
     *
     * `df.`[select][DataFrame.select]` { DataSchemaType::myColGroup `[`{`][KProperty.select]` colA `[and][ColumnsSelectionDsl.and]` colB `[`}`][KProperty.select]` }`
     *
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     * See also [except][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.except]/[allExcept][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.allExcept] for the inverted operation of this function.
     *
     * @param [selector] The [ColumnsSelector][org.jetbrains.kotlinx.dataframe.ColumnsSelector] to use for the selection.
     * @receiver The [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup] to select from.
     * @throws [IllegalArgumentException] If [this] is not a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     * @return A [ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet] containing the columns selected by [selector].
     * @see [SingleColumn.except]
     */
    public fun <C, R> KProperty<DataRow<C>>.select(selector: ColumnsSelector<C, R>): ColumnSet<R> =
        columnGroup(this).select(selector)

    /** ## Select from [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]
     *
     * Perform a selection of columns using the [Columns Selection DSL][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl] on
     * any [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]. This is more powerful than [ColumnsSelectionDsl.cols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols], because all operations of
     * the DSL are at your disposal.
     *
     * The [invoke][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.invoke] operator is overloaded to work as a shortcut for this method.
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColGroup.`[select][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` { someCol `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myGroupCol" `[{][kotlin.String.select]` "colA" and `[expr][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.expr]` { 0 } `[}][kotlin.String.select]` }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myGroupCol"].`[select][org.jetbrains.kotlinx.dataframe.columns.ColumnPath.select]` { "colA" and "colB" } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { it["myGroupCol"].`[asColumnGroup][org.jetbrains.kotlinx.dataframe.DataColumn.asColumnGroup]`()`[() {][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` "colA" and "colB" `[}][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup]`(Type::myColGroup).`[select][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` { someCol `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[colGroup][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup]`(Type::myColGroup)`[() `{`][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` colA `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` colB `[`}`][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { Type::myColGroup.`[select][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` { colA `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` colB } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { DataSchemaType::myColGroup.`[select][kotlin.reflect.KProperty.select]` { colA `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` colB } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { DataSchemaType::myColGroup `[`{`][kotlin.reflect.KProperty.select]` colA `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` colB `[`}`][kotlin.reflect.KProperty.select]` }`
     *
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     * See also [except][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.except]/[allExcept][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.allExcept] for the inverted operation of this function.
     *
     * @param [selector] The [ColumnsSelector][org.jetbrains.kotlinx.dataframe.ColumnsSelector] to use for the selection.
     * @receiver The [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup] to select from.
     * @throws [IllegalArgumentException] If [this] is not a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     * @return A [ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet] containing the columns selected by [selector].
     * @see [SingleColumn.except]
     */
    public operator fun <C, R> KProperty<DataRow<C>>.invoke(selector: ColumnsSelector<C, R>): ColumnSet<R> =
        select(selector)

    /**
     * ## Select from [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]
     *
     * Perform a selection of columns using the [Columns Selection DSL][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl] on
     * any [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]. This is more powerful than [ColumnsSelectionDsl.cols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols], because all operations of
     * the DSL are at your disposal.
     *
     * The [invoke][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.invoke] operator is overloaded to work as a shortcut for this method.
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColGroup.`[select][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` { someCol `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myGroupCol" `[{][kotlin.String.select]` "colA" and `[expr][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.expr]` { 0 } `[}][kotlin.String.select]` }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myGroupCol"].`[select][org.jetbrains.kotlinx.dataframe.columns.ColumnPath.select]` { "colA" and "colB" } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { it["myGroupCol"].`[asColumnGroup][org.jetbrains.kotlinx.dataframe.DataColumn.asColumnGroup]`()`[() {][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` "colA" and "colB" `[}][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][DataFrame.select]` { "myColGroup".`[select][String.select]` { someCol `[and][ColumnsSelectionDsl.and]` `[colsOf][SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][DataFrame.select]` { "myColGroup" `[{][String.select]` colA `[and][ColumnsSelectionDsl.and]` colB `[}][String.select]` }`
     *
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     * See also [except][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.except]/[allExcept][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.allExcept] for the inverted operation of this function.
     *
     * @param [selector] The [ColumnsSelector][org.jetbrains.kotlinx.dataframe.ColumnsSelector] to use for the selection.
     * @receiver The [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup] to select from.
     * @throws [IllegalArgumentException] If [this] is not a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     * @return A [ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet] containing the columns selected by [selector].
     * @see [SingleColumn.except]
     */
    public fun <R> String.select(selector: ColumnsSelector<*, R>): ColumnSet<R> =
        columnGroup(this).select(selector)

    /** ## Select from [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]
     *
     * Perform a selection of columns using the [Columns Selection DSL][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl] on
     * any [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]. This is more powerful than [ColumnsSelectionDsl.cols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols], because all operations of
     * the DSL are at your disposal.
     *
     * The [invoke][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.invoke] operator is overloaded to work as a shortcut for this method.
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColGroup.`[select][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` { someCol `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myGroupCol" `[{][kotlin.String.select]` "colA" and `[expr][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.expr]` { 0 } `[}][kotlin.String.select]` }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myGroupCol"].`[select][org.jetbrains.kotlinx.dataframe.columns.ColumnPath.select]` { "colA" and "colB" } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { it["myGroupCol"].`[asColumnGroup][org.jetbrains.kotlinx.dataframe.DataColumn.asColumnGroup]`()`[() {][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` "colA" and "colB" `[}][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myColGroup".`[select][kotlin.String.select]` { someCol `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myColGroup" `[{][kotlin.String.select]` colA `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` colB `[}][kotlin.String.select]` }`
     *
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     * See also [except][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.except]/[allExcept][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.allExcept] for the inverted operation of this function.
     *
     * @param [selector] The [ColumnsSelector][org.jetbrains.kotlinx.dataframe.ColumnsSelector] to use for the selection.
     * @receiver The [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup] to select from.
     * @throws [IllegalArgumentException] If [this] is not a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     * @return A [ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet] containing the columns selected by [selector].
     * @see [SingleColumn.except]
     */
    public operator fun <R> String.invoke(selector: ColumnsSelector<*, R>): ColumnSet<R> =
        select(selector)

    /**
     * ## Select from [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]
     *
     * Perform a selection of columns using the [Columns Selection DSL][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl] on
     * any [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]. This is more powerful than [ColumnsSelectionDsl.cols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols], because all operations of
     * the DSL are at your disposal.
     *
     * The [invoke][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.invoke] operator is overloaded to work as a shortcut for this method.
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColGroup.`[select][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` { someCol `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myGroupCol" `[{][kotlin.String.select]` "colA" and `[expr][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.expr]` { 0 } `[}][kotlin.String.select]` }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myGroupCol"].`[select][org.jetbrains.kotlinx.dataframe.columns.ColumnPath.select]` { "colA" and "colB" } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { it["myGroupCol"].`[asColumnGroup][org.jetbrains.kotlinx.dataframe.DataColumn.asColumnGroup]`()`[() {][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` "colA" and "colB" `[}][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][DataFrame.select]` { "pathTo"["myColGroup"].`[select][ColumnPath.select]` { someCol `[and][ColumnsSelectionDsl.and]` `[colsOf][SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][DataFrame.select]` { "pathTo"["myColGroup"] `[{][ColumnPath.select]` colA `[and][ColumnsSelectionDsl.and]` colB `[}][ColumnPath.select]` }`
     *
     * `df.`[select][DataFrame.select]` { `[pathOf][pathOf]`("pathTo", "myColGroup").`[select][ColumnPath.select]` { someCol `[and][ColumnsSelectionDsl.and]` `[colsOf][SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][DataFrame.select]` { `[pathOf][pathOf]`("pathTo", "myColGroup")`[() {][ColumnPath.select]` someCol `[and][ColumnsSelectionDsl.and]` `[colsOf][SingleColumn.colsOf]`<`[String][String]`>() `[}][ColumnPath.select]` }`
     *
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     * See also [except][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.except]/[allExcept][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.allExcept] for the inverted operation of this function.
     *
     * @param [selector] The [ColumnsSelector][org.jetbrains.kotlinx.dataframe.ColumnsSelector] to use for the selection.
     * @receiver The [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup] to select from.
     * @throws [IllegalArgumentException] If [this] is not a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     * @return A [ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet] containing the columns selected by [selector].
     * @see [SingleColumn.except]
     */
    public fun <R> ColumnPath.select(selector: ColumnsSelector<*, R>): ColumnSet<R> =
        columnGroup(this).select(selector)

    /** ## Select from [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]
     *
     * Perform a selection of columns using the [Columns Selection DSL][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl] on
     * any [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]. This is more powerful than [ColumnsSelectionDsl.cols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols], because all operations of
     * the DSL are at your disposal.
     *
     * The [invoke][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.invoke] operator is overloaded to work as a shortcut for this method.
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColGroup.`[select][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` { someCol `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myGroupCol" `[{][kotlin.String.select]` "colA" and `[expr][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.expr]` { 0 } `[}][kotlin.String.select]` }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myGroupCol"].`[select][org.jetbrains.kotlinx.dataframe.columns.ColumnPath.select]` { "colA" and "colB" } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { it["myGroupCol"].`[asColumnGroup][org.jetbrains.kotlinx.dataframe.DataColumn.asColumnGroup]`()`[() {][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` "colA" and "colB" `[}][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myColGroup"].`[select][org.jetbrains.kotlinx.dataframe.columns.ColumnPath.select]` { someCol `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myColGroup"] `[{][org.jetbrains.kotlinx.dataframe.columns.ColumnPath.select]` colA `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` colB `[}][org.jetbrains.kotlinx.dataframe.columns.ColumnPath.select]` }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[pathOf][org.jetbrains.kotlinx.dataframe.api.pathOf]`("pathTo", "myColGroup").`[select][org.jetbrains.kotlinx.dataframe.columns.ColumnPath.select]` { someCol `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[pathOf][org.jetbrains.kotlinx.dataframe.api.pathOf]`("pathTo", "myColGroup")`[() {][org.jetbrains.kotlinx.dataframe.columns.ColumnPath.select]` someCol `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() `[}][org.jetbrains.kotlinx.dataframe.columns.ColumnPath.select]` }`
     *
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     * See also [except][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.except]/[allExcept][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.allExcept] for the inverted operation of this function.
     *
     * @param [selector] The [ColumnsSelector][org.jetbrains.kotlinx.dataframe.ColumnsSelector] to use for the selection.
     * @receiver The [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup] to select from.
     * @throws [IllegalArgumentException] If [this] is not a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     * @return A [ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet] containing the columns selected by [selector].
     * @see [SingleColumn.except]
     */
    public operator fun <R> ColumnPath.invoke(selector: ColumnsSelector<*, R>): ColumnSet<R> =
        select(selector)

    @Deprecated(
        message = "Nested select is reserved for ColumnsSelector/ColumnsSelectionDsl behavior. " +
            "Use myGroup.cols(\"col1\", \"col2\") to select columns by name from a ColumnGroup.",
        replaceWith = ReplaceWith("this.cols(*columns)"),
        level = DeprecationLevel.ERROR,
    )
    public fun SingleColumn<DataRow<*>>.select(vararg columns: String): ColumnSet<*> =
        select { columns.toColumnSet() }

    @Deprecated(
        message = "Nested select is reserved for ColumnsSelector/ColumnsSelectionDsl behavior. " +
            "Use myGroup.cols(col1, col2) to select columns by name from a ColumnGroup.",
        replaceWith = ReplaceWith("this.cols(*columns)"),
        level = DeprecationLevel.ERROR,
    )
    public fun <R> SingleColumn<DataRow<*>>.select(vararg columns: ColumnReference<R>): ColumnSet<R> =
        select { columns.toColumnSet() }

    @Deprecated(
        message = "Nested select is reserved for ColumnsSelector/ColumnsSelectionDsl behavior. " +
            "Use myGroup.cols(Type::col1, Type::col2) to select columns by name from a ColumnGroup.",
        replaceWith = ReplaceWith("this.cols(*columns)"),
        level = DeprecationLevel.ERROR,
    )
    public fun <R> SingleColumn<DataRow<*>>.select(vararg columns: KProperty<R>): ColumnSet<R> =
        select { columns.toColumnSet() }

    // endregion
}

/**
 * ## Column Expression
 * Create a temporary new column by defining an expression to fill up each row.
 *
 * See [Column Expression][org.jetbrains.kotlinx.dataframe.documentation.ColumnExpression] for more information.
 *
 * #### For example:
 *
 * `df.`[groupBy][DataFrame.groupBy]` { `[expr][ColumnsSelectionDsl.expr]` { firstName.`[length][String.length]` + lastName.`[length][String.length]` } `[named][named]` "nameLength" }`
 *
 * `df.`[sortBy][DataFrame.sortBy]` { `[expr][ColumnsSelectionDsl.expr]` { name.`[length][String.length]` }.`[desc][SortDsl.desc]`() }`
 *
 * @param [name] The name the temporary column. Will be empty by default.
 * @param [infer] [An enum][org.jetbrains.kotlinx.dataframe.api.Infer.Infer] that indicates how [DataColumn.type][org.jetbrains.kotlinx.dataframe.DataColumn.type] should be calculated.
 * Either [None][org.jetbrains.kotlinx.dataframe.api.Infer.None], [Nulls][org.jetbrains.kotlinx.dataframe.api.Infer.Nulls], or [Type][org.jetbrains.kotlinx.dataframe.api.Infer.Type]. By default: [Nulls][Infer.Nulls].
 * @param [expression] An [AddExpression] to define what each new row of the temporary column should contain.
 */
public inline fun <T, reified R> ColumnsSelectionDsl<T>.expr(
    name: String = "",
    infer: Infer = Infer.Nulls,
    noinline expression: AddExpression<T, R>,
): DataColumn<R> = mapToColumn(name, infer, expression)
