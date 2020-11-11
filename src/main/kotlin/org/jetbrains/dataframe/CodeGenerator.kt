package org.jetbrains.dataframe

import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

// Public API

@Target(AnnotationTarget.CLASS)
annotation class DataFrameType(val isOpen: Boolean = true)

@Target(AnnotationTarget.PROPERTY)
annotation class ColumnType(val type: KClass<out DataCol>)

@Target(AnnotationTarget.PROPERTY)
annotation class ColumnName(val name: String)

enum class CodeGenerationMode {
    FullNames,
    ShortNames
}

data class DataFrameToListNamedStub(val df: TypedDataFrame<*>, val className: String)

data class DataFrameToListTypedStub(val df: TypedDataFrame<*>, val interfaceClass: KClass<*>)

fun <T> TypedDataFrame<T>.getScheme(name: String? = null, columnSelector: ColumnsSelector<T,*>? = null): String {
    val interfaceName = name ?: "DataRecord"
    val cols = columnSelector?.let { getColumns(it).map { this[it.name] } } ?: columns
    return CodeGenerator().generateInterfaceDeclarations(cols, interfaceName, withBaseInterfaces = false, isOpen = true).joinToString("\n")
}

interface CodeGeneratorApi {
    fun generate(df: TypedDataFrame<*>, property: KProperty<*>): List<String>
    fun generate(stub: DataFrameToListNamedStub): List<String>
    fun generate(stub: DataFrameToListTypedStub): List<String>

    fun generate(marker: KClass<*>): List<String>

    var mode: CodeGenerationMode

}

// Implementation

class CodeGenerator : CodeGeneratorApi {

    companion object {
        val Default: CodeGeneratorApi = CodeGenerator()

        private val GroupedColumnType: KClass<*> = GroupedColumnBase::class

        private val GroupedFieldType: KClass<*> = TypedDataFrameRow::class
    }

    // Data Frame Schema
    private data class FieldInfo(val fieldName: String, val columnName: String, private val type: KType, val childScheme: Scheme?) {

        val isGroup: Boolean get() = childScheme != null

        fun isSubFieldOf(other: FieldInfo) =
                columnName == other.columnName && isGroup == other.isGroup && type.isSubtypeOf(other.type)

        val columnType: KType get() = if(isGroup) GroupedColumnType.createType(listOf(KTypeProjection(KVariance.INVARIANT, type)))
            else ColumnData::class.createType(listOf(KTypeProjection(KVariance.INVARIANT, type)))

        val fieldType: KType get() = if(isGroup) GroupedFieldType.createType(listOf(KTypeProjection(KVariance.INVARIANT, type)))
            else type
    }

    private class Scheme(val values: List<FieldInfo>) {

        val byColumn: Map<String, FieldInfo> = values.associateBy { it.columnName }

        val byField: Map<String, FieldInfo> = values.associateBy { it.fieldName }

        fun contains(field: FieldInfo) = byField[field.fieldName]?.equals(field) ?: false

        fun isSuperTo(other: Scheme) =
                values.all {
                    other.byColumn[it.columnName]?.isSubFieldOf(it) ?: false
                }

        override fun equals(other: Any?): Boolean {
            if(this === other) return true
            val scheme = other as? Scheme ?: return false
            if (scheme.values.size != values.size) return false
            return values.all {
                val otherEntry = other.byColumn[it.columnName] ?: return false
                otherEntry.equals(it)
            }
        }

        override fun hashCode(): Int {
            return values.sortedBy { it.fieldName }.hashCode()
        }

    }

    private fun getFields(marker: KClass<*>, withBaseTypes: Boolean): Map<String, FieldInfo> {
        val result = mutableMapOf<String, FieldInfo>()
        if (withBaseTypes)
            marker.superclasses.forEach { result.putAll(getFields(it, withBaseTypes)) }

        result.putAll(marker.declaredMemberProperties.mapIndexed { index, it ->
            val fieldName = it.name
            val columnName = it.findAnnotation<ColumnName>()?.name ?: fieldName
            var valueType = it.returnType
            val valueClass = valueType.jvmErasure
            var marker: GeneratedMarker? = null
            if (valueClass == GroupedFieldType) {
                val typeArgument = valueType.arguments[0].type!!
                if (isMarkerType(typeArgument.jvmErasure)) {
                    marker = getMarkerScheme(typeArgument.jvmErasure)
                    valueType = typeArgument
                }
            }
            fieldName to FieldInfo(fieldName, columnName, valueType, marker?.fullScheme)
        })
        return result
    }

    private fun getScheme(marker: KClass<*>, withBaseTypes: Boolean) = Scheme(getFields(marker, withBaseTypes).values.toList())

    private val charsToQuote = """[ {}()<>'"/|.\\!?@:;%^&*#$-]""".toRegex()

