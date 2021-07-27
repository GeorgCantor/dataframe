package org.jetbrains.dataframe

import org.jetbrains.dataframe.columns.AnyCol
import org.jetbrains.dataframe.io.DisplayConfiguration
import org.jetbrains.dataframe.io.HtmlData
import org.jetbrains.dataframe.io.toHTML

public data class RGBColor(val r: Short, val g: Short, val b: Short)

private fun encRgb(r: Short, g: Short, b: Short): String = "#${encHex(r)}${encHex(g)}${encHex(b)}"

private fun encHex(v: Short): String = "${(v / 16).toString(16)}${(v % 16).toString(16)}"

internal fun RGBColor.encode() = encRgb(r, g, b)

private fun componentWise(color1: RGBColor, color2: RGBColor, f: (Short, Short) -> Short) = RGBColor(
    f(color1.r, color2.r),
    f(color1.g, color2.g),
    f(color1.b, color2.b)
)

public interface CellAttributes {

    public fun attributes(): List<Pair<String, String>>
}

public infix fun CellAttributes?.and(other: CellAttributes?): CellAttributes? = when {
    other == null -> this
    this == null -> other
    else -> MergedAttributes(listOf(this, other))
}

internal class SingleAttribute(val key: String, val value: String) : CellAttributes {
    override fun attributes() = listOf(key to value)
}

internal class MergedAttributes(private val attributes: List<CellAttributes>) : CellAttributes {
    override fun attributes() = attributes.flatMap { it.attributes() }.toMap().toList()
}

public object FormattingDSL {

    public fun rgb(r: Short, g: Short, b: Short): RGBColor = RGBColor(r, g, b)

    public val black: RGBColor = rgb(0, 0, 0)
    public val white: RGBColor = rgb(255, 255, 255)
    public val green: RGBColor = rgb(0, 255, 0)
    public val red: RGBColor = rgb(255, 0, 0)
    public val blue: RGBColor = rgb(0, 0, 255)
    public val gray: RGBColor = rgb(128, 128, 128)
    public val darkGray: RGBColor = rgb(169, 169, 169)
    public val lightGray: RGBColor = rgb(211, 211, 211)

    private fun attribute(name: String, value: String): CellAttributes = SingleAttribute(name, value)

    public fun background(color: RGBColor): CellAttributes = attribute("background-color", color.encode())
    public fun background(r: Short, g: Short, b: Short): CellAttributes = background(RGBColor(r, g, b))

    public fun textColor(color: RGBColor): CellAttributes = attribute("color", color.encode())
    public fun textColor(r: Short, g: Short, b: Short): CellAttributes = textColor(RGBColor(r, g, b))

    public val italic: CellAttributes = attribute("font-style", "italic")

    public val bold: CellAttributes = attribute("font-weight", "bold")

    public val underline: CellAttributes = attribute("text-decoration", "underline")

    public fun linearBg(value: Number, from: Pair<Number, RGBColor>, to: Pair<Number, RGBColor>): CellAttributes = background(linear(value, from, to))

    public fun linear(value: Number, from: Pair<Number, RGBColor>, to: Pair<Number, RGBColor>): RGBColor {
        val a = from.first.toDouble()
        val b = to.first.toDouble()
        if (a < b) return linearGradient(value.toDouble(), a, from.second, b, to.second)
        return linearGradient(value.toDouble(), b, to.second, a, from.second)
    }
}

internal fun linearGradient(
    x: Double,
    minValue: Double,
    minColor: RGBColor,
    maxValue: Double,
    maxColor: RGBColor
): RGBColor {
    if (x < minValue) return minColor
    if (x > maxValue) return maxColor
    val t = (x - minValue) / (maxValue - minValue)
    return componentWise(minColor, maxColor) { cmin, cmax ->
        (cmin + t * (cmax - cmin)).toInt().toShort()
    }
}

public typealias RowColFormatter<T> = (DataRow<T>, AnyCol) -> CellAttributes?

public class FormattedFrame<T>(
    internal val df: DataFrame<T>,
    internal val formatter: RowColFormatter<T>? = null,
) {
    public fun toHTML(configuration: DisplayConfiguration): HtmlData = df.toHTML(getDisplayConfiguration(configuration))

    public fun getDisplayConfiguration(configuration: DisplayConfiguration): DisplayConfiguration {
        return configuration.copy(cellFormatter = formatter as RowColFormatter<*>?)
    }
}

public data class ColorClause<T, C>(
    val df: DataFrame<T>,
    val selector: ColumnsSelector<T, C>? = null,
    val oldFormatter: RowColFormatter<T>? = null,
    val filter: RowCellFilter<T, C> = { true },
)

public fun <T, C> FormattedFrame<T>.format(selector: ColumnsSelector<T, C>): ColorClause<T, C> = ColorClause(df, selector, formatter)
public fun <T, C> DataFrame<T>.format(selector: ColumnsSelector<T, C>): ColorClause<T, C> = ColorClause(this, selector)

public fun <T> FormattedFrame<T>.format(): ColorClause<T, Any?> = ColorClause(df, null, formatter)
public fun <T> DataFrame<T>.format(): ColorClause<T, Any?> = ColorClause(this)

public fun <T, C> ColorClause<T, C>.where(filter: RowCellFilter<T, C>): ColorClause<T, C> = copy(filter = filter)

public typealias CellFormatter<V> = FormattingDSL.(V) -> CellAttributes?

public fun <T, C : Number?> ColorClause<T, C>.linearBg(from: Pair<Number, RGBColor>, to: Pair<Number, RGBColor>): FormattedFrame<T> = with {
    if (it != null) {
        background(linear(it, from, to))
    } else null
}

public fun <T, C> ColorClause<T, C>.with(formatter: CellFormatter<C>): FormattedFrame<T> {
    val columns =
        if (selector != null) df.getColumnsWithPaths(selector).mapNotNull { if (it.depth == 0) it.name else null }
            .toSet() else null
    return FormattedFrame(df) { row, col ->
        val oldAttributes = oldFormatter?.invoke(row, col)
        if (columns == null || columns.contains(col.name())) {
            val value = row[col] as C
            if (filter == null || filter(row, value)) {
                oldAttributes and formatter(FormattingDSL, value)
            } else oldAttributes
        } else oldAttributes
    }
}
