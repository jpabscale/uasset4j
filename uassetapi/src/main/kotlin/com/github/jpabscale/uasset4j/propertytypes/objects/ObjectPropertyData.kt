// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/ObjectPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.Import
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.exporttypes.Export
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.FString

open class ObjectPropertyData : PropertyData {
    var Value: FPackageIndex?
        get() = GetObject<FPackageIndex>()
        set(v) = SetObject(v)

    override val PropertyType: FString get() = CurrentPropertyType
    override val DefaultValue: Any get() = FPackageIndex.FromRawIndex(0)

    fun IsImport(): Boolean = Value!!.IsImport()

    fun IsExport(): Boolean = Value!!.IsExport()

    fun IsNull(): Boolean = Value!!.IsNull()

    fun ToImport(asset: UAsset): Import? = Value!!.ToImport(asset)

    fun ToExport(asset: UAsset): Export? = Value!!.ToExport(asset)

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = FPackageIndex(reader)
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        writer.WriteInt32(Value?.Index ?: 0)
        return 4
    }

    override fun toString(): String = Value?.toString() ?: "null"

    override fun FromString(d: Array<String>, asset: UAsset) {
        val res = d[0].toIntOrNull()
        if (res != null) {
            Value = FPackageIndex(res)
            return
        }
    }

    override fun CreateClone(): PropertyData = ObjectPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("ObjectProperty")
    }
}
