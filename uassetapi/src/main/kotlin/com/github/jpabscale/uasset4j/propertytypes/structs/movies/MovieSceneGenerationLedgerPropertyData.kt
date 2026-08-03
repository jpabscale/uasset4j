// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Movies/MovieSceneGenerationLedgerPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.movies

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.FormatException
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FGuid
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class MovieSceneGenerationLedgerPropertyData : PropertyData {
    var TrackReferenceCounts: LinkedHashMap<StructPropertyData, Int> = LinkedHashMap()
    var TrackSignatureToTrackIdentifier: LinkedHashMap<FGuid, StructPropertyData> = LinkedHashMap()

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        val NumReferenceCounts = reader.ReadInt32()
        TrackReferenceCounts = LinkedHashMap()

        for (i in 0 until NumReferenceCounts) {
            val identifier = StructPropertyData(FName.DefineDummy(reader.Asset, "MovieSceneTrackIdentifier"), FName.DefineDummy(reader.Asset, "Generic"))
            identifier.Ancestry.Initialize(Ancestry, Name)
            identifier.Read(reader, false, 1, 0, PropertySerializationContext.StructFallback)
            TrackReferenceCounts[identifier] = reader.ReadInt32()
        }
        val SignatureToTrackIDs = reader.ReadInt32()
        TrackSignatureToTrackIdentifier = LinkedHashMap()
        for (i in 0 until SignatureToTrackIDs) {
            val guid = reader.ReadGuid()
            val counts = reader.ReadInt32()
            if (counts != 1) throw FormatException("Invalid TrackSignatureToTrackIdentifier count")
            val identifier = StructPropertyData(FName.DefineDummy(reader.Asset, "MovieSceneTrackIdentifier"), FName.DefineDummy(reader.Asset, "Generic"))
            identifier.Ancestry.Initialize(Ancestry, Name)
            identifier.Read(reader, false, 1, 0, PropertySerializationContext.StructFallback)
            TrackSignatureToTrackIdentifier[guid] = identifier
        }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        val offset = writer.position

        writer.WriteInt32(TrackReferenceCounts.size)
        for ((k, v) in TrackReferenceCounts) {
            k.Write(writer, false, PropertySerializationContext.StructFallback)
            writer.WriteInt32(v)
        }

        writer.WriteInt32(TrackSignatureToTrackIdentifier.size)
        for ((k, v) in TrackSignatureToTrackIdentifier) {
            writer.WriteGuid(k)
            writer.WriteInt32(1)
            v.Write(writer, false, PropertySerializationContext.StructFallback)
        }

        return writer.position - offset
    }

    override fun CreateClone(): PropertyData = MovieSceneGenerationLedgerPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("MovieSceneGenerationLedger")
    }
}
