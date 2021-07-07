package org.jetbrains.dataframe.gradle

import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Test
import java.io.File
import java.nio.file.Files

internal class SchemaGeneratorPluginTes {

    @Test
    fun `plugin configured via configure`() {
        val result = runGradleBuild(":generateTest") {
            """
            import java.net.URL
            import org.jetbrains.dataframe.gradle.SchemaGeneratorExtension    
                
            plugins {
                kotlin("jvm") version "1.4.10"
                id("org.jetbrains.dataframe.schema-generator")
            }
            
            repositories {
                mavenCentral() 
            }

            configure<SchemaGeneratorExtension> {
                schema {
                    src = buildDir
                    data = URL("https://raw.githubusercontent.com/Kotlin/dataframe/8ea139c35aaf2247614bb227756d6fdba7359f6a/data/playlistItems.json")
                    interfaceName = "Test"
                    packageName = "org.test"
                }
            }
            """.trimIndent()
        }
        result.task(":generateTest")?.outcome shouldBe TaskOutcome.SUCCESS
    }

    @Test
    fun `plugin configured via extension DSL`() {
        val result = runGradleBuild(":generateTest") {
            """
            import java.net.URL
            import org.jetbrains.dataframe.gradle.SchemaGeneratorExtension    
                
            plugins {
                kotlin("jvm") version "1.4.10"
                id("org.jetbrains.dataframe.schema-generator")
            }
            
            repositories {
                mavenCentral() 
            }

            schemaGenerator {
                schema {
                    src = buildDir
                    data = URL("https://raw.githubusercontent.com/Kotlin/dataframe/8ea139c35aaf2247614bb227756d6fdba7359f6a/data/playlistItems.json")
                    interfaceName = "Test"
                    packageName = "org.test"
                }
            }
            """.trimIndent()
        }
        result.task(":generateTest")?.outcome shouldBe TaskOutcome.SUCCESS
    }

    @Test
    fun `plugin configure multiple schemas from URLs via extension`() {
        val result = runGradleBuild(":generateAll") {
            """
            import java.net.URL
            
            import org.jetbrains.dataframe.gradle.SchemaGeneratorExtension    
                
            plugins {
                kotlin("jvm") version "1.4.10"
                id("org.jetbrains.dataframe.schema-generator")
            }
            
            repositories {
                mavenCentral() 
            }

            schemaGenerator {
                schema {
                    src = buildDir
                    data = URL("https://raw.githubusercontent.com/Kotlin/dataframe/8ea139c35aaf2247614bb227756d6fdba7359f6a/data/playlistItems.json")
                    interfaceName = "Test"
                    packageName = "org.test"
                }
                schema {
                    src = buildDir
                    data = URL("https://raw.githubusercontent.com/Kotlin/dataframe/8ea139c35aaf2247614bb227756d6fdba7359f6a/data/ghost.json")
                    interfaceName = "Schema"
                    packageName = "org.test"
                }
            }
            """.trimIndent()
        }
        result.task(":generateTest")?.outcome shouldBe TaskOutcome.SUCCESS
        result.task(":generateSchema")?.outcome shouldBe TaskOutcome.SUCCESS
    }

    @Test
    fun `plugin configure multiple schemas from files via extension`() {
        val dataDir = File("../../../data")
        val result = runGradleBuild(":generateAll") {
            """
            import org.jetbrains.dataframe.gradle.SchemaGeneratorExtension    
                
            plugins {
                kotlin("jvm") version "1.4.10"
                id("org.jetbrains.dataframe.schema-generator")
            }
            
            repositories {
                mavenCentral() 
            }

            schemaGenerator {
                schema {
                    src = buildDir
                    data = File("$dataDir/ghost.json")
                    interfaceName = "Test"
                    packageName = "org.test"
                }
                schema {
                    src = buildDir
                    data = File("$dataDir/playlistItems.json")
                    interfaceName = "Schema"
                    packageName = "org.test"
                }
            }
            """.trimIndent()
        }
        result.task(":generateTest")?.outcome shouldBe TaskOutcome.SUCCESS
        result.task(":generateSchema")?.outcome shouldBe TaskOutcome.SUCCESS
    }

