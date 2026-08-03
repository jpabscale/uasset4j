// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Core/Math/FVector3f.cs
package com.github.jpabscale.uasset4j.unrealtypes.objects.core.math

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.StructAccessors

class FVector3f {
    var X: Float = 0f
    var Y: Float = 0f
    var Z: Float = 0f

    constructor(x: Float, y: Float, z: Float) {
        X = x
        Y = y
        Z = z
    }

    constructor(reader: AssetBinaryReader) {
        X = reader.ReadSingle()
        Y = reader.ReadSingle()
        Z = reader.ReadSingle()
    }

    constructor()

    fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteSingle(X)
        writer.WriteSingle(Y)
        writer.WriteSingle(Z)
        return Float.SIZE_BYTES * 3
    }

    fun clone(): FVector3f = FVector3f(X, Y, Z)

    override fun toString(): String = "($X, $Y, $Z)"

    companion object {
        val accessors = StructAccessors(
            read = { r -> FVector3f(r) },
            fromString = { d, a -> FromString(d, a) },
            write = { w, v -> v.Write(w) },
            defaultValue = { FVector3f() },
        )

        fun FromString(d: Array<String>, asset: UAsset): FVector3f {
            val X = d[0].toFloatOrNull() ?: 0f
            val Y = d[1].toFloatOrNull() ?: 0f
            val Z = d[2].toFloatOrNull() ?: 0f
            return FVector3f(X, Y, Z)
        }
    }
}
