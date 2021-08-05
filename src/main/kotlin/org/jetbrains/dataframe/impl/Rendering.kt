package org.jetbrains.dataframe.impl

import org.jetbrains.dataframe.AnyFrame
import org.jetbrains.dataframe.ColumnKind
import org.jetbrains.dataframe.Many
import org.jetbrains.dataframe.columns.AnyCol
import org.jetbrains.dataframe.columns.size
import org.jetbrains.dataframe.columns.type
import org.jetbrains.dataframe.impl.columns.asGroup
import org.jetbrains.dataframe.impl.columns.asTable
import org.jetbrains.dataframe.internal.schema.ColumnSchema
import org.jetbrains.dataframe.internal.schema.DataFrameSchema
import org.jetbrains.dataframe.io.escapeHTML
import org.jetbrains.dataframe.jupyter.RenderedContent
import org.jetbrains.dataframe.size
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.reflect.KType

internal fun String.truncate(limit: Int): RenderedContent = if (limit in 1 until length) {
    if (limit < 4) RenderedContent.truncatedText("...", this)
    else RenderedContent.truncatedText(substring(0, Math.max(limit - 3, 1)) + "...", this)
} else {
    RenderedContent.text(this)
}

internal fun renderSchema(df: AnyFrame): String =
    df.columns().map { "${it.name()}:${renderType(it)}" }.joinToString()

internal fun renderSchema(schema: DataFrameSchema): String =
    schema.columns.map { "${it.key}:${renderType(it.value)}" }.joinToString()

internal fun renderType(column: ColumnSchema) =
    when (column) {
        is ColumnSchema.Value -> {
            renderType(column.type)
        }
        is ColumnSchema.Frame -> {
            "[${renderSchema(column.schema)}]"
        }
        is ColumnSchema.Map -> {
            "{${renderSchema(column.schema)}}"
        }
        else -> throw NotImplementedError()
    }

internal fun renderType(type: KType): String {
    return when (type.classifier) {
        List::class -> {
            val argument = type.arguments[0].type?.let { renderType(it) } ?: "*"
            "List<$argument>"
        }
        Many::class -> {
            val argument = type.arguments[0].type?.let { renderType(it) } ?: "*"
            "Many<$argument>"
        }
        URL::class -> "URL"
        LocalDateTime::class -> "DateTime"
        LocalDate::class -> "Date"
        LocalTime::class -> "Time"
        else -> {
            val result = type.toString()
            if (result.startsWith("kotlin.")) result.substring(7)
            else result
        }
    }
}

internal fun renderType(column: AnyCol) =
    when (column.kind()) {
        ColumnKind.Value -> renderType(column.type)
        ColumnKind.Frame -> {
            val table = column.asTable()
            "[${renderSchema(table.schema.value)}]"
        }
        ColumnKind.Group -> {
            val group = column.asGroup()
            "{${renderSchema(group.df)}}"
        }
    }

internal fun AnyCol.renderShort() = when (kind()) {
    ColumnKind.Value -> "ValueColumn<${renderType(type)}>: $size entries".escapeHTML()
    ColumnKind.Frame -> "FrameColumn: $size entries"
    ColumnKind.Group -> "ColumnGroup ${asGroup().df.size}}"
}
