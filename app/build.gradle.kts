plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.clikt)
    implementation(libs.kotlin.logging)
    runtimeOnly(libs.slf4j.simple)
}

testing {
    suites {
        named<JvmTestSuite>("test") { useKotlinTest("2.3.21") }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass = "dev.mixero.kotlin.shop.AppKt"
    version = "0.0.1"
    applicationDefaultJvmArgs = listOf("-Dkotlin-logging.logStartupMessage=false")
}
