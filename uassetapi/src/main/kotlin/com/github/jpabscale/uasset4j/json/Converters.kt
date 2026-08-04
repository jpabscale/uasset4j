// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/JSON/*.cs
package com.github.jpabscale.uasset4j.json

import com.github.jpabscale.uasset4j.BitArray
import com.github.jpabscale.uasset4j.CustomSerializationFlags
import com.github.jpabscale.uasset4j.EPackageFlags
import com.github.jpabscale.uasset4j.UAPUtils
import com.github.jpabscale.uasset4j.exporttypes.FStringTable
import com.github.jpabscale.uasset4j.propertytypes.objects.ETextFlag
import com.github.jpabscale.uasset4j.propertytypes.objects.EPropertyTagExtension
import com.github.jpabscale.uasset4j.propertytypes.objects.EPropertyTagFlags
import com.github.jpabscale.uasset4j.exporttypes.EClassSerializationControlExtension
import com.github.jpabscale.uasset4j.propertytypes.structs.FLevelSequenceLegacyObjectReference
import com.github.jpabscale.uasset4j.unrealtypes.EObjectFlags
import com.github.jpabscale.uasset4j.unrealtypes.EClassFlags
import com.github.jpabscale.uasset4j.unrealtypes.EFunctionFlags
import com.github.jpabscale.uasset4j.unrealtypes.EObjectDataResourceFlags
import com.github.jpabscale.uasset4j.unrealtypes.EPropertyFlags
import com.github.jpabscale.uasset4j.unrealtypes.FGuid
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.FPropertyTypeName
import com.github.jpabscale.uasset4j.unrealtypes.FPropertyTypeNameNode
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.util.Base64

/**
 * The FName `$type`-style deferred-resolution mechanism: FNameJsonConverter cannot resolve a
 * string against the name map during deserialization (the owning [com.github.jpabscale.uasset4j.UAsset] is
 * not yet constructed), so it records a dummy FName + string here and the caller resolves them
 * once the asset exists (mirrors Newtonsoft's ToBeFilled dictionary in UAsset.cs).
 */
object FNameToBeFilled {
    private val state = ThreadLocal.withInitial { mutableListOf<Pair<FName, String>>() }
    private val counter = ThreadLocal.withInitial { 0 }

    fun current(): MutableList<Pair<FName, String>> = state.get()

    fun nextIndex(): Int {
        val c = counter.get()
        counter.set(c + 1)
        return c + 1
    }

    fun clear() {
        state.get().clear()
        counter.set(0)
    }
}

/** Newtonsoft FNameJsonConverter: writes the name's string form; reads as a deferred dummy. */
class FNameJsonConverter : StdSerializer<FName>(FName::class.java) {
    override fun serialize(value: FName, gen: JsonGenerator, provider: SerializerProvider) {
        val text =
            if (value.DummyValue != null) value.DummyValue.toString()
            else value.toString()
        gen.writeString(text)
    }

    companion object {
        val deserializer = object : StdDeserializer<FName>(FName::class.java) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): FName? {
                val text = p.valueAsString ?: return null
                val res = FName.DefineDummy(null, "temp", FNameToBeFilled.nextIndex())
                FNameToBeFilled.current().add(res to text)
                return res
            }
        }
    }
}

/** Newtonsoft FStringJsonConverter: writes the raw string value. */
class FStringJsonConverter : StdSerializer<FString>(FString::class.java) {
    override fun serialize(value: FString, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.Value)
    }

    companion object {
        val deserializer = object : StdDeserializer<FString>(FString::class.java) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): FString? {
                val v = p.valueAsString ?: return null
                return FString(v)
            }
        }
    }
}

/** Newtonsoft FPackageIndexJsonConverter: writes the raw index int. */
class FPackageIndexJsonConverter : StdSerializer<FPackageIndex>(FPackageIndex::class.java) {
    override fun serialize(value: FPackageIndex, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeNumber(value.Index)
    }

    companion object {
        val deserializer = object : StdDeserializer<FPackageIndex>(FPackageIndex::class.java) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): FPackageIndex =
                FPackageIndex(p.intValue)
        }
    }
}

/** Newtonsoft GuidJsonConverter: writes the uppercase braced "{XXXXXXXX-...}" form. */
class GuidJsonConverter : StdSerializer<FGuid>(FGuid::class.java) {
    override fun serialize(value: FGuid, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.toPrettyString())
    }

    companion object {
        val deserializer = object : StdDeserializer<FGuid>(FGuid::class.java) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): FGuid =
                UAPUtils.ConvertToGUID(p.valueAsString)
        }
    }
}

/** Newtonsoft ByteArrayJsonConverter: base64. */
class ByteArrayJsonConverter : StdSerializer<ByteArray>(ByteArray::class.java) {
    override fun serialize(value: ByteArray, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(Base64.getEncoder().encodeToString(value))
    }

    companion object {
        val deserializer = object : StdDeserializer<ByteArray>(ByteArray::class.java) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ByteArray? {
                val v = p.valueAsString ?: return null
                return Base64.getDecoder().decode(v)
            }
        }
    }
}

/** C# `byte` fields serialize as unsigned (0-255); Jackson writes Kotlin `Byte` signed. */
class UnsignedByteJsonConverter : StdSerializer<Byte>(Byte::class.java) {
    override fun serialize(value: Byte, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeNumber(value.toInt() and 0xFF)
    }

    companion object {
        val deserializer = object : StdDeserializer<Byte>(Byte::class.java) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Byte =
                (p.intValue and 0xFF).toByte()
        }
    }
}

/** Newtonsoft ColorConverter: `System.Drawing.Color` serializes as "R, G, B" or "R, G, B, A". */
class ColorJsonConverter : StdSerializer<com.github.jpabscale.uasset4j.propertytypes.structs.core.Color>(
    com.github.jpabscale.uasset4j.propertytypes.structs.core.Color::class.java,
) {
    override fun serialize(value: com.github.jpabscale.uasset4j.propertytypes.structs.core.Color, gen: JsonGenerator, provider: SerializerProvider) {
        if (value.A == 255) {
            gen.writeString("${value.R}, ${value.G}, ${value.B}")
        } else {
            gen.writeString("${value.R}, ${value.G}, ${value.B}, ${value.A}")
        }
    }

    companion object {
        val deserializer = object : StdDeserializer<com.github.jpabscale.uasset4j.propertytypes.structs.core.Color>(
            com.github.jpabscale.uasset4j.propertytypes.structs.core.Color::class.java,
        ) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): com.github.jpabscale.uasset4j.propertytypes.structs.core.Color {
                val parts = (p.valueAsString ?: "").split(", ").map { it.toIntOrNull() ?: 0 }
                return when {
                    parts.size >= 4 -> com.github.jpabscale.uasset4j.propertytypes.structs.core.Color.FromArgb(parts[3], parts[0], parts[1], parts[2])
                    parts.size == 3 -> com.github.jpabscale.uasset4j.propertytypes.structs.core.Color.FromArgb(255, parts[0], parts[1], parts[2])
                    else -> com.github.jpabscale.uasset4j.propertytypes.structs.core.Color.FromArgb(0)
                }
            }
        }
    }
}

