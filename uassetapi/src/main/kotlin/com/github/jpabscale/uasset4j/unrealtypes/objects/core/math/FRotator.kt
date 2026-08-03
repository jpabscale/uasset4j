// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Core/Math/FRotator.cs
package com.github.jpabscale.uasset4j.unrealtypes.objects.core.math

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.StructAccessors
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5

class FRotator {
    private var _pitch1: Float? = null
    private var _pitch2: Double = 0.0
    private var _yaw1: Float? = null
    private var _yaw2: Double = 0.0
    private var _roll1: Float? = null
    private var _roll2: Double = 0.0

    var Pitch: Double
        get() = _pitch1?.toDouble() ?: _pitch2
        set(value) {
            _pitch1 = null
            _pitch2 = value
        }

    val PitchFloat: Float get() = _pitch1 ?: _pitch2.toFloat()

    var Yaw: Double
        get() = _yaw1?.toDouble() ?: _yaw2
        set(value) {
            _yaw1 = null
            _yaw2 = value
        }

    val YawFloat: Float get() = _yaw1 ?: _yaw2.toFloat()

    var Roll: Double
        get() = _roll1?.toDouble() ?: _roll2
        set(value) {
            _roll1 = null
            _roll2 = value
        }

    val RollFloat: Float get() = _roll1 ?: _roll2.toFloat()

    constructor(pitch: Double, yaw: Double, roll: Double) {
        _pitch1 = null; _yaw1 = null; _roll1 = null
        _pitch2 = pitch
        _yaw2 = yaw
        _roll2 = roll
    }

    constructor(pitch: Float, yaw: Float, roll: Float) {
        _pitch2 = 0.0; _yaw2 = 0.0; _roll2 = 0.0
        _pitch1 = pitch
        _yaw1 = yaw
        _roll1 = roll
    }

    constructor(reader: AssetBinaryReader) {
        if (reader.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.LARGE_WORLD_COORDINATES) {
            _pitch1 = null; _yaw1 = null; _roll1 = null
            _pitch2 = reader.ReadDouble()
            _yaw2 = reader.ReadDouble()
            _roll2 = reader.ReadDouble()
        } else {
            _pitch2 = 0.0; _yaw2 = 0.0; _roll2 = 0.0
            _pitch1 = reader.ReadSingle()
            _yaw1 = reader.ReadSingle()
            _roll1 = reader.ReadSingle()
        }
    }

    constructor() {
        _pitch1 = null; _yaw1 = null; _roll1 = null
        _pitch2 = 0.0; _yaw2 = 0.0; _roll2 = 0.0
    }

    fun Write(writer: AssetBinaryWriter): Int {
        if (writer.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.LARGE_WORLD_COORDINATES) {
            writer.WriteDouble(Pitch)
            writer.WriteDouble(Yaw)
            writer.WriteDouble(Roll)
            return Double.SIZE_BYTES * 3
        } else {
            writer.WriteSingle(PitchFloat)
            writer.WriteSingle(YawFloat)
            writer.WriteSingle(RollFloat)
            return Float.SIZE_BYTES * 3
        }
    }

    override fun toString(): String = "($Roll, $Pitch, $Yaw)"

    companion object {
        val accessors = StructAccessors(
            read = { r -> FRotator(r) },
            fromString = { d, a -> FromString(d, a) },
            write = { w, v -> v.Write(w) },
            defaultValue = { FRotator() },
        )

        fun FromString(d: Array<String>, asset: UAsset): FRotator {
            val Roll = d[0].toDoubleOrNull() ?: 0.0
            val Pitch = d[1].toDoubleOrNull() ?: 0.0
            val Yaw = d[2].toDoubleOrNull() ?: 0.0
            return FRotator(Pitch, Yaw, Roll)
        }
    }
}
