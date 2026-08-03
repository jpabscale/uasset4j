// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/GameplayTagContainerPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class GameplayTagContainerPropertyData : PropertyData {
    var Value: Array<FName>?
        get() = GetObject()
        set(v) = SetObject(v)

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        val numEntries = reader.ReadInt32()
        Value = Array(numEntries) { reader.ReadFName() }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        if (Value == null) Value = emptyArray()
        writer.WriteInt32(Value!!.size)
        var totalSize = 4
        for (i in Value!!.indices) {
            writer.Write(Value!![i])
            totalSize += 4 * 2
        }
        return totalSize
    }

    override fun toString(): String {
        var oup = "("
        for (i in Value!!.indices) {
            oup += Value!![i].toString() + ", "
        }
        return oup.dropLast(2) + ")"
    }

    override fun CreateClone(): PropertyData = GameplayTagContainerPropertyData()

    override fun CloneInto(res: PropertyData) {
        super.CloneInto(res)
        val cloningProperty = res as GameplayTagContainerPropertyData

        if (this.Value != null) {
            val newData = arrayOfNulls<FName>(this.Value!!.size)
            for (i in this.Value!!.indices) {
                newData[i] = this.Value!![i].clone()
            }
            @Suppress("UNCHECKED_CAST")
            cloningProperty.Value = newData as Array<FName>
        } else {
            cloningProperty.Value = null
        }
    }

    constructor(name: FName?) : super(name) {
        Value = emptyArray()
    }

    constructor() : super() {
        Value = emptyArray()
    }

    companion object {
        private val CurrentPropertyType = FString("GameplayTagContainer")
    }
}
