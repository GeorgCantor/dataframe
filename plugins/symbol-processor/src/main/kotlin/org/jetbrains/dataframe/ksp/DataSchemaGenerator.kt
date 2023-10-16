package org.jetbrains.dataframe.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFile
import org.jetbrains.dataframe.impl.codeGen.CodeGenerator
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.annotations.CsvOptions
import org.jetbrains.kotlinx.dataframe.annotations.DataSchemaVisibility
import org.jetbrains.kotlinx.dataframe.annotations.ImportDataSchema
import org.jetbrains.kotlinx.dataframe.annotations.JdbcOptions
import org.jetbrains.kotlinx.dataframe.annotations.JsonOptions
import org.jetbrains.kotlinx.dataframe.api.JsonPath
import org.jetbrains.kotlinx.dataframe.codeGen.MarkerVisibility
import org.jetbrains.kotlinx.dataframe.codeGen.NameNormalizer
import org.jetbrains.kotlinx.dataframe.impl.codeGen.CodeGenerationReadResult
import org.jetbrains.kotlinx.dataframe.impl.codeGen.DfReadResult
import org.jetbrains.kotlinx.dataframe.impl.codeGen.from
import org.jetbrains.kotlinx.dataframe.impl.codeGen.toStandaloneSnippet
import org.jetbrains.kotlinx.dataframe.impl.codeGen.urlCodeGenReader
import org.jetbrains.kotlinx.dataframe.impl.codeGen.urlDfReader
import org.jetbrains.kotlinx.dataframe.io.*
import java.io.File
import java.lang.RuntimeException
import java.net.MalformedURLException
import java.net.URL
import java.sql.Connection
import java.sql.DriverManager
import org.jetbrains.kotlinx.dataframe.io.db.H2
import org.jetbrains.kotlinx.dataframe.io.db.MariaDb
import org.jetbrains.kotlinx.dataframe.io.db.MySql
import org.jetbrains.kotlinx.dataframe.io.db.PostgreSql
import org.jetbrains.kotlinx.dataframe.io.db.Sqlite
import org.jetbrains.kotlinx.dataframe.io.db.driverClassNameFromUrl
import org.jetbrains.kotlinx.dataframe.schema.DataFrameSchema

