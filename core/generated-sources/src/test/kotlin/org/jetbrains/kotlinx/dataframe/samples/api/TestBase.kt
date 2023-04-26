package org.jetbrains.kotlinx.dataframe.samples.api

import io.kotest.matchers.shouldBe
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.cast
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.dataframe.api.group
import org.jetbrains.kotlinx.dataframe.api.into
import org.jetbrains.kotlinx.dataframe.explainer.PluginCallback
import org.junit.After
import org.junit.Before

public open class TestBase {

    companion object {
        internal const val OUTPUTS = "DATAFRAME_SAVE_OUTPUTS"
    }

    @Before
    fun start() {
        if (System.getenv(OUTPUTS) != null) {
            PluginCallback.start()
        }
    }

    @After
    fun save() {
        if (System.getenv(OUTPUTS) != null) {
            PluginCallback.save()
        }
    }

    val df = dataFrameOf("firstName", "lastName", "age", "city", "weight", "isHappy")(
        "Alice", "Cooper", 15, "London", 54, true,
        "Bob", "Dylan", 45, "Dubai", 87, true,
        "Charlie", "Daniels", 20, "Moscow", null, false,
        "Charlie", "Chaplin", 40, "Milan", null, true,
        "Bob", "Marley", 30, "Tokyo", 68, true,
        "Alice", "Wolf", 20, null, 55, false,
        "Charlie", "Byrd", 30, "Moscow", 90, true
    ).group("firstName", "lastName").into("name").cast<Person>()

    @DataSchema
    interface Name {
        val firstName: String
        val lastName: String
    }

    @DataSchema
    interface Person {
        val age: Int
        val city: String?
        val name: DataRow<Name>
        val weight: Int?
        val isHappy: Boolean
    }

    infix fun <T, U : T> T.willBe(expected: U?) = shouldBe(expected)
}
