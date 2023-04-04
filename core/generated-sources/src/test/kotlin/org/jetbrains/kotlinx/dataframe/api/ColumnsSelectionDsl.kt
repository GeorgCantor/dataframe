package org.jetbrains.kotlinx.dataframe.api

import io.kotest.matchers.shouldBe
import org.jetbrains.kotlinx.dataframe.samples.api.TestBase
import org.jetbrains.kotlinx.dataframe.samples.api.age
import org.jetbrains.kotlinx.dataframe.samples.api.firstName
import org.jetbrains.kotlinx.dataframe.samples.api.isHappy
import org.jetbrains.kotlinx.dataframe.samples.api.lastName
import org.jetbrains.kotlinx.dataframe.samples.api.name
import org.junit.Test

class ColumnsSelectionDslTests : TestBase() {

    @Test
    fun first() {
        df.select { all().first() } shouldBe df.select { first() }

        df.select { all().first() } shouldBe df.select { name }

        df.select { first() } shouldBe df.select { name }

        df.select { first { it.name().startsWith("a") } } shouldBe df.select { age }

        df.select {
            name.first {
                it.any { it == "Alice" }
            }
        } shouldBe df.select {
            name.colsOf<String>().first {
                it.any { it == "Alice" }
            }
        }

        df.select {
            "name".first {
                it.any { it == "Alice" }
            }
        } shouldBe df.select { name.firstName }

        df.select {
            Person::name.first {
                it.any { it == "Alice" }
            }
        } shouldBe df.select { name.firstName }
    }

    @Test
    fun last() {
        df.select { all().last() } shouldBe df.select { last() }

        df.select { all().last() } shouldBe df.select { isHappy }

        df.select { last() } shouldBe df.select { isHappy }

        df.select { last { it.name().startsWith("a") } } shouldBe df.select { age }

        df.select {
            name.last {
                it.any { it == "Alice" }
            }
        } shouldBe df.select {
            name.colsOf<String>().last {
                it.any { it == "Alice" }
            }
        }
    }

}
