// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/SlateCore/FFontData.cs
package com.github.jpabscale.uasset4j.unrealtypes.objects.slatecore

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.unrealtypes.EngineVersion
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.FString

enum class EFontHinting {
    Default,
    Auto,
    AutoLight,
    Monochrome,
    None,
}

enum class EFontLoadingPolicy {
    LazyLoad,
    Stream,
    Inline,
}

class FFontData {
    var LocalFontFaceAsset: FPackageIndex? = null
    var FontFilename: FString? = null
    var Hinting: EFontHinting = EFontHinting.Default
    var LoadingPolicy: EFontLoadingPolicy = EFontLoadingPolicy.LazyLoad
    var SubFaceIndex: Int = 0
    var bIsCooked: Boolean

    constructor() {
        bIsCooked = false
    }

    constructor(reader: AssetBinaryReader) {
        bIsCooked = reader.ReadBooleanInt()
        if (bIsCooked) {
            LocalFontFaceAsset = FPackageIndex(reader)

            if (LocalFontFaceAsset!!.Index == 0) {
                FontFilename = reader.ReadFString()
                Hinting = EFontHinting.entries[reader.ReadByte()]
                LoadingPolicy = EFontLoadingPolicy.entries[reader.ReadByte()]
            }

            if (reader.Asset!!.GetEngineVersion().ordinal >= EngineVersion.VER_UE4_20.ordinal) {
                SubFaceIndex = reader.ReadInt32()
            }
        }
    }

    fun Write(writer: AssetBinaryWriter): Int {
        val offset = writer.position

        writer.WriteBooleanInt(bIsCooked)
        if (bIsCooked) {
            writer.WriteInt32(LocalFontFaceAsset?.Index ?: 0)

            if (LocalFontFaceAsset?.Index == 0) {
                writer.Write(FontFilename)
                writer.WriteByte(Hinting.ordinal)
                writer.WriteByte(LoadingPolicy.ordinal)
            }

            if (writer.Asset!!.GetEngineVersion().ordinal >= EngineVersion.VER_UE4_20.ordinal) {
                writer.WriteInt32(SubFaceIndex)
            }
        }

        return writer.position - offset
    }
}
