// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Movies/MovieSceneValue.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.movies

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.customversions.FSequencerObjectVersion

class FMovieSceneTangentData {
    var ArriveTangent: Float = 0f
    var LeaveTangent: Float = 0f
    var ArriveTangentWeight: Float = 0f
    var LeaveTangentWeight: Float = 0f
    var TangentWeightMode: ERichCurveTangentWeightMode = ERichCurveTangentWeightMode.RCTWM_WeightedNone
    var padding: ByteArray = ByteArray(0)

    constructor()

    constructor(reader: AssetBinaryReader) {
        ArriveTangent = reader.ReadSingle()
        LeaveTangent = reader.ReadSingle()
        if (reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FSequencerObjectVersion")) <
            FSequencerObjectVersion.SerializeFloatChannelCompletely.value
        ) {
            TangentWeightMode = ERichCurveTangentWeightMode.entries[reader.ReadByte()]
            ArriveTangentWeight = reader.ReadSingle()
            LeaveTangentWeight = reader.ReadSingle()
            padding = ByteArray(0)
        } else {
            ArriveTangentWeight = reader.ReadSingle()
            LeaveTangentWeight = reader.ReadSingle()
            TangentWeightMode = ERichCurveTangentWeightMode.entries[reader.ReadByte()]
            padding = reader.ReadBytes(3)
        }
    }

    fun Write(writer: AssetBinaryWriter) {
        writer.WriteSingle(ArriveTangent)
        writer.WriteSingle(LeaveTangent)
        if (writer.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FSequencerObjectVersion")) <
            FSequencerObjectVersion.SerializeFloatChannelCompletely.value
        ) {
            writer.WriteByte(TangentWeightMode.ordinal)
            writer.WriteSingle(ArriveTangentWeight)
            writer.WriteSingle(LeaveTangentWeight)
        } else {
            writer.WriteSingle(ArriveTangentWeight)
            writer.WriteSingle(LeaveTangentWeight)
            writer.WriteByte(TangentWeightMode.ordinal)
        }
        writer.WriteBytes(padding)
    }
}

open class FMovieSceneValue<T>(
    reader: AssetBinaryReader?,
    value: T,
) {
    var Value: T = value
    var Tangent: FMovieSceneTangentData = FMovieSceneTangentData()
    var InterpMode: ERichCurveInterpMode = ERichCurveInterpMode.RCIM_Linear
    var TangentMode: ERichCurveTangentMode = ERichCurveTangentMode.RCTM_Auto
    var padding: ByteArray = ByteArray(0)

    init {
        if (reader != null) {
            if (reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FSequencerObjectVersion")) <
                FSequencerObjectVersion.SerializeFloatChannelCompletely.value
            ) {
                InterpMode = ERichCurveInterpMode.entries[reader.ReadByte()]
                TangentMode = ERichCurveTangentMode.entries[reader.ReadByte()]
                Tangent = FMovieSceneTangentData(reader)
                padding = ByteArray(0)
            } else {
                Tangent = FMovieSceneTangentData(reader)
                InterpMode = ERichCurveInterpMode.entries[reader.ReadByte()]
                TangentMode = ERichCurveTangentMode.entries[reader.ReadByte()]
                padding = reader.ReadBytes(2)
            }
        }
    }

    fun Write(writer: AssetBinaryWriter, valueWriter: (T) -> Unit) {
        valueWriter(Value)
        if (writer.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FSequencerObjectVersion")) <
            FSequencerObjectVersion.SerializeFloatChannelCompletely.value
        ) {
            writer.WriteByte(InterpMode.ordinal)
            writer.WriteByte(TangentMode.ordinal)
            Tangent.Write(writer)
        } else {
            Tangent.Write(writer)
            writer.WriteByte(InterpMode.ordinal)
            writer.WriteByte(TangentMode.ordinal)
        }
        writer.WriteBytes(padding)
    }
}

class FMovieSceneFloatValue(reader: AssetBinaryReader?) : FMovieSceneValue<Float>(reader, reader?.ReadSingle() ?: 0f)

class FMovieSceneDoubleValue(reader: AssetBinaryReader?) : FMovieSceneValue<Double>(reader, reader?.ReadDouble() ?: 0.0)
