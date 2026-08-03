// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Core/Math/FIntVector2.cs
package com.github.jpabscale.uasset4j.unrealtypes.objects.core.math

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.StructAccessors

class FIntVector2 {
    var X: Int = 0
    var Y: Int = 0

    constructor(x: Int, y: Int) {
        X = x
        Y = y
    }

    constructor(reader: AssetBinaryReader) {
        X = reader.ReadInt32()
        Y = reader.ReadInt32()
    }

    constructor()

    fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteInt32(X)
        writer.WriteInt32(Y)
        return Int.SIZE_BYTES * 2
    }

    fun clone(): FIntVector2 = FIntVector2(X, Y)

    override fun toString(): String = "(" + X + ", " + Y + ")"

    companion object {
        val accessors = StructAccessors(
            read = { r -> FIntVector2(r) },
            fromString = { d, a -> FromString(d, a) },
            write = { w, v -> v.Write(w) },
            defaultValue = { FIntVector2() },
        )

        fun FromString(d: Array<String>, asset: UAsset): FIntVector2 {
            val X = d[0].toIntOrNull() ?: 0
            val Y = d[1].toIntOrNull() ?: 0
            return FIntVector2(X, Y)
        }
    }
}
