// Reimplemented from CUE4Parse (Apache-2.0) — Copyright (c) FabianFG and contributors
// Source: CUE4Parse/UE4/Objects/Engine/Curves/RichCurve.cs (FStructFallback ctors)
//
// Helpers that read the property-serialized form of a curve struct (the FStructFallback path in
// CUE4Parse) into the dedicated curve model types. These extend the existing MIT/UAssetAPI-ported
// FRichCurveKey without modifying it.
//@parity:on EXC-002
package com.github.jpabscale.uasset4j.curves

import com.github.jpabscale.uasset4j.propertytypes.objects.ArrayPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.BytePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.EnumPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.FloatPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.RichCurveKeyPropertyData
import com.github.jpabscale.uasset4j.unrealtypes.engineenums.ERichCurveInterpMode
import com.github.jpabscale.uasset4j.unrealtypes.engineenums.ERichCurveTangentMode
import com.github.jpabscale.uasset4j.unrealtypes.engineenums.ERichCurveTangentWeightMode
import com.github.jpabscale.uasset4j.unrealtypes.objects.engine.FRichCurveKey

/** Reads a property-serialized `RichCurveKey` struct into a [FRichCurveKey]. */
fun fromStructRichCurveKey(data: StructPropertyData): FRichCurveKey {
    val key = FRichCurveKey()
    val value = data.Value
    if (value == null) return key
    for (p in value) {
        when (p.Name?.Value?.Value) {
            "InterpMode" -> key.InterpMode = p.asByteOrEnumEnum()?.let { ERichCurveInterpMode.entries[it] } ?: key.InterpMode
            "TangentMode" -> key.TangentMode = p.asByteOrEnumEnum()?.let { ERichCurveTangentMode.entries[it] } ?: key.TangentMode
            "TangentWeightMode" -> key.TangentWeightMode = p.asByteOrEnumEnum()?.let { ERichCurveTangentWeightMode.entries[it] } ?: key.TangentWeightMode
            "Time" -> key.Time = p.asFloat() ?: key.Time
            "Value" -> key.Value = p.asFloat() ?: key.Value
            "ArriveTangent" -> key.ArriveTangent = p.asFloat() ?: key.ArriveTangent
            "ArriveTangentWeight" -> key.ArriveTangentWeight = p.asFloat() ?: key.ArriveTangentWeight
            "LeaveTangent" -> key.LeaveTangent = p.asFloat() ?: key.LeaveTangent
            "LeaveTangentWeight" -> key.LeaveTangentWeight = p.asFloat() ?: key.LeaveTangentWeight
        }
    }
    return key
}

/** Reads a property-serialized `SimpleCurveKey` struct (Time/Value floats) into a [FSimpleCurveKey]. */
fun fromStructSimpleCurveKey(data: StructPropertyData): FSimpleCurveKey {
    val key = FSimpleCurveKey()
    val value = data.Value
    if (value == null) return key
    for (p in value) {
        when (p.Name?.Value?.Value) {
            "Time" -> key.Time = p.asFloat() ?: key.Time
            "Value" -> key.Value = p.asFloat() ?: key.Value
        }
    }
    return key
}

/** Fills the FRealCurve base fields (DefaultValue, extrapolation) from a struct property list. */
internal fun FRealCurveCommon.populate(data: StructPropertyData, curve: FRealCurve) {
    val value = data.Value ?: return
    for (p in value) {
        when (p.Name?.Value?.Value) {
            "DefaultValue" -> curve.DefaultValue = p.asFloat() ?: curve.DefaultValue
            "PreInfinityExtrap" -> curve.PreInfinityExtrap = p.asExtrapolation() ?: curve.PreInfinityExtrap
            "PostInfinityExtrap" -> curve.PostInfinityExtrap = p.asExtrapolation() ?: curve.PostInfinityExtrap
        }
    }
}

/** Extrapolation enums are stored as a byte property (TEnumAsByte) or a byte-typed enum. */
internal fun PropertyData.asExtrapolation(): ERichCurveExtrapolation? =
    asByteOrEnumEnum()?.let { ERichCurveExtrapolation.entries[it] }

internal fun PropertyData.asByteOrEnumEnum(): Int? = when (this) {
    is BytePropertyData -> Value?.toInt()?.and(0xFF)
    is EnumPropertyData -> Value?.Value?.let { matchEnum(it.toString()) }
    else -> null
}

internal fun PropertyData.asFloat(): Float? = when (this) {
    is FloatPropertyData -> Value
    else -> null
}

private fun matchEnum(name: String): Int? {
    // e.g. "RCIM_Cubic", "ERichCurveInterpMode::RCIM_Cubic", or "RCIM_Cubic" with mappings prefix
    val clean = name.substringAfter("::").substringAfterLast(".")
    for (entries in listOf(
        ERichCurveInterpMode.entries,
        ERichCurveTangentMode.entries,
        ERichCurveTangentWeightMode.entries,
        ERichCurveExtrapolation.entries,
    )) {
        val idx = entries.indexOfFirst { it.name == clean }
        if (idx >= 0) return idx
    }
    return null
}

/** Extracts the `Keys` array of a property-serialized curve struct. */
internal fun keysFromStruct(data: StructPropertyData): List<FRichCurveKey> {
    val out = mutableListOf<FRichCurveKey>()
    val value = data.Value ?: return out
    for (p in value) {
        if (p is ArrayPropertyData && p.Name?.Value?.Value == "Keys") {
            val list = p.Value
            if (list != null) {
                for (k in list) {
                    when (k) {
                        is StructPropertyData -> out.add(fromStructRichCurveKey(k))
                        is RichCurveKeyPropertyData -> out.add(k.Value ?: FRichCurveKey())
                    }
                }
            }
        }
    }
    return out
}

/** Object holding the shared FRealCurve read-from-struct helpers. */
internal object FRealCurveCommon
//@parity:off EXC-002
