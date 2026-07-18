package dev.mixero.kotlin.shop

import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path
import java.util.*
import kotlin.io.path.absolute
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createDirectories
import kotlin.io.path.isRegularFile
import kotlin.io.path.useLines

private val logger = KotlinLogging.logger { }

typealias ItemsByGroup = TreeMap<String, TreeSet<String>>
typealias ItemsCount = HashMap<String, Int>
typealias GroupByItem = HashMap<String, String>
typealias GroupsDebts = HashMap<String, Int>

class Inventory private constructor(
    private val itemsByGroup: ItemsByGroup,
    private val itemsCount: ItemsCount,
    private val groupByItem: GroupByItem,
    private val groupsDebts: GroupsDebts
) {
    fun toCsv(filePath: Path) {
        logger.info { "Writing inventory balance to the $filePath" }

        filePath.absolute().parent?.createDirectories()
        filePath.bufferedWriter().use { writer ->
            val groupIds = TreeSet(itemsByGroup.keys).apply { addAll(groupsDebts.keys) }
            for (groupId in groupIds) {
                val groupDebt = groupsDebts[groupId] ?: 0
                if (groupDebt > 0) {
                    val line = "$groupId;${-groupDebt}"

                    logger.debug { "Writing inventory balance debt line to CSV file: $line" }
                    writer.append("$line\n")
                }

                for (itemId in itemsByGroup[groupId].orEmpty()) {
                    val count = itemsCount[itemId] ?: 0
                    val line = "$groupId;$itemId;$count"

                    logger.debug { "Writing inventory balance item line to CSV file: $line" }
                    writer.append("$line\n")
                }
            }
        }
    }

    companion object {
        private const val RECEIPT_TRANSACTION_CSV_FIELDS = 3
        private const val SALE_TRANSACTION_CSV_FIELDS = 2

        fun fromCsvTransactions(filePath: Path, skipBadLines: Boolean = false): Inventory {
            logger.info { "Constructing Inventory from transactions in the CSV format" }

            if (!filePath.isRegularFile()) throw InventoryError.TransactionsFileNotFound(filePath)

            val inventory = filePath.useLines { lines ->
                fun parseCount(count: String, line: String): Int? {
                    val count = count.toIntOrNull() ?: run {
                        val error = InventoryError.TransactionsCsvBadLine(line, "Count must be an integer")
                        if (!skipBadLines) throw error
                        logger.warn { error.message }
                        return null
                    }
                    if (count < 1) {
                        val error = InventoryError.TransactionsCsvBadLine(line, "Count must be positive")
                        if (!skipBadLines) throw error
                        logger.warn { error.message }
                        return null
                    }

                    return count
                }

                val itemsByGroup: ItemsByGroup = TreeMap()
                val itemsCount: ItemsCount = HashMap()
                val groupByItem: GroupByItem = HashMap()
                val groupsDebts: GroupsDebts = HashMap()

                lines.forEach { line ->
                    if (line.isBlank()) return@forEach

                    val fields = line.trim().split(';').map(String::trim)

                    when (fields.size) {
                        RECEIPT_TRANSACTION_CSV_FIELDS -> {
                            val (groupId, itemId, countString) = fields
                            var count = parseCount(countString, line) ?: return@forEach
                            val groupDebt = groupsDebts[groupId] ?: 0

                            if (groupByItem.getOrPut(itemId) { groupId } != groupId) {
                                val error = InventoryError.TransactionsCsvBadLine(
                                    line,
                                    "Item cannot be connected to more than one group"
                                )
                                if (skipBadLines) return@forEach logger.warn { error.message } else throw error
                            }

                            logger.debug { "Processing transactions CSV line: Receipt: $line" }

                            if (groupDebt > 0) {
                                count -= groupDebt
                                if (count < 0) {
                                    groupsDebts[groupId] = -count
                                    count = 0
                                } else groupsDebts.remove(groupId)
                            }

                            itemsByGroup.getOrPut(groupId) { TreeSet() }.add(itemId)
                            itemsCount.merge(itemId, count, Int::plus)
                        }

                        SALE_TRANSACTION_CSV_FIELDS -> {
                            val (groupId, countString) = fields
                            val groupItems = itemsByGroup[groupId]
                            var count = parseCount(countString, line) ?: return@forEach

                            logger.debug { "Processing transactions CSV line: Sale: $line" }

                            if (groupItems.isNullOrEmpty()) {
                                groupsDebts.merge(groupId, count, Int::plus)
                                return@forEach
                            }

                            for (itemId in groupItems) {
                                if (count < 1) break
                                if ((itemsCount[itemId] ?: 0) < 1) continue

                                val forSale = minOf(itemsCount[itemId] ?: 0, count)

                                count -= forSale
                                itemsCount.merge(itemId, forSale, Int::minus)
                            }

                            if (count > 0) {
                                val itemId = groupItems.last()
                                itemsCount.merge(itemId, count, Int::minus)
                            }
                        }

                        else -> {
                            val error = InventoryError.TransactionsCsvBadLine(line, "Incorrect number of fields")
                            if (skipBadLines) return@forEach logger.warn { error.message } else throw error
                        }
                    }
                }

                Inventory(itemsByGroup, itemsCount, groupByItem, groupsDebts)
            }

            return inventory
        }
    }
}

sealed class InventoryError(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    class TransactionsFileNotFound(val path: Path) : InventoryError("Failed to find transactions file at $path")

    class TransactionsCsvBadLine(val line: String, val details: String) :
        InventoryError("Failed to parse the transaction CSV line: $details: $line")
}
