// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/SoftObjectPathPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.FSoftObjectPath
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion

open class SoftObjectPathPropertyData : PropertyData {
    var Path: FString? = null

    var Value: FSoftObjectPath?
        get() = GetObject<FSoftObjectPath>()
        set(v) = SetObject(v)

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        if (reader.Asset!!.ObjectVersion < ObjectVersion.VER_UE4_ADDED_SOFT_OBJECT_PATH) {
            Path = reader.ReadFString()
        } else {
            Value = FSoftObjectPath(reader)
        }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        val here = writer.position

        if (writer.Asset!!.ObjectVersion < ObjectVersion.VER_UE4_ADDED_SOFT_OBJECT_PATH) {
            writer.Write(Path)
        } else {
            Value!!.Write(writer)
        }

        return writer.position - here
    }

    override fun toString(): String = "(" + Value?.AssetPath?.PackageName + ", " + Value?.AssetPath?.AssetName + ", " + Value?.SubPathString + ")"

    override fun FromString(d: Array<String>, asset: UAsset) {
        if (asset.ObjectVersion < ObjectVersion.VER_UE4_ADDED_SOFT_OBJECT_PATH) {
            Path = FString.FromString(d[0])
        } else {
            val one = FName.FromString(asset, d[0])
            val two = FName.FromString(asset, d[1])
            val three = if (d[2].isEmpty()) null else FString.FromString(d[2])
            Value = FSoftObjectPath(one, two, three)
        }
    }

    override fun CreateClone(): PropertyData = SoftObjectPathPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("SoftObjectPath")
    }
}

class SoftClassPathPropertyData : SoftObjectPathPropertyData {
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = SoftClassPathPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("SoftClassPath")
    }
}

class SoftAssetPathPropertyData : SoftObjectPathPropertyData {
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = SoftAssetPathPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("SoftAssetPath")
    }
}

class StringAssetReferencePropertyData : SoftObjectPathPropertyData {
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = StringAssetReferencePropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("StringAssetReference")
    }
}

class StringClassReferencePropertyData : SoftObjectPathPropertyData {
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = StringClassReferencePropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("StringClassReference")
    }
}