/** ECastToken: named string for defined tokens, raw number for undefined ones. */class ECastTokenJsonConverter : StdSerializer<com.github.jpabscale.uasset4j.kismet.bytecode.expressions.ECastTokenValue>(
    com.github.jpabscale.uasset4j.kismet.bytecode.expressions.ECastTokenValue::class.java,
) {
    override fun serialize(value: com.github.jpabscale.uasset4j.kismet.bytecode.expressions.ECastTokenValue, gen: JsonGenerator, provider: SerializerProvider) {
        val name = value.name
        if (name != null) gen.writeString(name) else gen.writeNumber(value.value)
    }

    companion object {
        val deserializer = object : StdDeserializer<com.github.jpabscale.uasset4j.kismet.bytecode.expressions.ECastTokenValue>(
            com.github.jpabscale.uasset4j.kismet.bytecode.expressions.ECastTokenValue::class.java,
        ) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): com.github.jpabscale.uasset4j.kismet.bytecode.expressions.ECastTokenValue {
                if (p.currentToken == JsonToken.VALUE_STRING) {
                    val t = com.github.jpabscale.uasset4j.kismet.bytecode.ECastToken.entries.first { it.name == p.text }
                    return com.github.jpabscale.uasset4j.kismet.bytecode.expressions.ECastTokenValue(t.value)
                }
                return com.github.jpabscale.uasset4j.kismet.bytecode.expressions.ECastTokenValue(p.intValue)
            }
        }
    }
}

/** EArrayDim: named string for defined values, raw number for undefined ones. */
class EArrayDimJsonConverter : StdSerializer<com.github.jpabscale.uasset4j.fieldtypes.EArrayDim>(
    com.github.jpabscale.uasset4j.fieldtypes.EArrayDim::class.java,
) {
    override fun serialize(value: com.github.jpabscale.uasset4j.fieldtypes.EArrayDim, gen: JsonGenerator, provider: SerializerProvider) {
        val name = value.name
        if (name != null) gen.writeString(name) else gen.writeNumber(value.value)
    }

    companion object {
        val deserializer = object : StdDeserializer<com.github.jpabscale.uasset4j.fieldtypes.EArrayDim>(
            com.github.jpabscale.uasset4j.fieldtypes.EArrayDim::class.java,
        ) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): com.github.jpabscale.uasset4j.fieldtypes.EArrayDim {
                if (p.currentToken == JsonToken.VALUE_STRING) {
                    return com.github.jpabscale.uasset4j.fieldtypes.EArrayDim.fromName(p.text)
                }
                return com.github.jpabscale.uasset4j.fieldtypes.EArrayDim.fromValue(p.intValue)
            }
        }
    }
}

/** ELifetimeCondition: named string for defined values, raw number for undefined ones. */
class ELifetimeConditionJsonConverter : StdSerializer<com.github.jpabscale.uasset4j.fieldtypes.ELifetimeCondition>(
    com.github.jpabscale.uasset4j.fieldtypes.ELifetimeCondition::class.java,
) {
    override fun serialize(value: com.github.jpabscale.uasset4j.fieldtypes.ELifetimeCondition, gen: JsonGenerator, provider: SerializerProvider) {
        val name = value.name
        if (name != null) gen.writeString(name) else gen.writeNumber(value.value)
    }

    companion object {
        val deserializer = object : StdDeserializer<com.github.jpabscale.uasset4j.fieldtypes.ELifetimeCondition>(
            com.github.jpabscale.uasset4j.fieldtypes.ELifetimeCondition::class.java,
        ) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): com.github.jpabscale.uasset4j.fieldtypes.ELifetimeCondition {
                if (p.currentToken == JsonToken.VALUE_STRING) {
                    return com.github.jpabscale.uasset4j.fieldtypes.ELifetimeCondition.fromName(p.text)
                }
                return com.github.jpabscale.uasset4j.fieldtypes.ELifetimeCondition.fromByte(p.intValue)
            }
        }
    }
}

/** Newtonsoft BitArrayJsonConverter: an array of booleans. */
class BitArrayJsonConverter : StdSerializer<BitArray>(BitArray::class.java) {
    override fun serialize(value: BitArray, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartArray()
        for (i in 0 until value.Length) gen.writeBoolean(value.get(i))
        gen.writeEndArray()
    }

    companion object {
        val deserializer = object : StdDeserializer<BitArray>(BitArray::class.java) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BitArray {
                val arr = p.readValueAsTree<JsonNode>()
                val res = BitArray()
                if (arr != null && arr.isArray) {
                    res.Length = arr.size()
                    for (i in 0 until arr.size()) {
                        if (arr.get(i).asBoolean()) res.bits.set(i)
                    }
                }
                return res
            }
        }
    }
}

/** Newtonsoft FSignedZeroJsonConverter: preserves -0.0f by emitting the string "-0". */
class FSignedZeroJsonConverter : StdSerializer<Float>(Float::class.java) {
    override fun serialize(value: Float, gen: JsonGenerator, provider: SerializerProvider) {
        val us = value.toDouble()
        if (us == 0.0) {
            gen.writeString(if (isNegativeZero(us)) "-0" else "+0")
        } else {
            gen.writeNumber(NewtonsoftDouble.format(us))
        }
    }

    companion object {
        private fun isNegativeZero(x: Double): Boolean =
            x == 0.0 && (1.0 / x).isInfinite() && (1.0 / x) < 0.0

        val deserializer = object : StdDeserializer<Float>(Float::class.java) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Float {
                val token = p.currentToken
                if (token == JsonToken.VALUE_STRING) {
                    val s = p.text
                    return when (s) {
                        "+0" -> 0f
                        "-0" -> -0.0f
                        else -> s.toFloatOrNull() ?: 0f
                    }
                }
                return p.floatValue
            }
        }
    }
}

/** Newtonsoft FSignedZeroJsonConverter, applied to doubles. */
class FSignedZeroDoubleJsonConverter : StdSerializer<Double>(Double::class.java) {
    override fun serialize(value: Double, gen: JsonGenerator, provider: SerializerProvider) {
        if (value == 0.0) {
            val negativeZero = (1.0 / value).isInfinite() && (1.0 / value) < 0.0
            gen.writeString(if (negativeZero) "-0" else "+0")
        } else {
            gen.writeNumber(NewtonsoftDouble.format(value))
        }
    }

    companion object {
        val deserializer = object : StdDeserializer<Double>(Double::class.java) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Double {
                if (p.currentToken == JsonToken.VALUE_STRING) {
                    val s = p.text
                    return when (s) {
                        "+0" -> 0.0
                        "-0" -> -0.0
                        else -> s.toDoubleOrNull() ?: 0.0
                    }
                }
                return p.doubleValue
            }
        }
    }
}

