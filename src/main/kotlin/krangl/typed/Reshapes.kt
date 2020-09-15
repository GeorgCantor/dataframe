package krangl.typed

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

fun <T> TypedDataFrame<T>.spread(columnSelector: TypedDataFrameForSpread<T>.(TypedDataFrameForSpread<T>) -> TypedCol<String?>): TypedDataFrame<T> {
    val receiver = TypedDataFrameForSpreadImpl(this)
    return when (val column = columnSelector(receiver, receiver)) {
        is TypedColumnPair<String?> -> {
            if (column.secondColumn != null)
                spreadToPair(column.firstColumn, column.secondColumn!!, column.groupingColumns)
            else spreadToBool(column.firstColumn, column.groupingColumns)
        }
        else -> spreadToBool(column, null)
    }
}

internal fun <T> TypedDataFrame<T>.spreadToPair(keyColumn: TypedCol<String?>, valueColumn: DataCol, groupBy: ColumnSet<*>?): TypedDataFrame<T> {

    val nameGenerator = nameGenerator()

    val (df1, keySrcColumn, keyColumnData) = extractConvertedColumn(this, keyColumn, nameGenerator)
    val (dataFrame, valueSrcColumn, valueColumnData) = extractConvertedColumn(df1, valueColumn, nameGenerator)

    val keys = keyColumnData.toSet()

    val groupingColumns = when {
        groupBy != null -> extractColumns(groupBy)
        else -> {
            val columnsToRemove = listOf(keySrcColumn, valueSrcColumn).filterNotNull()
            columns - columnsToRemove
        }
    }

    val valueColumnIndex = dataFrame.getColumnIndex(valueColumnData.name)

    assert(valueColumnIndex != -1)

    val grouped = dataFrame.groupBy(groupingColumns)
    return grouped.aggregate {
        keys.forEach { key ->
            if (key != null) {
                val columnName = nameGenerator.createUniqueName(key)
                var hasNulls = false
                val classes = mutableSetOf<KClass<*>>()
                val values = groups.map { group ->
                    val valueRows = group.filter { it[keyColumnData] == columnName }.distinct()
                    when (valueRows.nrow) {
                        0 -> {
                            hasNulls = true
                            null
                        }
                        1 -> {
                            val value = valueRows[0][valueColumnIndex]
                            if (value == null) hasNulls = true
                            else classes.add(value.javaClass.kotlin)
                            value
                        }
                        else -> {
                            val firstRow = group.first()
                            val groupKey = groupingColumns.map { "${it.name}: ${firstRow[it]}" }.joinToString()
                            val values = valueRows.map { it[valueColumnIndex] }.joinToString()
                            throw Exception("Different values ($values) for key '${key}' at entry [$groupKey]")
                        }
                    }
                }
                add(TypedDataCol(values, hasNulls, columnName, classes.commonParent()))
            }
        }
    }
}

internal fun extractOriginalColumn(column: Column): Column = when (column) {
    is ConvertedColumn<*> -> extractOriginalColumn(column.srcColumn)
    else -> column
}

internal fun <T, C> extractConvertedColumn(df: TypedDataFrame<T>, col: TypedCol<C>, nameGenerator: ColumnNameGenerator): Triple<TypedDataFrame<T>, Column?, TypedColData<C>> =
        when {
            col is ConvertedColumn<C> -> {
                val srcColumn = extractOriginalColumn(col.srcColumn)
                val columnData = col.data.ensureUniqueName(nameGenerator)
                Triple(df + columnData, srcColumn, columnData)
            }
            col.name.isEmpty() -> {
                val colData = col as TypedColData<C>
                val renamed = colData.rename(nameGenerator.createUniqueName("columnData"))
                Triple(df + renamed, null, renamed)
            }
            else -> Triple(df, col, df[col])
        }

