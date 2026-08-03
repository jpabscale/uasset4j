// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Movies/MovieScene.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.movies

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.customversions.FFortniteMainBranchObjectVersion
import com.github.jpabscale.uasset4j.customversions.FReleaseObjectVersion
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FGuid
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex

enum class ESectionEvaluationFlags {
    None,
    PreRoll,
    PostRoll,
    ForceKeepState,
    ForceRestoreState,
}

class FMovieSceneEvaluationKey {
    var SequenceID: Long = 0
    var TrackIdentifier: Long = 0
    var SectionIndex: Long = 0

    constructor(_SequenceID: Long, _TrackIdentifier: Long, _SectionIndex: Long) {
        SequenceID = _SequenceID
        TrackIdentifier = _TrackIdentifier
        SectionIndex = _SectionIndex
    }

    constructor(reader: AssetBinaryReader) {
        SequenceID = reader.ReadUInt32()
        TrackIdentifier = reader.ReadUInt32()
        SectionIndex = reader.ReadUInt32()
    }

    constructor()

    fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteUInt32(SequenceID)
        writer.WriteUInt32(TrackIdentifier)
        writer.WriteUInt32(SectionIndex)
        return 12
    }
}

class FMovieSceneSubSectionData {
    var Section: FPackageIndex? = null
    var ObjectBindingId: FGuid? = null
    var Flags: ESectionEvaluationFlags = ESectionEvaluationFlags.None

    constructor(section: FPackageIndex?, objectBindingId: FGuid?, flags: ESectionEvaluationFlags) {
        Section = section
        ObjectBindingId = objectBindingId
        Flags = flags
    }

    constructor(reader: AssetBinaryReader) {
        Section = FPackageIndex(reader)
        ObjectBindingId = reader.ReadGuid()
        Flags = ESectionEvaluationFlags.entries[reader.ReadByte()]
    }

    fun Write(writer: AssetBinaryWriter): Int {
        Section!!.Write(writer)
        writer.WriteGuid(ObjectBindingId!!)
        writer.WriteByte(Flags.ordinal)
        return 21
    }
}

class FEntityAndMetaDataIndex {
    var EntityIndex: Int = 0
    var MetaDataIndex: Int = 0

    constructor(entityIndex: Int, metaDataIndex: Int) {
        EntityIndex = entityIndex
        MetaDataIndex = metaDataIndex
    }

    constructor(reader: AssetBinaryReader) {
        EntityIndex = reader.ReadInt32()
        MetaDataIndex = reader.ReadInt32()
    }

    fun Write(writer: AssetBinaryWriter) {
        writer.WriteInt32(EntityIndex)
        writer.WriteInt32(MetaDataIndex)
    }
}

class FMovieSceneSubSequenceTreeEntry {
    var SequenceID: Long = 0
    var Flags: ESectionEvaluationFlags = ESectionEvaluationFlags.None
    var RootToSequenceWarpCounter: StructPropertyData? = null

    constructor(sequenceID: Long, flags: Byte, `_struct`: StructPropertyData? = null) {
        SequenceID = sequenceID
        Flags = ESectionEvaluationFlags.entries[flags.toInt()]
        RootToSequenceWarpCounter = `_struct`
    }

    constructor(reader: AssetBinaryReader) {
        SequenceID = reader.ReadUInt32()
        Flags = ESectionEvaluationFlags.entries[reader.ReadByte()]
        if (reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FReleaseObjectVersion")) >=
            FReleaseObjectVersion.AddedSubSequenceEntryWarpCounter.value ||
            reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFortniteMainBranchObjectVersion")) >=
            FFortniteMainBranchObjectVersion.AddedSubSequenceEntryWarpCounter.value
        ) {
            val data = StructPropertyData(FName.DefineDummy(reader.Asset, "RootToSequenceWarpCounter"), FName.DefineDummy(reader.Asset, "MovieSceneWarpCounter"))
            data.Read(reader, false, 1, 0, PropertySerializationContext.StructFallback)
            RootToSequenceWarpCounter = data
        }
    }

    fun Write(writer: AssetBinaryWriter) {
        writer.WriteUInt32(SequenceID)
        writer.WriteByte(Flags.ordinal)
        if (writer.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FReleaseObjectVersion")) >=
            FReleaseObjectVersion.AddedSubSequenceEntryWarpCounter.value ||
            writer.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFortniteMainBranchObjectVersion")) >=
            FFortniteMainBranchObjectVersion.AddedSubSequenceEntryWarpCounter.value
        ) {
            RootToSequenceWarpCounter?.Write(writer, false, PropertySerializationContext.StructFallback)
        }
    }
}

class FMovieSceneSubSectionFieldData(reader: AssetBinaryReader) {
    var Field: TMovieSceneEvaluationTree<FMovieSceneSubSectionData> = TMovieSceneEvaluationTree(reader) { FMovieSceneSubSectionData(reader) }

    fun Write(writer: AssetBinaryWriter): Int {
        val offset = writer.position

        Field.Write(writer) { it.Write(writer) }

        return writer.position - offset
    }
}

class FMovieSceneEvaluationFieldEntityTree(reader: AssetBinaryReader) {
    var SerializedData: TMovieSceneEvaluationTree<FEntityAndMetaDataIndex> = TMovieSceneEvaluationTree(reader) { FEntityAndMetaDataIndex(reader) }

    fun Write(writer: AssetBinaryWriter): Int {
        val offset = writer.position

        SerializedData.Write(writer) { it.Write(writer) }

        return writer.position - offset
    }
}

class FMovieSceneSubSequenceTree(reader: AssetBinaryReader) {
    var Data: TMovieSceneEvaluationTree<FMovieSceneSubSequenceTreeEntry> = TMovieSceneEvaluationTree(reader) { FMovieSceneSubSequenceTreeEntry(reader) }

    fun Write(writer: AssetBinaryWriter): Int {
        val offset = writer.position

        Data.Write(writer) { it.Write(writer) }

        return writer.position - offset
    }
}

class FSectionEvaluationDataTree {
    var Tree: TMovieSceneEvaluationTree<StructPropertyData>? = null

    constructor(reader: AssetBinaryReader?) {
        if (reader != null) {
            Tree = TMovieSceneEvaluationTree(reader) { ReadTree(reader) }
        }
    }

    private fun ReadTree(reader: AssetBinaryReader): StructPropertyData {
        val data = StructPropertyData(FName.DefineDummy(reader.Asset, "Tree"), FName.DefineDummy(reader.Asset, "SectionEvaluationDataTree"))
        data.Read(reader, false, 1, 0, PropertySerializationContext.StructFallback)
        return data
    }

    fun Write(writer: AssetBinaryWriter): Int {
        val offset = writer.position
        Tree?.Write(writer) { entry -> WriteTree(writer, entry) }
        return writer.position - offset
    }

    private fun WriteTree(writer: AssetBinaryWriter, data: StructPropertyData?) {
        if (data != null) {
            data.StructType = FName.DefineDummy(writer.Asset, "SectionEvaluationDataTree")
            data.Write(writer, false, PropertySerializationContext.StructFallback)
        }
    }
}

class FMovieSceneTrackFieldData(reader: AssetBinaryReader) {
    var Field: TMovieSceneEvaluationTree<Long> = TMovieSceneEvaluationTree(reader) { reader.ReadUInt32() }

    fun Write(writer: AssetBinaryWriter): Int {
        val offset = writer.position

        Field.Write(writer) { writer.WriteUInt32(it) }

        return writer.position - offset
    }
}
