// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Engine/PerQualityProperties.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.engine

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.TPerQualityLevel

abstract class TPerQualityLevelPropertyData<T> : PropertyData {
    var Value: TPerQualityLevel<T>?
        get() = GetObject<TPerQualityLevel<T>>()
        set(v) = SetObject(v)

    constructor(name: FName?) : super(name)
    constructor() : super()
}

class PerQualityLevelFloatPropertyData : TPerQualityLevelPropertyData<Float> {
    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        Value = TPerQualityLevel(reader) { reader.ReadSingle() }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        return Value!!.Write(writer) { v -> writer.WriteSingle(v) }
    }

    override fun CreateClone(): PropertyData = PerQualityLevelFloatPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("PerQualityLevelFloat")
    }
}

class PerQualityLevelIntPropertyData : TPerQualityLevelPropertyData<Int> {
    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        Value = TPerQualityLevel(reader) { reader.ReadInt32() }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        return Value!!.Write(writer) { v -> writer.WriteInt32(v) }
    }

    override fun CreateClone(): PropertyData = PerQualityLevelIntPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("PerQualityLevelInt")
    }
}
