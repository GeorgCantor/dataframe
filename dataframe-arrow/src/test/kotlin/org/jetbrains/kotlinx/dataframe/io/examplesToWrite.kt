package org.jetbrains.kotlinx.dataframe.io

import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import java.net.URL
import java.time.LocalDate

/**
 * DataFrame to be saved in Apache Arrow
 */
val citiesExampleFrame = dataFrameOf(
    DataColumn.createValueColumn(
        "name",
        listOf(
            "Berlin",
            "Hamburg",
            "New York",
            "Washington",
            "Saint Petersburg",
            "Vatican",
        ),
    ),
    DataColumn.createValueColumn(
        "affiliation",
        listOf(
            "Germany",
            "Germany",
            "The USA",
            "The USA",
            "Russia",
            null,
        ),
    ),
    DataColumn.createValueColumn(
        "is_capital",
        listOf(
            true,
            false,
            false,
            true,
            false,
            null,
        ),
    ),
    DataColumn.createValueColumn(
        "population",
        listOf(
            3_769_495,
            1_845_229,
            8_467_513,
            689_545,
            5_377_503,
            825,
        ),
    ),
    DataColumn.createValueColumn(
        "area",
        listOf(
            891.7,
            755.22,
            1223.59,
            177.0,
            1439.0,
            0.44,
        ),
    ),
    DataColumn.createValueColumn(
        "settled",
        listOf(
            LocalDate.of(1237, 1, 1),
            LocalDate.of(1189, 5, 7),
            LocalDate.of(1624, 1, 1),
            LocalDate.of(1790, 7, 16),
            LocalDate.of(1703, 5, 27),
            LocalDate.of(1929, 2, 11),
        ),
    ),
    DataColumn.createValueColumn(
        "page_in_wiki",
        listOf(
            URL("https://en.wikipedia.org/wiki/Berlin"),
            URL("https://en.wikipedia.org/wiki/Hamburg"),
            URL("https://en.wikipedia.org/wiki/New_York_City"),
            URL("https://en.wikipedia.org/wiki/Washington,_D.C."),
            URL("https://en.wikipedia.org/wiki/Saint_Petersburg"),
            URL("https://en.wikipedia.org/wiki/Vatican_City"),
        ),
    ),
)

/**
 * [citiesExampleFrame] Apache Arrow schema with some changes.
 * Originally generated by `citiesExampleFrame.columns().toArrowSchema().toJson()`
 * Changes made to test converting and schema matching:
 * field "population" changed to nullable Long;
 * field "area" changed to single Float;
 * field "settled" changed to datetime (date with millisecond precision);
 * field "page_in_wiki" removed, nullable field "film_in_youtube" added.
 */
val citiesExampleSchema = """{
  "fields" : [ {
    "name" : "name",
    "nullable" : false,
    "type" : {
      "name" : "utf8"
    },
    "children" : [ ]
  }, {
    "name" : "affiliation",
    "nullable" : true,
    "type" : {
      "name" : "utf8"
    },
    "children" : [ ]
  }, {
    "name" : "is_capital",
    "nullable" : true,
    "type" : {
      "name" : "bool"
    },
    "children" : [ ]
  }, {
    "name" : "population",
    "nullable" : true,
    "type" : {
      "name" : "int",
      "bitWidth" : 64,
      "isSigned" : true
    },
    "children" : [ ]
  }, {
    "name" : "area",
    "nullable" : false,
    "type" : {
      "name" : "floatingpoint",
      "precision" : "SINGLE"
    },
    "children" : [ ]
  }, {
    "name" : "settled",
    "nullable" : false,
    "type" : {
      "name" : "date",
      "unit" : "MILLISECOND"
    },
    "children" : [ ]
  }, {
    "name" : "film_in_youtube",
    "nullable" : true,
    "type" : {
      "name" : "utf8"
    },
    "children" : [ ]
  } ]
}
""".trimIndent()

/**
 * String column (variable length vector) with size >1 MiB
 */
val bigStringColumn = run {
    val list = ArrayList<String>()
    for (i in 0 until 1024) {
        val row = StringBuilder()
        for (j in 0 until 64) {
            row.append("abcd")
        }
        list.add(row.toString())
    }
    for (i in 0 until 1024) {
        val row = StringBuilder()
        for (j in 0 until 64) {
            row.append("гдёж")
        }
        list.add(row.toString())
    }
    for (i in 0 until 1024) {
        val row = StringBuilder()
        for (j in 0 until 64) {
            row.append("αβγδ")
        }
        list.add(row.toString())
    }
    for (i in 0 until 1024) {
        val row = StringBuilder()
        for (j in 0 until 64) {
            row.append("正体字")
        }
        list.add(row.toString())
    }
    DataColumn.createValueColumn("bigStringColumn", list)
}
