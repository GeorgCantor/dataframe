package org.jetbrains.kotlin.fir.dataframe.api

import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.fir.dataframe.Names
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.utils.effectiveVisibility
import org.jetbrains.kotlin.fir.declarations.utils.isEnumClass
import org.jetbrains.kotlin.fir.declarations.utils.isStatic
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.java.JavaTypeParameterStack
import org.jetbrains.kotlin.fir.java.declarations.FirJavaClass
import org.jetbrains.kotlin.fir.java.resolveIfJavaType
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.scopes.collectAllFunctions
import org.jetbrains.kotlin.fir.scopes.collectAllProperties
import org.jetbrains.kotlin.fir.scopes.unsubstitutedScope
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.ConeClassLikeLookupTagImpl
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.ConeStarProjection
import org.jetbrains.kotlin.fir.types.ConeTypeParameterType
import org.jetbrains.kotlin.fir.types.canBeNull
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.fir.types.isArrayTypeOrNullableArrayType
import org.jetbrains.kotlin.fir.types.isNullable
import org.jetbrains.kotlin.fir.types.isStarProjection
import org.jetbrains.kotlin.fir.types.isSubtypeOf
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.fir.types.toRegularClassSymbol
import org.jetbrains.kotlin.fir.types.type
import org.jetbrains.kotlin.fir.types.upperBoundIfFlexible
import org.jetbrains.kotlin.fir.types.withArguments
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlinx.dataframe.KotlinTypeFacade
import org.jetbrains.kotlinx.dataframe.annotations.TypeApproximation
import org.jetbrains.kotlinx.dataframe.plugin.PluginDataFrameSchema
import org.jetbrains.kotlinx.dataframe.plugin.SimpleCol
import org.jetbrains.kotlinx.dataframe.plugin.SimpleColumnGroup
import org.jetbrains.kotlinx.dataframe.plugin.SimpleFrameColumn
import java.util.Locale

@OptIn(SymbolInternals::class)
internal fun KotlinTypeFacade.toDataFrame(
    maxDepth: Int,
    call: FirFunctionCall
): PluginDataFrameSchema {
    fun ConeKotlinType.isValueType() =
        this.isArrayTypeOrNullableArrayType ||
            this.classId == StandardClassIds.Any ||
            this.classId == StandardClassIds.String ||
            this.classId == StandardClassIds.Boolean ||
            classId in setOf(Names.DURATION_CLASS_ID, Names. LOCAL_DATE_CLASS_ID, Names.LOCAL_DATE_TIME_CLASS_ID, Names.INSTANT_CLASS_ID) ||
            this.isSubtypeOf(session.builtinTypes.numberType.type, session) ||
            this.isSubtypeOf(StandardClassIds.Number.constructClassLikeType(emptyArray(), isNullable = true), session) ||
            this.toRegularClassSymbol(session)?.isEnumClass ?: false ||
            this.isSubtypeOf(
                ConeClassLikeTypeImpl(
                    ConeClassLikeLookupTagImpl(
                        ClassId(FqName("java.time.temporal"), Name.identifier("Temporal"))
                    ), arrayOf(), isNullable = false
                ), session
            )

    fun convert(classLike: ConeKotlinType, depth: Int): List<SimpleCol> {
        val symbol = classLike.toRegularClassSymbol(session) ?: return emptyList()
        val scope = symbol.unsubstitutedScope(session, ScopeSession(), false, FirResolvePhase.STATUS)
        val declarations = if (symbol.fir is FirJavaClass) {
            scope
                .collectAllFunctions()
                .filter { !it.isStatic && it.valueParameterSymbols.isEmpty() && it.typeParameterSymbols.isEmpty() }
                .mapNotNull { function ->
                    val name = function.name.identifier
                    if (name.startsWith("get") || name.startsWith("is")) {
                        val propertyName = name
                            .replaceFirst("get", "")
                            .replaceFirst("is", "")
                            .let {
                                if (it.firstOrNull()?.isUpperCase() == true) {
                                    it.replaceFirstChar { it.lowercase(Locale.getDefault()) }
                                } else {
                                    null
                                }
                            }
                        propertyName?.let { function to it }
                    } else {
                        null
                    }
                }
        } else {
            scope
                .collectAllProperties()
                .filterIsInstance<FirPropertySymbol>()
                .map {
                    it to it.name.identifier
                }
        }

        return declarations
            .filter { it.first.effectiveVisibility == EffectiveVisibility.Public }
            .map { (it, name) ->
                var resolvedReturnType = it.fir.returnTypeRef.resolveIfJavaType(session, JavaTypeParameterStack.EMPTY, null)
                    .coneType.upperBoundIfFlexible()

                resolvedReturnType = if (resolvedReturnType is ConeTypeParameterType) {
                    if (resolvedReturnType.canBeNull(session)) {
                        session.builtinTypes.nullableAnyType.type
                    } else {
                        session.builtinTypes.anyType.type
                    }
                } else {
                    resolvedReturnType.withArguments {
                        val type = it.type
                        if (type is ConeTypeParameterType) {
                            session.builtinTypes.nullableAnyType.type
                        } else {
                            type?.upperBoundIfFlexible() ?: it
                        }
                    }
                }

                if (depth >= maxDepth || resolvedReturnType.isValueType()) {
                    SimpleCol(name, TypeApproximation(resolvedReturnType))
                } else if (
                    resolvedReturnType.isSubtypeOf(StandardClassIds.Iterable.constructClassLikeType(arrayOf(ConeStarProjection)), session) ||
                    resolvedReturnType.isSubtypeOf(StandardClassIds.Iterable.constructClassLikeType(arrayOf(ConeStarProjection), isNullable = true), session)
                ) {
                    val typeArgument = resolvedReturnType.typeArguments[0]
                    val type: ConeKotlinType = when (typeArgument) {
                        is ConeKotlinType -> typeArgument
                        ConeStarProjection -> session.builtinTypes.nullableAnyType.type
                        else -> session.builtinTypes.nullableAnyType.type
                    }
                    if (type.isValueType()) {
                        SimpleCol(name, TypeApproximation(StandardClassIds.List.constructClassLikeType(arrayOf(type), resolvedReturnType.isNullable)))
                    } else {
                        SimpleFrameColumn(name, convert(type, depth + 1), nullable = false, anyDataFrame)
                    }
                } else {
                    SimpleColumnGroup(name, convert(resolvedReturnType, depth + 1), anyRow)
                }
            }
    }

    val receiver = call.explicitReceiver ?: error("abv")
    val arg = receiver.resolvedType.typeArguments.firstOrNull() ?: error("abe")
    return when {
        arg.isStarProjection -> PluginDataFrameSchema(emptyList())
        else -> {
            val classLike = arg.type as ConeClassLikeType
            PluginDataFrameSchema(convert(classLike, 0))
        }
    }
}