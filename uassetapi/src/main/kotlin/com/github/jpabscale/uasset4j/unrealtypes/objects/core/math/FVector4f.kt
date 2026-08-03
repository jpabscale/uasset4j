// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Core/Math/FVector4f.cs
package com.github.jpabscale.uasset4j.unrealtypes.objects.core.math

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.StructAccessors

class FVector4f {
    var X: Float = 0f
    var Y: Float = 0f
    var Z: Float = 0f
    var W: Float = 0f

    constructor(x: Float, y: Float, z: Float, w: Float) {
        X = x
        Y = y
        Z = z
        W = w
    }

    constructor(reader: AssetBinaryReader) {
        X = reader.ReadSingle()
        Y = reader.ReadSingle()
        Z = reader.ReadSingle()
        W = reader.ReadSingle()
    }

    constructor()

    fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteSingle(X)
        writer.WriteSingle(Y)
        writer.WriteSingle(Z)
        writer.WriteSingle(W)
        return Float.SIZE_BYTES * 4
    }

    fun clone(): FVector4f = FVector4f(X, Y, Z, W)

    override fun toString(): String = "($X, $Y, $Z, $W)"

    companion object {
        val accessors = StructAccessors(
            read = { r -> FVector4f(r) },
            fromString = { d, a -> FromString(d, a) },
            write = { w, v -> v.Write(w) },
            defaultValue = { FVector4f() },
        )

        fun FromString(d: Array<String>, asset: UAsset): FVector4f {
            val X = d[0].toFloatOrNull() ?: 0f
            val Y = d[1].toFloatOrNull() ?: 0f
            val Z = d[2].toFloatOrNull() ?: 0f
            val W = d[3].toFloatOrNull() ?: 0f
            return FVector4f(X, Y, Z, W)
        }
    }
}
