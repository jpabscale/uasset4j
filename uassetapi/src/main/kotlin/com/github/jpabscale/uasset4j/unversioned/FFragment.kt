// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Unversioned/FFragment.cs
package com.github.jpabscale.uasset4j.unversioned

class FFragment {
    var SkipNum: Int = 0
    var ValueNum: Int = 0
    var bIsLast: Boolean = false
    var FirstNum: Int = -1
    val LastNum: Int get() = FirstNum + ValueNum - 1
    var bHasAnyZeroes: Boolean = false

    override fun toString(): String = "{$SkipNum,$ValueNum,$bHasAnyZeroes,$bIsLast}"

    fun Pack(): Int {
        if (SkipNum > SkipMax) throw IllegalStateException("Skip num $SkipNum is greater than maximum possible value $SkipMax")
        if (ValueNum > ValueMax) throw IllegalStateException("Value num $ValueNum is greater than maximum possible value $ValueMax")
        return (SkipNum and 0xFF) or (if (bHasAnyZeroes) HasZeroMask else 0) or ((ValueNum and 0xFF) shl ValueNumShift) or (if (bIsLast) IsLastMask else 0)
    }

    constructor()

    constructor(skipNum: Int, valueNum: Int, bIsLast: Boolean, bHasAnyZeroes: Boolean, firstNum: Int = -1) {
        SkipNum = skipNum
        ValueNum = valueNum
        this.bIsLast = bIsLast
        this.bHasAnyZeroes = bHasAnyZeroes
        this.FirstNum = firstNum
    }

    companion object {
        internal val SkipMax = 127
        internal val ValueMax = 127
        internal val SkipNumMask = 0x007f
        internal val HasZeroMask = 0x0080
        internal val ValueNumShift = 9
        internal val IsLastMask = 0x0100

        fun Unpack(Int: Int): FFragment {
            val fragment = FFragment()
            fragment.SkipNum = Int and SkipNumMask
            fragment.bHasAnyZeroes = (Int and HasZeroMask) != 0
            fragment.ValueNum = (Int ushr ValueNumShift) and 0xFF
            fragment.bIsLast = (Int and IsLastMask) != 0
            return fragment
        }

        fun GetFromBounds(LastNumBefore: Int, FirstNum: Int, LastNum: Int, hasAnyZeros: Boolean, isLast: Boolean): FFragment {
            val fragment = FFragment()
            fragment.SkipNum = FirstNum - LastNumBefore - 1
            fragment.ValueNum = LastNum - FirstNum + 1
            fragment.bHasAnyZeroes = hasAnyZeros
            fragment.bIsLast = isLast
            fragment.FirstNum = FirstNum
            return fragment
        }
    }
}