/** Replicates .NET's shortest round-trip double formatting (Newtonsoft `JsonWriter.WriteValue`). */
object NewtonsoftDouble {
    fun format(v: Double): String {
        val s = v.toString()
        val eIdx = s.indexOf('E')
        val mantissa = if (eIdx >= 0) s.substring(0, eIdx) else s
        val eExp = if (eIdx >= 0) s.substring(eIdx + 1).toInt() else 0

        val neg = mantissa.startsWith("-")
        val digitsPart = if (neg) mantissa.substring(1) else mantissa
        val dot = digitsPart.indexOf('.')
        val intPart = if (dot >= 0) digitsPart.substring(0, dot) else digitsPart
        val fracPart = if (dot >= 0) digitsPart.substring(dot + 1) else ""
        val combined = intPart + fracPart

        val first = combined.indexOfFirst { it != '0' }
        if (first < 0) return if (neg) "-0" else "0"
        val digits = combined.substring(first).dropLastWhile { it == '0' }
        var exp10 = intPart.length - 1 - first + eExp

        val sb = StringBuilder()
        if (neg) sb.append('-')
        if (exp10 >= -4 && exp10 < 17) {
            if (exp10 >= 0) {
                if (exp10 + 1 >= digits.length) {
                    sb.append(digits)
                    repeat(exp10 + 1 - digits.length) { sb.append('0') }
                } else {
                    sb.append(digits, 0, exp10 + 1)
                    val frac = digits.substring(exp10 + 1)
                    if (frac.any { it != '0' }) {
                        sb.append('.')
                        sb.append(frac)
                    }
                }
            } else {
                sb.append("0.")
                repeat(-exp10 - 1) { sb.append('0') }
                sb.append(digits)
            }
            if (sb.indexOf('.') < 0) sb.append(".0")
        } else {
            sb.append(digits[0])
            if (digits.length > 1) {
                sb.append('.')
                sb.append(digits, 1, digits.length)
            }
            sb.append('E')
            if (exp10 < 0) {
                sb.append('-')
                val a = -exp10
                sb.append(if (a < 10) "0$a" else a.toString())
            } else {
                sb.append('+')
                val a = exp10
                sb.append(if (a < 10) "0$a" else a.toString())
            }
        }
        return sb.toString()
    }
}

/**
 * Dynamic serializer for generic-erased `Value` properties (e.g. `MaterialInputPropertyData<T>.Value`
 * and `TPerPlatformPropertyData<T>.Value`). jackson-module-kotlin's type refinement for nullable
 * `T?`/`Array<T>?` properties bypasses the type-registered Float/Double converters, so the property
 * is written through Jackson's default serializers (plain `0.002` instead of the promoted double,
 * plain `0.0` instead of `+0`). This dispatches on the runtime value and routes Float/Double (and
 * arrays thereof) through the FSignedZero converters, mirroring Newtonsoft's global converter.
 */
class DynamicScalarValueSerializer : JsonSerializer<Any>() {
    private val floatConverter = FSignedZeroJsonConverter()
    private val doubleConverter = FSignedZeroDoubleJsonConverter()

    override fun serialize(value: Any?, gen: JsonGenerator, provider: SerializerProvider) {
        when (value) {
            null -> gen.writeNull()
            is Float -> floatConverter.serialize(value, gen, provider)
            is Double -> doubleConverter.serialize(value, gen, provider)
            is FloatArray -> {
                gen.writeStartArray()
                for (f in value) floatConverter.serialize(f, gen, provider)
                gen.writeEndArray()
            }
            is DoubleArray -> {
                gen.writeStartArray()
                for (d in value) doubleConverter.serialize(d, gen, provider)
                gen.writeEndArray()
            }
            is Array<*> -> {
                gen.writeStartArray()
                for (el in value) {
                    when (el) {
                        is Float -> floatConverter.serialize(el, gen, provider)
                        is Double -> doubleConverter.serialize(el, gen, provider)
                        else -> gen.writeObject(el)
                    }
                }
                gen.writeEndArray()
            }
            else -> provider.findValueSerializer(value.javaClass, null).serialize(value, gen, provider)
        }
    }

    override fun serializeWithType(value: Any?, gen: JsonGenerator, provider: SerializerProvider, typeSer: TypeSerializer) {
        if (value == null) {
            typeSer.writeTypePrefixForScalar(value, gen)
            typeSer.writeTypeSuffixForScalar(value, gen)
            return
        }
        when (value) {
            is Float -> floatConverter.serialize(value, gen, provider)
            is Double -> doubleConverter.serialize(value, gen, provider)
            is FloatArray, is DoubleArray -> serialize(value, gen, provider)
            is Array<*> -> serialize(value, gen, provider)
            else -> provider.findValueSerializer(value.javaClass, null).serializeWithType(value, gen, provider, typeSer)
        }
    }
}

/** Generic POCO `$type` emission (Newtonsoft TypeNameHandling.Objects): a generic container's C# type
 * string embeds its type argument, e.g. `TRange`1[[System.Single, System.Private.CoreLib]]`. The
 * Kotlin generic param is erased at runtime, so the element is detected from the value's fields
 * (Float / FFrameNumber / FVector). */
private fun genericTypeId(kind: String, element: String): String =
    """$kind`1[[$element]], UAssetAPI"""

private fun genericElementId(v: Any): String? = when (v) {
    is Float -> "System.Single, System.Private.CoreLib"
    is com.github.jpabscale.uasset4j.propertytypes.structs.movies.FFrameNumber -> "UAssetAPI.UnrealTypes.FFrameNumber, UAssetAPI"
    is com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector -> "UAssetAPI.UnrealTypes.FVector, UAssetAPI"
    else -> null
}

/** FFrameNumber: `$type` + Value. */
class FFrameNumberJsonSerializer : JsonSerializer<com.github.jpabscale.uasset4j.propertytypes.structs.movies.FFrameNumber>() {
    override fun serialize(value: com.github.jpabscale.uasset4j.propertytypes.structs.movies.FFrameNumber, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("\$type", "UAssetAPI.UnrealTypes.FFrameNumber, UAssetAPI")
        gen.writeNumberField("Value", value.Value)
        gen.writeEndObject()
    }
}

/** TRangeBound<T>: `$type` (with generic arg) + Type/Value. */
class TRangeBoundJsonSerializer : JsonSerializer<com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRangeBound<*>>() {
    private val scalar = DynamicScalarValueSerializer()
    override fun serialize(value: com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRangeBound<*>, gen: JsonGenerator, provider: SerializerProvider) {
        val element = genericElementId(value.Value as Any ?: 0f)
            ?: return provider.defaultSerializeValue(value, gen)
        gen.writeStartObject()
        gen.writeStringField("\$type", genericTypeId("UAssetAPI.UnrealTypes.TRangeBound", element))
        gen.writeStringField("Type", value.Type.name)
        gen.writeFieldName("Value")
        scalar.serialize(value.Value, gen, provider)
        gen.writeEndObject()
    }
}

/** TRange<T>: `$type` (with generic arg) + LowerBound/UpperBound. */
class TRangeJsonSerializer : JsonSerializer<com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRange<*>>() {
    override fun serialize(value: com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRange<*>, gen: JsonGenerator, provider: SerializerProvider) {
        val element = genericElementId(value.LowerBound.Value as Any ?: 0f)
            ?: return provider.defaultSerializeValue(value, gen)
        gen.writeStartObject()
        gen.writeStringField("\$type", genericTypeId("UAssetAPI.UnrealTypes.TRange", element))
        gen.writeFieldName("LowerBound")
        provider.defaultSerializeValue(value.LowerBound, gen)
        gen.writeFieldName("UpperBound")
        provider.defaultSerializeValue(value.UpperBound, gen)
        gen.writeEndObject()
    }
}

/** TBox<T>: `$type` (with generic arg) + Min/Max/IsValid. */
class TBoxJsonSerializer : JsonSerializer<com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.TBox<*>>() {
    override fun serialize(value: com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.TBox<*>, gen: JsonGenerator, provider: SerializerProvider) {
        val element = genericElementId(value.Min as Any)
            ?: return provider.defaultSerializeValue(value, gen)
        gen.writeStartObject()
        gen.writeStringField("\$type", genericTypeId("UAssetAPI.UnrealTypes.TBox", element))
        gen.writeFieldName("Min")
        provider.defaultSerializeValue(value.Min, gen)
        gen.writeFieldName("Max")
        provider.defaultSerializeValue(value.Max, gen)
        gen.writeNumberField("IsValid", value.IsValid.toInt())
        gen.writeEndObject()
    }
}