    @Test
    fun `plugin configure multiple schemas from strings via extension`() {
        val dataDir = File("../../../data")
        val result = runGradleBuild(":generateAll") { buildDir ->
            """
            import org.jetbrains.dataframe.gradle.SchemaGeneratorExtension 
               
            plugins {
                kotlin("jvm") version "1.4.10"
                id("org.jetbrains.dataframe.schema-generator")
            }
            
            repositories {
                mavenCentral() 
            }

            schemaGenerator {
                schema {
                    src = buildDir
                    data = "$dataDir/ghost.json"
                    interfaceName = "Test"
                    packageName = "org.test"
                }
                schema {
                    src = buildDir
                    data = "$dataDir/playlistItems.json"
                    interfaceName = "Schema"
                    packageName = "org.test"
                }
            }
            """.trimIndent()
        }
        result.task(":generateTest")?.outcome shouldBe TaskOutcome.SUCCESS
        result.task(":generateSchema")?.outcome shouldBe TaskOutcome.SUCCESS
    }

    @Test
    fun `compileKotlin depends on generateAll task`() {
        val dataDir = File("../../../data")
        val result = runGradleBuild(":compileKotlin") { buildDir ->
            """
            import org.jetbrains.dataframe.gradle.SchemaGeneratorExtension    
                
            plugins {
                kotlin("jvm") version "1.4.10"
                id("org.jetbrains.dataframe.schema-generator")
            }
            
            repositories {
                mavenCentral() 
            }

            schemaGenerator {
                schema {
                    src = buildDir
                    data = File("$dataDir/ghost.json")
                    interfaceName = "Test"
                    packageName = "org.test"
                }
                schema {
                    src = buildDir
                    data = File("$dataDir/playlistItems.json")
                    interfaceName = "Schema"
                    packageName = "org.test"
                }
            }
            """.trimIndent()
        }
        result.task(":generateTest")?.outcome shouldBe TaskOutcome.SUCCESS
        result.task(":generateSchema")?.outcome shouldBe TaskOutcome.SUCCESS
    }

    @Test
    fun `generated code resolved`() {
        val result = runGradleBuild(":build") { buildDir ->
            val dataFile = File(buildDir, "data.csv")
            dataFile.writeText(TestData.csvSample)

            val kotlin = File(buildDir, "src/main/kotlin").also { it.mkdirs() }
            val main = File(kotlin, "Main.kt")
            main.writeText("""
                import org.jetbrains.dataframe.DataFrame
                import org.jetbrains.dataframe.io.read
                import org.jetbrains.dataframe.typed
                import org.jetbrains.dataframe.filter
                
                fun main() {
                    val df = DataFrame.read("$dataFile").typed<Schema>()
                    val df1 = df.filter { age != null }
                }
            """.trimIndent())

            """
                import org.jetbrains.dataframe.gradle.SchemaGeneratorExtension    
                    
                plugins {
                    kotlin("jvm") version "1.4.10"
                   id("org.jetbrains.dataframe.schema-generator")
                }
                
                repositories {
                    mavenCentral() 
                }
                
                dependencies {
                    implementation("org.jetbrains.kotlinx:dataframe:0.7.3-dev-277-0.10.0.53")
                }
                
                schemaGenerator {
                    schema {
                        data = "$dataFile"
                        src = File("$kotlin")
                        interfaceName = "Schema"
                        packageName = ""
                    }
                }
            """.trimIndent()
        }
        result.task(":build")?.outcome shouldBe TaskOutcome.SUCCESS
    }

    @Test
    fun `src is defaulted to main source set`() {
        val result = runGradleBuild(":build") { buildDir ->
            val dataFile = File(buildDir, "data.csv")
            dataFile.writeText(TestData.csvSample)

            val kotlin = File(buildDir, "src/main/kotlin").also { it.mkdirs() }
            val main = File(kotlin, "Main.kt")
            main.writeText("""
                import org.jetbrains.dataframe.DataFrame
                import org.jetbrains.dataframe.io.read
                import org.jetbrains.dataframe.typed
                import org.jetbrains.dataframe.filter
                
                fun main() {
                    val df = DataFrame.read("$dataFile").typed<Schema>()
                    val df1 = df.filter { age != null }
                }
            """.trimIndent())

            """
                import org.jetbrains.dataframe.gradle.SchemaGeneratorExtension    
                    
                plugins {
                    kotlin("jvm") version "1.4.10"
                    id("org.jetbrains.dataframe.schema-generator")
                }
                
                repositories {
                    mavenCentral() 
                }
                
                dependencies {
                    implementation("org.jetbrains.kotlinx:dataframe:0.7.3-dev-277-0.10.0.53")
                }
                
                schemaGenerator {
                    schema {
                        data = "$dataFile"
                        interfaceName = "Schema"
                        packageName = ""
                    }
                }
            """.trimIndent()
        }
        result.task(":build")?.outcome shouldBe TaskOutcome.SUCCESS
    }

