// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Core/Math/FVector2D.cs
package com.github.jpabscale.uasset4j.unrealtypes.objects.core.math

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.StructAccessors
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5

class FVector2D {
    private var _x1: Float? = null
    private var _x2: Double = 0.0
    private var _y1: Float? = null
    private var _y2: Double = 0.0

    var X: Double
        get() = _x1?.toDouble() ?: _x2
        set(value) {
            _x1 = null
            _x2 = value
        }

    val XFloat: Float get() = _x1 ?: _x2.toFloat()

    var Y: Double
        get() = _y1?.toDouble() ?: _y2
        set(value) {
            _y1 = null
            _y2 = value
        }

    val YFloat: Float get() = _y1 ?: _y2.toFloat()

    constructor(x: Double, y: Double) {
        _x1 = null; _y1 = null
        _x2 = x
        _y2 = y
    }

    constructor(x: Float, y: Float, z: Float) {
        _x2 = 0.0; _y2 = 0.0
        _x1 = x
        _y1 = y
    }

    constructor(reader: AssetBinaryReader) {
        if (reader.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.LARGE_WORLD_COORDINATES) {
            _x1 = null; _y1 = null
            _x2 = reader.ReadDouble()
            _y2 = reader.ReadDouble()
        } else {
            _x2 = 0.0; _y2 = 0.0
            _x1 = reader.ReadSingle()
            _y1 = reader.ReadSingle()
        }
    }

    constructor() {
        _x1 = null; _y1 = null
        _x2 = 0.0; _y2 = 0.0
    }

    fun Write(writer: AssetBinaryWriter): Int {
        if (writer.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.LARGE_WORLD_COORDINATES) {
            writer.WriteDouble(X)
            writer.WriteDouble(Y)
            return Double.SIZE_BYTES * 2
        } else {
            writer.WriteSingle(XFloat)
            writer.WriteSingle(YFloat)
            return Float.SIZE_BYTES * 2
        }
    }

    override fun toString(): String = "($X, $Y)"

    companion object {
        val accessors = StructAccessors(
            read = { r -> FVector2D(r) },
            fromString = { d, a -> FromString(d, a) },
            write = { w, v -> v.Write(w) },
            defaultValue = { FVector2D() },
        )

        fun FromString(d: Array<String>, asset: UAsset): FVector2D {
            val X = d[0].toDoubleOrNull() ?: 0.0
            val Y = d[1].toDoubleOrNull() ?: 0.0
            return FVector2D(X, Y)
        }
    }
}