    private fun generateValidFieldName(name: String, index: Int, usedNames: Collection<String>): String {
        var result = name
        val needsQuote = name.contains(charsToQuote)
        if (needsQuote) {
            result = name.replace("<", "{")
                    .replace(">", "}")
                    .replace("::", " - ")
                    .replace(": ", " - ")
                    .replace(":", " - ")
                    .replace(".", " ")
                    .replace("/", "-")
                    .let { "`$it`" }
        }
        if (result.isEmpty()) result = "_$index"
        val baseName = result
        result = if (needsQuote) "`$baseName`" else baseName
        var attempt = 2
        while (usedNames.contains(result)) {
            result = if (needsQuote) "`$baseName ($attempt)`" else "${baseName}_$attempt"
            attempt++
        }
        return result
    }

    private fun getScheme(columns: Iterable<DataCol>): Scheme {
        val generatedFieldNames = mutableSetOf<String>()
        return Scheme(columns.mapIndexed { index, it ->
            val fieldName = generateValidFieldName(it.name, index, generatedFieldNames)
            generatedFieldNames.add(fieldName)
            var type = it.type
            var childScheme : Scheme? = null
            if(it is GroupedColumn<*>){
                childScheme = it.df.scheme
                type = it.dfType
            }
            FieldInfo(fieldName, it.name, type, childScheme)
        })
    }

    private val TypedDataFrame<*>.scheme: Scheme
        get() = getScheme(columns)

    // Rendering

    override var mode = CodeGenerationMode.ShortNames

    private fun shortTypeName(type: KType) =
            if (type.arguments.isNotEmpty()) null
            else type.jvmErasure.simpleName + if (type.isMarkedNullable) "?" else ""

    private fun render(clazz: KClass<*>) = when (mode) {
        CodeGenerationMode.ShortNames -> clazz.simpleName
        CodeGenerationMode.FullNames -> clazz.qualifiedName
    }

    private fun render(type: KType) = when (mode) {
        CodeGenerationMode.FullNames -> type.toString()
        CodeGenerationMode.ShortNames -> shortTypeName(type) ?: type.toString()
    }

    fun renderColumnName(name: String) = name
            .replace("\\", "\\\\")
            .replace("$", "\\\$")
            .replace("\"", "\\\"")

    // Generated marker interfaces tracking

    private data class GeneratedMarker(val fullScheme: Scheme, val ownScheme: Scheme, val kclass: KClass<*>, val isOpen: Boolean)

    private val registeredMarkers = mutableMapOf<KClass<*>,GeneratedMarker>()

    private val registeredMarkerClassNames = mutableSetOf<String>()

    private fun Scheme.getAllBaseMarkers() = registeredMarkers.values
            .filter { it.fullScheme.isSuperTo(this) }

    private fun List<GeneratedMarker>.onlyLeafs(): List<GeneratedMarker> {
        val skip = flatMap { it.kclass.allSuperclasses }.toSet()
        return filter { !skip.contains(it.kclass) }
    }

    private fun Scheme.getRequiredBaseMarkers() = registeredMarkers.values
            .filter { it.isOpen && it.fullScheme.isSuperTo(this) }

    // Code Generation

    private fun generateExtensionProperties(scheme: Scheme, markerType: String): List<String> {

        fun generatePropertyCode(typeName: String, name: String, propertyType: String, getter: String): String {
            return "val $typeName.$name: $propertyType get() = $getter as $propertyType"
        }

        val declarations = mutableListOf<String>()
        val dfTypename = render(DataFrameBase::class) + "<$markerType>"
        val rowTypename = render(DataFrameRowBase::class) + "<$markerType>"
        scheme.values.sortedBy { it.columnName }.forEach { field ->
            val getter = "this[\"${field.columnName}\"]"
            val name = field.fieldName
            val fieldType = render(field.fieldType)
            val columnType = render(field.columnType)
            declarations.add(generatePropertyCode(dfTypename, name, columnType, getter))
            declarations.add(generatePropertyCode(rowTypename, name, fieldType, getter))
        }
        return declarations
    }

    private fun isMarkerType(marker: KClass<*>) = marker.hasAnnotation<DataFrameType>()

    private fun getMarkerScheme(marker: KClass<*>) =
        registeredMarkers.getOrPut(marker) {
            val annotation = marker.findAnnotation<DataFrameType>() ?: throw Exception()
            val fullSet = getScheme(marker, withBaseTypes = true)
            val ownProperties = marker.declaredMemberProperties.map { it.name }.toSet()
            val ownSet = Scheme(fullSet.values.filter { ownProperties.contains(it.fieldName) })
            val simpleName = marker.simpleName!!
            registeredMarkerClassNames.add(simpleName)
            GeneratedMarker(fullSet, ownSet, marker, annotation.isOpen)
        }

