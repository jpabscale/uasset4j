// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/Utf8StrPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class Utf8StrPropertyData : PropertyData {
    var Value: FString?
        get() = GetObject<FString>()
        set(v) = SetObject(v)

    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = reader.ReadUtf8String()
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        val here = writer.position
        writer.WriteUtf8String(Value)
        return writer.position - here
    }

    override fun toString(): String = Value.toString()

    override fun FromString(d: Array<String>, asset: UAsset) {
        Value = FString.FromString(d[0], Charsets.UTF_8)
    }

    override fun CreateClone(): PropertyData = Utf8StrPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("Utf8StrProperty")
    }
}
