// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Core/Math/FLinearColor.cs
package com.github.jpabscale.uasset4j.unrealtypes.objects.core.math

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAPUtils
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.StructAccessors
import kotlin.math.floor
import kotlin.math.pow

class Color(val A: Int, val R: Int, val G: Int, val B: Int)

object LinearHelpers {
    fun Convert(color: FLinearColor): Color {
        var FloatR = UAPUtils.Clamp(color.R, 0.0f, 1.0f)
        var FloatG = UAPUtils.Clamp(color.G, 0.0f, 1.0f)
        var FloatB = UAPUtils.Clamp(color.B, 0.0f, 1.0f)
        var FloatA = UAPUtils.Clamp(color.A, 0.0f, 1.0f)

        FloatR = if (FloatR <= 0.0031308f) FloatR * 12.92f else ((FloatR.toDouble()).pow(1.0 / 2.4).toFloat() * 1.055f - 0.055f)
        FloatG = if (FloatG <= 0.0031308f) FloatG * 12.92f else ((FloatG.toDouble()).pow(1.0 / 2.4).toFloat() * 1.055f - 0.055f)
        FloatB = if (FloatB <= 0.0031308f) FloatB * 12.92f else ((FloatB.toDouble()).pow(1.0 / 2.4).toFloat() * 1.055f - 0.055f)

        return Color(
            floor(FloatA * 255.999f).toInt(),
            floor(FloatR * 255.999f).toInt(),
            floor(FloatG * 255.999f).toInt(),
            floor(FloatB * 255.999f).toInt(),
        )
    }
}

class FLinearColor {
    var R: Float = 0f
    var G: Float = 0f
    var B: Float = 0f
    var A: Float = 0f

    constructor(R: Float, G: Float, B: Float, A: Float) {
        this.R = R
        this.G = G
        this.B = B
        this.A = A
    }

    constructor(reader: AssetBinaryReader) {
        R = reader.ReadSingle()
        G = reader.ReadSingle()
        B = reader.ReadSingle()
        A = reader.ReadSingle()
    }

    constructor()

    fun clone(): FLinearColor = FLinearColor(R, G, B, A)

    fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteSingle(R)
        writer.WriteSingle(G)
        writer.WriteSingle(B)
        writer.WriteSingle(A)
        return Float.SIZE_BYTES * 4
    }

    override fun toString(): String = "(" + R + ", " + G + ", " + B + ", " + A + ")"

    companion object {
        val accessors = StructAccessors(
            read = { r -> FLinearColor(r) },
            fromString = { d, a -> FromString(d, a) },
            write = { w, v -> v.Write(w) },
            defaultValue = { FLinearColor() },
        )

        fun FromString(d: Array<String>, asset: UAsset): FLinearColor {
            val R = d[0].toFloatOrNull() ?: 0f
            val G = d[1].toFloatOrNull() ?: 0f
            val B = d[2].toFloatOrNull() ?: 0f
            val A = d[3].toFloatOrNull() ?: 0f
            return FLinearColor(R, G, B, A)
        }
    }
}
