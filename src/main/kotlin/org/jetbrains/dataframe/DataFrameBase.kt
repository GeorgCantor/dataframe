package org.jetbrains.dataframe

import kotlin.reflect.KProperty

interface DataFrameBase<out T> {

    operator fun get(columnName: String): DataCol

    fun getGroup(columnName: String) = get(columnName).asGrouped()
    fun getGroup(columnPath: List<String>): GroupedColumn<*> = get(columnPath).asGrouped()

    operator fun <R> get(column: ColumnDef<R>): ColumnData<R>
    operator fun <R> get(column: ColumnDef<DataFrameRow<R>>): GroupedColumn<R>
    operator fun <R> get(column: ColumnDef<DataFrame<R>>): TableColumn<R>

    operator fun get(index: Int): DataFrameRow<T>
    fun getColumn(columnIndex: Int): DataCol
    fun columns(): List<DataCol>
    val ncol: Int
}

operator fun <T, R> DataFrameBase<T>.get(column: KProperty<DataFrame<R>>) = get(column.toColumnDef())

operator fun <T, R> DataFrameBase<T>.get(column: KProperty<DataFrameRow<R>>) = get(column.toColumnDef())

operator fun <T, R> DataFrameBase<T>.get(column: KProperty<R>) = get(column.toColumnDef())