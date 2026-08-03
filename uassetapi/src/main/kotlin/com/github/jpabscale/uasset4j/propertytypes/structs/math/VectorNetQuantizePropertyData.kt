// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Math/VectorNetQuantizePropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.math

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.FormatException
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5

open class VectorNetQuantizePropertyData : StructPropertyData {
    constructor(name: FName?, forcedType: FName?) : super(name, forcedType) {
        Value!!.add(VectorPropertyData(name))
    }

    constructor(name: FName?) : super(name) {
        Value!!.add(VectorPropertyData(name))
    }

    constructor() : super()

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = VectorNetQuantizePropertyData()

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        Value = mutableListOf()
        if (reader.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.LARGE_WORLD_COORDINATES) {
            val data = VectorPropertyData(Name)
            data.Read(reader, includeHeader, leng1, leng2, serializationContext)
            Value!!.add(data)
        } else {
            StructType = FName.DefineDummy(reader.Asset, PropertyType)
            super.Read(reader, includeHeader, 1, leng2, PropertySerializationContext.StructFallback)
        }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (writer.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.LARGE_WORLD_COORDINATES) {
            var value = Value
            if (value == null) {
                value = mutableListOf()
                value.add(VectorPropertyData(Name))
                Value = value
            }

            if (value.size == 1 && value[0] is VectorPropertyData) {
                return value[0].Write(writer, includeHeader, serializationContext)
            }
            throw FormatException("$PropertyType must have a VectorPropertyData child")
        } else {
            StructType = FName.DefineDummy(writer.Asset, PropertyType)
            return super.Write(writer, includeHeader, PropertySerializationContext.StructFallback)
        }
    }

    companion object {
        private val CurrentPropertyType = FString("Vector_NetQuantize")
    }
}

class VectorNetQuantizeNormalPropertyData : VectorNetQuantizePropertyData {
    constructor(name: FName?) : super(name)
    constructor() : super()

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = VectorNetQuantizeNormalPropertyData()

    companion object {
        private val CurrentPropertyType = FString("Vector_NetQuantizeNormal")
    }
}

class VectorNetQuantize10PropertyData : VectorNetQuantizePropertyData {
    constructor(name: FName?) : super(name)
    constructor() : super()

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = VectorNetQuantize10PropertyData()

    companion object {
        private val CurrentPropertyType = FString("Vector_NetQuantize10")
    }
}

class VectorNetQuantize100PropertyData : VectorNetQuantizePropertyData {
    constructor(name: FName?) : super(name)
    constructor() : super()

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = VectorNetQuantize100PropertyData()

    companion object {
        private val CurrentPropertyType = FString("Vector_NetQuantize100")
    }
}
