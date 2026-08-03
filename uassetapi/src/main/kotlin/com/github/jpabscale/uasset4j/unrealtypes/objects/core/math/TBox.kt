// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Core/Math/TBox.cs
package com.github.jpabscale.uasset4j.unrealtypes.objects.core.math

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter

class TBox<T> {
    var Min: T
    var Max: T
    var IsValid: Byte

    @Suppress("UNCHECKED_CAST")
    constructor() : this(null as T, null as T, 0)

    constructor(min: T, max: T, isValid: Byte) {
        Min = min
        Max = max
        IsValid = isValid
    }

    constructor(reader: AssetBinaryReader, valueReader: () -> T) {
        Min = valueReader()
        Max = valueReader()
        IsValid = reader.ReadByte().toByte()
    }

    fun Write(writer: AssetBinaryWriter, valueWriter: (T) -> Unit): Int {
        val offset = writer.position
        valueWriter(Min)
        valueWriter(Max)
        writer.WriteByte(IsValid.toInt() and 0xFF)
        return writer.position - offset
    }

    fun clone(): TBox<T> = TBox(Min, Max, IsValid)
}
