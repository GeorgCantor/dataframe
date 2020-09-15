package krangl.typed.person

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import krangl.typed.*
import org.junit.Test
import java.io.Serializable

class SpreadTests {

    val df = dataFrameOf("name", "key", "value")(
            "Alice", "age", 15,
            "Alice", "city", "London",
            "Alice", "weight", 54,
            "Bob", "age", 45,
            "Bob", "city", "Dubai",
            "Bob", "weight", 87,
            "Mark", "age", 20,
            "Mark", "city", "Moscow",
            "Mark", "weight", null
    )

// Generated Code

    @DataFrameType
    interface Person {
        val name: String
        val key: String
        val value: Any?
    }

    val TypedDataFrameRow<Person>.name get() = this["name"] as String
    val TypedDataFrameRow<Person>.key get() = this["key"] as String
    val TypedDataFrameRow<Person>.value get() = this["value"] as Any?
    val TypedDataFrame<Person>.name get() = this["name"].cast<String>()
    val TypedDataFrame<Person>.key get() = this["key"].cast<String>()
    val TypedDataFrame<Person>.value get() = this["value"].cast<Any?>()

    val typed: TypedDataFrame<Person> = df.typed()

// Tests

    @Test
    fun `spread to pair`() {

        val res = typed.spread { key into value }
        res.ncol shouldBe 1 + typed.key.ndistinct
        res.nrow shouldBe typed.name.ndistinct

        val expected = typed.map { (name to key) to value }.toMap()
        val actual = res.columns.subList(1, res.ncol).flatMap {
            val columnName = it.name
            res.map { (name to columnName) to it[columnName] }
        }.toMap()

        actual shouldBe expected
        res["age"].valueClass shouldBe Int::class
        res["age"].nullable shouldBe false
        res["city"].valueClass shouldBe String::class
        res["city"].nullable shouldBe false
        res["weight"].valueClass shouldBe Int::class
        res["weight"].nullable shouldBe true
    }

    @Test
    fun `spread to pair with group key and conversion`() {

        val res = typed.spread { key into { value.toString() } groupedBy name }

        res.ncol shouldBe 1 + typed.key.ndistinct
        res.nrow shouldBe typed.name.ndistinct

        val expected = typed.map { (name to key) to value.toString() }.toMap()
        val actual = res.columns.subList(1, res.ncol).flatMap {
            val columnName = it.name
            res.map { (name to columnName) to it[columnName] }
        }.toMap()

        actual shouldBe expected
    }

    @Test
    fun `spread exception`() {

        val first = typed[0]
        val values = first.values.toTypedArray()
        values[2] = 20
        val modified = typed.addRow(*values)
        shouldThrow<Exception> { modified.spread { key into value } }
    }

    @Test
    fun gather() {

        val res = typed.spread { key into value }
        val gathered = res.gather { cols[1 until ncol] }.into("key" to "value")
        gathered shouldBe typed
    }

    @Test
    fun `gather with filter`() {

        val spread = typed.spread { key into value }
        val gathered = spread.gather { cols[1 until ncol] }.where { it != null }.into("key" to "value")
        gathered shouldBe typed.filterNotNull { value }
    }

    @Test
    fun `spread with key conversion`() {

        val spread = typed.spread { key.map { "__$it" } into value }
        val gathered = spread.gather { cols[1 until ncol] }.into("key" to "value")
        gathered shouldBe typed.update { key }.with { "__$it" }
    }

    @Test
    fun `spread with value conversion`() {

        val converter: (Any?) -> Any? = { if (it is Int) it.toDouble() else it }
        val spread = typed.spread { key into value.map(converter) }
        val gathered = spread.gather { cols[1 until ncol] }.into("key" to "value")
        val expected = typed.update { value }.with { converter(it) as? Serializable }
        gathered shouldBe expected
    }

    @Test
    fun `gather with value conversion`() {

        val spread = typed.spread { key into value.map {if(it is Int) it.toDouble() else it} }
        val gathered = spread.gather { cols[1 until ncol] }.mapValues { if(it is Double) it.toInt() else it }.into("key" to "value")
        gathered shouldBe typed
    }

    @Test
    fun `gather with name conversion`() {

        val spread = typed.spread { key.map { "__$it" } into value }
        val gathered = spread.gather { cols[1 until ncol] }.mapNames { it.substring(2) }.into("key" to "value")
        gathered shouldBe typed
    }
}