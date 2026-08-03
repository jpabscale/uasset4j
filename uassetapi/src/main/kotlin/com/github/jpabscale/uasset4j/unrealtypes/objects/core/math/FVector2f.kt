// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Core/Math/FVector2f.cs
package com.github.jpabscale.uasset4j.unrealtypes.objects.core.math

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.StructAccessors

class FVector2f {
    var X: Float = 0f
    var Y: Float = 0f

    constructor(x: Float, y: Float) {
        X = x
        Y = y
    }

    constructor(reader: AssetBinaryReader) {
        X = reader.ReadSingle()
        Y = reader.ReadSingle()
    }

    constructor()

    fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteSingle(X)
        writer.WriteSingle(Y)
        return Float.SIZE_BYTES * 2
    }

    fun clone(): FVector2f = FVector2f(X, Y)

    override fun toString(): String = "($X, $Y)"

    companion object {
        val accessors = StructAccessors(
            read = { r -> FVector2f(r) },
            fromString = { d, a -> FromString(d, a) },
            write = { w, v -> v.Write(w) },
            defaultValue = { FVector2f() },
        )

        fun FromString(d: Array<String>, asset: UAsset): FVector2f {
            val X = d[0].toFloatOrNull() ?: 0f
            val Y = d[1].toFloatOrNull() ?: 0f
            return FVector2f(X, Y)
        }
    }
}