/** FMovieSceneSegment: `$type` + RangeOld/Range/ID/bAllowEmpty/Impls. The unused range (C# struct,
 * never null) is emitted as its default TRange rather than JSON null. */
class FMovieSceneSegmentJsonSerializer : JsonSerializer<com.github.jpabscale.uasset4j.propertytypes.structs.movies.FMovieSceneSegment>() {
    override fun serialize(value: com.github.jpabscale.uasset4j.propertytypes.structs.movies.FMovieSceneSegment, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("\$type", "UAssetAPI.PropertyTypes.Structs.FMovieSceneSegment, UAssetAPI")
        gen.writeFieldName("RangeOld")
        val rangeOld = value.RangeOld ?: defaultFloatRange()
        provider.defaultSerializeValue(rangeOld, gen)
        gen.writeFieldName("Range")
        val range = value.Range ?: defaultFrameRange()
        provider.defaultSerializeValue(range, gen)
        gen.writeFieldName("ID")
        gen.writeNumber(value.ID)
        gen.writeFieldName("bAllowEmpty")
        gen.writeBoolean(value.bAllowEmpty)
        gen.writeFieldName("Impls")
        gen.writeStartArray()
        for (impl in value.Impls) {
            gen.writeObject(impl)
        }
        gen.writeEndArray()
        gen.writeEndObject()
    }

    private fun defaultFrameRange() =
        com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRange(
            com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRangeBound(
                com.github.jpabscale.uasset4j.propertytypes.structs.movies.ERangeBoundTypes.Exclusive,
                com.github.jpabscale.uasset4j.propertytypes.structs.movies.FFrameNumber(0),
            ),
            com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRangeBound(
                com.github.jpabscale.uasset4j.propertytypes.structs.movies.ERangeBoundTypes.Exclusive,
                com.github.jpabscale.uasset4j.propertytypes.structs.movies.FFrameNumber(0),
            ),
        )

    private fun defaultFloatRange() =
        com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRange(
            com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRangeBound(
                com.github.jpabscale.uasset4j.propertytypes.structs.movies.ERangeBoundTypes.Exclusive,
                0f,
            ),
            com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRangeBound(
                com.github.jpabscale.uasset4j.propertytypes.structs.movies.ERangeBoundTypes.Exclusive,
                0f,
            ),
        )
}

/** FFrameNumber deserializer. */
class FFrameNumberJsonDeserializer : JsonDeserializer<com.github.jpabscale.uasset4j.propertytypes.structs.movies.FFrameNumber>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): com.github.jpabscale.uasset4j.propertytypes.structs.movies.FFrameNumber {
        val node = p.readValueAsTree<com.fasterxml.jackson.databind.JsonNode>() ?: return com.github.jpabscale.uasset4j.propertytypes.structs.movies.FFrameNumber(0)
        return com.github.jpabscale.uasset4j.propertytypes.structs.movies.FFrameNumber(node.get("Value")?.asInt() ?: 0)
    }
}

/** TRangeBound<T> deserializer: the element type is encoded in the `$type` generic arg. */
class TRangeBoundJsonDeserializer : JsonDeserializer<com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRangeBound<*>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRangeBound<*> {
        val node = p.readValueAsTree<com.fasterxml.jackson.databind.JsonNode>() ?: return com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRangeBound<Float>()
        val type = com.github.jpabscale.uasset4j.propertytypes.structs.movies.ERangeBoundTypes.entries
            .firstOrNull { it.name == node.get("Type")?.asText() }
            ?: com.github.jpabscale.uasset4j.propertytypes.structs.movies.ERangeBoundTypes.Exclusive
        val valueNode = node.get("Value")
        val isFrame = node.get("\$type")?.asText()?.contains("FFrameNumber") == true
        val codec = p.codec as ObjectMapper
        return if (isFrame) {
            val fn = codec.treeToValue(valueNode, com.github.jpabscale.uasset4j.propertytypes.structs.movies.FFrameNumber::class.java)
            com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRangeBound(type, fn)
        } else {
            val f = valueNode?.asDouble()?.toFloat() ?: 0f
            com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRangeBound(type, f)
        }
    }
}

/** TRange<T> deserializer. */
class TRangeJsonDeserializer : JsonDeserializer<com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRange<*>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRange<*> {
        val node = p.readValueAsTree<com.fasterxml.jackson.databind.JsonNode>() ?: return com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRange(
            com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRangeBound(
                com.github.jpabscale.uasset4j.propertytypes.structs.movies.ERangeBoundTypes.Exclusive, 0f,
            ),
            com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRangeBound(
                com.github.jpabscale.uasset4j.propertytypes.structs.movies.ERangeBoundTypes.Exclusive, 0f,
            ),
        )
        val codec = p.codec as ObjectMapper
        @Suppress("UNCHECKED_CAST")
        val lower = codec.treeToValue(node.get("LowerBound"), com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRangeBound::class.java) as com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRangeBound<Any>
        @Suppress("UNCHECKED_CAST")
        val upper = codec.treeToValue(node.get("UpperBound"), com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRangeBound::class.java) as com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRangeBound<Any>
        @Suppress("UNCHECKED_CAST")
        return com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRange(lower, upper) as com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRange<*>
    }
}

/** TBox<T> deserializer: element type from the `$type` generic arg (FVector supported). */
class TBoxJsonDeserializer : JsonDeserializer<com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.TBox<*>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.TBox<*> {
        val node = p.readValueAsTree<com.fasterxml.jackson.databind.JsonNode>() ?: return com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.TBox<com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector>()
        val codec = p.codec as ObjectMapper
        val min = codec.treeToValue(node.get("Min"), com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector::class.java)
        val max = codec.treeToValue(node.get("Max"), com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector::class.java)
        val isValid = node.get("IsValid")?.asInt() ?: 0
        return com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.TBox(min, max, isValid.toByte())
    }
}

/** FMovieSceneSegment deserializer. */
class FMovieSceneSegmentJsonDeserializer : JsonDeserializer<com.github.jpabscale.uasset4j.propertytypes.structs.movies.FMovieSceneSegment>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): com.github.jpabscale.uasset4j.propertytypes.structs.movies.FMovieSceneSegment {
        val node = p.readValueAsTree<com.fasterxml.jackson.databind.JsonNode>() ?: return com.github.jpabscale.uasset4j.propertytypes.structs.movies.FMovieSceneSegment()
        val codec = p.codec as ObjectMapper
        val res = com.github.jpabscale.uasset4j.propertytypes.structs.movies.FMovieSceneSegment()
        res.ID = node.get("ID")?.asInt() ?: 0
        res.bAllowEmpty = node.get("bAllowEmpty")?.asBoolean() ?: false
        @Suppress("UNCHECKED_CAST")
        res.Range = codec.treeToValue(node.get("Range"), com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRange::class.java) as com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRange<com.github.jpabscale.uasset4j.propertytypes.structs.movies.FFrameNumber>
        @Suppress("UNCHECKED_CAST")
        res.RangeOld = codec.treeToValue(node.get("RangeOld"), com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRange::class.java) as com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRange<Float>
        val impls = node.get("Impls")
        res.Impls = if (impls != null && impls.isArray) {
            val list = codec.readValue(impls.traverse(p.codec as ObjectMapper), object : com.fasterxml.jackson.core.type.TypeReference<List<com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData>>() {})
            list.toTypedArray()
        } else emptyArray()
        return res
    }
}

