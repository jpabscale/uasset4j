// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Slate/FontDataPropertyData.cs
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/SlateCore/FFontData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.slate

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
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
    var bIsCooked: Boolean = false

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

            if (reader.Asset!!.GetEngineVersion() >= EngineVersion.VER_UE4_20) {
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

            if (writer.Asset!!.GetEngineVersion() >= EngineVersion.VER_UE4_20) {
                writer.WriteInt32(SubFaceIndex)
            }
        }

        return writer.position - offset
    }
}

class FontDataPropertyData : PropertyData {
    var Value: FFontData?
        get() = GetObject<FFontData>()
        set(v) = SetObject(v)

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = FFontData(reader)
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        if (Value == null) Value = FFontData()
        return Value!!.Write(writer)
    }

    override fun CreateClone(): PropertyData = FontDataPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("FontData")
    }
}
