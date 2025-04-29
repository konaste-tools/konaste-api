import java.util.*

plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version("2.0.20")
    id("com.ncorti.ktfmt.gradle") version "0.17.0"

    alias(libs.plugins.ktor)
    idea
    application
}

group = "dev.bauxe.konaste"
version = "0.1.1a"

repositories {
    mavenCentral()
}

sourceSets {
    create("it") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

idea {
    module {
        testSources.from(sourceSets["it"].kotlin.srcDirs)
    }
}

val itImplementation by configurations.getting {
    extendsFrom(configurations.implementation.get())
}
val itRuntimeOnly by configurations.getting

configurations["itRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())

dependencies {
    implementation(libs.bundles.jna)

    implementation(libs.bundles.ktorServer)
    implementation(libs.bundles.ktorClient)
    implementation(libs.bundles.ktorCommon)

    implementation(libs.bundles.koin)

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0-RC.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.8.0")
    implementation("com.typesafe:config:1.4.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.18.1")
    implementation("com.fasterxml.woodstox:woodstox-core:6.5.0")

    implementation(libs.bundles.logging)

    implementation("io.github.smiley4:ktor-swagger-ui:3.4.0")
    implementation("io.ktor:ktor-client-logging:2.3.12")

    implementation(libs.bundles.sqlite)

    testImplementation(kotlin("test"))
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.bundles.ktorTest)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("io.mockk:mockk:1.13.10")

    itImplementation(kotlin("test"))
    itImplementation(libs.bundles.kotest)
    itImplementation(libs.bundles.ktorTest)
    itImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    itImplementation("io.mockk:mockk:1.13.10")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>()
    .configureEach {
        compilerOptions
            .languageVersion
            .set(
                org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1
            )
        compilerOptions
            .apiVersion
            .set(
                org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1
            )
    }

tasks.withType<ProcessResources>() {
    doLast {
        val propertiesFile = layout.buildDirectory.file("resources/main/version.properties").get().asFile
        propertiesFile.parentFile.mkdirs()
        val properties = Properties()
        properties.setProperty("version", rootProject.version.toString())
        propertiesFile.writer().use { properties.store(it, null) }
    }
}

application {
    mainClass.set(
        "dev.bauxe.konaste.MainKt",
    )
}
