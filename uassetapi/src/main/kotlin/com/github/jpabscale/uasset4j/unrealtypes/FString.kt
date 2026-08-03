// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/FString.cs
package com.github.jpabscale.uasset4j.unrealtypes

import java.nio.charset.Charset

/**
 * Unreal string — consists of a string and an Encoding.
 *
 * C# `Encoding` is mapped to `Charset`: `Encoding.UTF8` -> `Charsets.UTF_8`,
 * `Encoding.Unicode` (UTF-16LE) -> `Charsets.UTF_16LE`. `null` Encoding means the Value was
 * constructed without one (the FString constructor auto-selects).
 */
class FString {
    var Value: String? = null
    var Encoding: Charset? = null

    /** Is this FString case preserving? */
    var IsCasePreserving: Boolean = true

    override fun toString(): String {
        if (Value == null) return NullCase
        return Value!!
    }

    override fun equals(other: Any?): Boolean {
        val fStr = other as? FString
        if (fStr != null) {
            return this.Value == fStr.Value && this.Encoding == fStr.Encoding
        }
        val str = other as? String
        if (str != null) {
            return this.Value == str
        }
        return false
    }

    override fun hashCode(): Int = Value?.hashCode() ?: 0

    fun clone(): FString = FString(Value, Encoding)

    constructor(Value: String?, Encoding: Charset? = null) {
        var enc = Encoding
        if (enc == null && Value != null) {
            enc = if (Value.toByteArray(Charsets.UTF_8).size == Value.length) Charsets.UTF_8 else Charsets.UTF_16LE
        }
        this.Value = Value
        this.Encoding = enc
    }

    constructor()

    companion object {
        const val NullCase = "null"

        fun FromString(Value: String?, Encoding: Charset? = null): FString? {
            if (Value == NullCase || Value == null) return null
            return FString(Value, Encoding)
        }
    }
}