    @Test
    fun `interfaceName convention is data file name`() {
        val result = runGradleBuild(":build") { buildDir ->
            val dataFile = File(buildDir, "data.csv")
            dataFile.writeText(TestData.csvSample)

            """
                import org.jetbrains.dataframe.gradle.SchemaGeneratorExtension    
                    
                plugins {
                    kotlin("jvm") version "1.4.10"
                    id("org.jetbrains.dataframe.schema-generator")
                }
                
                repositories {
                    mavenCentral() 
                }
                
                dependencies {
                    implementation("org.jetbrains.kotlinx:dataframe:0.7.3-dev-277-0.10.0.53")
                }
                
                schemaGenerator {
                    schema {
                        data = "$dataFile"
                        src = file("src/gen/kotlin")
                        packageName = ""
                    }
                }
            """.trimIndent()
        }
        result.task(":generateData")?.outcome shouldBe TaskOutcome.SUCCESS
    }

    @Test
    fun `packageName convention is default package`() {
        var dir: File? = null
        val result = runGradleBuild(":build") { buildDir ->
            val dataFile = File(buildDir, "data.csv")
            dir = buildDir
            dataFile.writeText(TestData.csvSample)

            """
                import org.jetbrains.dataframe.gradle.SchemaGeneratorExtension    
                    
                plugins {
                    kotlin("jvm") version "1.4.10"
                    id("org.jetbrains.dataframe.schema-generator")
                }
                
                repositories {
                    mavenCentral() 
                }
                
                dependencies {
                    implementation("org.jetbrains.kotlinx:dataframe:0.7.3-dev-277-0.10.0.53")
                }
                
                schemaGenerator {
                    schema {
                        data = "$dataFile"
                        src = file("src/gen/kotlin")
                        interfaceName = "Data"
                    }
                }
            """.trimIndent()
        }
        result.task(":generateData")?.outcome shouldBe TaskOutcome.SUCCESS
        dir?.let { File(it, "src/gen/kotlin/GeneratedData.kt").exists() } shouldBe true
    }

    @Test
    fun `fallback all properties to conventions`() {
        var dir: File? = null
        val result = runGradleBuild(":build") { buildDir ->
            val dataFile = File(buildDir, "data.csv")
            dir = buildDir
            dataFile.writeText(TestData.csvSample)

            """
                import org.jetbrains.dataframe.gradle.SchemaGeneratorExtension    
                    
                plugins {
                    kotlin("jvm") version "1.4.10"
                    id("org.jetbrains.dataframe.schema-generator")
                }
                
                repositories {
                    mavenCentral() 
                }
                
                dependencies {
                    implementation("org.jetbrains.kotlinx:dataframe:0.7.3-dev-277-0.10.0.53")
                }
                
                schemaGenerator {
                    schema {
                        data = "$dataFile"
                    }
                }
            """.trimIndent()
        }
        result.task(":generateData")?.outcome shouldBe TaskOutcome.SUCCESS
        dir?.let { File(it, "src/gen/kotlin/GeneratedData.kt").exists() } shouldBe true
    }

    private fun runGradleBuild(task: String, build: (File) -> String): BuildResult {
        val buildDir = Files.createTempDirectory("test").toFile()
        val buildFile = File(buildDir, "build.gradle.kts")
        buildFile.writeText(build(buildDir))
        return gradleRunner(buildDir, task).build()
    }

    private fun gradleRunner(buildDir: File, task: String) = GradleRunner.create()
        .withProjectDir(buildDir)
        .withGradleVersion("7.0")
        .withPluginClasspath()
        .withArguments(task)
        .withDebug(true)
}
