package krangl.typed

internal fun <T> GroupedDataFrame<T>.getColumns(selector: ColumnSelector<T>) =
        TypedDataFrameWithColumnsForSelectImpl((this as GroupedDataFrameImpl<T>).groups.first().df)
                .let { selector(it, it).extractColumns() }

typealias GroupKey = List<Any?>

interface DataGroup<out T> {
    val groupKey: GroupKey
    val df: TypedDataFrame<T>
}

class DataGroupImpl<T>(override val groupKey: GroupKey,
                       override val df: TypedDataFrame<T>) : DataGroup<T> {
    override fun toString(): String {
        return "DataGroup($groupKey)" // just needed for debugging
    }
}

interface GroupedDataFrame<out T> {

    val groups: List<DataGroup<T>>
    val columnNames: List<String>

    operator fun get(vararg values: Any?) = get(values.toList())
    operator fun get(key: GroupKey) = groups.firstOrNull { it.groupKey.equals(key) }?.df

    fun ungroup() = groups.map { it.df }.merge()
    fun groupedBy() = groups.map { it.df.take(1).select(*columnNames.toTypedArray()) }.merge()
    fun groups() = groups.map { it.df }

    fun sortBy(columns: Iterable<NamedColumn>) = modify { sortBy(columns) }
    fun sortBy(selector: ColumnSelector<T>) = sortBy(getColumns(selector))

    fun sortByDesc(selector: ColumnSelector<T>) = sortByDesc(getColumns(selector))
    fun sortByDesc(columns: Iterable<NamedColumn>) = modify { sortByDesc(columns) }

    fun modify(transform: TypedDataFrame<T>.() -> TypedDataFrame<*>): GroupedDataFrame<T>

    fun count(columnName: String = "n") = aggregate { count into columnName }
    fun count(columnName: String = "n", filter: RowFilter<T>) = aggregate { count(filter) into columnName }
}

class GroupedDataFrameImpl<T>(override val columnNames: List<String>, override val groups: List<DataGroup<T>>) : GroupedDataFrame<T> {

    override fun modify(transform: TypedDataFrame<T>.() -> TypedDataFrame<*>) =
            GroupedDataFrameImpl(columnNames, groups.map { DataGroupImpl<T>(it.groupKey, transform(it.df).typed()) })
}

class ValuesList<T>(val list: List<T>)

class GroupAggregateBuilder<T>(private val dataFrame: GroupedDataFrame<T>) {
    internal val columns = mutableListOf<DataCol>()

    fun <T> List<T>.wrap() = ValuesList(this)

    val groups by lazy { dataFrame.groups() }

    fun add(column: DataCol) = columns.add(column)

    fun <N : Comparable<N>> minBy(selector: RowSelector<T, N>) = compute { minBy(selector)!! }
    fun <N : Comparable<N>> minBy(column: TypedCol<N>) = compute { minBy { column() }!! }

    fun <N : Comparable<N>> maxBy(selector: RowSelector<T, N>) = compute { maxBy(selector)!! }
    fun <N : Comparable<N>> maxBy(column: TypedCol<N>) = compute { maxBy { column() }!! }

    fun <R> ValuesList<TypedDataFrameRow<T>>.map(selector: RowSelector<T, R>) =
            list.map{selector(it,it)}.wrap()

    fun <R> TypedDataFrame<T>.map(selector: RowSelector<T, R>) =
            rows.map{selector(it,it)}

    val count by lazy { groups.map { it.nrow }.wrap() }

    fun count(filter: RowFilter<T>) = groups.map { it.count(filter) }.wrap()

    inline fun <reified R : Comparable<R>> median(noinline selector: RowSelector<T, R>) = groups.map { it.map(selector).median() }.wrap()
    inline fun <reified R : Comparable<R>> median(column: TypedCol<R>) = groups.map { it.map { column() }.median() }.wrap()

    inline fun <reified R : Number> mean(noinline selector: RowSelector<T, R>) = groups.map { it.map(selector).mean() }.wrap()
    inline fun <reified R : Number> mean(column: TypedCol<R>) = groups.map { it.map { column() }.mean() }.wrap()

    inline fun <reified R : Number> sum(column: TypedCol<R>) = groups.map { sum(it[column].values) }.wrap()
    inline fun <reified R : Number> sum(noinline selector: RowSelector<T, R>) = groups.map { sum(it.map(selector)) }.wrap()

    fun checkAll(predicate: RowFilter<T>) = groups.map { it.all(predicate) }.wrap()
    fun any(predicate: RowFilter<T>) = groups.map { it.any(predicate) }.wrap()

    inline fun <reified R : Comparable<R>> min(noinline selector: RowSelector<T, R>) = groups.map { it.min(selector)!! }.wrap()
    inline fun <reified R : Comparable<R>> min(column: TypedCol<R>) = groups.map { it.min(column)!! }.wrap()

    inline fun <reified R : Comparable<R>> max(noinline selector: RowSelector<T, R>) = groups.map { it.max(selector)!! }.wrap()
    inline fun <reified R : Comparable<R>> max(column: TypedCol<R>) = groups.map { it.max(column)!! }.wrap()

    inline infix fun <reified R> ValuesList<R>.into(columnName: String) = add(newColumn(columnName, list))

    fun <R> compute(selector: TypedDataFrame<T>.() -> R) = groups.map(selector).wrap()

    inline fun <reified R> add(name: String, noinline expression: TypedDataFrame<T>.() -> R?) = add(newColumn(name, groups.map { expression(it) }))

    inline infix fun <reified R> String.to(noinline expression: TypedDataFrame<T>.() -> R?) = add(this, expression)

    inline operator fun <reified R> String.invoke(noinline expression: TypedDataFrame<T>.() -> R?) = add(this, expression)

    infix operator fun String.invoke(column: DataCol) = add(column.rename(this))
}

inline fun <T, reified R> GroupedDataFrame<T>.compute(columnName: String = "map", noinline selector: TypedDataFrame<T>.() -> R) = aggregate { compute(selector) into columnName }

fun <T> GroupedDataFrame<T>.aggregate(body: GroupAggregateBuilder<T>.() -> Unit): TypedDataFrame<T> {
    val builder = GroupAggregateBuilder(this)
    body(builder)
    return (this.groupedBy() + builder.columns).typed<T>()
}

inline fun <T, reified R : Comparable<R>> GroupedDataFrame<T>.median(columnName: String = "median", noinline selector: RowSelector<T, R>) = aggregate { median(selector) into columnName }
inline fun <T, reified R : Number> GroupedDataFrame<T>.mean(columnName: String = "mean", noinline selector: RowSelector<T, R>) = aggregate { mean(selector) into columnName }
inline fun <T, reified R : Comparable<R>> GroupedDataFrame<T>.min(columnName: String = "min", noinline selector: RowSelector<T, R>) = aggregate { min(selector) into columnName }
inline fun <T, reified R : Comparable<R>> GroupedDataFrame<T>.max(columnName: String = "max", noinline selector: RowSelector<T, R>) = aggregate { max(selector) into columnName }