    override fun generate(marker: KClass<*>): List<String> {
        val generatedMarker = getMarkerScheme(marker)
        val qualifiedName = marker.qualifiedName!!
        return generateExtensionProperties(generatedMarker.ownScheme, qualifiedName)
    }

    private val processedProperties = mutableSetOf<KProperty<*>>()

    private fun generateUniqueMarkerClassName(usedNames: Set<String> = setOf()): String {
        var id = 1
        val prefix = "DataFrameType"
        while (registeredMarkerClassNames.contains("$prefix$id") || usedNames.contains("$prefix$id"))
            id++
        return "$prefix$id"
    }

    private fun KClass<*>.implements(targetBaseMarkers: Iterable<KClass<*>>): Boolean {
        val superclasses = allSuperclasses + this
        return targetBaseMarkers.all { superclasses.contains(it) }
    }

    override fun generate(df: TypedDataFrame<*>, property: KProperty<*>): List<String> {

        var targetScheme = df.scheme
        val wasProcessedBefore = property in processedProperties
        processedProperties.add(property)
        val isMutable = property is KMutableProperty

        // maybe property is already properly typed, let's do some checks
        val currentMarkerType = getMarker(property.returnType)
        if (currentMarkerType != null) {
            // if property is mutable, we need to make sure that its marker type is open in order to let data frames with more columns be assignable to it
            if (!isMutable || currentMarkerType.findAnnotation<DataFrameType>()?.isOpen == true) {
                val markerScheme = getScheme(currentMarkerType, withBaseTypes = true)
                // for mutable properties we do strong typing only at the first processing, after that we allow its type to be more general than actual data frame type
                if (wasProcessedBefore || markerScheme == targetScheme) {
                    // property scheme is valid for current data frame, but we should also check that all compatible open markers are implemented by it
                    val requiredBaseMarkers = markerScheme.getRequiredBaseMarkers().map { it.kclass }
                    if (currentMarkerType.implements(requiredBaseMarkers))
                        return emptyList()
                    // use current marker scheme as a target for generation of new marker interface, so that available properties won't change
                    targetScheme = markerScheme
                }
            }
        }

        // property needs to be recreated. First, try to find existing marker for it
        val declarations = mutableListOf<String>()
        val markerType = findOrCreateMarker(targetScheme, isMutable, mutableSetOf(), declarations)

        val converter = "\$it.retype<$markerType>()"
        declarations.add(converter)
        return declarations
    }

    private fun findOrCreateMarker(targetScheme: Scheme, isMutable: Boolean, usedNames: MutableSet<String>, declarations: MutableList<String>): String {
        val markerType: String
        val requiredBaseMarkers = targetScheme.getRequiredBaseMarkers().map { it.kclass }
        val existingMarker = registeredMarkers.values.firstOrNull {
            isMutable == it.isOpen && it.fullScheme.equals(targetScheme) && it.kclass.implements(requiredBaseMarkers)
        }
        if (existingMarker != null) {
            markerType = existingMarker.kclass.qualifiedName!!
        } else {
            markerType = generateUniqueMarkerClassName(usedNames)
            usedNames.add(markerType)
            declarations.addAll(generateInterfaceDeclarations(targetScheme, markerType, usedNames, withBaseInterfaces = true, isOpen = isMutable))
        }
        return markerType
    }

    private fun getMarker(dataFrameType: KType) =
            when (dataFrameType.jvmErasure) {
                TypedDataFrame::class -> dataFrameType.arguments[0].type?.jvmErasure
                else -> null
            }

    private enum class FieldGenerationMode { declare, override, skip }

    private fun computeFieldGenerationModes(scheme: Scheme, requiredBaseMarkers: List<GeneratedMarker>): List<Pair<FieldInfo, FieldGenerationMode>> {
        return scheme.values.map { field ->
            val fieldName = field.fieldName
            var generationMode = FieldGenerationMode.declare
            for (baseScheme in requiredBaseMarkers) {
                val baseField = baseScheme.fullScheme.byField[fieldName]
                if (baseField != null) {
                    generationMode = if (baseField.fieldType == field.fieldType || (baseField.isGroup && field.isGroup && baseField.childScheme!!.equals(field.childScheme!!))) FieldGenerationMode.skip
                    else if (field.fieldType.isSubtypeOf(baseField.fieldType) || (baseField.isGroup && field.isGroup && baseField.childScheme!!.isSuperTo(field.childScheme!!))) {
                        generationMode = FieldGenerationMode.override
                        break
                    } else throw Exception()
                }
            }
            field to generationMode
        }
    }

