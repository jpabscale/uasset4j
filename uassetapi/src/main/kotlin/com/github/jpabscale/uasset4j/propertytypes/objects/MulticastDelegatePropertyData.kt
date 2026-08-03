// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/MulticastDelegatePropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

open class MulticastDelegatePropertyData : PropertyData {
    var Value: List<FDelegate>?
        get() = GetObject<List<FDelegate>>()
        set(v) = SetObject(v)

    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        Value = reader.ReadArray { FDelegate(reader) }.toList()
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        var v = Value
        if (v == null) {
            v = emptyList()
            Value = v
        }
        writer.WriteInt32(v.size)
        var size = 4
        for (i in v.indices) {
            size += v[i].Write(writer)
        }
        return size
    }

    override fun toString(): String {
        val oup = StringBuilder("(")
        val v = Value!!
        for (i in v.indices) {
            oup.append("(").append(v[i].Object!!.Index).append(", ").append(v[i].Delegate?.Value?.Value).append("), ")
        }
        return oup.substring(0, oup.length - 2) + ")"
    }

    override fun FromString(d: Array<String>, asset: UAsset) {
    }

    override fun CloneInto(res: PropertyData) {
        super.CloneInto(res)
        val cloningProperty = res as MulticastDelegatePropertyData

        val v = this.Value
        if (v != null) {
            val newData = mutableListOf<FDelegate>()
            for (i in v.indices) {
                newData.add(FDelegate(v[i].Object, v[i].Delegate?.clone()))
            }
            cloningProperty.Value = newData
        } else {
            cloningProperty.Value = null
        }
    }

    override fun CreateClone(): PropertyData = MulticastDelegatePropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("MulticastDelegateProperty")
    }
}

class MulticastSparseDelegatePropertyData : MulticastDelegatePropertyData {
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = MulticastSparseDelegatePropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("MulticastSparseDelegateProperty")
    }
}

class MulticastInlineDelegatePropertyData : MulticastDelegatePropertyData {
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = MulticastInlineDelegatePropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("MulticastInlineDelegateProperty")
    }
}
