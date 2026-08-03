// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Core/Math/TPerQualityLevel.cs
package com.github.jpabscale.uasset4j.unrealtypes.objects.core.math

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter

class TPerQualityLevel<T> {
    var bCooked: Boolean
    var Default: T
    var PerQuality: MutableMap<Int, T>?

    constructor(_bCooked: Boolean, _default: T, perQuality: MutableMap<Int, T>?) {
        bCooked = _bCooked
        Default = _default
        PerQuality = perQuality
    }

    constructor(reader: AssetBinaryReader, valueReader: () -> T) {
        bCooked = reader.ReadBooleanInt()
        Default = valueReader()
        PerQuality = mutableMapOf()
        val numElements = reader.ReadInt32()
        for (i in 0 until numElements) {
            PerQuality!![reader.ReadInt32()] = valueReader()
        }
    }

    fun Write(writer: AssetBinaryWriter, valueWriter: (T) -> Unit): Int {
        val offset = writer.position
        writer.WriteBooleanInt(bCooked)
        valueWriter(Default)
        writer.WriteInt32(PerQuality?.size ?: 0)
        if (PerQuality != null) {
            for ((key, value) in PerQuality!!) {
                writer.WriteInt32(key)
                valueWriter(value)
            }
        }
        return writer.position - offset
    }
}
