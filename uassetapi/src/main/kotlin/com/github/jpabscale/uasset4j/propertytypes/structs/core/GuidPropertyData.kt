// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Core/GuidPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.core

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAPUtils
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.unrealtypes.FGuid
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class GuidPropertyData : PropertyData {
    var Value: FGuid?
        get() = GetObject<FGuid>()
        set(v) = SetObject(v)

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        Value = reader.ReadGuid()
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        writer.WriteGuid(Value ?: FGuid(0u, 0u, 0u, 0u))
        return 16
    }

    override fun toString(): String = UAPUtils.ConvertToString(Value ?: FGuid(0u, 0u, 0u, 0u))

    override fun FromString(d: Array<String>, asset: UAsset) {
        Value = UAPUtils.ConvertToGUID(d[0])
    }

    override fun CreateClone(): PropertyData = GuidPropertyData()

    override fun CloneInto(res: PropertyData) {
        super.CloneInto(res)
        val cloningProperty = res as GuidPropertyData
        cloningProperty.Value = FGuid.fromBytes((Value ?: FGuid(0u, 0u, 0u, 0u)).toByteArray())
    }

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("Guid")
    }
}
