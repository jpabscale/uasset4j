// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Engine/CoreUObject/CoreUObjectStructs.cs
package com.github.jpabscale.uasset4j.unrealtypes.objects.engine

import com.github.jpabscale.uasset4j.util.Out

class FFrameRate {
    var Numerator: Int = 0
    var Denominator: Int = 0

    constructor()

    constructor(numerator: Int, denominator: Int) {
        Numerator = numerator
        Denominator = denominator
    }

    override fun toString(): String = "$Numerator/$Denominator"

    companion object {
        fun TryParse(s: String, result: Out<FFrameRate>): Boolean {
            result.value = null
            val parts = s.trim().split('/')

            if (parts.size != 2) return false
            val numer = parts[0].toIntOrNull() ?: return false
            val denom = parts[1].toIntOrNull() ?: return false

            result.value = FFrameRate(numer, denom)
            return true
        }
    }
}
