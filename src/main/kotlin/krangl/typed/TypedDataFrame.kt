package krangl.typed

import krangl.DataFrame
import java.util.*
import kotlin.reflect.full.isSubclassOf

interface TypedDataFrameWithColumns<out T> : TypedDataFrame<T> {

    fun columns(vararg col: DataCol) = ColumnGroup(col.toList())

    operator fun String.invoke() = asColumnName()
}

interface TypedDataFrameWithColumnsForSelect<out T> : TypedDataFrameWithColumns<T> {

    val allColumns: ColumnGroup

    infix fun ColumnSet.and(other: ColumnSet) = ColumnGroup(this, other)

    infix fun String.and(other: String) = asColumnName() and other.asColumnName()

    infix fun String.and(other: ColumnSet) = asColumnName() and other

    infix fun ColumnSet.and(other: String) = this and other.asColumnName()

    operator fun ColumnSet.plus(other: ColumnSet) = this and other
}

interface TypedDataFrameWithColumnsForSort<out T> : TypedDataFrameWithColumns<T> {

    val NamedColumn.desc get() = ReversedColumn(this)

    val String.desc get() = ReversedColumn(asColumnName())

    infix fun ColumnSet.then(other: ColumnSet) = ColumnGroup(this, other)
    infix fun ColumnSet.then(other: String) = this then other.asColumnName()
    infix fun String.then(other: ColumnSet) = asColumnName() then other
    infix fun String.then(other: String) = asColumnName() then other.asColumnName()
}

class TypedDataFrameWithColumnsForSelectImpl<T>(df: TypedDataFrame<T>) : TypedDataFrame<T> by df, TypedDataFrameWithColumnsForSelect<T> {

    override val allColumns get() = ColumnGroup(columns)
}

class TypedDataFrameWithColumnsForSortImpl<T>(df: TypedDataFrame<T>) : TypedDataFrame<T> by df, TypedDataFrameWithColumnsForSort<T> {

}

data class DataFrameSize(val ncol: Int, val nrow: Int) {
    override fun toString() = "$nrow x $ncol"
}

typealias ColumnSelector<T> = TypedDataFrameWithColumnsForSelect<T>.(TypedDataFrameWithColumnsForSelect<T>) -> ColumnSet

typealias SortColumnSelector<T> = TypedDataFrameWithColumnsForSort<T>.(TypedDataFrameWithColumnsForSort<T>) -> ColumnSet

internal fun ColumnSet.extractColumns(): List<NamedColumn> = when (this) {
    is ColumnGroup -> columns.flatMap { it.extractColumns() }
    is NamedColumn -> listOf(this)
    else -> throw Exception()
}

internal fun ColumnSet.extractSortColumns(): List<SortColumnDescriptor> = when (this) {
    is ColumnGroup -> columns.flatMap { it.extractSortColumns() }
    is ReversedColumn -> column.extractSortColumns().map { SortColumnDescriptor(it.column, it.direction.reversed()) }
    is NamedColumn -> listOf(SortColumnDescriptor(name, SortDirection.Asc))
    else -> throw Exception()
}

internal fun <T> TypedDataFrame<T>.getColumns(selector: ColumnSelector<T>) = TypedDataFrameWithColumnsForSelectImpl(this).let { selector(it, it).extractColumns() }

internal fun <T> TypedDataFrame<T>.getSortColumns(selector: SortColumnSelector<T>) = TypedDataFrameWithColumnsForSortImpl(this).let { selector(it, it).extractSortColumns() }

internal fun <T> TypedDataFrame<T>.getColumns(columnNames: Array<out String>) = columnNames.map { this[it] }

enum class SortDirection { Asc, Desc }

fun SortDirection.reversed() = when (this) {
    SortDirection.Asc -> SortDirection.Desc
    SortDirection.Desc -> SortDirection.Asc
}

class SortColumnDescriptor(val column: String, val direction: SortDirection)

typealias UntypedDataFrame = TypedDataFrame<Unit>

internal fun <T> TypedDataFrame<T>.new(columns: Iterable<DataCol>) = dataFrameOf(columns).typed<T>()

interface TypedDataFrame<out T> {

    val nrow: Int
    val ncol: Int get() = columns.size
    val columns: List<DataCol>
    val rows: Iterable<TypedDataFrameRow<T>>

    fun columnNames() = columns.map { it.name }

    operator fun get(rowIndex: Int): TypedDataFrameRow<T>
    operator fun get(columnName: String) = tryGetColumn(columnName) ?: throw Exception("Column not found") // TODO
    operator fun get(column: NamedColumn) = get(column.name)
    operator fun <R> get(column: TypedCol<R>) = get(column.name) as TypedColData<R>
    operator fun get(indices: IntArray) = getRows(indices)
    operator fun get(indices: List<Int>) = getRows(indices)
    operator fun get(mask: BooleanArray) = getRows(mask)
    operator fun get(range: IntRange) = getRows(range)

