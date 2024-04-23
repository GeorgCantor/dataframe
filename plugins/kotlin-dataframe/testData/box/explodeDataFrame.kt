package org.jetbrains.kotlinx.dataframe

import org.jetbrains.kotlinx.dataframe.api.explode
import org.jetbrains.kotlinx.dataframe.io.*

fun box(): String {
    val df = DataFrame.readJson("testResources/achievements_all.json")

    val df1 = df.explode { achievements }
    df1.achievements.order
    return "OK"
}