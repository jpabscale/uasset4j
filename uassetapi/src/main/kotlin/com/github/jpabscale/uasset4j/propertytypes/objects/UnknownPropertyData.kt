// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/UnknownPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class UnknownPropertyData : PropertyData {
    var Value: ByteArray?
        get() = GetObject<ByteArray>()
        set(v) = SetObject(v)

    var SerializingPropertyType: FString = CurrentPropertyType

    override val PropertyType: FString get() = CurrentPropertyType

    fun SetSerializingPropertyType(newType: FString) {
        SerializingPropertyType = newType
    }

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = reader.ReadBytes(leng1.toInt())
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        val v = Value ?: ByteArray(0)
        writer.WriteBytes(v)
        return v.size
    }

    override fun toString(): String = Value.toString()

    override fun HandleCloned(res: PropertyData) {
        val cloningProperty = res as UnknownPropertyData
        cloningProperty.SerializingPropertyType = SerializingPropertyType.clone()
    }

    override fun CreateClone(): PropertyData = UnknownPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("UnknownProperty")
    }
}
