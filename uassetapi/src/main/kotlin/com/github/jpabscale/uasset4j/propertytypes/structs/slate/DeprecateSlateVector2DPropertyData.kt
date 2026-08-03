// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Slate/DeprecateSlateVector2DPropertyData.cs
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Core/Math/FVector2f.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.slate

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.BasePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.StructAccessors
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class FVector2f {
    var X: Float = 0f
    var Y: Float = 0f

    constructor(x: Float, y: Float) {
        X = x
        Y = y
    }

    constructor() : this(0f, 0f)

    constructor(reader: AssetBinaryReader) {
        X = reader.ReadSingle()
        Y = reader.ReadSingle()
    }

    fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteSingle(X)
        writer.WriteSingle(Y)
        return 8
    }

    fun clone(): FVector2f = FVector2f(X, Y)

    override fun toString(): String = "($X, $Y)"

    companion object {
        fun Read(reader: AssetBinaryReader): FVector2f = FVector2f(reader)

        fun FromString(d: Array<String>, asset: UAsset): FVector2f =
            FVector2f(d[0].toFloatOrNull() ?: 0f, d[1].toFloatOrNull() ?: 0f)

        val accessors = StructAccessors<FVector2f>(
            read = { r -> FVector2f(r) },
            fromString = { d, a -> FromString(d, a) },
            write = { w, v -> v.Write(w) },
            defaultValue = { FVector2f(0f, 0f) },
        )
    }
}

class DeprecateSlateVector2DPropertyData() : BasePropertyData<FVector2f>(FVector2f.accessors) {
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = DeprecateSlateVector2DPropertyData()

    constructor(name: FName?) : this() {
        this.Name = name
    }

    companion object {
        private val CurrentPropertyType = FString("DeprecateSlateVector2D")
    }
}
