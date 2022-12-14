import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val corda_gradle_plugins_version: String by project
val corda_release_version: String by project
val kmongo_version: String by project

plugins {
    application
    kotlin("jvm") version "1.6.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.21"
}

group = "com.rewera"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()

    maven { url = uri("https://software.r3.com/artifactory/corda") }
    maven { url = uri("https://software.r3.com/artifactory/corda-dependencies") }
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-jackson:$ktor_version")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")

    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("com.google.inject:guice:5.1.0")

    implementation("org.litote.kmongo:kmongo:$kmongo_version")
//    implementation("org.litote.kmongo:kmongo-async:$kmongo_version")

    implementation("net.corda:corda-rpc:$corda_release_version")

    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("io.ktor:ktor-client-content-negotiation-jvm:$ktor_version")
    testImplementation("io.kotest:kotest-runner-junit5:5.4.1")
    runtimeOnly("io.kotest:kotest-assertions-core:5.4.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")

}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    testLogging {
        events(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED
        )
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true

        debug {
            events(
                TestLogEvent.STARTED,
                TestLogEvent.FAILED,
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED,
                TestLogEvent.STANDARD_ERROR,
                TestLogEvent.STANDARD_OUT
            )
            exceptionFormat = TestExceptionFormat.FULL
        }

        info.events = debug.events
        info.exceptionFormat = debug.exceptionFormat

//        afterSuite { (desc, result) ->
//            if (!desc.parent) { // will match the outermost suite
//                def output = "Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} passed, ${result.failedTestCount} failed, ${result.skippedTestCount} skipped)"
//                def startItem = '|  ', endItem = '  |'
//                def repeatLength = startItem.length() + output.length() + endItem.length()
//                println('\n' + ('-' * repeatLength) + '\n' + startItem + output + endItem + '\n' + ('-' * repeatLength))
//            }
//        }
    }
}
