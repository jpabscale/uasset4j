// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/SoftObjectPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.FormatException
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.customversions.FFortniteMainBranchObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5

class AssetObjectPropertyData : PropertyData {
    var Value: FString?
        get() = GetObject<FString>()
        set(v) = SetObject(v)

    var ID: Long = 0

    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = reader.ReadFString()
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        return writer.Write(Value)
    }

    override fun toString(): String = "(" + Value + ", " + ID + ")"

    override fun FromString(d: Array<String>, asset: UAsset) {
        Value = FString.FromString(d[0])
    }

    override fun CreateClone(): PropertyData = AssetObjectPropertyData()

    override fun CloneInto(res: PropertyData) {
        super.CloneInto(res)
        (res as AssetObjectPropertyData).ID = ID
    }

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("AssetObjectProperty")
    }
}

class FTopLevelAssetPath {
    var PackageName: FName? = null
    var AssetName: FName? = null

    constructor(packageName: FName?, assetName: FName?) {
        PackageName = packageName
        AssetName = assetName
    }

    constructor()
}

class FSoftObjectPath {
    var AssetPath: FTopLevelAssetPath = FTopLevelAssetPath()
    var SubPathString: FString? = null

    constructor(packageName: FName?, assetName: FName?, subPathString: FString?) {
        AssetPath = FTopLevelAssetPath(packageName, assetName)
        SubPathString = subPathString
    }

    constructor(assetPath: FTopLevelAssetPath, subPathString: FString?) {
        AssetPath = assetPath
        SubPathString = subPathString
    }

    constructor()

    constructor(reader: AssetBinaryReader, allowIndex: Boolean = true) {
        val asset = reader.Asset
        val softObjectPathList = asset?.SoftObjectPathList
        if (allowIndex && softObjectPathList != null && softObjectPathList.size > 0) {
            val idx = reader.ReadInt32()
            val target = softObjectPathList[idx]
            this.AssetPath = target.AssetPath
            this.SubPathString = target.SubPathString
        } else {
            if (asset != null && asset.ObjectVersionUE5 >= ObjectVersionUE5.FSOFTOBJECTPATH_REMOVE_ASSET_PATH_FNAMES) {
                AssetPath = FTopLevelAssetPath(reader.ReadFName(), reader.ReadFName())
            } else if (asset != null && asset.ObjectVersion >= ObjectVersion.VER_UE4_ADDED_SOFT_OBJECT_PATH) {
                AssetPath = FTopLevelAssetPath(null, reader.ReadFName())
            } else {
                AssetPath = FTopLevelAssetPath(null, null)
            }
            val fortniteVersion = if (asset != null) {
                asset.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFortniteMainBranchObjectVersion"))
            } else {
                -1
            }
            SubPathString = if (fortniteVersion < FFortniteMainBranchObjectVersion.SoftObjectPathUtf8SubPaths.ordinal) {
                reader.ReadFString()
            } else {
                reader.ReadUtf8String()
            }
        }
    }

    fun Write(writer: AssetBinaryWriter, allowIndex: Boolean = true): Int {
        val asset = writer.Asset
        val softObjectPathList = asset?.SoftObjectPathList
        if (allowIndex && softObjectPathList != null && softObjectPathList.size > 0) {
            var idx = -1
            for (i in softObjectPathList.indices) {
                if (softObjectPathList[i] == this) {
                    idx = i
                    break
                }
            }
            if (idx < 0) throw FormatException("Failed to find AssetPath in SoftObjectPathList")
            writer.WriteInt32(idx)
            return 4
        }

        if (asset != null && asset.ObjectVersion < ObjectVersion.VER_UE4_ADDED_SOFT_OBJECT_PATH) {
            return writer.Write(SubPathString)
        }

        val offset = writer.position
        if (asset != null && asset.ObjectVersionUE5 >= ObjectVersionUE5.FSOFTOBJECTPATH_REMOVE_ASSET_PATH_FNAMES) {
            writer.Write(AssetPath.PackageName)
        }
        writer.Write(AssetPath.AssetName)

        val fortniteVersion = if (asset != null) {
            asset.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFortniteMainBranchObjectVersion"))
        } else {
            -1
        }
        if (fortniteVersion < FFortniteMainBranchObjectVersion.SoftObjectPathUtf8SubPaths.ordinal) {
            writer.Write(SubPathString)
        } else {
            writer.WriteUtf8String(SubPathString)
        }

        return writer.position - offset
    }

    override fun equals(other: Any?): Boolean {
        val otherSop = other as? FSoftObjectPath ?: return false
        return AssetPath.PackageName == otherSop.AssetPath.PackageName &&
            AssetPath.AssetName == otherSop.AssetPath.AssetName &&
            SubPathString == otherSop.SubPathString
    }

    override fun hashCode(): Int {
        var result = AssetPath.PackageName?.hashCode() ?: 0
        result = 31 * result + (AssetPath.AssetName?.hashCode() ?: 0)
        result = 31 * result + (SubPathString?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "(${AssetPath.PackageName}, ${AssetPath.AssetName}, ${SubPathString})"

    companion object {
        val accessors = StructAccessors(
            read = { r -> FSoftObjectPath(r) },
            fromString = { d, a -> FromString(d, a) },
            write = { w, v -> v.Write(w) },
            defaultValue = { FSoftObjectPath() },
        )

        fun Read(reader: AssetBinaryReader): FSoftObjectPath = FSoftObjectPath(reader)

        fun FromString(d: Array<String>, asset: UAsset): FSoftObjectPath {
            val one = FName.FromString(asset, d[0])
            val two = FName.FromString(asset, d[1])
            val three = if (d[2].isEmpty()) null else FString.FromString(d[2])
            return FSoftObjectPath(one, two, three)
        }
    }
}

class SoftObjectPropertyData : BasePropertyData<FSoftObjectPath> {
    override val PropertyType: FString get() = CurrentPropertyType
    override val HasCustomStructSerialization: Boolean get() = false

    override fun CreateClone(): PropertyData = SoftObjectPropertyData()

    constructor(name: FName?) : super(FSoftObjectPath.accessors) {
        Name = name
    }

    constructor() : super(FSoftObjectPath.accessors)

    companion object {
        private val CurrentPropertyType = FString("SoftObjectProperty")
    }
}
