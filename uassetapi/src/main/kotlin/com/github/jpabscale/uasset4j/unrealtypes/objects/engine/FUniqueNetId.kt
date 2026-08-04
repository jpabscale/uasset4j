// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Engine/GameFramework/UniqueNetIdReplPropertyData.cs
package com.github.jpabscale.uasset4j.unrealtypes.objects.engine

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.unrealtypes.EngineVersion
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import java.nio.charset.Charset

class FUniqueNetId {
    var Type: FName? = null
    var Contents: FString? = null

    constructor(Type: FName?, Contents: FString?) {
        this.Type = Type
        this.Contents = Contents
    }

    constructor()

    constructor(reader: AssetBinaryReader) {
        if (reader.ReadInt32() <= 0) return
        if (reader.Asset!!.GetEngineVersion() >= EngineVersion.VER_UE4_20) {
            Type = reader.ReadFName()
        }
        Contents = reader.ReadFString()
    }

    fun Write(writer: AssetBinaryWriter): Int {
        if (Type == null && Contents == null) {
            writer.WriteInt32(0)
            return 4
        }

        var size = 4
        if (writer.Asset!!.GetEngineVersion() >= EngineVersion.VER_UE4_20) {
            size += 4 * 2
        }

        val contents = Contents
        if (contents != null && contents.Value != null) {
            size += if (contents.Encoding == Charsets.UTF_16LE) (contents.Value!!.length + 1) * 2 else (contents.Value!!.length + 1)
        }

        writer.WriteInt32((size + 3) and -4)

        if (writer.Asset!!.GetEngineVersion() >= EngineVersion.VER_UE4_20) {
            writer.Write(Type!!)
        }
        writer.Write(Contents!!)

        return size + 4
    }
}
