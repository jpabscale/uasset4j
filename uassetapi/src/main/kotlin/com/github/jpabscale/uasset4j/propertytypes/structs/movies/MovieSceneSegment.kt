// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Movies/MovieSceneSegment.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.movies

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.customversions.FSequencerObjectVersion
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FName

class FMovieSceneSegment {
    var RangeOld: TRange<Float>? = null
    var Range: TRange<FFrameNumber>? = null
    var ID: Int = 0
    var bAllowEmpty: Boolean = false
    var Impls: Array<StructPropertyData?> = emptyArray()

    constructor()

    constructor(reader: AssetBinaryReader) {
        if (reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FSequencerObjectVersion")) <
            FSequencerObjectVersion.FloatToIntConversion.value
        ) {
            RangeOld = TRange(reader) { reader.ReadSingle() }
        } else {
            Range = TRange(reader) { FFrameNumber(reader) }
        }

        if (reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FSequencerObjectVersion")) >
            FSequencerObjectVersion.EvaluationTree.value
        ) {
            ID = reader.ReadInt32()
            bAllowEmpty = reader.ReadBooleanInt()
        }

        val length = reader.ReadInt32()
        Impls = Array(length) {
            val data = StructPropertyData(FName.DefineDummy(reader.Asset, "Impls"), FName.DefineDummy(reader.Asset, "SectionEvaluationData"))
            data.Read(reader, false, 1, 0, PropertySerializationContext.StructFallback)
            data
        }
    }

    fun Write(writer: AssetBinaryWriter): Int {
        val offset = writer.position
        if (writer.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FSequencerObjectVersion")) <
            FSequencerObjectVersion.FloatToIntConversion.value
        ) {
            RangeOld!!.Write(writer) { writer.WriteSingle(it) }
        } else {
            Range!!.Write(writer) { it.Write(writer) }
        }

        if (writer.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FSequencerObjectVersion")) >
            FSequencerObjectVersion.EvaluationTree.value
        ) {
            writer.WriteInt32(ID)
            writer.WriteBooleanInt(bAllowEmpty)
        }

        writer.WriteInt32(Impls.size)
        for (i in Impls.indices) {
            Impls[i]?.Write(writer, false, PropertySerializationContext.StructFallback)
        }

        return writer.position - offset
    }
}