    operator fun plus(col: DataCol) = dataFrameOf(columns + col).typed<T>()
    operator fun plus(col: Iterable<DataCol>) = new(columns + col)
    operator fun plus(stub: AddRowNumberStub) = addRowNumber(stub.columnName)

    fun getRows(indices: IntArray): TypedDataFrame<T>
    fun getRows(mask: BooleanArray): TypedDataFrame<T>
    fun getRows(indices: List<Int>) = getRows(indices.toIntArray())
    fun getRows(range: IntRange) = getRows(range.toList())

    fun tryGetColumn(name: String): DataCol?

    fun select(columns: Iterable<NamedColumn>) = new(columns.map { this[it.name] })
    fun select(vararg columns: Column) = select(columns.toList())
    fun select(vararg columns: String) = select(getColumns(columns))
    fun select(selector: ColumnSelector<T>) = select(getColumns(selector))
    fun selectIf(filter: DataCol.(DataCol) -> Boolean) = select(columns.filter { filter(it, it) })

    fun sortBy(columns: List<SortColumnDescriptor>): TypedDataFrame<T>
    fun sortBy(columns: Iterable<NamedColumn>) = sortBy(columns.map { SortColumnDescriptor(it.name, SortDirection.Asc) })
    fun sortBy(vararg columns: Column) = sortBy(columns.toList())
    fun sortBy(vararg columns: String) = sortBy(getColumns(columns))
    fun sortBy(selector: SortColumnSelector<T>) = sortBy(getSortColumns(selector))

    fun sortByDesc(columns: Iterable<NamedColumn>) = sortBy(columns.map { SortColumnDescriptor(it.name, SortDirection.Desc) })
    fun sortByDesc(vararg columns: Column) = sortByDesc(columns.toList())
    fun sortByDesc(vararg columns: String) = sortByDesc(getColumns(columns))
    fun sortByDesc(selector: SortColumnSelector<T>) = sortBy(getSortColumns(selector).map { SortColumnDescriptor(it.column, SortDirection.Desc) })

    fun remove(cols: Iterable<NamedColumn>) = cols.map { it.name }.toSet().let { exclude -> new(columns.filter { !exclude.contains(it.name) }) }
    fun remove(vararg cols: NamedColumn) = remove(cols.toList())
    fun remove(vararg columns: String) = remove(getColumns(columns))
    fun remove(selector: ColumnSelector<T>) = remove(getColumns(selector))

    infix operator fun minus(cols: ColumnSelector<T>) = remove(cols)
    infix operator fun minus(cols: Iterable<NamedColumn>) = remove(cols)
    infix operator fun minus(column: NamedColumn) = remove(column)
    infix operator fun minus(column: String) = remove(column)

    fun groupBy(cols: Iterable<NamedColumn>): GroupedDataFrame<T>
    fun groupBy(vararg cols: Column) = groupBy(cols.toList())
    fun groupBy(vararg cols: String) = groupBy(getColumns(cols))
    fun groupBy(cols: ColumnSelector<T>) = groupBy(getColumns(cols))

    fun update(cols: Iterable<NamedColumn>) = UpdateClauseImpl(this, cols.toList()) as UpdateClause<T>
    fun update(vararg cols: Column) = update(cols.toList())
    fun update(vararg cols: String) = update(getColumns(cols))
    fun update(cols: ColumnSelector<T>) = update(getColumns(cols))

    fun addRowNumber(columnName: String = "id") = new(columns + createColumn(columnName, (0 until nrow).toList()))

    fun filter(predicate: RowFilter<T>): TypedDataFrame<T>

    fun nullToZero(cols: Iterable<TypedCol<Number?>>) = cols.fold(this) { df, col -> df.nullColumnToZero(col) }
    fun nullToZero(vararg cols: TypedCol<Number?>) = nullToZero(cols.toList())
    fun nullToZero(vararg cols: String) = nullToZero(getColumns(cols).map { it as TypedCol<Number?> })
    fun nullToZero(cols: ColumnSelector<T>) = nullToZero(getColumns(cols).map { it as TypedCol<Number?> })

    fun filterNotNull(cols: Iterable<NamedColumn>) = filter { cols.all { col -> this[col.name] != null } }
    fun filterNotNull(vararg cols: Column) = filterNotNull(cols.toList())
    fun filterNotNull(vararg cols: String) = filterNotNull(getColumns(cols))
    fun filterNotNull(cols: ColumnSelector<T>) = filterNotNull(getColumns(cols))