/** Newtonsoft FPropertyTypeNameConverter: nulls out unless ShouldSerializeNodes; else the nodes array. */
class FPropertyTypeNameJsonConverter : StdSerializer<FPropertyTypeName>(FPropertyTypeName::class.java) {    override fun serialize(value: FPropertyTypeName, gen: JsonGenerator, provider: SerializerProvider) {
        if (!value.ShouldSerializeNodes) {
            gen.writeNull()
        } else {
            val nodesType = provider.typeFactory.constructCollectionType(MutableList::class.java, FPropertyTypeNameNode::class.java)
            provider.findTypedValueSerializer(nodesType, true, null).serialize(value.Nodes, gen, provider)
        }
    }

    companion object {
        val deserializer = object : StdDeserializer<FPropertyTypeName>(FPropertyTypeName::class.java) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): FPropertyTypeName? {
                val node = p.readValueAsTree<JsonNode>() ?: return null
                if (node.isNull) return null
                val codec = p.codec
                val nodes = mutableListOf<FPropertyTypeNameNode>()
                for (entry in node) {
                    val name = codec.treeToValue(entry.get("Name"), FName::class.java) ?: FName.DefineDummy(null, "None")
                    nodes.add(FPropertyTypeNameNode(name, entry.get("InnerCount").asInt()))
                }
                return FPropertyTypeName(nodes, true)
            }
        }
    }
}

/** Newtonsoft FStringTableJsonConverter: { TableNamespace, Value: [[key, value], ...] }. */
class FStringTableJsonConverter : StdSerializer<FStringTable>(FStringTable::class.java) {
    override fun serialize(value: FStringTable, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("TableNamespace", value.TableNamespace?.Value)
        gen.writeFieldName("Value")
        gen.writeStartArray()
        for ((k, v) in value) {
            gen.writeStartArray()
            gen.writeObject(k)
            gen.writeObject(v)
            gen.writeEndArray()
        }
        gen.writeEndArray()
        gen.writeEndObject()
    }

    companion object {
        val deserializer = object : StdDeserializer<FStringTable>(FStringTable::class.java) {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): FStringTable {
                val node = p.readValueAsTree<JsonNode>()
                val res = FStringTable()
                val ns = node.get("TableNamespace")
                res.TableNamespace = if (ns == null || ns.isNull) null else FString(ns.asText())
                val codec = p.codec
                val arr = node.get("Value")
                if (arr != null) {
                    for (pair in arr) {
                        val key = codec.treeToValue(pair.get(0), FString::class.java)
                        val value = codec.treeToValue(pair.get(1), FString::class.java)
                        if (key != null && value != null) res.put(key, value)
                    }
                }
                return res
            }
        }
    }
}

/**
 * Newtonsoft TMapJsonConverter: emits a map as an ordered array of [key, value] pairs, preserving
 * insertion order (C# IOrderedDictionary semantics). Used for MapPropertyData.Value and
 * UAsset.ImportTypeHierarchies.
 */
class TMapJsonSerializer : JsonSerializer<Map<*, *>>() {
    override fun serialize(value: Map<*, *>, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartArray()
        for ((k, v) in value) {
            gen.writeStartArray()
            gen.writeObject(k)
            gen.writeObject(v)
            gen.writeEndArray()
        }
        gen.writeEndArray()
    }
}

/**
 * Newtonsoft's default (no TMapJsonConverter attribute) serialization of
 * `LevelSequenceObjectReferenceMapPropertyData.Value` (`TMap<Guid, FLevelSequenceLegacyObjectReference>`):
 * a JSON object whose `$type` is the generic TMap full name, each entry keyed by the Guid "D" string
 * and each value a `$type`-bearing FLevelSequenceLegacyObjectReference (TypeNameHandling.Objects).
 */
private const val LEVEL_SEQUENCE_MAP_TYPE =
    "UAssetAPI.UnrealTypes.TMap\u00602[[System.Guid, System.Private.CoreLib]," +
        "[UAssetAPI.PropertyTypes.Structs.FLevelSequenceLegacyObjectReference, UAssetAPI]], UAssetAPI"

class LevelSequenceObjectReferenceMapJsonSerializer : JsonSerializer<Any>() {
    override fun serialize(value: Any?, gen: JsonGenerator, provider: SerializerProvider) {
        val map = value as LinkedHashMap<FGuid, FLevelSequenceLegacyObjectReference>
        gen.writeStartObject()
        gen.writeStringField("\$type", LEVEL_SEQUENCE_MAP_TYPE)
        for ((k, v) in map) {
            gen.writeFieldName(k.toString())
            gen.writeStartObject()
            gen.writeStringField("\$type", "UAssetAPI.PropertyTypes.Structs.FLevelSequenceLegacyObjectReference, UAssetAPI")
            gen.writeStringField("ObjectId", v.ObjectId.toPrettyString())
            gen.writeStringField("ObjectPath", v.ObjectPath?.Value)
            gen.writeEndObject()
        }
        gen.writeEndObject()
    }

    override fun serializeWithType(value: Any?, gen: JsonGenerator, provider: SerializerProvider, typeSer: TypeSerializer) {
        serialize(value, gen, provider)
    }
}

class LevelSequenceObjectReferenceMapJsonDeserializer : JsonDeserializer<Any>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Any {
        val res = LinkedHashMap<FGuid, FLevelSequenceLegacyObjectReference>()
        val node = p.readValueAsTree<JsonNode>() ?: return res
        val fields = node.fields()
        while (fields.hasNext()) {
            val entry = fields.next()
            if (entry.key == "\$type") continue
            val v = entry.value
            val objectPath = v.get("ObjectPath")
            res[UAPUtils.ConvertToGUID(entry.key)] = FLevelSequenceLegacyObjectReference(
                UAPUtils.ConvertToGUID(v.get("ObjectId")?.asText() ?: ""),
                objectPath?.takeUnless { it.isNull }?.asText()?.let { FString(it) },
            )
        }
        return res
    }
}

class TMapJsonDeserializer : JsonDeserializer<LinkedHashMap<Any?, Any?>>(), ContextualDeserializer {
    private var keyType: JavaType? = null
    private var valueType: JavaType? = null

    override fun createContextual(ctxt: DeserializationContext, property: BeanProperty?): JsonDeserializer<*> {
        val copy = TMapJsonDeserializer()
        val t = ctxt.contextualType
        if (t != null && t.containedTypeCount() == 2) {
            copy.keyType = t.containedType(0)
            copy.valueType = t.containedType(1)
        }
        return copy
    }

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LinkedHashMap<Any?, Any?> {
        val res = LinkedHashMap<Any?, Any?>()
        val node = p.readValueAsTree<JsonNode>()
        if (node == null || node.isNull || !node.isArray) return res
        val codec = p.codec
        val mapper = codec as ObjectMapper
        val defaultType = ctxt.constructType(Any::class.java)
        for (pair in node) {
            val kt: JavaType = keyType ?: defaultType
            val vt: JavaType = valueType ?: defaultType
            val key: Any? = mapper.convertValue(pair.get(0), kt)
            val value: Any? = mapper.convertValue(pair.get(1), vt)
            res[key] = value
        }
        return res
    }
}

// ---------------------------------------------------------------------------
// [Flags] enum value classes — StringEnumConverter parity ("A, B" / "None")
// ---------------------------------------------------------------------------

