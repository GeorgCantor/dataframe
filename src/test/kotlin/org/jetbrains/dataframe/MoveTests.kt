package org.jetbrains.dataframe

import io.kotlintest.shouldBe
import org.junit.Test

class MoveTests {

    val columnNames = listOf("q", "a.b", "b.c", "w", "a.c.d", "e.f", "b.d", "r")
    val columns = columnNames.map { column(it, emptyList<Int>()) }
    val df = columns.asDataFrame<Unit>()
    val grouped = df.move { cols { it.name.contains(".") } }.into { it.name.split(".") }

    @Test
    fun batchGrouping(){

        grouped.columnNames() shouldBe listOf("q", "a", "b", "w", "e", "r")
        grouped["a"].asGroup().columnNames() shouldBe listOf("b", "c")
        grouped["a"]["c"].asGroup().columnNames() shouldBe listOf("d")
        grouped["b"].asGroup().columnNames() shouldBe listOf("c", "d")
        grouped["e"].asGroup().columnNames() shouldBe listOf("f")
    }

    @Test
    fun batchUngrouping(){

        val ungrouped = grouped.move { colsDfs { it.depth() > 0 && !it.isGrouped() } }.into { path(it.path.joinToString(".")) }
        ungrouped.columnNames() shouldBe listOf("q", "a.b", "a.c.d", "b.c", "b.d", "w", "e.f", "r")
    }

    @Test
    fun `ungroup one`(){

        val ungrouped = grouped.remove("b").ungroup { it["a"] }
        ungrouped.columnNames() shouldBe listOf("q", "b", "c", "w", "e", "r")
        ungrouped["c"].asGroup().columnNames() shouldBe listOf("d")
    }

    @Test
    fun `flatten one`(){

        val flattened = grouped.flatten { it["a"] }
        flattened.columnNames() shouldBe listOf("q", "a.b", "a.c.d", "b", "w", "e", "r")
    }

    @Test
    fun `flatten all`(){

        val flattened = grouped.flatten()
        flattened.columnNames() shouldBe listOf("q", "a.b", "a.c.d", "b.c", "b.d", "w", "e.f", "r")
    }
}