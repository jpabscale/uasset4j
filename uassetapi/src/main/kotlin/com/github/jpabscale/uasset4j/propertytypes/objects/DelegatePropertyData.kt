// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/DelegatePropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.FString

class FDelegate {
    var Object: FPackageIndex? = null
    var Delegate: FName? = null

    constructor(_object: FPackageIndex?, delegate: FName?) {
        Object = _object
        Delegate = delegate
    }

    constructor()

    constructor(reader: AssetBinaryReader) {
        Object = FPackageIndex(reader)
        Delegate = reader.ReadFName()
    }

    fun Write(writer: AssetBinaryWriter): Int {
        writer.XFERPTR(Object)
        val size = 4
        writer.Write(Delegate)
        return size + 8
    }
}

class DelegatePropertyData : PropertyData {
    var Value: FDelegate?
        get() = GetObject<FDelegate>()
        set(v) = SetObject(v)

    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        Value = FDelegate(reader)
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        var v = Value
        if (v == null) {
            v = FDelegate(FPackageIndex.FromRawIndex(0), null)
            Value = v
        }
        return v.Write(writer)
    }

    override fun toString(): String = "null"

    override fun FromString(d: Array<String>, asset: UAsset) {
    }

    override fun CloneInto(res: PropertyData) {
        super.CloneInto(res)
        val cloningProperty = res as DelegatePropertyData

        cloningProperty.Value = FDelegate(this.Value!!.Object, this.Value!!.Delegate)
    }

    override fun CreateClone(): PropertyData = DelegatePropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("DelegateProperty")
    }
}
