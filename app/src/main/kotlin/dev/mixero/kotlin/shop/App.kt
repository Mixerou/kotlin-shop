package dev.mixero.kotlin.shop

import com.github.ajalt.clikt.core.CoreCliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

class App : CoreCliktCommand() {
    val inputFilePath by option(
        "-i", "--input",
        help = "Input CSV file path",
    ).path().required()
    val outputFilePath by option(
        "-o", "--output",
        help = "Output CSV file path",
    ).path().required()

    override fun run() {
        try {
            val inventory = Inventory.fromCsvTransactions(inputFilePath, skipBadLines = true)
            inventory.toCsv(outputFilePath)
        } catch (error: InventoryError) {
            logger.error { error.message }
            throw ProgramResult(1)
        }
    }
}

fun main(args: Array<String>) = App().main(args)
