// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Core/Math/FVector4.cs
package com.github.jpabscale.uasset4j.unrealtypes.objects.core.math

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.StructAccessors
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5

class FVector4 {
    private var _x1: Float? = null
    private var _x2: Double = 0.0
    private var _y1: Float? = null
    private var _y2: Double = 0.0
    private var _z1: Float? = null
    private var _z2: Double = 0.0
    private var _w1: Float? = null
    private var _w2: Double = 0.0

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

    var Z: Double
        get() = _z1?.toDouble() ?: _z2
        set(value) {
            _z1 = null
            _z2 = value
        }

    val ZFloat: Float get() = _z1 ?: _z2.toFloat()

    var W: Double
        get() = _w1?.toDouble() ?: _w2
        set(value) {
            _w1 = null
            _w2 = value
        }

    val WFloat: Float get() = _w1 ?: _w2.toFloat()

    constructor(x: Double, y: Double, z: Double, w: Double) {
        _x1 = null; _y1 = null; _z1 = null; _w1 = null
        _x2 = x
        _y2 = y
        _z2 = z
        _w2 = w
    }

    constructor(x: Float, y: Float, z: Float, w: Float) {
        _x2 = 0.0; _y2 = 0.0; _z2 = 0.0; _w2 = 0.0
        _x1 = x
        _y1 = y
        _z1 = z
        _w1 = w
    }

    constructor(reader: AssetBinaryReader) {
        if (reader.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.LARGE_WORLD_COORDINATES) {
            _x1 = null; _y1 = null; _z1 = null; _w1 = null
            _x2 = reader.ReadDouble()
            _y2 = reader.ReadDouble()
            _z2 = reader.ReadDouble()
            _w2 = reader.ReadDouble()
        } else {
            _x2 = 0.0; _y2 = 0.0; _z2 = 0.0; _w2 = 0.0
            _x1 = reader.ReadSingle()
            _y1 = reader.ReadSingle()
            _z1 = reader.ReadSingle()
            _w1 = reader.ReadSingle()
        }
    }

    constructor() {
        _x1 = null; _y1 = null; _z1 = null; _w1 = null
        _x2 = 0.0; _y2 = 0.0; _z2 = 0.0; _w2 = 0.0
    }

    fun Write(writer: AssetBinaryWriter): Int {
        if (writer.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.LARGE_WORLD_COORDINATES) {
            writer.WriteDouble(_x2)
            writer.WriteDouble(_y2)
            writer.WriteDouble(_z2)
            writer.WriteDouble(_w2)
            return Double.SIZE_BYTES * 4
        } else {
            writer.WriteSingle(XFloat)
            writer.WriteSingle(YFloat)
            writer.WriteSingle(ZFloat)
            writer.WriteSingle(WFloat)
            return Float.SIZE_BYTES * 4
        }
    }

    override fun toString(): String = "($X, $Y, $Z, $W)"

    companion object {
        val accessors = StructAccessors(
            read = { r -> FVector4(r) },
            fromString = { d, a -> FromString(d, a) },
            write = { w, v -> v.Write(w) },
            defaultValue = { FVector4() },
        )

        fun FromString(d: Array<String>, asset: UAsset): FVector4 {
            val X = d[0].toDoubleOrNull() ?: 0.0
            val Y = d[1].toDoubleOrNull() ?: 0.0
            val Z = d[2].toDoubleOrNull() ?: 0.0
            val W = d[3].toDoubleOrNull() ?: 0.0
            return FVector4(X, Y, Z, W)
        }
    }
}
