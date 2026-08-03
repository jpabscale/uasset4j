// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/JSON/*.cs (StringEnumConverter parity on [Flags] value classes)
package com.github.jpabscale.uasset4j.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier
import com.fasterxml.jackson.databind.deser.SettableBeanProperty
import com.github.jpabscale.uasset4j.propertytypes.objects.TextPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.LevelSequenceObjectReferenceMapPropertyData

/**
 * jackson-module-kotlin unwraps `@JvmInline value class` flags to their underlying primitive during
 * deserialization type refinement, which bypasses type-registered deserializers. This modifier
 * re-attaches the StringEnumConverter-style deserializers to the exact bean properties that hold
 * them, where property-level deserializers take precedence over type resolution. The property's
 * JVM type is the inlined primitive (byte/int/long), so the attached deserializers return the
 * primitive the Kotlin setter accepts.
 */
class UAssetBeanDeserializerModifier : BeanDeserializerModifier() {
    override fun updateBuilder(
        config: DeserializationConfig,
        beanDesc: BeanDescription,
        builder: BeanDeserializerBuilder,
    ): BeanDeserializerBuilder {
        val props = builder.getProperties().asSequence().toList()
        for (prop in props) {
            val deser = deserializerFor(beanDesc.beanClass, prop.name)
            if (deser != null) {
                builder.addOrReplaceProperty(prop.withValueDeserializer(deser), true)
            }
        }
        return builder
    }

    private fun deserializerFor(beanClass: Class<*>, propName: String): JsonDeserializer<Any>? = when (propName) {
        "PropertyTagFlags" -> PrimitiveFlagsDeserializer { s -> FlagsConverters.parsePropertyTagFlags(s).value }
        "PropertyTagExtensions" -> PrimitiveFlagsDeserializer { s -> FlagsConverters.parsePropertyTagExtension(s).value }
        "SerializationControl" -> PrimitiveFlagsDeserializer { s -> FlagsConverters.parseClassSerializationControlExtension(s).value }
        "ObjectFlags" -> PrimitiveFlagsDeserializer { s -> FlagsConverters.parseObjectFlags(s).value }
        "PackageFlags" -> PrimitiveFlagsDeserializer { s -> FlagsConverters.parsePackageFlags(s).value }
        "ClassFlags" -> PrimitiveFlagsDeserializer { s -> FlagsConverters.parseClassFlags(s).value }
        "PropertyFlags" -> PrimitiveFlagsDeserializer { s -> FlagsConverters.parsePropertyFlags(s).value }
        "FunctionFlags" -> PrimitiveFlagsDeserializer { s -> FlagsConverters.parseFunctionFlags(s).value }
        "Flags" -> when {
            beanClass == TextPropertyData::class.java ->
                PrimitiveFlagsDeserializer { s -> FlagsConverters.parseTextFlag(s).value }
            beanClass == com.github.jpabscale.uasset4j.unrealtypes.FObjectDataResource::class.java ->
                PrimitiveFlagsDeserializer { s -> FlagsConverters.parseObjectDataResourceFlags(s).value }
            com.github.jpabscale.uasset4j.fieldtypes.FField::class.java.isAssignableFrom(beanClass) ->
                PrimitiveFlagsDeserializer { s -> FlagsConverters.parseObjectFlags(s).value }
            else -> null
        }
        "Value" -> when (beanClass) {
            LevelSequenceObjectReferenceMapPropertyData::class.java ->
                LevelSequenceObjectReferenceMapJsonDeserializer()
            else -> null
        }
        else -> null
    }
}

private class PrimitiveFlagsDeserializer(
    private val parse: (String) -> Any,
) : JsonDeserializer<Any>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Any =
        parse(p.valueAsString ?: "")
}
