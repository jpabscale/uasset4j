// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/FieldPathPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.FormatException
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.FString

class FFieldPath {
    var Path: Array<FName> = emptyArray()
    var ResolvedOwner: FPackageIndex = FPackageIndex.FromRawIndex(0)

    constructor(path: Array<FName>, resolvedOwner: FPackageIndex, numExports: Int = -1) {
        Path = path
        ResolvedOwner = resolvedOwner
        if (numExports > 0 && resolvedOwner.Index > numExports) {
            throw FormatException("Received nonsensical FFieldPath ResolvedOwner: " + resolvedOwner.Index)
        }
    }

    constructor()

    constructor(reader: AssetBinaryReader) {
        Path = reader.ReadArray { reader.ReadFName() }
        ResolvedOwner = FPackageIndex(reader.ReadInt32())
    }

    fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteInt32(Path.size)
        for (name in Path) {
            writer.Write(name)
        }
        writer.WriteInt32(ResolvedOwner.Index)
        return 4 * (2 + Path.size * 2)
    }

    companion object {
        fun Read(reader: AssetBinaryReader): FFieldPath = FFieldPath(reader)
    }
}

class FieldPathPropertyData : PropertyData {
    var Value: FFieldPath?
        get() = GetObject<FFieldPath>()
        set(v) = SetObject(v)

    override val PropertyType: FString get() = CurrentPropertyType
    override val HasCustomStructSerialization: Boolean get() = false
    override val DefaultValue: Any get() = FFieldPath()

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = FFieldPath.Read(reader)
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        if (Value == null) Value = FFieldPath()
        return Value!!.Write(writer)
    }

    override fun toString(): String = ""

    override fun FromString(d: Array<String>, asset: UAsset) {
        Value = FFieldPath()
    }

    override fun CreateClone(): PropertyData = FieldPathPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("FieldPathProperty")
    }
}
