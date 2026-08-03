// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/FMetaData.cs
package com.github.jpabscale.uasset4j.unrealtypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.propertytypes.objects.FSoftObjectPath

/** Editor metadata for the package and its objects. */
class FMetaData {
    var ObjectMetaDataMap: LinkedHashMap<FSoftObjectPath, LinkedHashMap<FName, FString?>> = linkedMapOf()
    var RootMetaDataMap: LinkedHashMap<FName, FString?> = linkedMapOf()

    constructor()

    constructor(reader: AssetBinaryReader) {
        val objectMDCount = reader.ReadInt32()
        val rootMDCount = reader.ReadInt32()
        ObjectMetaDataMap = reader.ReadMap(
            objectMDCount,
            { FSoftObjectPath(reader) },
            { reader.ReadMap({ reader.ReadFName() }, { reader.ReadFString() }) },
        )
        RootMetaDataMap = reader.ReadMap(rootMDCount, { reader.ReadFName() }, { reader.ReadFString() })
    }

    fun Write(writer: AssetBinaryWriter) {
        writer.WriteInt32(ObjectMetaDataMap.size)
        writer.WriteInt32(RootMetaDataMap.size)
        for ((key, value) in ObjectMetaDataMap) {
            key.Write(writer)
            writer.WriteInt32(value.size)
            for ((name, v) in value) {
                writer.Write(name)
                writer.Write(v)
            }
        }

        for ((name, value) in RootMetaDataMap) {
            writer.Write(name)
            writer.Write(value)
        }
    }
}
