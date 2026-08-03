// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Engine/CoreUObject/CoreUObjectStructs.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.movies

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.StructAccessors

inline fun <reified T> newArray(size: Int, init: (Int) -> T): Array<T> =
    Array(size) { init(it) }

inline fun <reified T> emptyGenericArray(): Array<T> = emptyArray()

class FFrameNumber {
    var Value: Int = 0

    constructor(value: Int) {
        this.Value = value
    }

    constructor(reader: AssetBinaryReader?) {
        Value = reader?.ReadInt32() ?: 0
    }

    constructor()

    fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteInt32(Value)
        return 4
    }

    companion object {
        fun Read(reader: AssetBinaryReader): FFrameNumber = FFrameNumber(reader)

        fun FromString(d: Array<String>, asset: UAsset): FFrameNumber {
            val val_ = d[0].toIntOrNull()
            if (val_ != null) return FFrameNumber(val_)
            return FFrameNumber()
        }

        val accessors = StructAccessors<FFrameNumber>(
            read = { r -> FFrameNumber(r) },
            fromString = { d, a -> FromString(d, a) },
            write = { w, v -> v.Write(w) },
            defaultValue = { FFrameNumber() },
        )
    }
}

class FFrameRate {
    var Numerator: Int = 0
    var Denominator: Int = 0

    constructor()

    constructor(numerator: Int, denominator: Int) {
        Numerator = numerator
        Denominator = denominator
    }

    constructor(reader: AssetBinaryReader) {
        Numerator = reader.ReadInt32()
        Denominator = reader.ReadInt32()
    }

    fun Write(writer: AssetBinaryWriter) {
        writer.WriteInt32(Numerator)
        writer.WriteInt32(Denominator)
    }

    override fun toString(): String = "$Numerator/$Denominator"

    companion object {
        fun TryParse(s: String, result: com.github.jpabscale.uasset4j.util.Out<FFrameRate>): Boolean {
            result.value = FFrameRate()
            val parts = s.trim().split('/')
            if (parts.size != 2) return false
            val numer = parts[0].toIntOrNull() ?: return false
            val denom = parts[1].toIntOrNull() ?: return false
            result.value = FFrameRate(numer, denom)
            return true
        }
    }
}

enum class ERangeBoundTypes {
    Exclusive,
    Inclusive,
    Open,
}

class TRangeBound<T> {
    var Type: ERangeBoundTypes = ERangeBoundTypes.Exclusive
    var Value: T? = null

    constructor()

    constructor(type: ERangeBoundTypes, value: T) {
        Type = type
        Value = value
    }

    constructor(reader: AssetBinaryReader?, valueReader: () -> T) {
        Type = if (reader == null) ERangeBoundTypes.Exclusive else ERangeBoundTypes.entries[reader.ReadByte()]
        Value = valueReader()
    }

    fun Write(writer: AssetBinaryWriter, valueWriter: (T) -> Unit) {
        writer.WriteByte(Type.ordinal)
        valueWriter(Value as T)
    }
}

class TRange<T> {
    var LowerBound: TRangeBound<T>
    var UpperBound: TRangeBound<T>

    constructor(lowerBound: TRangeBound<T>, upperBound: TRangeBound<T>) {
        LowerBound = lowerBound
        UpperBound = upperBound
    }

    constructor(reader: AssetBinaryReader, valueReader: () -> T) : this(TRangeBound(reader, valueReader), TRangeBound(reader, valueReader))

    fun Write(writer: AssetBinaryWriter, valueWriter: (T) -> Unit) {
        LowerBound.Write(writer, valueWriter)
        UpperBound.Write(writer, valueWriter)
    }
}

enum class EInterpCurveMode {
    CIM_Linear,
    CIM_CurveAuto,
    CIM_Constant,
    CIM_CurveUser,
    CIM_CurveBreak,
    CIM_CurveAutoClamped,
    CIM_Unknown,
}

enum class EAxis {
    None,
    X,
    Y,
    Z,
}

class FFrameTime {
    var FrameNumber: FFrameNumber? = null
    var SubFrame: Float = 0F

    constructor()

    constructor(frameNumber: FFrameNumber, subFrame: Float) {
        FrameNumber = frameNumber
        SubFrame = subFrame
    }

    constructor(reader: AssetBinaryReader) {
        FrameNumber = FFrameNumber(reader)
        SubFrame = reader.ReadSingle()
    }

    fun Write(writer: AssetBinaryWriter) {
        FrameNumber!!.Write(writer)
        writer.WriteSingle(SubFrame)
    }
}

class FQualifiedFrameTime {
    var Time: FFrameTime? = null
    var Rate: FFrameRate? = null

    constructor()

    constructor(time: FFrameTime, rate: FFrameRate) {
        Time = time
        Rate = rate
    }

    constructor(reader: AssetBinaryReader) {
        Time = FFrameTime(reader)
        Rate = FFrameRate(reader)
    }

    fun Write(writer: AssetBinaryWriter) {
        Time!!.Write(writer)
        Rate!!.Write(writer)
    }
}

class FTimecode {
    var Hours: Int = 0
    var Minutes: Int = 0
    var Seconds: Int = 0
    var Frames: Int = 0
    var bDropFrameFormat: Boolean = false

    constructor()

    constructor(hours: Int, minutes: Int, seconds: Int, frames: Int, bDropFrameFormat: Boolean) {
        Hours = hours
        Minutes = minutes
        Seconds = seconds
        Frames = frames
        this.bDropFrameFormat = bDropFrameFormat
    }

    constructor(reader: AssetBinaryReader) {
        Hours = reader.ReadInt32()
        Minutes = reader.ReadInt32()
        Seconds = reader.ReadInt32()
        Frames = reader.ReadInt32()
        bDropFrameFormat = reader.ReadBooleanByte()
    }

    fun Write(writer: AssetBinaryWriter) {
        writer.WriteInt32(Hours)
        writer.WriteInt32(Minutes)
        writer.WriteInt32(Seconds)
        writer.WriteInt32(Frames)
        writer.WriteBooleanByte(bDropFrameFormat)
    }
}
