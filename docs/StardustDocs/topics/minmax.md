[//]: # (title: min / max)

<!---IMPORT org.jetbrains.kotlinx.dataframe.samples.api.Analyze-->

Computes the minimum / maximum of values.

Is available for [`Comparable`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-comparable/) columns. 
`null` and `NaN` values are ignored.

<!---FUN minmaxModes-->

```kotlin
df.min() // min of values per every comparable column
df.min { age and weight } // min of all values in `age` and `weight`
df.minFor { age and weight } // min of values per `age` and `weight` separately
df.minOf { (weight ?: 0) / age } // min of expression evaluated for every row
df.minBy { age } // DataRow with minimal `age`
```

<dataFrame src="org.jetbrains.kotlinx.dataframe.samples.api.Analyze.minmaxModes.html"/>
<!---END-->

<!---FUN minmaxAggregations-->

```kotlin
df.min()
df.age.min()
df.groupBy { city }.min()
df.pivot { city }.min()
df.pivot { city }.groupBy { name.lastName }.min()
```

<dataFrame src="org.jetbrains.kotlinx.dataframe.samples.api.Analyze.minmaxAggregations.html"/>
<!---END-->

See [statistics](summaryStatistics.md#groupby-statistics) for details on complex data aggregations.
