package org.jetbrains.dataframe.person

import io.kotlintest.shouldBe
import org.jetbrains.dataframe.*
import org.junit.Ignore
import org.junit.Test

class DataFrameTreeTests : BaseTest() {

    @DataFrameType
    interface NameAndCity {
        val name: String
        val city: String?
    }

    @DataFrameType
    interface GroupedPerson {
        val nameAndCity: TypedDataFrameRow<NameAndCity>
        val age: Int
        val weight: Int?
    }

    val df2 = df.move { name and city }.into("nameAndCity")
    val typed2 = df2.typed<GroupedPerson>()

    val DataFrameRowBase<NameAndCity>.name get() = this["name"] as String
    val DataFrameRowBase<NameAndCity>.city get() = this["city"] as String?
    val DataFrameBase<NameAndCity>.name get() = this["name"].typed<String>()
    val DataFrameBase<NameAndCity>.city get() = this["city"].typed<String?>()

    val DataFrameRowBase<GroupedPerson>.age get() = this["age"] as Int
    val DataFrameRowBase<GroupedPerson>.weight get() = this["weight"] as Int?
    val DataFrameRowBase<GroupedPerson>.nameAndCity get() = this["nameAndCity"] as TypedDataFrameRow<NameAndCity>
    val DataFrameBase<GroupedPerson>.age get() = this["age"].typed<Int>()
    val DataFrameBase<GroupedPerson>.weight get() = this["weight"].typed<Int?>()
    val DataFrameBase<GroupedPerson>.nameAndCity get() = this["nameAndCity"].grouped<NameAndCity>()

    val nameAndCity by columnGroup<NameAndCity>()

    @Test
    fun `group indexing`() {

        df2[nameAndCity][city] shouldBe typed.city
        typed2.nameAndCity.city shouldBe typed.city
        df2["nameAndCity"]["city"] shouldBe typed.city
    }

    @Test
    fun `update`() {
        val expected = typed.select { city.rename("nameAndCity") and age and weight }

        df2.update { nameAndCity }.with { nameAndCity().city } shouldBe expected
        typed2.update { nameAndCity }.with { nameAndCity.city } shouldBe expected
    }

    @Test
    fun `slice`() {

        val expected = typed[0..2].name
        val actual = typed2[0..2].nameAndCity.name
        actual shouldBe expected
    }

    @Test
    fun `filter`() {

        val expected = typed.filter { city == null }.select { weight }
        typed2.filter { nameAndCity.city == null }.select { weight } shouldBe expected
        df2.filter { it[nameAndCity][city] == null }.select { weight } shouldBe expected
    }

    @Test
    fun `select`() {
        val expected = typed.select { name and age }
        typed2.select { nameAndCity.name and age } shouldBe expected
        df2.select { it[nameAndCity][name] and age } shouldBe expected
    }

    @Test
    fun `sort`() {
        val expected = typed.sortBy { name then age }.moveTo(1) { city }
        typed2.sortBy { nameAndCity.name then age }.ungroup { nameAndCity } shouldBe expected
    }

    @Test
    fun `groupBy`() {

        val expected = typed.groupBy { name }.max { age }
        typed2.groupBy { nameAndCity.name }.max { it.age } shouldBe expected
    }

    @Test
    @Ignore // TODO: fix
    fun splitRows() {
        val selected = typed2.select { nameAndCity }
        val nested = selected.mergeRows { nameAndCity.city }
        val mergedCity by columnList<String?>("city")
        val res = nested.splitRows { nameAndCity[mergedCity] }
        res.sortBy { nameAndCity.name } shouldBe selected.sortBy { nameAndCity.name }
    }

    @Test
    fun extensionPropertiesTest() {
        val code = CodeGenerator().generate(GroupedPerson::class)
        val dataFrameBase = DataFrameBase::class.simpleName
        val dataFrameRowBase = DataFrameRowBase::class.simpleName
        val dataFrameRow = TypedDataFrameRow::class.qualifiedName
        val className = GroupedPerson::class.qualifiedName
        val shortName = GroupedPerson::class.simpleName!!
        val nameAndCity = NameAndCity::class.qualifiedName
        val groupedColumn = GroupedColumnBase::class.qualifiedName
        val columnData = ColumnData::class.qualifiedName
        val expected = """
            val $dataFrameBase<$className>.age: $columnData<kotlin.Int> @JvmName("${shortName}_age") get() = this["age"] as $columnData<kotlin.Int>
            val $dataFrameRowBase<$className>.age: Int @JvmName("${shortName}_age") get() = this["age"] as Int
            val $dataFrameBase<$className>.nameAndCity: $groupedColumn<$nameAndCity> @JvmName("${shortName}_nameAndCity") get() = this["nameAndCity"] as $groupedColumn<$nameAndCity>
            val $dataFrameRowBase<$className>.nameAndCity: $dataFrameRow<$nameAndCity> @JvmName("${shortName}_nameAndCity") get() = this["nameAndCity"] as $dataFrameRow<$nameAndCity>
            val $dataFrameBase<$className>.weight: $columnData<kotlin.Int?> @JvmName("${shortName}_weight") get() = this["weight"] as $columnData<kotlin.Int?>
            val $dataFrameRowBase<$className>.weight: Int? @JvmName("${shortName}_weight") get() = this["weight"] as Int?
        """.trimIndent()
        code.joinToString("\n") shouldBe expected
    }
}