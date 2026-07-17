package dev.mixero.kotlin.shop

import com.github.ajalt.clikt.core.CoreCliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

class App : CoreCliktCommand() {
    val name by argument("name", help = "Your name").optional()

    val greeting: String
        get() = if (name.isNullOrBlank()) "Hello!" else "Hello, $name!"

    override fun run() = logger.info { greeting }
}

fun main(args: Array<String>) = App().main(args)
