package org.jetbrains.dataframe

import java.math.BigDecimal
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmErasure

inline fun <reified T : Comparable<T>> Iterable<T>.median(): Double {
    val sorted = sorted()
    val size = sorted.size
    val index = size / 2
    return when (T::class) {
        Double::class -> if (size % 2 == 0) (sorted[index - 1] as Double + sorted[index] as Double) / 2.0 else sorted[index] as Double
        Int::class -> if (size % 2 == 0) (sorted[index - 1] as Int + sorted[index] as Int) / 2.0 else (sorted[index] as Int).toDouble()
        Long::class -> if (size % 2 == 0) (sorted[index - 1] as Long + sorted[index] as Long) / 2.0 else (sorted[index] as Long).toDouble()
        else -> throw IllegalArgumentException()
    }
}

class Counter(var value: Int = 0){
    operator fun inc(): Counter {
        value++
        return this
    }
}

fun <T> Iterable<T>.computeSize(counter: Counter) = map {
    counter.inc()
    it
}

internal fun Int.zeroToOne() = if(this == 0) 1 else this

inline fun <reified T : Number> Iterable<T>.mean() = mean(T::class)

fun <T : Number> Iterable<T>.mean(clazz: KClass<T>) = when (clazz) {
    Double::class -> (this as Iterable<Double>).mean()
    Int::class -> (this as Iterable<Int>).mean()
    Long::class -> (this as Iterable<Long>).mean()
    BigDecimal::class -> (this as Iterable<BigDecimal>).mean()
    else -> throw IllegalArgumentException()
}

fun <T: Number> ColumnData<T>.mean() = values.mean(type.jvmErasure as KClass<T>)

@JvmName("doubleMean")
fun Iterable<Double>.mean(): Double {
    val counter = Counter()
    return computeSize(counter).sum() / counter.value.zeroToOne()
}

@JvmName("intMean")
fun Iterable<Int>.mean(): Double {
    val counter = Counter()
    return computeSize(counter).map { it.toDouble() }.sum() / counter.value.zeroToOne()
}

@JvmName("longMean")
fun Iterable<Long>.mean(): Double {
    val counter = Counter()
    return computeSize(counter).sum().toDouble() / counter.value.zeroToOne()
}

@JvmName("bigDecimalMean")
fun Iterable<BigDecimal>.mean(): Double {
    val counter = Counter()
    return computeSize(counter).sum().toDouble() / counter.value.zeroToOne()
}

fun <T: Number> Iterable<T>.sum(clazz: KClass<T>) = when (clazz) {
    Double::class -> (this as Iterable<Double>).sum() as T
    Int::class -> (this as Iterable<Int>).map { it.toDouble() }.sum() as T
    Long::class -> (this as Iterable<Long>).sum() as T
    BigDecimal::class -> (this as Iterable<BigDecimal>).sum() as T
    else -> throw IllegalArgumentException()
}

fun Iterable<BigDecimal>.sum(): BigDecimal {
    var sum: BigDecimal = BigDecimal.ZERO
    for (element in this) {
        sum += element
    }
    return sum
}

inline fun <reified T : Number> sum(list: Iterable<T>): T = list.sum(T::class)

fun <T: Number> ColumnData<T>.sum() = values.sum(type.jvmErasure as KClass<T>)

internal fun <T> TypedDataFrame<T>.nullColumnToZero(col: ColumnDef<Number?>) =
        when (this[col].type.jvmErasure) {
            Double::class -> update(col) { col() as Double? ?: .0 }
            Int::class -> update(col) { col() as Int? ?: 0 }
            Long::class -> update(col) { col() as Long? ?: 0 }
            else -> throw IllegalArgumentException()
        }