internal fun <T> TypedDataFrame<T>.spreadToBool(col: TypedCol<String?>, groupBy: ColumnSet<*>?): TypedDataFrame<T> {

    val nameGenerator = nameGenerator()

    val (dataFrame, srcColumn, columnData) = extractConvertedColumn(this, col, nameGenerator)

    val valueColumnIndex = dataFrame.columns.indexOf(columnData)
    val values = columnData.toSet()

    val groupingColumns = groupBy?.let { extractColumns(it) } ?: srcColumn?.let { columns - it } ?: columns

    val grouped = dataFrame.groupBy(groupingColumns)
    return grouped.aggregate {
        values.forEach { value ->
            if (value != null) {
                val columnName = nameGenerator.createUniqueName(value.toString())
                add(columnName) {
                    it.any { it[valueColumnIndex] == value }
                }
            }
        }
    }
}

class GatherClause<T, C, K, R>(val df: TypedDataFrame<T>, val selector: ColumnsSelector<T, C>, val filter: ((C) -> Boolean)? = null,
                               val nameTransform: ((String) -> K), val valueTransform: ((C) -> R))

typealias Predicate<T> = (T) -> Boolean

internal infix fun <T> (Predicate<T>).and(other: Predicate<T>): Predicate<T> = { this(it) && other(it) }

fun <T, C> TypedDataFrame<T>.gather(selector: ColumnsSelector<T, C>) = GatherClause<T, C, String, C>(this, selector, null, { it }, { it })

fun <T, C, K, R> GatherClause<T, C, K, R>.where(filter: Predicate<C>) = GatherClause(df, selector, this.filter?.let { it and filter }
        ?: filter,
        nameTransform, valueTransform)

fun <T, C, K, R> GatherClause<T, C, *, R>.mapNames(transform: (String) -> K) = GatherClause<T, C, K, R>(df, selector, filter, transform, valueTransform)

fun <T, C, K, R> GatherClause<T, C, K, *>.mapValues(transform: (C) -> R) = GatherClause<T, C, K, R>(df, selector, filter, nameTransform, transform)

inline fun <T, C, reified K, R> GatherClause<T, C, K, R>.into(keyColumn: String) = gatherImpl(keyColumn, null, K::class)

inline fun <T, C, reified K, R> GatherClause<T, C, K, R>.into(columnPair: Pair<String, String>) = gatherImpl(columnPair.first, columnPair.second, K::class)

fun <T, C, K, R> GatherClause<T, C, K, R>.gatherImpl(namesTo: String, valuesTo: String? = null, keyColumnType: KClass<*>): TypedDataFrame<T> {

    val keyColumns = df.getColumns(selector).map { df[it] }
    val otherColumns = df.columns - keyColumns
    val outputColumnsData = otherColumns.map { ArrayList<Any?>() }.toMutableList()
    val keyColumnData = ArrayList<K>()
    val valueColumnData = ArrayList<R>()
    val classes = mutableSetOf<KClass<*>>()
    val include = filter ?: { true }
    var hasNullValues = false
    val keys = keyColumns.map { nameTransform(it.name) }
    (0 until df.nrow).forEach { row ->
        keyColumns.forEachIndexed { colIndex, col ->
            val value = col[row]
            if (include(value)) {
                outputColumnsData.forEachIndexed { index, list ->
                    list.add(otherColumns[index][row])
                }
                keyColumnData.add(keys[colIndex])
                if (valuesTo != null) {
                    val dstValue = valueTransform(value)
                    valueColumnData.add(dstValue)
                    if (dstValue == null)
                        hasNullValues = true
                    else classes.add((dstValue as Any).javaClass.kotlin)
                }
            }
        }
    }
    val resultColumns = outputColumnsData.mapIndexed { index, values ->
        val srcColumn = otherColumns[index]
        srcColumn.withValues(values, srcColumn.nullable)
    }.toMutableList()
    resultColumns.add(column(namesTo, keyColumnData, keys.any { it == null }, keyColumnType))
    if (valuesTo != null)
        resultColumns.add(column(valuesTo, valueColumnData, hasNullValues, classes.commonParent()))
    return dataFrameOf(resultColumns).typed()
}