/**
 * TextPropertyData.Arguments elements are C# `FFormatArgumentValue` (TypeNameHandling.Objects
 * under TypeNameHandling; the `Value` field is a plain `object`). Jackson never emits `$type` for
 * values behind an `Any`-typed property, so write the C# shape explicitly (`$type` + int `Type` +
 * a type-id-bearing `Value`) and mirror it in the deserializer.
 */
class FFormatArgumentValueJsonSerializer : JsonSerializer<com.github.jpabscale.uasset4j.propertytypes.objects.FFormatArgumentValue>() {
    override fun serialize(value: com.github.jpabscale.uasset4j.propertytypes.objects.FFormatArgumentValue, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("\$type", "UAssetAPI.PropertyTypes.Objects.FFormatArgumentValue, UAssetAPI")
        gen.writeNumberField("Type", value.Type.ordinal)
        gen.writeFieldName("Value")
        val v = value.Value
        if (v == null) gen.writeNull() else provider.defaultSerializeValue(v, gen)
        gen.writeEndObject()
    }
}

class FFormatArgumentValueJsonDeserializer : JsonDeserializer<com.github.jpabscale.uasset4j.propertytypes.objects.FFormatArgumentValue>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): com.github.jpabscale.uasset4j.propertytypes.objects.FFormatArgumentValue {
        val node = p.readValueAsTree<com.fasterxml.jackson.databind.JsonNode>()!!
        val typeName = node.get("Type")?.asText() ?: ""
        val type = com.github.jpabscale.uasset4j.unrealtypes.engineenums.EFormatArgumentType.entries
            .firstOrNull { it.name == typeName }
            ?: com.github.jpabscale.uasset4j.unrealtypes.engineenums.EFormatArgumentType.entries
            .getOrElse(typeName.toIntOrNull() ?: 0) { com.github.jpabscale.uasset4j.unrealtypes.engineenums.EFormatArgumentType.Int }
        val valueNode = node.get("Value")
        val mapper = p.codec as ObjectMapper
        val value: Any? = when (type) {
            com.github.jpabscale.uasset4j.unrealtypes.engineenums.EFormatArgumentType.Int,
            com.github.jpabscale.uasset4j.unrealtypes.engineenums.EFormatArgumentType.UInt ->
                valueNode?.takeUnless { it.isNull }?.asLong()
            com.github.jpabscale.uasset4j.unrealtypes.engineenums.EFormatArgumentType.Double ->
                valueNode?.takeUnless { it.isNull }?.asDouble()
            com.github.jpabscale.uasset4j.unrealtypes.engineenums.EFormatArgumentType.Float ->
                valueNode?.takeUnless { it.isNull }?.floatValue()
            com.github.jpabscale.uasset4j.unrealtypes.engineenums.EFormatArgumentType.Text ->
                valueNode?.takeUnless { it.isNull }?.let {
                    mapper.treeToValue(it, com.github.jpabscale.uasset4j.propertytypes.objects.TextPropertyData::class.java)
                }
            else -> null
        }
        return com.github.jpabscale.uasset4j.propertytypes.objects.FFormatArgumentValue(type, value)
    }
}

/** Name/value table for a C# `[Flags]` enum mapped to a Kotlin value class. */
private data class FlagsDef(
    val bits: List<Pair<String, Long>>,
) {
    fun toString(v: Long): String {
        if (v == 0L) return bits.firstOrNull { it.second == 0L }?.first ?: "0"
        val matched = bits.filter { it.second != 0L && (v and it.second) == it.second }.map { it.first }
        var remaining = v
        for ((_, bit) in bits) remaining = remaining and bit.inv()
        val parts = matched.toMutableList()
        if (remaining != 0L) parts.add(remaining.toString())
        return parts.joinToString(", ")
    }

    fun fromString(s: String): Long {
        if (s.isNullOrEmpty()) return 0L
        var res = 0L
        for (part in s.split(", ")) {
            if (part.isEmpty()) continue
            val bit = bits.firstOrNull { it.first == part }?.second ?: part.toLongOrNull() ?: 0L
            res = res or bit
        }
        return res
    }

    /** Can this value be represented by names? (Newtonsoft StringEnumConverter writes named values
     * as strings and unnamed values as raw integers.) */
    fun canName(v: Long): Boolean {
        if (v == 0L) return bits.any { it.second == 0L }
        var remaining = v
        for ((_, bit) in bits) remaining = remaining and bit.inv()
        return remaining == 0L
    }
}

private fun flagsSerializer(def: FlagsDef, wrap: (Long) -> Any): JsonSerializer<Any> {
    return object : JsonSerializer<Any>() {
        override fun serialize(value: Any, gen: JsonGenerator, provider: SerializerProvider) {
            // Int-backed flags sign-extend when widened to Long; mask to 32 bits.
            val v = (value::class.java.getMethod("getValue").invoke(value) as Number).toLong() and 0xFFFFFFFFL
            if (def.canName(v)) gen.writeString(def.toString(v)) else gen.writeNumber(v)
        }
    }
}

private fun flagsSerializer64(def: FlagsDef, wrap: (Long) -> Any): JsonSerializer<Any> {
    return object : JsonSerializer<Any>() {
        override fun serialize(value: Any, gen: JsonGenerator, provider: SerializerProvider) {
            val v = (value::class.java.getMethod("getValue").invoke(value) as Number).toLong()
            if (def.canName(v)) gen.writeString(def.toString(v)) else gen.writeNumber(java.lang.Long.toUnsignedString(v))
        }
    }
}

private fun flagsDeserializer(def: FlagsDef, wrap: (Long) -> Any): JsonDeserializer<Any> {
    return object : JsonDeserializer<Any>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Any {
            val v = def.fromString(p.valueAsString ?: "")
            return wrap(v)
        }
    }
}

