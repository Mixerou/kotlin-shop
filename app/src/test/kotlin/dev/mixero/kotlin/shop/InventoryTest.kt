package dev.mixero.kotlin.shop

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readLines
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InventoryTest {
    private val tempDir: Path = Files.createTempDirectory("kotlin-shop")

    private fun balanceOf(vararg transactions: String, skipBadLines: Boolean): List<String> {
        val input = tempDir.resolve("input.csv")
        val output = tempDir.resolve("output.csv")

        input.writeText(transactions.joinToString("\n"))
        Inventory.fromCsvTransactions(input, skipBadLines).toCsv(output)

        return output.readLines()
    }

    @AfterTest
    fun cleanUp() {
        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun simpleTransactions() {
        assertEquals(
            listOf("1;100;4", "1;200;3", "2;1;-2", "3;-9"),
            balanceOf(
                "1;100;10", "1;200;5", "1;12", "2;1;3", "2;5", "1;100;4", "3;9",
                skipBadLines = false,
            ),
        )
    }

    @Test
    fun skipExhaustedItems() {
        assertEquals(
            listOf("1;A;0", "1;B;0", "1;C;8"),
            balanceOf(
                "1;A;5", "1;5", "1;B;3", "1;C;10", "1;5", skipBadLines = false
            ),
        )
    }

    @Test
    fun lexicographicRank() {
        assertEquals(
            listOf("1;10;2", "1;2;5"),
            balanceOf("1;2;5", "1;10;5", "1;3", skipBadLines = false),
        )
    }

    @Test
    fun groupDebt() {
        assertEquals(
            listOf("9;-2", "9;A;0"),
            balanceOf("9;5", "9;A;3", skipBadLines = false),
        )
    }

    @Test
    fun restoreNegativeBalance() {
        assertEquals(
            listOf("1;A;2"),
            balanceOf("1;A;5", "1;10", "1;A;7", skipBadLines = false),
        )
    }

    @Test
    fun itemBelongsToExactlyOneGroup() {
        assertEquals(
            listOf("1;A;3"),
            balanceOf("1;A;5", "2;A;7", "1;2", skipBadLines = true),
        )
    }

    @Test
    fun skipBadLine() {
        assertEquals(
            listOf("1;A;7"),
            balanceOf("1;A;10", "I'm a Barbie girl", "", "1;A;0", "1;3", skipBadLines = true),
        )
    }

    @Test
    fun failBadLine() {
        assertFailsWith<InventoryError.TransactionsCsvBadLine> {
            balanceOf("1;A;ten", skipBadLines = false)
        }
    }
}
