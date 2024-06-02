package org.jetbrains.kotlinx.dataframe.impl.columns

import org.jetbrains.kotlinx.dataframe.AnyRow
import org.jetbrains.kotlinx.dataframe.ColumnDataHolder
import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.columns.ColumnGroup
import org.jetbrains.kotlinx.dataframe.columns.ColumnResolutionContext
import org.jetbrains.kotlinx.dataframe.columns.ValueColumn
import org.jetbrains.kotlinx.dataframe.impl.isArray
import org.jetbrains.kotlinx.dataframe.impl.isPrimitiveArray
import org.jetbrains.kotlinx.dataframe.toColumnDataHolder
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.withNullability

private const val DEBUG = true

internal open class ValueColumnImpl<T>(
    values: ColumnDataHolder<T>,
    name: String,
    type: KType,
    val defaultValue: T? = null,
) : DataColumnImpl<T>(values, name, type), ValueColumn<T> {

    private infix fun <T> T?.matches(type: KType) =
        when {
            this == null -> type.isMarkedNullable
            this.isPrimitiveArray -> type.isPrimitiveArray &&
                this!!::class.qualifiedName == type.classifier?.let { (it as KClass<*>).qualifiedName }
            this.isArray -> type.isArray // cannot check the precise type of array
            else -> this!!::class.isSubclassOf(type.classifier as KClass<*>)
        }

    init {
        if (DEBUG) {
            require(values.all { it matches type }) {
                val types = values.map { if (it == null) "Nothing?" else it!!::class.simpleName }.distinct()
                "Values of column '$name' have types '$types' which are not compatible given with column type '$type'"
            }
        }
    }

    override fun distinct() = ValueColumnImpl(
        values = toSet().toColumnDataHolder(type, distinct),
        name = name,
        type = type,
        defaultValue = defaultValue,
    )

    override fun rename(newName: String) = ValueColumnImpl(values, newName, type, defaultValue)

    override fun changeType(type: KType) = ValueColumnImpl(values, name, type, defaultValue)

    override fun addParent(parent: ColumnGroup<*>): DataColumn<T> = ValueColumnWithParent(parent, this)

    override fun createWithValues(values: List<T>, hasNulls: Boolean?): ValueColumn<T> {
        val nulls = hasNulls ?: values.any { it == null }
        return DataColumn.createValueColumn(name, values, type.withNullability(nulls))
    }

    override fun get(indices: Iterable<Int>): ValueColumn<T> {
        var nullable = false
        val newValues = indices.map {
            val value = values[it]
            if (value == null) nullable = true
            value
        }
        return createWithValues(newValues, nullable)
    }

    override fun get(columnName: String) =
        throw UnsupportedOperationException("Can not get nested column '$columnName' from ValueColumn '$name'")

    override operator fun get(range: IntRange): ValueColumn<T> = super<DataColumnImpl>.get(range) as ValueColumn<T>

    override fun defaultValue() = defaultValue

    override fun forceResolve() = ResolvingValueColumn(this)
}

internal class ResolvingValueColumn<T>(
    override val source: ValueColumn<T>,
) : ValueColumn<T> by source, ForceResolvedColumn<T> {

    override fun resolve(context: ColumnResolutionContext) = super<ValueColumn>.resolve(context)

    override fun resolveSingle(context: ColumnResolutionContext) =
        context.df.getColumn<T>(source.name(), context.unresolvedColumnsPolicy)?.addPath()

    override fun getValue(row: AnyRow) = super<ValueColumn>.getValue(row)

    override fun getValueOrNull(row: AnyRow) = super<ValueColumn>.getValueOrNull(row)

    override fun rename(newName: String) = ResolvingValueColumn(source.rename(newName))

    override fun toString(): String = source.toString()

    override fun equals(other: Any?) = source.checkEquals(other)

    override fun hashCode(): Int = source.hashCode()
}
