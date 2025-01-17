package org.jetbrains.kotlinx.dataframe.impl

import org.jetbrains.kotlinx.dataframe.AnyCol
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.api.asColumnGroup
import org.jetbrains.kotlinx.dataframe.api.asDataFrame
import org.jetbrains.kotlinx.dataframe.columns.ColumnKind
import org.jetbrains.kotlinx.dataframe.impl.columns.asAnyFrameColumn
import org.jetbrains.kotlinx.dataframe.io.escapeHTML
import org.jetbrains.kotlinx.dataframe.jupyter.RenderedContent
import org.jetbrains.kotlinx.dataframe.schema.ColumnSchema
import org.jetbrains.kotlinx.dataframe.schema.DataFrameSchema
import org.jetbrains.kotlinx.dataframe.size
import org.jetbrains.kotlinx.dataframe.type
import java.net.URL
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.typeOf

internal fun String.truncate(limit: Int): RenderedContent = if (limit in 1 until length) {
    if (limit < 4) RenderedContent.truncatedText("...", this)
    else RenderedContent.truncatedText(substring(0, (limit - 3).coerceAtLeast(1)) + "...", this)
} else {
    RenderedContent.text(this)
}

internal fun renderSchema(df: AnyFrame): String =
    df.columns().joinToString { "${it.name()}:${renderType(it)}" }

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

        is ColumnSchema.Group -> {
            "{${renderSchema(column.schema)}}"
        }

        else -> throw NotImplementedError()
    }

internal fun renderType(type: KType?): String {
    return when (type?.classifier) {
        null -> "*"

        Nothing::class -> "Nothing" + if (type.isMarkedNullable) "?" else ""

        else -> {
            val fullName = type.jvmErasure.qualifiedName ?: return type.toString()
            val name = when {
                // catching cases like `typeOf<Array<Int>>().jvmErasure.qualifiedName == "IntArray"`
                // https://github.com/Kotlin/dataframe/issues/678
                type.isSubtypeOf(typeOf<Array<*>>()) ->
                    "Array"

                type.classifier == URL::class ->
                    fullName.removePrefix("java.net.")

                type.classifier in listOf(LocalDateTime::class, LocalTime::class) ->
                    fullName.removePrefix("java.time.")

                fullName.startsWith("kotlin.collections.") ->
                    fullName.removePrefix("kotlin.collections.")

                fullName.startsWith("kotlin.") ->
                    fullName.removePrefix("kotlin.")

                fullName.startsWith("org.jetbrains.kotlinx.dataframe.") ->
                    fullName.removePrefix("org.jetbrains.kotlinx.dataframe.")

                else -> fullName
            }

            buildString {
                append(name)
                if (type.arguments.isNotEmpty()) {
                    val arguments = type.arguments.joinToString {
                        renderType(it.type)
                    }
                    append("<$arguments>")
                }
                if (type.isMarkedNullable) {
                    append("?")
                }
            }
        }
    }
}

internal fun renderType(column: AnyCol) =
    when (column.kind()) {
        ColumnKind.Value -> renderType(column.type)
        ColumnKind.Frame -> {
            val table = column.asAnyFrameColumn()
            "[${renderSchema(table.schema.value)}]"
        }

        ColumnKind.Group -> {
            val group = column.asColumnGroup()
            "{${renderSchema(group)}}"
        }
    }

internal fun AnyCol.renderShort() = when (kind()) {
    ColumnKind.Value -> "ValueColumn<${renderType(type)}>: $size entries".escapeHTML()
    ColumnKind.Frame -> "FrameColumn: $size entries"
    ColumnKind.Group -> "ColumnGroup ${asColumnGroup().asDataFrame().size}}"
}
