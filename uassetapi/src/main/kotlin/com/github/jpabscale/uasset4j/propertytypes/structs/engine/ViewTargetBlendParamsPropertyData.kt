// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Engine/ViewTargetBlendParamsPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.engine

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

enum class ViewTargetBlendFunction {
    VTBlend_Linear,
    VTBlend_Cubic,
    VTBlend_EaseIn,
    VTBlend_EaseOut,
    VTBlend_EaseInOut,
    VTBlend_MAX,
}

class ViewTargetBlendParamsPropertyData : PropertyData {
    var BlendTime: Float = 0f
    var BlendFunction: ViewTargetBlendFunction = ViewTargetBlendFunction.VTBlend_Linear
    var BlendExp: Float = 0f
    var bLockOutgoing: Boolean = false

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        BlendTime = reader.ReadSingle()
        BlendFunction = ViewTargetBlendFunction.entries[reader.ReadByte()]
        BlendExp = reader.ReadSingle()
        bLockOutgoing = reader.ReadInt32() != 0
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        writer.WriteSingle(BlendTime)
        writer.WriteByte(BlendFunction.ordinal)
        writer.WriteSingle(BlendExp)
        writer.WriteBooleanInt(bLockOutgoing)
        return 4 * 2 + 1 + 4
    }

    override fun FromString(d: Array<String>, asset: UAsset) {
        d[0].toFloatOrNull()?.let { BlendTime = it }
        ViewTargetBlendFunction.entries.firstOrNull { it.name.equals(d[1], ignoreCase = true) }?.let { BlendFunction = it }
        d[2].toFloatOrNull()?.let { BlendExp = it }
        TryParseBool(d[3])?.let { bLockOutgoing = it }
    }

    override fun toString(): String {
        var oup = "("
        oup += "$BlendTime, "
        oup += "$BlendFunction, "
        oup += "$BlendExp, "
        oup += "$bLockOutgoing, "
        return oup.dropLast(2) + ")"
    }

    override fun CreateClone(): PropertyData = ViewTargetBlendParamsPropertyData()

    override fun CloneInto(res: PropertyData) {
        super.CloneInto(res)
        val cloningProperty = res as ViewTargetBlendParamsPropertyData
        cloningProperty.BlendTime = BlendTime
        cloningProperty.BlendFunction = BlendFunction
        cloningProperty.BlendExp = BlendExp
        cloningProperty.bLockOutgoing = bLockOutgoing
    }

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("ViewTargetBlendParams")

        private fun TryParseBool(s: String): Boolean? = when (s.trim().lowercase()) {
            "true" -> true
            "false" -> false
            else -> null
        }
    }
}
