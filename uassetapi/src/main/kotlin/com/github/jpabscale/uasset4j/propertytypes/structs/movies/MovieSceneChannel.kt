// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Movies/MovieSceneChannel.cs
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Engine/EngineEnums.cs (ERichCurve* enums)
package com.github.jpabscale.uasset4j.propertytypes.structs.movies

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.customversions.FFortniteMainBranchObjectVersion

enum class ERichCurveExtrapolation {
    RCCE_Cycle,
    RCCE_CycleWithOffset,
    RCCE_Oscillate,
    RCCE_Linear,
    RCCE_Constant,
    RCCE_None,
    RCCE_MAX,
}

enum class ERichCurveInterpMode {
    RCIM_Linear,
    RCIM_Constant,
    RCIM_Cubic,
    RCIM_None,
    RCIM_MAX,
}

enum class ERichCurveTangentWeightMode {
    RCTWM_WeightedNone,
    RCTWM_WeightedArrive,
    RCTWM_WeightedLeave,
    RCTWM_WeightedBoth,
    RCTWM_MAX,
}

enum class ERichCurveTangentMode {
    RCTM_Auto,
    RCTM_User,
    RCTM_Break,
    RCTM_None,
    RCTM_MAX,
}

open class FMovieSceneChannel<T>(
    reader: AssetBinaryReader?,
    valueReader: (() -> T)?,
) {
    var PreInfinityExtrap: ERichCurveExtrapolation = ERichCurveExtrapolation.RCCE_Constant
    var PostInfinityExtrap: ERichCurveExtrapolation = ERichCurveExtrapolation.RCCE_Constant
    var TimesStructLength: Int = 0
    var Times: Array<FFrameNumber> = emptyArray()
    var ValuesStructLength: Int = 0
    var Values: Array<FMovieSceneValue<T>> = emptyGenericArray()
    var DefaultValue: T? = null
    var bHasDefaultValue: Boolean = false
    var TickResolution: FFrameRate = FFrameRate(60000, 1)
    var bShowCurve: Boolean = false

    init {
        if (reader != null && valueReader != null) {
            PreInfinityExtrap = ERichCurveExtrapolation.entries[reader.ReadByte()]
            PostInfinityExtrap = ERichCurveExtrapolation.entries[reader.ReadByte()]

            TimesStructLength = reader.ReadInt32()
            Times = reader.ReadArray { FFrameNumber(reader) }

            ValuesStructLength = reader.ReadInt32()
            Values = newArray(reader.ReadInt32()) { FMovieSceneValue(reader, valueReader()) }

            DefaultValue = valueReader()
            bHasDefaultValue = reader.ReadBooleanInt()
            TickResolution = FFrameRate(reader)
            bShowCurve = (reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFortniteMainBranchObjectVersion")) >
                FFortniteMainBranchObjectVersion.SerializeFloatChannelShowCurve.value) && reader.ReadBooleanInt()
        }
    }

    constructor() : this(null, null)

    fun Write(writer: AssetBinaryWriter, valueWriter: (T) -> Unit) {
        writer.WriteByte(PreInfinityExtrap.ordinal)
        writer.WriteByte(PostInfinityExtrap.ordinal)

        writer.WriteInt32(TimesStructLength)
        writer.WriteInt32(Times.size)
        for (i in Times.indices) {
            Times[i].Write(writer)
        }

        writer.WriteInt32(ValuesStructLength)
        writer.WriteInt32(Values.size)
        for (i in Values.indices) {
            Values[i].Write(writer, valueWriter)
        }

        valueWriter(DefaultValue as T)
        writer.WriteBooleanInt(bHasDefaultValue)
        TickResolution.Write(writer)
        if (writer.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFortniteMainBranchObjectVersion")) >
            FFortniteMainBranchObjectVersion.SerializeFloatChannelShowCurve.value
        ) {
            writer.WriteBooleanInt(bShowCurve)
        }
    }
}

class FMovieSceneFloatChannel(reader: AssetBinaryReader?) :
    FMovieSceneChannel<Float>(reader, reader?.let { r -> ({ r.ReadSingle() }) })

class FMovieSceneDoubleChannel(reader: AssetBinaryReader?) :
    FMovieSceneChannel<Double>(reader, reader?.let { r -> ({ r.ReadDouble() }) })