    fun filterNotNullAny(cols: Iterable<NamedColumn>) = filter { cols.any { col -> this[col.name] != null } }
    fun filterNotNullAny(vararg cols: Column) = filterNotNullAny(cols.toList())
    fun filterNotNullAny(vararg cols: String) = filterNotNullAny(getColumns(cols))
    fun filterNotNullAny(cols: ColumnSelector<T>) = filterNotNullAny(getColumns(cols))

    fun filterNotNullAny() = filter { values.any { it != null } }
    fun filterNotNull() = filter { values.all { it != null } }

    fun <D : Comparable<D>> min(selector: RowSelector<T, D?>): D? = rows.asSequence().map { selector(it, it) }.filterNotNull().min()
    fun <D : Comparable<D>> min(col: TypedCol<D?>): D? = get(col).values.asSequence().filterNotNull().min()

    fun <D : Comparable<D>> max(selector: RowSelector<T, D?>): D? = rows.asSequence().map { selector(it, it) }.filterNotNull().max()
    fun <D : Comparable<D>> max(col: TypedCol<D?>): D? = get(col).values.asSequence().filterNotNull().max()

    fun <D : Comparable<D>> maxBy(selector: RowSelector<T, D>) = rows.maxBy { selector(it, it) }
    fun <D : Comparable<D>> maxBy(col: TypedCol<D>) = rows.maxBy { col(it) }
    fun <D : Comparable<D>> maxBy(col: String) = rows.maxBy { it[col] as D }

    fun <D : Comparable<D>> minBy(selector: RowSelector<T, D>) = rows.minBy { selector(it, it) }
    fun <D : Comparable<D>> minBy(col: TypedCol<D>) = rows.minBy { col(it) }
    fun <D : Comparable<D>> minBy(col: String) = rows.minBy { it[col] as D }

    fun all(predicate: RowFilter<T>): Boolean = rows.all { predicate(it, it) }
    fun any(predicate: RowFilter<T>): Boolean = rows.any { predicate(it, it) }

    fun count(predicate: RowFilter<T>) = rows.count { predicate(it, it) }

    fun first() = rows.first()
    fun firstOrNull() = rows.firstOrNull()
    fun last() = rows.last() // TODO: optimize (don't iterate through the whole data frame)
    fun lastOrNull() = rows.lastOrNull()
    fun take(numRows: Int = 5) = getRows(0 until numRows)
    fun skip(numRows: Int = 5) = getRows(numRows until nrow)
    fun takeLast(numRows: Int) = getRows(nrow - numRows until nrow)
    fun skipLast(numRows: Int = 5) = getRows(0 until nrow - numRows)
    fun head(numRows: Int = 5) = take(numRows)
    fun tail(numRows: Int = 5) = takeLast(numRows)
    fun reorder(permutation: List<Int>) = columns.map { it.reorder(permutation) }.let { dataFrameOf(it).typed<T>() }
    fun shuffled() = reorder((0 until nrow).shuffled())

    fun <R> map(selector: RowSelector<T, R>) = rows.map { selector(it, it) }
    fun forEach(selector: RowSelector<T, Unit>) = rows.forEach { selector(it, it) }

    val size get() = DataFrameSize(ncol, nrow)
}

interface UpdateClause<out T> {
    val df: TypedDataFrame<T>
    val cols: List<NamedColumn>
}

class UpdateClauseImpl<T>(override val df: TypedDataFrame<T>, override val cols: List<NamedColumn>) : UpdateClause<T>

inline infix fun <T, reified R> UpdateClause<T>.with(noinline expression: RowSelector<T, R>): TypedDataFrame<T> {
    val newCol = df.new("", expression)
    val names = cols.map { it.name }
    val newColumns = df.columns.map { if (names.contains(it.name)) newCol.rename(it.name) else it }
    return dataFrameOf(newColumns).typed<T>()
}

inline fun <T, reified R> TypedDataFrame<T>.update(vararg cols: Column, noinline expression: RowSelector<T, R>) =
        update(*cols).with(expression)

inline fun <T, reified R> TypedDataFrame<T>.update(vararg cols: String, noinline expression: RowSelector<T, R>) =
        update(*cols).with(expression)

inline fun <T> UpdateClause<T>.withNull() = with { null as Any? }

internal class TypedDataFrameImpl<T>(override val columns: List<DataCol>) : TypedDataFrame<T> {

    override val nrow = columns.firstOrNull()?.length ?: 0

    private val columnsMap by lazy { columns.map { it.name to it }.toMap() }

    init {
        val invalidSizeColumns = columns.filter { it.length != nrow }
        if (invalidSizeColumns.size > 0)
            throw Exception("Invalid column sizes: ${invalidSizeColumns}") // TODO

        val columnNames = columns.groupBy { it.name }.filter { it.value.size > 1 }
        if (columnNames.size > 0)
            throw Exception("Duplicate column names: ${columnNames}") // TODO
    }

