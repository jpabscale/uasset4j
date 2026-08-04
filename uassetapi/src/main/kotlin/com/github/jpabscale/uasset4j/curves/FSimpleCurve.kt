// Reimplemented from CUE4Parse (Apache-2.0) — Copyright (c) FabianFG and contributors
// Source: CUE4Parse/UE4/Objects/Engine/Curves/SimpleCurve.cs
//@parity:on EXC-002
package com.github.jpabscale.uasset4j.curves

import com.github.jpabscale.uasset4j.unrealtypes.engineenums.ERichCurveInterpMode

/** One key in a simple (2-float) curve. */
class FSimpleCurveKey {
    var Time: Float = 0f
    var Value: Float = 0f

    constructor()

    constructor(time: Float, value: Float) {
        Time = time
        Value = value
    }
}

/**
 * A simple curve (row type of `UCurveTable` in `ECurveTableMode.SimpleCurves` mode).
 * Mirrors CUE4Parse's `FSimpleCurve`.
 */
class FSimpleCurve : FRealCurve() {
    var InterpMode: ERichCurveInterpMode = ERichCurveInterpMode.RCIM_Linear
    var Keys: MutableList<FSimpleCurveKey> = mutableListOf()

    companion object {
        /**
         * Builds an [FSimpleCurve] from a property-serialized `SimpleCurve` struct property,
         * mirroring CUE4Parse's `FSimpleCurve(FStructFallback)`.
         */
        fun fromStruct(data: com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData): FSimpleCurve {
            val curve = FSimpleCurve()
            FRealCurveCommon.populate(data, curve)
            val value = data.Value
            if (value != null) {
                for (p in value) {
                    when (p.Name?.Value?.Value) {
                        "InterpMode" -> p.asByteOrEnumEnum()?.let { curve.InterpMode = ERichCurveInterpMode.entries[it] }
                        "Keys" -> {
                            if (p is com.github.jpabscale.uasset4j.propertytypes.objects.ArrayPropertyData) {
                                val list = p.Value
                                if (list != null) {
                                    for (k in list) {
                                        val s = k as? com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData
                                        if (s != null) {
                                            curve.Keys.add(fromStructSimpleCurveKey(s))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return curve
        }
    }

    override fun RemapTimeValue(inTime: Float, cycleValueOffset: Float): Pair<Float, Float> {
        var t = inTime
        var offset = cycleValueOffset
        val numKeys = Keys.size
        if (numKeys < 2) return t to offset

        if (t <= Keys[0].Time) {
            if (PreInfinityExtrap != ERichCurveExtrapolation.RCCE_Linear && PreInfinityExtrap != ERichCurveExtrapolation.RCCE_Constant) {
                val minTime = Keys[0].Time
                val maxTime = Keys[numKeys - 1].Time
                val (newT, cycleCount) = CycleTime(minTime, maxTime, t, 0)
                t = newT
                when (PreInfinityExtrap) {
                    ERichCurveExtrapolation.RCCE_CycleWithOffset -> {
                        val dv = Keys[0].Value - Keys[numKeys - 1].Value
                        offset = dv * cycleCount
                    }
                    ERichCurveExtrapolation.RCCE_Oscillate -> {
                        if (cycleCount % 2 == 1) t = minTime + (maxTime - t)
                    }
                    else -> {}
                }
            }
        } else if (t >= Keys[numKeys - 1].Time) {
            if (PostInfinityExtrap != ERichCurveExtrapolation.RCCE_Linear && PostInfinityExtrap != ERichCurveExtrapolation.RCCE_Constant) {
                val minTime = Keys[0].Time
                val maxTime = Keys[numKeys - 1].Time
                val (newT, cycleCount) = CycleTime(minTime, maxTime, t, 0)
                t = newT
                when (PostInfinityExtrap) {
                    ERichCurveExtrapolation.RCCE_CycleWithOffset -> {
                        val dv = Keys[numKeys - 1].Value - Keys[0].Value
                        offset = dv * cycleCount
                    }
                    ERichCurveExtrapolation.RCCE_Oscillate -> {
                        if (cycleCount % 2 == 1) t = minTime + (maxTime - t)
                    }
                    else -> {}
                }
            }
        }
        return t to offset
    }

    override fun Eval(inTime: Float, inDefaultValue: Float): Float {
        var time = inTime
        val (remapped, cycleValueOffset) = RemapTimeValue(time, 0f)
        time = remapped

        val numKeys = Keys.size
        var interpVal = if (DefaultValue == Float.MAX_VALUE) inDefaultValue else DefaultValue

        if (numKeys == 0) {
            // fall through
        } else if (numKeys < 2 || time <= Keys[0].Time) {
            if (PreInfinityExtrap == ERichCurveExtrapolation.RCCE_Linear && numKeys > 1) {
                val dt = Keys[1].Time - Keys[0].Time
                if (Math.abs(dt) <= SMALL_NUMBER) {
                    interpVal = Keys[0].Value
                } else {
                    val dv = Keys[1].Value - Keys[0].Value
                    val slope = dv / dt
                    interpVal = slope * (time - Keys[0].Time) + Keys[0].Value
                }
            } else {
                interpVal = Keys[0].Value
            }
        } else if (time < Keys[numKeys - 1].Time) {
            var first = 1
            var last = numKeys - 1
            var count = last - first
            while (count > 0) {
                val step = count / 2
                val middle = first + step
                if (time >= Keys[middle].Time) {
                    first = middle + 1
                    count -= step + 1
                } else {
                    count = step
                }
            }
            interpVal = EvalForTwoKeys(Keys[first - 1], Keys[first], time)
        } else {
            if (PostInfinityExtrap == ERichCurveExtrapolation.RCCE_Linear) {
                val dt = Keys[numKeys - 2].Time - Keys[numKeys - 1].Time
                if (Math.abs(dt) <= SMALL_NUMBER) {
                    interpVal = Keys[numKeys - 1].Value
                } else {
                    val dv = Keys[numKeys - 2].Value - Keys[numKeys - 1].Value
                    val slope = dv / dt
                    interpVal = slope * (time - Keys[numKeys - 1].Time) + Keys[numKeys - 1].Value
                }
            } else {
                interpVal = Keys[numKeys - 1].Value
            }
        }

        return interpVal + cycleValueOffset
    }

    private fun EvalForTwoKeys(key1: FSimpleCurveKey, key2: FSimpleCurveKey, inTime: Float): Float {
        val diff = key2.Time - key1.Time
        if (diff > 0f && InterpMode != ERichCurveInterpMode.RCIM_Constant) {
            val alpha = (inTime - key1.Time) / diff
            val p0 = key1.Value
            val p3 = key2.Value
            return lerp(p0, p3, alpha)
        }
        return key1.Value
    }
}
//@parity:off EXC-002
