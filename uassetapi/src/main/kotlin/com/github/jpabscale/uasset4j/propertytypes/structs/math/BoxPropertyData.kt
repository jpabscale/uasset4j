// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Math/BoxPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.math

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector
import com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector2D
import com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector2f
import com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.TBox

abstract class TBoxPropertyData<T> : PropertyData {
    var Value: TBox<T>?
        get() = GetObject()
        set(v) = SetObject(v)

    constructor(name: FName?) : super(name)
    constructor() : super()
}

class BoxPropertyData : TBoxPropertyData<FVector> {
    constructor(name: FName?) : super(name)
    constructor() : super()

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType
        
    override fun CreateClone(): PropertyData = BoxPropertyData()

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = TBox(reader) { FVector(reader) }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        val v = Value ?: TBox(FVector(), FVector(), 0)
        return v.Write(writer) { entry -> entry.Write(writer) }
    }

    companion object {
        private val CurrentPropertyType = FString("Box")
    }
}

class Box2fPropertyData : TBoxPropertyData<FVector2f> {
    constructor(name: FName?) : super(name)
    constructor() : super()

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType
        
    override fun CreateClone(): PropertyData = Box2fPropertyData()

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = TBox(reader) { FVector2f(reader) }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        val v = Value ?: TBox(FVector2f(), FVector2f(), 0)
        return v.Write(writer) { entry -> entry.Write(writer) }
    }

    companion object {
        private val CurrentPropertyType = FString("Box2f")
    }
}

class Box2DPropertyData : TBoxPropertyData<FVector2D> {
    constructor(name: FName?) : super(name)
    constructor() : super()

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType
        
    override fun CreateClone(): PropertyData = Box2DPropertyData()

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = TBox(reader) { FVector2D(reader) }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        val v = Value ?: TBox(FVector2D(), FVector2D(), 0)
        return v.Write(writer) { entry -> entry.Write(writer) }
    }

    companion object {
        private val CurrentPropertyType = FString("Box2D")
    }
}
