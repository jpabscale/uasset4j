// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/JSON/UAssetContractResolver.cs (JSON property naming parity)
package com.github.jpabscale.uasset4j.json

import com.fasterxml.jackson.databind.AnnotationIntrospector
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.PropertyName
import com.fasterxml.jackson.databind.cfg.MapperConfig
import com.fasterxml.jackson.databind.introspect.Annotated
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaMethod
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * Preserves the exact C# `[JsonProperty]` member names (PascalCase, e.g. `ObjectName`, `Value`).
 *
 * Newtonsoft serializes the C# field/property name verbatim; Jackson's default bean naming
 * decapitalizes it (`ObjectName` -> `objectName`). jackson-module-kotlin cannot be relied on here
 * (the pinned jackson-module-kotlin 2.19 predates the Kotlin 2.4 metadata it is fed), so this
 * introspector re-derives the name from the class's Kotlin properties via kotlin-reflect: every
 * getter/setter method is mapped back to the Kotlin property it belongs to, and that property name
 * is emitted. `@JsonProperty(...)`-annotated names still win.
 */
class UAssetAnnotationIntrospector : JacksonAnnotationIntrospector() {
    private val methodNameCache = ConcurrentHashMap<Class<*>, Map<Method, String>>()

    private fun methodNames(clazz: Class<*>): Map<Method, String> {
        methodNameCache[clazz]?.let { return it }
        val map = HashMap<Method, String>()
        runCatching {
            for (p in clazz.kotlin.memberProperties) {
                p.getter.javaMethod?.let { map[it] = p.name }
                (p as? KMutableProperty<*>)?.setter?.javaMethod?.let { map[it] = p.name }
            }
        }
        methodNameCache[clazz] = map
        return map
    }

    private fun kotlinNameOf(a: Annotated): String? {
        val m = (a as? AnnotatedMethod)?.annotated ?: return null
        return methodNames(m.declaringClass)[m]
    }

    override fun findNameForSerialization(a: Annotated): PropertyName? {
        val explicit = super.findNameForSerialization(a)
        if (explicit != null && explicit.hasSimpleName()) return explicit
        return kotlinNameOf(a)?.let { PropertyName(it) }
    }

    override fun findNameForDeserialization(a: Annotated): PropertyName? {
        val explicit = super.findNameForDeserialization(a)
        if (explicit != null && explicit.hasSimpleName()) return explicit
        return kotlinNameOf(a)?.let { PropertyName(it) }
    }

    override fun refineSerializationType(
        config: MapperConfig<*>,
        a: Annotated,
        baseType: JavaType,
    ): JavaType = super.refineSerializationType(config, a, baseType)

    override fun refineDeserializationType(
        config: MapperConfig<*>,
        a: Annotated,
        baseType: JavaType,
    ): JavaType = super.refineDeserializationType(config, a, baseType)
}
