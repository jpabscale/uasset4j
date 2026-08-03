// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Core/ColorPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.core

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class Color(private val argb: Int) {
    val A: Int get() = (argb ushr 24) and 0xFF
    val R: Int get() = (argb ushr 16) and 0xFF
    val G: Int get() = (argb ushr 8) and 0xFF
    val B: Int get() = argb and 0xFF

    fun ToArgb(): Int = argb

    override fun toString(): String = "Color [A=$A, R=$R, G=$G, B=$B]"

    companion object {
        fun FromArgb(argb: Int): Color = Color(argb)

        fun FromArgb(alpha: Int, red: Int, green: Int, blue: Int): Color =
            Color(((alpha and 0xFF) shl 24) or ((red and 0xFF) shl 16) or ((green and 0xFF) shl 8) or (blue and 0xFF))
    }
}

class ColorPropertyData : PropertyData {
    var Value: Color?
        get() = GetObject<Color>()
        set(v) = SetObject(v)

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        Value = Color.FromArgb(reader.ReadInt32())
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        writer.WriteInt32((Value ?: Color.FromArgb(0)).ToArgb())
        return 4
    }

    override fun toString(): String = Value.toString()

    override fun FromString(d: Array<String>, asset: UAsset) {
        val colorR = d[0].toIntOrNull() ?: return
        val colorG = d[1].toIntOrNull() ?: return
        val colorB = d[2].toIntOrNull() ?: return
        val colorA = d[3].toIntOrNull() ?: return
        Value = Color.FromArgb(colorA, colorR, colorG, colorB)
    }

    override fun CreateClone(): PropertyData = ColorPropertyData()

    override fun CloneInto(res: PropertyData) {
        super.CloneInto(res)
        val cloningProperty = res as ColorPropertyData
        cloningProperty.Value = Color.FromArgb((this.Value ?: Color.FromArgb(0)).ToArgb())
    }

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("Color")
    }
}
