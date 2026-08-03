// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Core/Math/FIntVector.cs
package com.github.jpabscale.uasset4j.unrealtypes.objects.core.math

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.StructAccessors

class FIntVector {
    var X: Int = 0
    var Y: Int = 0
    var Z: Int = 0

    constructor(x: Int, y: Int, z: Int) {
        X = x
        Y = y
        Z = z
    }

    constructor(reader: AssetBinaryReader) {
        X = reader.ReadInt32()
        Y = reader.ReadInt32()
        Z = reader.ReadInt32()
    }

    constructor()

    fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteInt32(X)
        writer.WriteInt32(Y)
        writer.WriteInt32(Z)
        return Int.SIZE_BYTES * 3
    }

    fun clone(): FIntVector = FIntVector(X, Y, Z)

    override fun toString(): String = "(" + X + ", " + Y + ", " + Z + ")"

    companion object {
        val accessors = StructAccessors(
            read = { r -> FIntVector(r) },
            fromString = { d, a -> FromString(d, a) },
            write = { w, v -> v.Write(w) },
            defaultValue = { FIntVector() },
        )

        fun FromString(d: Array<String>, asset: UAsset): FIntVector {
            val X = d[0].toIntOrNull() ?: 0
            val Y = d[1].toIntOrNull() ?: 0
            val Z = d[2].toIntOrNull() ?: 0
            return FIntVector(X, Y, Z)
        }
    }
}
