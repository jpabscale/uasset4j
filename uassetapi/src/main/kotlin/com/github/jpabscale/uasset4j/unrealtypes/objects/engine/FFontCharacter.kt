// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Engine/FFontCharacter.cs
package com.github.jpabscale.uasset4j.unrealtypes.objects.engine

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter

class FFontCharacter {
    var StartU: Int = 0
    var StartV: Int = 0
    var USize: Int = 0
    var VSize: Int = 0
    var TextureIndex: Int = 0
    var VerticalOffset: Int = 0

    constructor()

    constructor(reader: AssetBinaryReader) {
        StartU = reader.ReadInt32()
        StartV = reader.ReadInt32()
        USize = reader.ReadInt32()
        VSize = reader.ReadInt32()
        TextureIndex = reader.ReadByte()
        VerticalOffset = reader.ReadInt32()
    }

    fun Write(writer: AssetBinaryWriter): Int {
        val offset = writer.position
        writer.WriteInt32(StartU)
        writer.WriteInt32(StartV)
        writer.WriteInt32(USize)
        writer.WriteInt32(VSize)
        writer.WriteByte(TextureIndex)
        writer.WriteInt32(VerticalOffset)
        return writer.position - offset
    }
}