@OptIn(KspExperimental::class)
class DataSchemaGenerator(
    private val resolver: Resolver,
    private val resolutionDir: String?,
    private val logger: KSPLogger,
    private val codeGenerator: com.google.devtools.ksp.processing.CodeGenerator,
) {

    fun resolveImportStatements() = listOf(
        ::resolvePathImports,
    ).flatMap { it(resolver) }

    class ImportDataSchemaStatement(
        val origin: KSFile,
        val name: String,
        val dataSource: CodeGeneratorDataSource,
        val visibility: MarkerVisibility,
        val normalizationDelimiters: List<Char>,
        val withDefaultPath: Boolean,
        val csvOptions: CsvOptions,
        val jsonOptions: JsonOptions,
        val jdbcOptions: JdbcOptions,
        val isJdbc: Boolean = false,
    )

    class CodeGeneratorDataSource(val pathRepresentation: String, val data: URL)

    private fun resolvePathImports(resolver: Resolver) = resolver
        .getSymbolsWithAnnotation(ImportDataSchema::class.qualifiedName!!)
        .filterIsInstance<KSFile>()
        .flatMap { file ->
            file.getAnnotationsByType(ImportDataSchema::class).mapNotNull { it.toStatement(file, logger) }
        }

    private fun ImportDataSchema.toStatement(file: KSFile, logger: KSPLogger): ImportDataSchemaStatement? {
        val url = if (isURL(path)) {
            try {
                URL(this.path)
            } catch (exception: MalformedURLException) {
                logger.error("'${this.path}' is not valid URL: ${exception.message}", file)
                return null
            }
        } else {
            // revisit architecture for an addition of the new data source https://github.com/Kotlin/dataframe/issues/450
            if(path.startsWith("jdbc")) {
                return ImportDataSchemaStatement(
                    origin = file,
                    name = name,
                    // URL better to make nullable or make hierarchy here
                    dataSource = CodeGeneratorDataSource(this.path, URL("http://example.com/pages/")),
                    visibility = visibility.toMarkerVisibility(),
                    normalizationDelimiters = normalizationDelimiters.toList(),
                    withDefaultPath = withDefaultPath,
                    csvOptions = csvOptions,
                    jsonOptions = jsonOptions,
                    jdbcOptions = jdbcOptions,
                    isJdbc = true
                )
            }

            val resolutionDir: String = resolutionDir ?: run {
                reportMissingKspArgument(file)
                return null
            }

            val relativeFile = File(resolutionDir, path)
            val absoluteFile = File(path)
            val data = if (relativeFile.exists()) relativeFile else absoluteFile
            try {
                data.toURI().toURL() ?: return null
            } catch (exception: MalformedURLException) {
                logger.error(
                    "Failed to convert resolved path '${relativeFile.absolutePath}' or '${absoluteFile.absolutePath}' to URL: ${exception.message}",
                    file
                )
                return null
            }
        }

        return ImportDataSchemaStatement(
            origin = file,
            name = name,
            dataSource = CodeGeneratorDataSource(this.path, url),
            visibility = visibility.toMarkerVisibility(),
            normalizationDelimiters = normalizationDelimiters.toList(),
            withDefaultPath = withDefaultPath,
            csvOptions = csvOptions,
            jsonOptions = jsonOptions,
            jdbcOptions = jdbcOptions,
        )
    }

    private fun DataSchemaVisibility.toMarkerVisibility(): MarkerVisibility = when (this) {
        DataSchemaVisibility.INTERNAL -> MarkerVisibility.INTERNAL
        DataSchemaVisibility.IMPLICIT_PUBLIC -> MarkerVisibility.IMPLICIT_PUBLIC
        DataSchemaVisibility.EXPLICIT_PUBLIC -> MarkerVisibility.EXPLICIT_PUBLIC
    }

    private fun reportMissingKspArgument(file: KSFile) {
        logger.error(
            """
            |KSP option with key "dataframe.resolutionDir" must be set in order to use relative path in @${ImportDataSchema::class.simpleName}
            |DataFrame Gradle plugin should set it by default to "project.projectDir".
            |If you do not use DataFrame Gradle plugin, configure option manually 
            """.trimMargin(),
            symbol = file
        )
    }

    fun generateDataSchema(importStatement: ImportDataSchemaStatement) {
        val packageName = importStatement.origin.packageName.asString()
        val name = importStatement.name
        val schemaFile =
            codeGenerator.createNewFile(Dependencies(true, importStatement.origin), packageName, "$name.Generated")

        val formats = listOf(
            CSV(delimiter = importStatement.csvOptions.delimiter),
            JSON(
                typeClashTactic = importStatement.jsonOptions.typeClashTactic,
                keyValuePaths = importStatement.jsonOptions.keyValuePaths.map(::JsonPath),
            ),
            Excel(),
            TSV(),
            ArrowFeather(),
            OpenApi(),
        )

        // revisit architecture for an addition of the new data source https://github.com/Kotlin/dataframe/issues/450
        if (importStatement.isJdbc) {
            val url = importStatement.dataSource.pathRepresentation

            // Force classloading
            Class.forName(driverClassNameFromUrl(url))

            val connection = DriverManager.getConnection(
                url,
                importStatement.jdbcOptions.user,
                importStatement.jdbcOptions.password
            )

            connection.use {
                val schema = generateSchemaForImport(importStatement, connection)

                val codeGenerator = CodeGenerator.create(useFqNames = false)

                val additionalImports: List<String> = listOf()

                val codeGenResult = codeGenerator.generate(
                    schema = schema,
                    name = name,
                    fields = true,
                    extensionProperties = false,
                    isOpen = true,
                    visibility = importStatement.visibility,
                    knownMarkers = emptyList(),
                    readDfMethod = null,
                    fieldNameNormalizer = NameNormalizer.from(importStatement.normalizationDelimiters.toSet())
                )
                val code = codeGenResult.toStandaloneSnippet(packageName, additionalImports)
                schemaFile.bufferedWriter().use {
                    it.write(code)
                }
                return
            }
        }

        // revisit architecture for an addition of the new data source https://github.com/Kotlin/dataframe/issues/450
        // works for JDBC and OpenAPI only
        // first try without creating a dataframe
        when (val codeGenResult = if (importStatement.isJdbc) {
            CodeGenerator.databaseCodeGenReader(importStatement.dataSource.data, name)
        } else {
            CodeGenerator.urlCodeGenReader(importStatement.dataSource.data, name, formats, false)
        }) {
            is CodeGenerationReadResult.Success -> {
                val readDfMethod = codeGenResult.getReadDfMethod(
                    pathRepresentation = importStatement
                        .dataSource
                        .pathRepresentation
                        .takeIf { importStatement.withDefaultPath },
                )

                val code = codeGenResult
                    .code
                    .toStandaloneSnippet(packageName, readDfMethod.additionalImports)

                schemaFile.bufferedWriter().use {
                    it.write(code)
                }
                return
            }

            is CodeGenerationReadResult.Error -> {
//                logger.warn("Error while reading types-only from data at ${importStatement.dataSource.pathRepresentation}: ${codeGenResult.reason}")
            }
        }

        // Usually works for others
        // on error, try with reading dataframe first
        val parsedDf = when (val readResult = CodeGenerator.urlDfReader(importStatement.dataSource.data, formats)) {
            is DfReadResult.Error -> {
                logger.error("Error while reading dataframe from data at ${importStatement.dataSource.pathRepresentation}: ${readResult.reason}")
                return
            }

            is DfReadResult.Success -> readResult
        }

        val readDfMethod =
            parsedDf.getReadDfMethod(importStatement.dataSource.pathRepresentation.takeIf { importStatement.withDefaultPath })
        val codeGenerator = CodeGenerator.create(useFqNames = false)

        val codeGenResult = codeGenerator.generate(
            schema = parsedDf.schema,
            name = name,
            fields = true,
            extensionProperties = false,
            isOpen = true,
            visibility = importStatement.visibility,
            knownMarkers = emptyList(),
            readDfMethod = readDfMethod,
            fieldNameNormalizer = NameNormalizer.from(importStatement.normalizationDelimiters.toSet())
        )
        val code = codeGenResult.toStandaloneSnippet(packageName, readDfMethod.additionalImports)
        schemaFile.bufferedWriter().use {
            it.write(code)
        }
    }

    private fun generateSchemaForImport(
        importStatement: ImportDataSchemaStatement,
        connection: Connection
    ): DataFrameSchema {
        logger.info("Table name: ${importStatement.jdbcOptions.tableName}")
        logger.info("SQL query: ${importStatement.jdbcOptions.sqlQuery}")

        return if (importStatement.jdbcOptions.tableName.isNotBlank())
            DataFrame.getSchemaForSqlTable(connection, importStatement.jdbcOptions.tableName)
        else if(importStatement.jdbcOptions.sqlQuery.isNotBlank())
            DataFrame.getSchemaForSqlQuery(connection, importStatement.jdbcOptions.sqlQuery)
        else throw RuntimeException("Table name: ${importStatement.jdbcOptions.tableName}, " +
            "SQL query: ${importStatement.jdbcOptions.sqlQuery} both are empty! " +
            "Populate 'tableName' or 'sqlQuery' in jdbcOptions with value to generate schema " +
            "for SQL table or result of SQL query!")
    }
}

