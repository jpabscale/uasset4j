// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/FieldTypes/EArrayDim.cs
package com.github.jpabscale.uasset4j.fieldtypes

class EArrayDim(val value: Int) {
    val name: String? get() = names[value]
    val ordinal: Int get() = value

    override fun toString(): String = name ?: value.toString()

    companion object {
        val NotAnArray = EArrayDim(0)
        val TArray = EArrayDim(1)
        val CArray = EArrayDim(2)

        private val names: Map<Int, String> = mapOf(
            0 to "NotAnArray",
            1 to "TArray",
            2 to "CArray",
        )

        fun fromValue(v: Int): EArrayDim = EArrayDim(v)

        fun fromName(nm: String): EArrayDim = EArrayDim(names.entries.first { it.value == nm }.key)
    }
}
