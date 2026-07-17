package dev.mixero.kotlin.shop

import com.github.ajalt.clikt.core.parse
import kotlin.test.Test
import kotlin.test.assertEquals

class AppTest {
    private fun greetingFrom(vararg argv: String): String =
        App().apply { parse(argv.asList()) }.greeting

    @Test
    fun greetWithName() {
        assertEquals(
            "Hello, Kotlin!",
            greetingFrom("Kotlin"),
            "App should greet with a specified name",
        )
    }

    @Test
    fun greetWithoutName() {
        assertEquals(
            "Hello!",
            greetingFrom(),
            "App should greet without any name",
        )
    }
}
