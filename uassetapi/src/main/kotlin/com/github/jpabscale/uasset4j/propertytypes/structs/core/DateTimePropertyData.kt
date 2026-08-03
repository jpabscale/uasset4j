// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Core/DateTimePropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.core

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class DateTime(private val ticksValue: Long) {
    val Ticks: Long get() = ticksValue

    override fun toString(): String {
        val dt = toLocalDateTime()
        val hour = dt.hour
        val minute = dt.minute
        val second = dt.second
        val month = dt.monthValue
        val day = dt.dayOfMonth
        val year = dt.year
        val amPm = if (hour >= 12) "PM" else "AM"
        val hour12 = if (hour % 12 == 0) 12 else hour % 12
        return "%d/%d/%d %d:%02d:%02d %s".format(month, day, year, hour12, minute, second, amPm)
    }

    private fun toLocalDateTime(): java.time.LocalDateTime {
        val ms = (ticksValue - UnixEpochTicks) / TicksPerMillisecond
        return java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(ms), java.time.ZoneOffset.UTC)
    }

    companion object {
        private const val UnixEpochTicks = 621355968000000000L
        private const val TicksPerMillisecond = 10000L

        fun Parse(value: String): DateTime {
            val s = value.trim()
            var ldt = try {
                java.time.LocalDateTime.parse(s)
            } catch (_: Exception) {
                null
            }
            if (ldt == null) {
                ldt = try {
                    java.time.LocalDateTime.parse(s, dateTimeFormatter)
                } catch (_: Exception) {
                    null
                }
            }
            if (ldt == null) {
                val d = try {
                    java.time.LocalDate.parse(s)
                } catch (_: Exception) {
                    null
                }
                if (d != null) ldt = d.atStartOfDay()
            }
            if (ldt == null) throw IllegalArgumentException("DateTime parse failed: $s")
            val ms = ldt.toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
            return DateTime(UnixEpochTicks + ms * TicksPerMillisecond)
        }

        private val dateTimeFormatter = java.time.format.DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a")
    }
}

class DateTimePropertyData : PropertyData {
    var Value: DateTime?
        get() = GetObject<DateTime>()
        set(v) = SetObject(v)

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        Value = DateTime(reader.ReadInt64())
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        writer.WriteInt64((Value ?: DateTime(0)).Ticks)
        return 8
    }

    override fun FromString(d: Array<String>, asset: UAsset) {
        Value = DateTime.Parse(d[0])
    }

    override fun toString(): String = Value.toString()

    override fun CreateClone(): PropertyData = DateTimePropertyData()

    override fun CloneInto(res: PropertyData) {
        super.CloneInto(res)
        val cloningProperty = res as DateTimePropertyData
        cloningProperty.Value = DateTime((this.Value ?: DateTime(0)).Ticks)
    }

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("DateTime")
    }
}