private object FlagsDefs {
    val propertyTagFlags = FlagsDef(
        listOf(
            "None" to 0x00L, "HasArrayIndex" to 0x01L, "HasPropertyGuid" to 0x02L,
            "HasPropertyExtensions" to 0x04L, "HasBinaryOrNativeSerialize" to 0x08L,
            "BoolTrue" to 0x10L, "SkippedSerialize" to 0x20L,
        ),
    )
    val propertyTagExtension = FlagsDef(
        listOf(
            "NoExtension" to 0x00L, "ReserveForFutureUse" to 0x01L, "OverridableInformation" to 0x02L,
        ),
    )
    val textFlag = FlagsDef(
        listOf(
            "Transient" to 1L, "CultureInvariant" to 2L, "ConvertedProperty" to 4L,
            "Immutable" to 8L, "InitializedFromString" to 16L,
        ),
    )
    val classSerializationControlExtension = FlagsDef(
        listOf(
            "NoExtension" to 0x00L, "ReserveForFutureUse" to 0x01L, "OverridableSerializationInformation" to 0x02L,
        ),
    )
    val customSerializationFlags = FlagsDef(
        listOf(
            "None" to 0L, "NoDummies" to 1L, "SkipParsingBytecode" to 2L,
            "SkipPreloadDependencyLoading" to 4L, "SkipParsingExports" to 8L, "SkipLoadingExports" to 16L,
        ),
    )
    val packageFlags = FlagsDef(
        listOf(
            "PKG_None" to 0x00000000L, "PKG_NewlyCreated" to 0x00000001L, "PKG_ClientOptional" to 0x00000002L,
            "PKG_ServerSideOnly" to 0x00000004L, "PKG_CompiledIn" to 0x00000010L, "PKG_ForDiffing" to 0x00000020L,
            "PKG_EditorOnly" to 0x00000040L, "PKG_Developer" to 0x00000080L, "PKG_UncookedOnly" to 0x00000100L,
            "PKG_Cooked" to 0x00000200L, "PKG_ContainsNoAsset" to 0x00000400L, "PKG_UnversionedProperties" to 0x00002000L,
            "PKG_ContainsMapData" to 0x00004000L, "PKG_Compiling" to 0x00010000L, "PKG_ContainsMap" to 0x00020000L,
            "PKG_RequiresLocalizationGather" to 0x00040000L, "PKG_PlayInEditor" to 0x00100000L,
            "PKG_ContainsScript" to 0x00200000L, "PKG_DisallowExport" to 0x00400000L, "PKG_DynamicImports" to 0x10000000L,
            "PKG_RuntimeGenerated" to 0x20000000L, "PKG_ReloadingForCooker" to 0x40000000L,
            "PKG_FilterEditorOnly" to 0x80000000L,
        ),
    )
    val objectFlags = FlagsDef(
        listOf(
            "RF_NoFlags" to 0x00000000L, "RF_Public" to 0x00000001L, "RF_Standalone" to 0x00000002L,
            "RF_MarkAsNative" to 0x00000004L, "RF_Transactional" to 0x00000008L, "RF_ClassDefaultObject" to 0x00000010L,
            "RF_ArchetypeObject" to 0x00000020L, "RF_Transient" to 0x00000040L, "RF_MarkAsRootSet" to 0x00000080L,
            "RF_TagGarbageTemp" to 0x00000100L, "RF_NeedInitialization" to 0x00000200L, "RF_NeedLoad" to 0x00000400L,
            "RF_KeepForCooker" to 0x00000800L, "RF_NeedPostLoad" to 0x00001000L, "RF_NeedPostLoadSubobjects" to 0x00002000L,
            "RF_NewerVersionExists" to 0x00004000L, "RF_BeginDestroyed" to 0x00008000L, "RF_FinishDestroyed" to 0x00010000L,
            "RF_BeingRegenerated" to 0x00020000L, "RF_DefaultSubObject" to 0x00040000L, "RF_WasLoaded" to 0x00080000L,
            "RF_TextExportTransient" to 0x00100000L, "RF_LoadCompleted" to 0x00200000L,
            "RF_InheritableComponentTemplate" to 0x00400000L, "RF_DuplicateTransient" to 0x00800000L,
            "RF_StrongRefOnFrame" to 0x01000000L, "RF_NonPIEDuplicateTransient" to 0x02000000L,
            "RF_Dynamic" to 0x04000000L, "RF_WillBeLoaded" to 0x08000000L, "RF_HasExternalPackage" to 0x10000000L,
        ),
    )
    val classFlags = FlagsDef(
        listOf(
            "CLASS_None" to 0x00000000L, "CLASS_Abstract" to 0x00000001L, "CLASS_DefaultConfig" to 0x00000002L,
            "CLASS_Config" to 0x00000004L, "CLASS_Transient" to 0x00000008L, "CLASS_Parsed" to 0x00000010L,
            "CLASS_MatchedSerializers" to 0x00000020L, "CLASS_ProjectUserConfig" to 0x00000040L,
            "CLASS_Native" to 0x00000080L, "CLASS_NoExport" to 0x00000100L, "CLASS_NotPlaceable" to 0x00000200L,
            "CLASS_PerObjectConfig" to 0x00000400L, "CLASS_ReplicationDataIsSetUp" to 0x00000800L,
            "CLASS_EditInlineNew" to 0x00001000L, "CLASS_CollapseCategories" to 0x00002000L,
            "CLASS_Interface" to 0x00004000L, "CLASS_CustomConstructor" to 0x00008000L, "CLASS_Const" to 0x00010000L,
            "CLASS_LayoutChanging" to 0x00020000L, "CLASS_CompiledFromBlueprint" to 0x00040000L,
            "CLASS_MinimalAPI" to 0x00080000L, "CLASS_RequiredAPI" to 0x00100000L,
            "CLASS_DefaultToInstanced" to 0x00200000L, "CLASS_TokenStreamAssembled" to 0x00400000L,
            "CLASS_HasInstancedReference" to 0x00800000L, "CLASS_Hidden" to 0x01000000L,
            "CLASS_Deprecated" to 0x02000000L, "CLASS_HideDropDown" to 0x04000000L,
            "CLASS_GlobalUserConfig" to 0x08000000L, "CLASS_Intrinsic" to 0x10000000L,
            "CLASS_Constructed" to 0x20000000L, "CLASS_ConfigDoNotCheckDefaults" to 0x40000000L,
            "CLASS_NewerVersionExists" to 0x80000000L,
        ),
    )
    val propertyFlags = FlagsDef(
        listOf(
            "CPF_None" to 0L, "CPF_Edit" to 0x0000000000000001L, "CPF_ConstParm" to 0x0000000000000002L,
            "CPF_BlueprintVisible" to 0x0000000000000004L, "CPF_ExportObject" to 0x0000000000000008L,
            "CPF_BlueprintReadOnly" to 0x0000000000000010L, "CPF_Net" to 0x0000000000000020L,
            "CPF_EditFixedSize" to 0x0000000000000040L, "CPF_Parm" to 0x0000000000000080L,
            "CPF_OutParm" to 0x0000000000000100L, "CPF_ZeroConstructor" to 0x0000000000000200L,
            "CPF_ReturnParm" to 0x0000000000000400L, "CPF_DisableEditOnTemplate" to 0x0000000000000800L,
            "CPF_Transient" to 0x0000000000002000L, "CPF_Config" to 0x0000000000004000L,
            "CPF_DisableEditOnInstance" to 0x0000000000010000L, "CPF_EditConst" to 0x0000000000020000L,
            "CPF_GlobalConfig" to 0x0000000000040000L, "CPF_InstancedReference" to 0x0000000000080000L,
            "CPF_DuplicateTransient" to 0x0000000000200000L, "CPF_SaveGame" to 0x0000000001000000L,
            "CPF_NoClear" to 0x0000000002000000L, "CPF_ReferenceParm" to 0x0000000008000000L,
            "CPF_BlueprintAssignable" to 0x0000000010000000L, "CPF_Deprecated" to 0x0000000020000000L,
            "CPF_IsPlainOldData" to 0x0000000040000000L, "CPF_RepSkip" to 0x0000000080000000L,
            "CPF_RepNotify" to 0x0000000100000000L, "CPF_Interp" to 0x0000000200000000L,
            "CPF_NonTransactional" to 0x0000000400000000L, "CPF_EditorOnly" to 0x0000000800000000L,
            "CPF_NoDestructor" to 0x0000001000000000L, "CPF_AutoWeak" to 0x0000004000000000L,
            "CPF_ContainsInstancedReference" to 0x0000008000000000L, "CPF_AssetRegistrySearchable" to 0x0000010000000000L,
            "CPF_SimpleDisplay" to 0x0000020000000000L, "CPF_AdvancedDisplay" to 0x0000040000000000L,
            "CPF_Protected" to 0x0000080000000000L, "CPF_BlueprintCallable" to 0x0000100000000000L,
            "CPF_BlueprintAuthorityOnly" to 0x0000200000000000L, "CPF_TextExportTransient" to 0x0000400000000000L,
            "CPF_NonPIEDuplicateTransient" to 0x0000800000000000L, "CPF_ExposeOnSpawn" to 0x0001000000000000L,
            "CPF_PersistentInstance" to 0x0002000000000000L, "CPF_UObjectWrapper" to 0x0004000000000000L,
            "CPF_HasGetValueTypeHash" to 0x0008000000000000L, "CPF_NativeAccessSpecifierPublic" to 0x0010000000000000L,
            "CPF_NativeAccessSpecifierProtected" to 0x0020000000000000L, "CPF_NativeAccessSpecifierPrivate" to 0x0040000000000000L,
            "CPF_SkipSerialization" to 0x0080000000000000L,
        ),
    )
    val functionFlags = FlagsDef(
        listOf(
            "FUNC_None" to 0x00000000L, "FUNC_Final" to 0x00000001L, "FUNC_RequiredAPI" to 0x00000002L,
            "FUNC_BlueprintAuthorityOnly" to 0x00000004L, "FUNC_BlueprintCosmetic" to 0x00000008L,
            "FUNC_Net" to 0x00000040L, "FUNC_NetReliable" to 0x00000080L, "FUNC_NetRequest" to 0x00000100L,
            "FUNC_Exec" to 0x00000200L, "FUNC_Native" to 0x00000400L, "FUNC_Event" to 0x00000800L,
            "FUNC_NetResponse" to 0x00001000L, "FUNC_Static" to 0x00002000L, "FUNC_NetMulticast" to 0x00004000L,
            "FUNC_UbergraphFunction" to 0x00008000L, "FUNC_MulticastDelegate" to 0x00010000L,
            "FUNC_Public" to 0x00020000L, "FUNC_Private" to 0x00040000L, "FUNC_Protected" to 0x00080000L,
            "FUNC_Delegate" to 0x00100000L, "FUNC_NetServer" to 0x00200000L, "FUNC_HasOutParms" to 0x00400000L,
            "FUNC_HasDefaults" to 0x00800000L, "FUNC_NetClient" to 0x01000000L, "FUNC_DLLImport" to 0x02000000L,
            "FUNC_BlueprintCallable" to 0x04000000L, "FUNC_BlueprintEvent" to 0x08000000L,
            "FUNC_BlueprintPure" to 0x10000000L, "FUNC_EditorOnly" to 0x20000000L, "FUNC_Const" to 0x40000000L,
            "FUNC_NetValidate" to 0x80000000L,
        ),
    )
    val objectDataResourceFlags = FlagsDef(
        listOf(
            "None" to 0L, "Inline" to 1L, "Streaming" to 2L, "Optional" to 4L,
            "Duplicate" to 8L, "MemoryMapped" to 16L, "DerivedDataReference" to 32L,
        ),
    )
}

