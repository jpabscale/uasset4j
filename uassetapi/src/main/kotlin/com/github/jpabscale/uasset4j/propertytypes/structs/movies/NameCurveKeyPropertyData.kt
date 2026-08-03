// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Movies/NameCurveKeyPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.movies

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.BasePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.StructAccessors
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class FNameCurveKey {
    var Time: Float = 0f
    var Value: FName? = null

    constructor(time: Float, value: FName?) {
        Time = time
        Value = value
    }

    constructor(reader: AssetBinaryReader) {
        Time = reader.ReadSingle()
        Value = reader.ReadFName()
    }

    fun Write(writer: AssetBinaryWriter): Int {
        val offset = writer.position

        writer.WriteSingle(Time)
        writer.Write(Value)

        return writer.position - offset
    }

    override fun toString(): String = "($Time, $Value)"

    companion object {
        fun Read(reader: AssetBinaryReader): FNameCurveKey = FNameCurveKey(reader)

        fun FromString(d: Array<String>, asset: UAsset): FNameCurveKey {
            val time = d[0].toFloatOrNull() ?: 0f
            val value = FName.FromString(asset, d[1])
            return FNameCurveKey(time, value)
        }

        val accessors = StructAccessors<FNameCurveKey>(
            read = { r -> FNameCurveKey(r) },
            fromString = { d, a -> FromString(d, a) },
            write = { w, v -> v.Write(w) },
            defaultValue = { FNameCurveKey(0f, null) },
        )
    }
}

class NameCurveKeyPropertyData() : BasePropertyData<FNameCurveKey>(FNameCurveKey.accessors) {
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = NameCurveKeyPropertyData()

    constructor(name: FName?) : this() {
        this.Name = name
    }

    companion object {
        private val CurrentPropertyType = FString("NameCurveKey")
    }
}
