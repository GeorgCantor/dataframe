package org.jetbrains.dataframe

operator fun DataFrame<*>.plus(col: DataCol) = dataFrameOf(columns + col)

operator fun DataFrame<*>.plus(col: Iterable<DataCol>) = dataFrameOf(columns + col)

inline fun <reified R, T> DataFrame<T>.add(name: String, noinline expression: RowSelector<T, R>) =
        (this + new(name, expression))

inline fun <reified R, T, G> GroupedDataFrame<T, G>.add(name: String, noinline expression: RowSelector<G, R>) =
        modify { add(name, expression) }

inline fun <reified R, T> DataFrame<T>.add(column: ColumnDefinition<R>, noinline expression: RowSelector<T, R>) =
        (this + new(column.name, expression))

fun <T> DataFrame<T>.add(body: TypedColumnsFromDataRowBuilder<T>.() -> Unit) =
        with(TypedColumnsFromDataRowBuilder(this)) {
            body(this)
            dataFrameOf(this@add.columns + columns).typed<T>()
        }

operator fun <T> DataFrame<T>.plus(body: TypedColumnsFromDataRowBuilder<T>.() -> Unit) = add(body)

class TypedColumnsFromDataRowBuilder<T>(val dataFrame: DataFrame<T>) {
    internal val columns = mutableListOf<DataCol>()

    fun add(column: DataCol) = columns.add(column)

    inline fun <reified R> add(name: String, noinline expression: RowSelector<T, R>) = add(dataFrame.new(name, expression))

    inline infix fun <reified R> String.to(noinline expression: RowSelector<T, R>) = add(this, expression)

    inline operator fun <reified R> String.invoke(noinline expression: RowSelector<T, R>) = add(this, expression)
}