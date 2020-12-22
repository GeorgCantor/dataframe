package org.jetbrains.dataframe.api.columns

import org.jetbrains.dataframe.*

interface GroupedColumn<T> : ColumnData<DataFrameRow<T>>, NestedColumn<T>, GroupedColumnBase<T> {

    override fun get(index: Int): DataFrameRow<T> {
        return super<GroupedColumnBase>.get(index)
    }

    override fun get(columnName: String): DataCol {
        return super<GroupedColumnBase>.get(columnName)
    }

    override fun kind() = ColumnKind.Group
}