    private fun generateInterfaceDeclarations(scheme: Scheme, name: String, usedNames: MutableSet<String>, withBaseInterfaces: Boolean, isOpen: Boolean): List<String> {

        val markers = mutableListOf<GeneratedMarker>()
        val fields = if (withBaseInterfaces) {
            markers += scheme.getRequiredBaseMarkers().onlyLeafs()
            val generatedFields = computeFieldGenerationModes(scheme, markers)

            // try to reduce number of generated fields by implementing some additional interfaces
            val remainedFields = generatedFields.filter { it.second == FieldGenerationMode.declare }.map { it.first }.toMutableList()
            var markersAdded = false

            if (remainedFields.size > 0) {
                val availableMarkers = scheme.getAllBaseMarkers().toMutableList()
                availableMarkers -= markers

                while (remainedFields.size > 0) {
                    val bestMarker = availableMarkers.map { marker -> marker to remainedFields.count { marker.fullScheme.contains(it) } }.maxBy { it.second }
                    if (bestMarker != null && bestMarker.second > 0) {
                        remainedFields.removeAll { bestMarker.first.fullScheme.byField[it.fieldName]?.fieldType == it.fieldType }
                        markers += bestMarker.first
                        markersAdded = true
                        availableMarkers -= bestMarker.first
                    } else break
                }
            }
            if (markersAdded) computeFieldGenerationModes(scheme, markers) else generatedFields
        } else scheme.values.map { it to FieldGenerationMode.declare }

        val leafMarkers = markers.onlyLeafs()
        val header = "@DataFrameType${if(isOpen) "" else "(isOpen = false)"}\ninterface $name"
        val baseInterfacesDeclaration = if (leafMarkers.isNotEmpty()) " : " + leafMarkers.map { it.kclass.qualifiedName!! }.joinToString() else ""
        val resultDeclarations = mutableListOf<String>()

        val fieldsDeclaration = fields.filter { it.second != FieldGenerationMode.skip }.map {
            val field = it.first
            val override = when (it.second) {
                FieldGenerationMode.declare -> ""
                FieldGenerationMode.override -> "override "
                FieldGenerationMode.skip -> throw Exception()
            }
            val columnNameAnnotation = if (field.columnName != field.fieldName) "\t@ColumnName(\"${renderColumnName(field.columnName)}\")\n" else ""

            val fieldType = when {
                field.isGroup -> {
                    val markerType = findOrCreateMarker(field.childScheme!!, false, usedNames, resultDeclarations)
                    val rowTypeName = render(GroupedFieldType)
                    "$rowTypeName<$markerType>"
                }
                else -> render(field.fieldType)
            }

            "${columnNameAnnotation}    ${override}val ${field.fieldName}: $fieldType"
        }.joinToString("\n")
        val body = if (fieldsDeclaration.isNotBlank()) "{\n$fieldsDeclaration\n}" else ""
        resultDeclarations.add(header + baseInterfacesDeclaration + body)
        return resultDeclarations
    }

    internal fun generateInterfaceDeclarations(columns: Iterable<DataCol>, name: String, withBaseInterfaces: Boolean, isOpen: Boolean) = generateInterfaceDeclarations(getScheme(columns), name, mutableSetOf(), withBaseInterfaces, isOpen)

    // DataFrame -> List converters

    private fun generateToListConverter(className: String, columnNames: List<String>, scheme: Scheme, interfaceName: String? = null): List<String> {
        val override = if (interfaceName != null) "override " else ""
        val baseTypes = if (interfaceName != null) " : $interfaceName" else ""
        val classDeclaration = "data class ${className}(" +
                columnNames.map {
                    val field = scheme.byColumn[it]!!
                    "${override}val ${field.fieldName}: ${render(field.fieldType)}"
                }.joinToString() + ") " + baseTypes

        val converter = "\$it.df.rows.map { $className(" +
                columnNames.map {
                    val field = scheme.byColumn[it]!!
                    "it[\"${field.columnName}\"] as ${render(field.fieldType)}"
                }.joinToString() + ")}"

        return listOf(classDeclaration, converter)
    }

    override fun generate(stub: DataFrameToListTypedStub): List<String> {
        val df = stub.df
        val dfScheme = df.scheme
        val interfaceScheme = getScheme(stub.interfaceClass, withBaseTypes = true)
        if (!interfaceScheme.isSuperTo(dfScheme))
            throw Exception()
        val interfaceName = stub.interfaceClass.simpleName!!
        val interfaceFullName = stub.interfaceClass.qualifiedName!!
        val className = interfaceName + "Impl"
        val columnNames = interfaceScheme.byColumn.keys.toList()

        return generateToListConverter(className, columnNames, dfScheme, interfaceFullName)
    }

    override fun generate(stub: DataFrameToListNamedStub) =
            generateToListConverter(stub.className, stub.df.columns.map { it.name }, stub.df.scheme, null)

}
