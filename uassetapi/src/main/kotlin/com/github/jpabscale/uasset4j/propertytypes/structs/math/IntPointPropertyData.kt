// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Math/IntPointPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.math

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class IntPointPropertyData : PropertyData {
    var Value: Array<Int>?
        get() = GetObject()
        set(v) = SetObject(v)

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = Array(2) { reader.ReadInt32() }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        var value = Value
        if (value == null) value = arrayOf(0, 0)
        for (i in 0 until 2) {
            writer.WriteInt32(value[i])
        }
        return Int.SIZE_BYTES * 2
    }

    override fun FromString(d: Array<String>, asset: UAsset) {
        Value = arrayOf(0, 0)
        d[0].toIntOrNull()?.let { Value!![0] = it }
        d[1].toIntOrNull()?.let { Value!![1] = it }
    }

    override fun toString(): String {
        var oup = "("
        for (i in 0 until Value!!.size) {
            oup += Value!![i].toString() + ", "
        }
        return oup.removeRange(oup.length - 2, oup.length) + ")"
    }

    override fun CreateClone(): PropertyData = IntPointPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("IntPoint")
    }
}