fun <T> TypedDataFrame<T>.mergeRows(selector: ColumnsSelector<T, *>): TypedDataFrame<T> {

    val nestColumns = getColumns(selector).map { this[it] }
    val otherColumns = columns - nestColumns

    return groupBy(otherColumns).aggregate {
        nestColumns.forEach { col ->
            val values = groups.map { it[col].values }
            val newColumn = column(col.name, values, false, List::class)
            add(newColumn)
        }
    }
}

fun <T> TypedDataFrame<T>.splitRows(selector: ColumnSelector<T, List<*>>): TypedDataFrame<T> {

    val nestedColumn = getColumn(selector)
    if (!nestedColumn.valueClass.isSubclassOf(List::class)) {
        throw Exception("Column ${nestedColumn.name} must contain values of type `List`")
    }
    val nestedColumnIndex = columns.indexOf(nestedColumn)
    val column = nestedColumn.cast<List<*>?>()
    val outputRowsCount = (0 until nrow).sumBy { row ->
        column[row]?.size ?: 0
    }
    val outputColumnsData = Array<Array<Any?>>(ncol) { arrayOfNulls(outputRowsCount) }
    var dstRow = 0
    val classes = mutableSetOf<KClass<*>>()
    var hasNulls = false
    for (srcRow in 0 until nrow) {
        column[srcRow]?.forEach { value ->
            if (value == null) hasNulls = true
            else classes.add(value.javaClass.kotlin)
            for (col in 0 until ncol) {
                outputColumnsData[col][dstRow] = if (col == nestedColumnIndex) value else columns[col][srcRow]
            }
            dstRow++
        }
    }
    val resultColumns = columns.mapIndexed { index, col ->
        val (nullable, clazz) = if (index == nestedColumnIndex) hasNulls to classes.commonParent() else col.nullable to col.valueClass
        column(col.name, outputColumnsData[index].asList(), nullable, clazz)
    }
    return dataFrameOf(resultColumns).typed<T>()
}

fun <T> TypedDataFrame<T>.mergeCols(newColumn: String, selector: ColumnsSelector<T, *>) = mergeCols(newColumn, { list -> list }, selector)

fun <T> TypedDataFrame<T>.mergeColsToString(newColumnName: String, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", selector: ColumnsSelector<T, *>) = mergeCols(newColumnName, { list -> list.joinToString(separator = separator, prefix = prefix, postfix = postfix) }, selector)

internal inline fun <T, C, reified R> TypedDataFrame<T>.mergeCols(newColumn: String, crossinline transform: (List<C>) -> R, noinline selector: ColumnsSelector<T, C>): TypedDataFrame<T> {
    val nestColumns = getColumns(selector).map { this[it] }
    return add(newColumn) { row ->
        transform(nestColumns.map { it[row.index] })
    } - nestColumns
}

internal class ColumnDataCollector(initCapacity: Int = 0) {
    private val classes = mutableSetOf<KClass<*>>()
    private var hasNulls = false
    private val values = ArrayList<Any?>(initCapacity)

    fun add(value: Any?) {
        if (value == null) hasNulls = true
        else classes.add(value.javaClass.kotlin)
        values.add(value)
    }

    fun toColumn(name: String) = column(name, values, hasNulls, classes.commonParent())
}

fun <T> TypedDataFrame<T>.splitCol(vararg names: String, selector: ColumnSelector<T, *>): TypedDataFrame<T> {
    val column = getColumn(selector)

    val splitter: (Any?) -> List<Any?>?
    splitter = when (column.valueClass) {
        List::class -> { it: Any? -> it as? List<Any?> }
        String::class -> { it: Any? -> (it as? String?)?.split(",")?.map { it.trim() } }
        else -> throw Exception("Column type should be `List`")
    }

    val columnsCount = names.size
    val columnCollectors = Array(columnsCount) { ColumnDataCollector(nrow) }
    for (i in 0 until nrow) {
        val list = splitter(column[i])
        val listSize = list?.size ?: 0
        for (j in 0 until listSize) {
            columnCollectors[j].add(list!![j])
        }
        for (j in listSize until columnsCount)
            columnCollectors[j].add(null)
    }
    return this - column + columnCollectors.mapIndexed { i, col -> col.toColumn(names[i]) }
}