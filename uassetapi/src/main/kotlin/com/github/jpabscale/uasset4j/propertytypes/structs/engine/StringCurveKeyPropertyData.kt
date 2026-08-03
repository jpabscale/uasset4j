// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Engine/StringCurveKeyPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.engine

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.BasePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.StructAccessors
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class FStringCurveKey {
    var Time: Float
    var Value: FString?

    constructor(time: Float, value: FString?) {
        Time = time
        Value = value
    }

    constructor() {
        Time = 0f
        Value = null
    }

    constructor(reader: AssetBinaryReader) {
        Time = reader.ReadSingle()
        Value = reader.ReadFString()
    }

    fun Write(writer: AssetBinaryWriter): Int {
        val offset = writer.position

        writer.WriteSingle(Time)
        writer.Write(Value)

        return writer.position - offset
    }

    override fun toString(): String = "($Time, $Value)"

    companion object {
        val accessors = StructAccessors(
            read = { r -> FStringCurveKey(r) },
            fromString = { d, _ -> FStringCurveKey(d[0].toFloatOrNull() ?: 0f, FString.FromString(d[1])) },
            write = { w, v -> v.Write(w) },
            defaultValue = { FStringCurveKey() },
        )
    }
}

class StringCurveKeyPropertyData : BasePropertyData<FStringCurveKey> {
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = StringCurveKeyPropertyData()

    constructor(name: FName?) : super(FStringCurveKey.accessors, name)
    constructor() : super(FStringCurveKey.accessors)

    companion object {
        private val CurrentPropertyType = FString("StringCurveKey")
    }
}
