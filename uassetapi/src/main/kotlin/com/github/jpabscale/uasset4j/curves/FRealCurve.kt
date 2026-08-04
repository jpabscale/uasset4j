// Reimplemented from CUE4Parse (Apache-2.0) — Copyright (c) FabianFG and contributors
// Source: CUE4Parse/UE4/Objects/Engine/Curves/RealCurve.cs
//
// NOTE (EXC-002): CUE4Parse's FRichCurveKey raw-archive constructor reads the six floats
// BEFORE the three mode bytes; uasset4j reads modes-first, which matches the actual on-disk
// bytes of every extracted curve asset (verified against StellarBlade UE4.26 UCurveFloat
// assets and the uasset4j/UAssetAPI byte-identical round-trip oracle). CUE4Parse reaches its
// struct types through the [StructFallback] property-tagged path for real assets, so its raw
// constructor order is not observed in practice. Keep uasset4j modes-first.
//@parity:on EXC-002
package com.github.jpabscale.uasset4j.curves

import com.github.jpabscale.uasset4j.unrealtypes.engineenums.ERichCurveInterpMode

/** Enumerates extrapolation options. */
enum class ERichCurveExtrapolation {
    /** Repeat the curve without an offset. */
    RCCE_Cycle,
    /** Repeat the curve with an offset relative to the first or last key's value. */
    RCCE_CycleWithOffset,
    /** Sinusoidally extrapolate. */
    RCCE_Oscillate,
    /** Use a linearly increasing value for extrapolation. */
    RCCE_Linear,
    /** Use a constant value for extrapolation. */
    RCCE_Constant,
    /** No extrapolation. */
    RCCE_None,
}

/**
 * A rich, editable float curve base. Mirrors CUE4Parse's abstract `FRealCurve`.
 */
abstract class FRealCurve {
    var DefaultValue: Float = Float.MAX_VALUE
    var PreInfinityExtrap: ERichCurveExtrapolation = ERichCurveExtrapolation.RCCE_Constant
    var PostInfinityExtrap: ERichCurveExtrapolation = ERichCurveExtrapolation.RCCE_Constant

    abstract fun RemapTimeValue(inTime: Float, cycleValueOffset: Float): Pair<Float, Float>

    /** Evaluates the curve at [inTime], returning the interpolated value. */
    abstract fun Eval(inTime: Float, inDefaultValue: Float = 0f): Float

    protected companion object {
        const val SMALL_NUMBER: Double = 1e-8

        /** Mirrors CUE4Parse's `FRealCurve.CycleTime`. */
        fun CycleTime(minTime: Float, maxTime: Float, inTime: Float, cycleCount: Int): Pair<Float, Int> {
            var t = inTime
            var cc = cycleCount
            val initTime = inTime
            val duration = maxTime - minTime

            if (inTime > maxTime) {
                cc = ((maxTime - inTime) / duration).toInt()
                t += duration * cc
            } else if (inTime < minTime) {
                cc = ((inTime - minTime) / duration).toInt()
                t -= duration * cc
            }

            if (t == maxTime && initTime < minTime) t = minTime
            if (t == minTime && initTime > maxTime) t = maxTime

            cc = Math.abs(cc)
            return t to cc
        }
    }
}

internal fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

internal fun cbrt(v: Double): Double = Math.cbrt(v)
//@parity:off EXC-002
