// Reimplemented from CUE4Parse (Apache-2.0) — Copyright (c) FabianFG and contributors
// Source: CUE4Parse/UE4/Objects/Engine/Curves/RichCurve.cs
//
// Wire-order note (EXC-002): see FRealCurve.kt — uasset4j keeps the modes-first FRichCurveKey
// layout that matches the real on-disk bytes and the UAssetAPI oracle.
//@parity:on EXC-002
package com.github.jpabscale.uasset4j.curves

import com.github.jpabscale.uasset4j.unrealtypes.engineenums.ERichCurveInterpMode
import com.github.jpabscale.uasset4j.unrealtypes.engineenums.ERichCurveTangentMode
import com.github.jpabscale.uasset4j.unrealtypes.engineenums.ERichCurveTangentWeightMode
import com.github.jpabscale.uasset4j.unrealtypes.objects.engine.FRichCurveKey

/**
 * A rich, editable float curve container. Mirrors CUE4Parse's `FRichCurve`.
 */
class FRichCurve : FRealCurve() {
    var Keys: MutableList<FRichCurveKey> = mutableListOf()

    companion object {
        /**
         * Builds an [FRichCurve] from a property-serialized `RichCurve` struct property
         * (`StructPropertyData`), mirroring CUE4Parse's `FRichCurve(FStructFallback)`.
         */
        fun fromStruct(data: com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData): FRichCurve {
            val curve = FRichCurve()
            FRealCurveCommon.populate(data, curve)
            curve.Keys.addAll(keysFromStruct(data))
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

    private fun EvalForTwoKeys(key1: FRichCurveKey, key2: FRichCurveKey, inTime: Float): Float {
        val diff = key2.Time - key1.Time

        if (diff > 0f && key1.InterpMode != ERichCurveInterpMode.RCIM_Constant) {
            val alpha = (inTime - key1.Time) / diff
            val p0 = key1.Value
            val p3 = key2.Value

            if (key1.InterpMode == ERichCurveInterpMode.RCIM_Linear) {
                return lerp(p0, p3, alpha)
            }

            if (IsItNotWeighted(key1, key2)) {
                val oneThird = 1f / 3f
                val p1 = p0 + key1.LeaveTangent * diff * oneThird
                val p2 = p3 - key2.ArriveTangent * diff * oneThird
                return BezierInterp(p0, p1, p2, p3, alpha)
            }

            return WeightedEvalForTwoKeys(key1, key2, inTime)
        }

        return key1.Value
    }

    private fun WeightedEvalForTwoKeys(key1: FRichCurveKey, key2: FRichCurveKey, inTime: Float): Float {
        val diff = key2.Time - key1.Time
        val alpha = (inTime - key1.Time) / diff
        val p0 = key1.Value
        val p3 = key2.Value
        val oneThird = 1f / 3f
        val time1 = key1.Time
        val time2 = key2.Time
        val x = time2 - time1
        var angle = Math.atan(key1.LeaveTangent.toDouble())
        var cosAngle = Math.cos(angle)
        var sinAngle = Math.sin(angle)

        var leaveWeight: Double = key1.LeaveTangentWeight.toDouble()
        if (key1.TangentWeightMode == ERichCurveTangentWeightMode.RCTWM_WeightedNone ||
            key1.TangentWeightMode == ERichCurveTangentWeightMode.RCTWM_WeightedArrive
        ) {
            val leaveTangentNormalized = key1.LeaveTangent
            val y = leaveTangentNormalized * x
            leaveWeight = Math.sqrt((x * x + y * y).toDouble()) * oneThird
        }

        val key1TanX = cosAngle * leaveWeight + time1
        val key1TanY = sinAngle * leaveWeight + key1.Value

        angle = Math.atan(key2.ArriveTangent.toDouble())
        cosAngle = Math.cos(angle)
        sinAngle = Math.cos(angle)

        var arriveWeight: Double = key2.ArriveTangentWeight.toDouble()
        if (key2.TangentWeightMode == ERichCurveTangentWeightMode.RCTWM_WeightedNone ||
            key2.TangentWeightMode == ERichCurveTangentWeightMode.RCTWM_WeightedLeave
        ) {
            val arriveTangentNormalized = key2.ArriveTangent
            val y = arriveTangentNormalized * x
            arriveWeight = Math.sqrt((x * x + y * y).toDouble()) * oneThird
        }

        val key2TanX = -cosAngle * arriveWeight + time2
        val key2TanY = -sinAngle * arriveWeight + key2.Value

        val rangeX = time2 - time1
        val dx1 = key1TanX - time1
        val dx2 = key2TanX - time1
        val normalizedX1 = dx1 / rangeX
        val normalizedX2 = dx2 / rangeX

        val results = DoubleArray(3)
        val coeff = BezierToPower(0.0, normalizedX1, normalizedX2, 1.0)
        coeff[0] -= alpha

        val numResults = CubicCurve2D.SolveCubic(coeff, results)
        val newInterp: Float
        if (numResults == 1) {
            newInterp = results[0].toFloat()
        } else {
            var best = Float.MIN_VALUE
            for (result in results) {
                if (result >= 0.0 && result <= 1.0) {
                    if (best == Float.MIN_VALUE || result > best) {
                        best = result.toFloat()
                    }
                }
            }
            if (best == Float.MIN_VALUE) best = 0f
            return BezierInterp(p0, key1TanY.toFloat(), key2TanY.toFloat(), p3, best)
        }

        return BezierInterp(p0, key1TanY.toFloat(), key2TanY.toFloat(), p3, newInterp)
    }

    private fun BezierToPower(a1: Double, b1: Double, c1: Double, d1: Double): DoubleArray {
        val o = DoubleArray(4)
        val a = b1 - a1
        val b = c1 - b1
        val c = d1 - c1
        val d = b - a
        o[3] = c - b - d
        o[2] = 3.0 * d
        o[1] = 3.0 * a
        o[0] = a1
        return o
    }

    private fun BezierInterp(p0: Float, p1: Float, p2: Float, p3: Float, alpha: Float): Float {
        val p01 = lerp(p0, p1, alpha)
        val p12 = lerp(p1, p2, alpha)
        val p23 = lerp(p2, p3, alpha)
        val p012 = lerp(p01, p12, alpha)
        val p123 = lerp(p12, p23, alpha)
        return lerp(p012, p123, alpha)
    }

    private fun IsItNotWeighted(key1: FRichCurveKey, key2: FRichCurveKey): Boolean {
        return (key1.TangentWeightMode == ERichCurveTangentWeightMode.RCTWM_WeightedNone ||
            key1.TangentWeightMode == ERichCurveTangentWeightMode.RCTWM_WeightedArrive) &&
            (key2.TangentWeightMode == ERichCurveTangentWeightMode.RCTWM_WeightedNone ||
                key2.TangentWeightMode == ERichCurveTangentWeightMode.RCTWM_WeightedLeave)
    }

    private object CubicCurve2D {
        fun SolveCubic(eqn: DoubleArray, res: DoubleArray): Int {
            val d = eqn[3]
            if (d == 0.0) return 0
            val a = eqn[2] / d
            val b = eqn[1] / d
            val c = eqn[0] / d
            var p = b - a * a / 3.0
            var q = a * (2.0 * a * a - 9.0 * b) / 27.0 + c
            val p3 = p * p * p
            val dq = q * q
            val discrim = dq / 4.0 + p3 / 27.0
            if (Math.abs(discrim) > 1e-12) {
                if (discrim > 0.0) {
                    p = -p
                    val u = Math.sqrt(discrim)
                    var root = -q / 2.0 + u
                    root = cbrt(root)
                    var root2 = -q / 2.0 - u
                    root2 = cbrt(root2)
                    res[0] = root + root2 - a / 3.0
                    return 1
                } else {
                    val angle = Math.acos(Math.sqrt(-discrim) * 27.0 / (2.0 * Math.sqrt(Math.pow(-p, 3.0))))
                    val cx = 2.0 * Math.sqrt(-p)
                    res[0] = cx * Math.cos(angle / 3.0) - a / 3.0
                    res[1] = cx * Math.cos((angle + 2.0 * Math.PI) / 3.0) - a / 3.0
                    res[2] = cx * Math.cos((angle + 4.0 * Math.PI) / 3.0) - a / 3.0
                    return 3
                }
            } else {
                if (dq == 0.0) {
                    res[0] = -cbrt(c)
                    return 1
                } else {
                    val u = 1.5 * q / p * Math.sqrt(-p / 3.0)
                    val angle = Math.acos(u)
                    val cx = 2.0 * Math.sqrt(-p)
                    res[0] = cx * Math.cos(angle / 3.0) - a / 3.0
                    res[1] = cx * Math.cos((angle + 2.0 * Math.PI) / 3.0) - a / 3.0
                    res[2] = cx * Math.cos((angle + 4.0 * Math.PI) / 3.0) - a / 3.0
                    return 3
                }
            }
        }
    }
}
//@parity:off EXC-002
