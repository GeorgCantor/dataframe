package org.jetbrains.dataframe

fun <T> DataFrameRow<T>.movingAverage(k: Int, selector: RowSelector<T, Number>): Double {
    var count = 0
    return backwardIterable().take(k).sumByDouble {
        count++
        selector(it).toDouble()
    } / count
}