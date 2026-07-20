#!/usr/bin/env kotlin

import java.io.File
import kotlin.random.Random
import kotlin.system.exitProcess

val defaultTransactionCount = 1_000
val defaultRandomSeed = 339L

fun exitWithHelp(exitStatus: Int): Nothing {
    println("Transactions Generator")
    println("Usage: $__FILE__ <outputFile> [transactions] [seed]")
    println("Defaults: transactions=$defaultTransactionCount, seed=$defaultRandomSeed")
    exitProcess(exitStatus)
}

if (args.firstOrNull() == "-h" || args.firstOrNull() == "--help") exitWithHelp(0)

val outputFile = File(args.getOrNull(0) ?: exitWithHelp(1))
val transactionCount = args.getOrNull(1)?.toIntOrNull() ?: defaultTransactionCount
val seed = args.getOrNull(2)?.toLongOrNull() ?: defaultRandomSeed

val random = Random(seed)

val regularGroupCount = maxOf(3, transactionCount / 100)
val debtOnlyGroupCount = maxOf(1, regularGroupCount / 20)

val receiptRate = 0.65
val saleRate = 0.34

var nextItemId = 1
val itemsByGroup = (1..regularGroupCount).associateWith {
    List(random.nextInt(3, 13)) { (nextItemId++).toString() }
}

val stockedGroups = mutableListOf<Int>()
val alreadyStocked = mutableSetOf<Int>()

val transactions = buildList {
    repeat(transactionCount) {
        val roll = random.nextDouble()

        when {
            // Receipts
            roll < receiptRate || stockedGroups.isEmpty() -> {
                val group = random.nextInt(1, regularGroupCount + 1)
                val item = itemsByGroup.getValue(group).random(random)

                if (alreadyStocked.add(group)) stockedGroups += group

                add("$group;$item;${random.nextInt(1, 101)}")
            }

            // Sales
            roll < receiptRate + saleRate -> {
                val group = stockedGroups.random(random)
                val isHighSale = random.nextDouble() < 0.02
                val count = if (isHighSale) random.nextInt(400, 1_501) else random.nextInt(1, 81)

                add("$group;$count")
            }

            // Debts
            else -> {
                val group = regularGroupCount + random.nextInt(1, debtOnlyGroupCount + 1)
                add("$group;${random.nextInt(1, 31)}")
            }
        }
    }
}

outputFile.parentFile?.mkdirs()
outputFile.writeText(transactions.joinToString("\n", postfix = "\n"))

println("Generated $transactionCount transactions to ${outputFile.path}")
