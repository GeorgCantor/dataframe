package org.jetbrains.kotlinx.dataframe.plugin

data class DataFrameCallableId(
    val packageName: String,
    val className: String,
    val callableName: String
)