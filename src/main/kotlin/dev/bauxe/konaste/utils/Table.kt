package dev.bauxe.konaste.utils

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class TableBuilder<T1, T2, T3>(private val data: List<T1>) {
  private val columns = mutableListOf<T2>()
  private val rows = mutableListOf<T3>()

  var aggregation: AggregationDirection = AggregationDirection.NONE
  lateinit var columnAccessor: (T1) -> T2
  lateinit var rowAccessor: (T1) -> T3

  var columnSorter: Comparator<in T2> = compareBy { 0 }
  var rowSorter: Comparator<in T3> = compareBy { 0 }

  var columnFilter: ((T2) -> Boolean) = { true }
  var rowFilter: ((T3) -> Boolean) = { true }
  var filters = mutableListOf<suspend (T1) -> Boolean>()

  infix fun column(column: T2) {
    columns.add(column)
  }

  fun columns(vararg columns: T2) {
    this.columns.addAll(columns)
  }

  infix fun row(row: T3) {
    rows.add(row)
  }

  fun rows(vararg rows: T3) {
    this.rows.addAll(rows)
  }

  private fun aggregate(
      data: MutableMap<Pair<T2, T3>, Int>,
      groupByKey: (Map.Entry<Pair<T2, T3>, Int>) -> Any?,
      reverse: Boolean
  ): MutableMap<Pair<T2, T3>, Int> {
    data.entries.groupBy(groupByKey).forEach { (_, group) ->
      var accum = 0
      (if (reverse) group.reversed() else group).forEach { (k, v) ->
        accum += v
        data[k] = accum
      }
    }
    return data
  }

  suspend fun build(): Table<T2, T3> {
    val groupedData =
        (columns.flatMap { a -> rows.map { b -> Pair(a, b) to 0 } }.toMap() +
                data
                    .filter {
                      columns.contains(columnAccessor(it)) &&
                          rows.contains(rowAccessor(it)) &&
                          filters.all { filter -> filter(it) }
                    }
                    .groupingBy { columnAccessor(it) to rowAccessor(it) }
                    .eachCount())
            .toMutableMap()
    val sortedColumns =
        columns.sortedWith(columnSorter).map { col ->
          col to groupedData.filterKeys { it.first == col }.values.sum()
        }
    val sortedRows =
        rows.sortedWith(rowSorter).map { row ->
          row to groupedData.filterKeys { it.second == row }.values.sum()
        }

    val result =
        groupedData.let {
          when (aggregation) {
            AggregationDirection.NONE -> it
            AggregationDirection.LEFT -> aggregate(it, { td -> td.key.second }, true)
            AggregationDirection.RIGHT -> aggregate(it, { td -> td.key.second }, false)
            AggregationDirection.UP -> aggregate(it, { td -> td.key.first }, true)
            AggregationDirection.DOWN -> aggregate(it, { td -> td.key.first }, false)
          }
        }

    val finalColumns =
        sortedColumns.filter { columnFilter(it.first) }.map { Title(it.first, it.second) }
    val finalRows = sortedRows.filter { rowFilter(it.first) }.map { Title(it.first, it.second) }

    return Table(
        finalColumns,
        finalRows,
        finalRows
            .map { it.title }
            .sortedWith(rowSorter)
            .map { row ->
              finalColumns
                  .map { it.title }
                  .sortedWith(columnSorter)
                  .map { col -> result[Pair(col, row)]!! }
                  .let { Row(it) }
            })
  }
}

@Serializable
enum class AggregationDirection(val value: String) {
  @SerialName("none") NONE("none"),
  @SerialName("left") LEFT("left"),
  @SerialName("right") RIGHT("right"),
  @SerialName("up") UP("up"),
  @SerialName("down") DOWN("down");

  companion object {
    fun from(s: String): AggregationDirection {
      return entries.first { it.value == s }
    }
  }
}

suspend fun <T1, T2, T3> table(
    data: List<T1>,
    block: TableBuilder<T1, T2, T3>.() -> Unit
): Table<T2, T3> {
  return TableBuilder<T1, T2, T3>(data).apply(block).build()
}

@Serializable
data class Table<T1, T2>(
    val columnTitles: List<Title<T1>>,
    val rowTitles: List<Title<T2>>,
    val rows: List<Row>
) {
  fun <N1, N2> transform(columnConverter: (T1) -> N1, rowConverter: (T2) -> N2): Table<N1, N2> {
    return Table(
        columnTitles.map { Title(columnConverter(it.title), it.count) },
        rowTitles.map { Title(rowConverter(it.title), it.count) },
        rows)
  }
}

@Serializable data class Row(val data: List<Int>) {}

@Serializable data class Title<T>(val title: T, val count: Int)