    private val rowResolver = RowResolver(this)

    override val rows = object : Iterable<TypedDataFrameRow<T>> {
        override fun iterator() =

                object : Iterator<TypedDataFrameRow<T>> {
                    var curRow = 0

                    val resolver = RowResolver(this@TypedDataFrameImpl)

                    override fun hasNext(): Boolean = curRow < nrow

                    override fun next() = resolver.let { resolver[curRow++]!! }
                }
    }

    override fun get(rowIndex: Int) = rowResolver[rowIndex]!!

    override fun filter(predicate: RowFilter<T>): TypedDataFrame<T> =
            rowWise { getRow ->
                BooleanArray(nrow) { index ->
                    val row = getRow(index)!!
                    predicate(row, row)
                }
            }.let(::getRows).typed()

    override fun groupBy(cols: Iterable<NamedColumn>): GroupedDataFrame<T> {

        val columns = cols.map { this[it] }

        val groups = (0 until nrow)
                .map { index -> columns.map { it[index] } to index }
                .groupBy { it.first }
                .map {
                    val rowIndices = it.value.map { it.second }.toIntArray()
                    val groupColumns = this.columns.map { it[rowIndices] }
                    it.key to dataFrameOf(groupColumns).typed<T>()
                }

        val keyColumns = columns.mapIndexed { index, column ->
            var nullable = false
            val values = groups.map {
                it.first[index].also { if (it == null) nullable = true }
            }
            TypedDataCol(values, nullable, column.name, column.valueClass)
        }

        val groupFrames = groups.map { it.second }
        val groupsColumn = columnForGroupedData.withValues(groupFrames, false)

        val df = dataFrameOf(keyColumns + groupsColumn).typed<T>()
        return GroupedDataFrameImpl(df)
    }

    private fun DataCol.createComparator(naLast: Boolean = true): Comparator<Int> {

        if (!valueClass.isSubclassOf(Comparable::class))
            throw UnsupportedOperationException()

        return Comparator<Any?> { left, right ->
            (left as Comparable<Any?>).compareTo(right)
        }.let { if (naLast) nullsLast(it) else nullsFirst(it) }
                .let { Comparator { left, right -> it.compare(values[left], values[right]) } }
    }

    override fun sortBy(columns: List<SortColumnDescriptor>): TypedDataFrame<T> {

        val compChain = columns.map {
            val column = this[it.column]
            when (it.direction) {
                SortDirection.Asc -> column.createComparator()
                SortDirection.Desc -> column.createComparator().reversed()
            }
        }.reduce { a, b -> a.then(b) }

        val permutation = (0 until nrow).sortedWith(compChain)

        return reorder(permutation)
    }

    override fun getRows(indices: IntArray) = columns.map { col -> col.slice(indices) }.let { dataFrameOf(it).typed<T>() }

    override fun getRows(mask: BooleanArray) = columns.map { col -> col.getRows(mask) }.let { dataFrameOf(it).typed<T>() }

    override fun tryGetColumn(columnName: String) = columnsMap[columnName]

    override fun equals(other: Any?): Boolean {
        val df = other as? TypedDataFrame<*>
        if (df == null) return false
        return columns == df.columns
    }
}

internal fun <T> LinkedList<T>.popSafe() = if (isEmpty()) null else pop()

internal class RowResolver<T>(val dataFrame: TypedDataFrame<T>) {
    private val pool = LinkedList<TypedDataFrameRowImpl<T>>()
    private val map = mutableMapOf<Int, TypedDataFrameRowImpl<T>>()

    fun resetMapping() {
        pool.addAll(map.values)
        map.clear()
    }

    operator fun get(index: Int): TypedDataFrameRow<T>? =
            if (index < 0 || index >= dataFrame.nrow) null
            else map[index] ?: pool.let { it.popSafe() }?.also {
                it.index = index
                map[index] = it
            } ?: TypedDataFrameRowImpl(index, this).also { map[index] = it }
}

typealias RowAccessor<T> = (Int) -> TypedDataFrameRow<T>?

fun <R, T> TypedDataFrame<T>.rowWise(body: (RowAccessor<T>) -> R): R {
    val resolver = RowResolver(this)
    fun getRow(index: Int): TypedDataFrameRow<T>? {
        resolver.resetMapping()
        return resolver[index]
    }
    return body(::getRow)
}

fun <T> DataFrame.typed(): TypedDataFrame<T> = TypedDataFrameImpl(cols.map { it.typed() })

fun <T> TypedDataFrame<*>.typed(): TypedDataFrame<T> = TypedDataFrameImpl(columns)

fun <T> TypedDataFrameRow<T>.toDataFrame() = owner.columns.map {
    val value = it[index]
    it.withValues(listOf(value), value == null)
}.let { dataFrameOf(it).typed<T>() }