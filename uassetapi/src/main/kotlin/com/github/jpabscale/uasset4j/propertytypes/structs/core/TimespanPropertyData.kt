// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Core/TimespanPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.core

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import kotlin.math.abs

class TimeSpan(private val ticksValue: Long) {
    val Ticks: Long get() = ticksValue

    override fun toString(): String {
        var neg = ticksValue < 0
        var rem = abs(ticksValue)
        val days = rem / TicksPerDay; rem %= TicksPerDay
        val hours = rem / TicksPerHour; rem %= TicksPerHour
        val minutes = rem / TicksPerMinute; rem %= TicksPerMinute
        val seconds = rem / TicksPerSecond; rem %= TicksPerSecond
        val frac = rem

        val sb = StringBuilder()
        if (neg) sb.append('-')
        if (days > 0) sb.append(days).append('.')
        sb.append("%02d:%02d:%02d".format(hours, minutes, seconds))
        if (frac > 0) sb.append('.').append("%07d".format(frac))
        return sb.toString()
    }

    companion object {
        private const val TicksPerSecond = 10000000L
        private const val TicksPerMinute = 60L * TicksPerSecond
        private const val TicksPerHour = 60L * TicksPerMinute
        private const val TicksPerDay = 24L * TicksPerHour

        fun Parse(value: String): TimeSpan {
            var str = value.trim()
            var neg = false
            if (str.startsWith("-")) {
                neg = true
                str = str.substring(1)
            }
            var days = 0L
            var timePart = str
            val colonIdx = str.indexOf(':')
            val dotIdx = str.indexOf('.')
            if (dotIdx >= 0 && (colonIdx < 0 || dotIdx < colonIdx)) {
                days = str.substring(0, dotIdx).toLong()
                timePart = str.substring(dotIdx + 1)
            }
            val parts = timePart.split(':')
            val hours = parts[0].toLong()
            val minutes = parts[1].toLong()
            var secStr = parts[2]
            var frac = 0L
            val secDot = secStr.indexOf('.')
            if (secDot >= 0) {
                frac = secStr.substring(secDot + 1).padEnd(7, '0').take(7).toLong()
                secStr = secStr.substring(0, secDot)
            }
            val seconds = secStr.toLong()
            var ticks = days * TicksPerDay + hours * TicksPerHour + minutes * TicksPerMinute + seconds * TicksPerSecond + frac
            if (neg) ticks = -ticks
            return TimeSpan(ticks)
        }
    }
}

class TimespanPropertyData : PropertyData {
    var Value: TimeSpan?
        get() = GetObject<TimeSpan>()
        set(v) = SetObject(v)

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        Value = TimeSpan(reader.ReadInt64())
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        writer.WriteInt64((Value ?: TimeSpan(0)).Ticks)
        return 8
    }

    override fun FromString(d: Array<String>, asset: UAsset) {
        Value = TimeSpan.Parse(d[0])
    }

    override fun toString(): String = Value.toString()

    override fun CreateClone(): PropertyData = TimespanPropertyData()

    override fun CloneInto(res: PropertyData) {
        super.CloneInto(res)
        val cloningProperty = res as TimespanPropertyData
        cloningProperty.Value = TimeSpan((this.Value ?: TimeSpan(0)).Ticks)
    }

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("Timespan")
    }
}