/** Serializer/deserializer registration for the Kotlin `[Flags]` value classes. */
object FlagsConverters {
    val propertyTagFlagsSerializer = flagsSerializer(FlagsDefs.propertyTagFlags) { EPropertyTagFlags(it.toInt()) }
    val propertyTagFlagsDeserializer = flagsDeserializer(FlagsDefs.propertyTagFlags) { EPropertyTagFlags(it.toInt()) }

    val propertyTagExtensionSerializer = flagsSerializer(FlagsDefs.propertyTagExtension) { EPropertyTagExtension(it.toByte()) }
    val propertyTagExtensionDeserializer = flagsDeserializer(FlagsDefs.propertyTagExtension) { EPropertyTagExtension(it.toByte()) }

    val classSerializationControlExtensionSerializer = flagsSerializer(FlagsDefs.classSerializationControlExtension) { EClassSerializationControlExtension(it.toByte()) }
    val classSerializationControlExtensionDeserializer = flagsDeserializer(FlagsDefs.classSerializationControlExtension) { EClassSerializationControlExtension(it.toByte()) }

    val customSerializationFlagsSerializer = flagsSerializer(FlagsDefs.customSerializationFlags) { CustomSerializationFlags(it.toInt()) }
    val customSerializationFlagsDeserializer = flagsDeserializer(FlagsDefs.customSerializationFlags) { CustomSerializationFlags(it.toInt()) }

    val packageFlagsSerializer = flagsSerializer(FlagsDefs.packageFlags) { EPackageFlags(it.toInt()) }
    val packageFlagsDeserializer = flagsDeserializer(FlagsDefs.packageFlags) { EPackageFlags(it.toInt()) }

    val objectFlagsSerializer = flagsSerializer(FlagsDefs.objectFlags) { EObjectFlags(it) }
    val objectFlagsDeserializer = flagsDeserializer(FlagsDefs.objectFlags) { EObjectFlags(it) }

    val textFlagSerializer = flagsSerializer(FlagsDefs.textFlag) { ETextFlag(it.toInt()) }
    val textFlagDeserializer = flagsDeserializer(FlagsDefs.textFlag) { ETextFlag(it.toInt()) }

    val classFlagsSerializer = flagsSerializer(FlagsDefs.classFlags) { EClassFlags(it.toInt()) }
    val classFlagsDeserializer = flagsDeserializer(FlagsDefs.classFlags) { EClassFlags(it.toInt()) }

    val propertyFlagsSerializer = flagsSerializer64(FlagsDefs.propertyFlags) { EPropertyFlags(it) }
    val propertyFlagsDeserializer = flagsDeserializer(FlagsDefs.propertyFlags) { EPropertyFlags(it) }

    val functionFlagsSerializer = flagsSerializer(FlagsDefs.functionFlags) { EFunctionFlags(it.toInt()) }
    val functionFlagsDeserializer = flagsDeserializer(FlagsDefs.functionFlags) { EFunctionFlags(it.toInt()) }

    val objectDataResourceFlagsSerializer = flagsSerializer(FlagsDefs.objectDataResourceFlags) { EObjectDataResourceFlags(it.toInt()) }
    val objectDataResourceFlagsDeserializer = flagsDeserializer(FlagsDefs.objectDataResourceFlags) { EObjectDataResourceFlags(it.toInt()) }

    fun parsePropertyTagFlags(s: String) = EPropertyTagFlags(FlagsDefs.propertyTagFlags.fromString(s).toInt())
    fun parsePropertyTagExtension(s: String) = EPropertyTagExtension(FlagsDefs.propertyTagExtension.fromString(s).toByte())
    fun parseClassSerializationControlExtension(s: String) = EClassSerializationControlExtension(FlagsDefs.classSerializationControlExtension.fromString(s).toByte())
    fun parseCustomSerializationFlags(s: String) = CustomSerializationFlags(FlagsDefs.customSerializationFlags.fromString(s).toInt())
    fun parsePackageFlags(s: String) = EPackageFlags(FlagsDefs.packageFlags.fromString(s).toInt())
    fun parseObjectFlags(s: String) = EObjectFlags(FlagsDefs.objectFlags.fromString(s))
    fun parseTextFlag(s: String) = ETextFlag(FlagsDefs.textFlag.fromString(s).toInt())
    fun parseClassFlags(s: String) = EClassFlags(FlagsDefs.classFlags.fromString(s).toInt())
    fun parsePropertyFlags(s: String) = EPropertyFlags(FlagsDefs.propertyFlags.fromString(s))
    fun parseFunctionFlags(s: String) = EFunctionFlags(FlagsDefs.functionFlags.fromString(s).toInt())
    fun parseObjectDataResourceFlags(s: String) = EObjectDataResourceFlags(FlagsDefs.objectDataResourceFlags.fromString(s).toInt())
}
