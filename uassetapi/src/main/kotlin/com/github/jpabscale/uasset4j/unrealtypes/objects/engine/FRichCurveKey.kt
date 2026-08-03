// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Engine/FRichCurveKey.cs
package com.github.jpabscale.uasset4j.unrealtypes.objects.engine

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.StructAccessors
import com.github.jpabscale.uasset4j.unrealtypes.engineenums.ERichCurveInterpMode
import com.github.jpabscale.uasset4j.unrealtypes.engineenums.ERichCurveTangentMode
import com.github.jpabscale.uasset4j.unrealtypes.engineenums.ERichCurveTangentWeightMode

class FRichCurveKey {
    var InterpMode: ERichCurveInterpMode
    var TangentMode: ERichCurveTangentMode
    var TangentWeightMode: ERichCurveTangentWeightMode
    var Time: Float
    var Value: Float
    var ArriveTangent: Float
    var ArriveTangentWeight: Float
    var LeaveTangent: Float
    var LeaveTangentWeight: Float

    constructor() {
        InterpMode = ERichCurveInterpMode.RCIM_Linear
        TangentMode = ERichCurveTangentMode.RCTM_Auto
        TangentWeightMode = ERichCurveTangentWeightMode.RCTWM_WeightedNone
        Time = 0f
        Value = 0f
        ArriveTangent = 0f
        ArriveTangentWeight = 0f
        LeaveTangent = 0f
        LeaveTangentWeight = 0f
    }

    constructor(
        interpMode: ERichCurveInterpMode,
        tangentMode: ERichCurveTangentMode,
        tangentWeightMode: ERichCurveTangentWeightMode,
        time: Float,
        value: Float,
        arriveTangent: Float,
        arriveTangentWeight: Float,
        leaveTangent: Float,
        leaveTangentWeight: Float,
    ) {
        InterpMode = interpMode
        TangentMode = tangentMode
        TangentWeightMode = tangentWeightMode
        Time = time
        Value = value
        ArriveTangent = arriveTangent
        ArriveTangentWeight = arriveTangentWeight
        LeaveTangent = leaveTangent
        LeaveTangentWeight = leaveTangentWeight
    }

    constructor(reader: AssetBinaryReader) {
        InterpMode = ERichCurveInterpMode.entries[reader.ReadByte()]
        TangentMode = ERichCurveTangentMode.entries[reader.ReadByte()]
        TangentWeightMode = ERichCurveTangentWeightMode.entries[reader.ReadByte()]
        Time = reader.ReadSingle()
        Value = reader.ReadSingle()
        ArriveTangent = reader.ReadSingle()
        ArriveTangentWeight = reader.ReadSingle()
        LeaveTangent = reader.ReadSingle()
        LeaveTangentWeight = reader.ReadSingle()
    }

    fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteByte(InterpMode.ordinal)
        writer.WriteByte(TangentMode.ordinal)
        writer.WriteByte(TangentWeightMode.ordinal)
        writer.WriteSingle(Time)
        writer.WriteSingle(Value)
        writer.WriteSingle(ArriveTangent)
        writer.WriteSingle(ArriveTangentWeight)
        writer.WriteSingle(LeaveTangent)
        writer.WriteSingle(LeaveTangentWeight)
        return 6 * 4 + 3
    }

    override fun toString(): String =
        "($InterpMode, $TangentMode, $TangentWeightMode, $Time, $Value, $ArriveTangent, $ArriveTangentWeight, $LeaveTangent, $LeaveTangentWeight)"

    companion object {
        val accessors = StructAccessors(
            read = { r -> FRichCurveKey(r) },
            fromString = { d, _ ->
                FRichCurveKey(
                    ERichCurveInterpMode.entries.firstOrNull { it.name.equals(d[0], ignoreCase = true) }
                        ?: ERichCurveInterpMode.RCIM_Linear,
                    ERichCurveTangentMode.entries.firstOrNull { it.name.equals(d[1], ignoreCase = true) }
                        ?: ERichCurveTangentMode.RCTM_Auto,
                    ERichCurveTangentWeightMode.entries.firstOrNull { it.name.equals(d[2], ignoreCase = true) }
                        ?: ERichCurveTangentWeightMode.RCTWM_WeightedNone,
                    d[3].toFloatOrNull() ?: 0f,
                    d[4].toFloatOrNull() ?: 0f,
                    d[5].toFloatOrNull() ?: 0f,
                    d[6].toFloatOrNull() ?: 0f,
                    d[7].toFloatOrNull() ?: 0f,
                    d[8].toFloatOrNull() ?: 0f,
                )
            },
            write = { w, v -> v.Write(w) },
            defaultValue = { FRichCurveKey() },
        )
    }
}
