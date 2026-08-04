// Reimplemented from CUE4Parse (Apache-2.0) — Copyright (c) FabianFG and contributors
// Source: CUE4Parse/UE4/Objects/Engine/Curves/UCurveLinearColor.cs
//@parity:on EXC-002
package com.github.jpabscale.uasset4j.curves

import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData
import com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FLinearColor
import kotlin.math.pow

/**
 * A linear-color curve asset (`UCurveLinearColor`), holding four `FRichCurve`s (R/G/B/A).
 * Mirrors CUE4Parse's `UCurveLinearColor` including the HSV-adjusted evaluation.
 */
class UCurveLinearColor {
    var FloatCurves: Array<FRichCurve> = arrayOf(FRichCurve(), FRichCurve(), FRichCurve(), FRichCurve())

    var AdjustBrightness: Float = 0f
    var AdjustBrightnessCurve: Float = 0f
    var AdjustVibrance: Float = 0f
    var AdjustSaturation: Float = 0f
    var AdjustHue: Float = 0f
    var AdjustMinAlpha: Float = 0f
    var AdjustMaxAlpha: Float = 0f

    constructor()

    constructor(properties: MutableList<PropertyData>) {
        for (p in properties) {
            val name = p.Name?.Value?.Value
            when (name) {
                "AdjustBrightness" -> AdjustBrightness = p.asFloat() ?: 0f
                "AdjustBrightnessCurve" -> AdjustBrightnessCurve = p.asFloat() ?: 0f
                "AdjustVibrance" -> AdjustVibrance = p.asFloat() ?: 0f
                "AdjustSaturation" -> AdjustSaturation = p.asFloat() ?: 0f
                "AdjustHue" -> AdjustHue = p.asFloat() ?: 0f
                "AdjustMinAlpha" -> AdjustMinAlpha = p.asFloat() ?: 0f
                "AdjustMaxAlpha" -> AdjustMaxAlpha = p.asFloat() ?: 0f
                "FloatCurves" -> {
                    val value = (p as? StructPropertyData)?.Value
                    if (value != null) {
                        val curves = mutableListOf<FRichCurve>()
                        for (element in value) {
                            if (element is StructPropertyData) curves.add(FRichCurve.fromStruct(element))
                        }
                        if (curves.isNotEmpty()) FloatCurves = curves.toTypedArray()
                    }
                }
            }
        }
    }

    fun GetUnadjustedLinearColorValue(inTime: Float): FLinearColor {
        val r = FloatCurves.getOrElse(0) { FRichCurve() }.Eval(inTime)
        val g = FloatCurves.getOrElse(1) { FRichCurve() }.Eval(inTime)
        val b = FloatCurves.getOrElse(2) { FRichCurve() }.Eval(inTime)
        val a = if (FloatCurves.getOrElse(3) { FRichCurve() }.Keys.size == 0) 1.0f
        else FloatCurves[3].Eval(inTime)
        return FLinearColor(r, g, b, a)
    }

    fun GetLinearColorValue(inTime: Float): FLinearColor {
        val originalColor = GetUnadjustedLinearColorValue(inTime)

        val bShouldClampValue = originalColor.R <= 1.0f && originalColor.G <= 1.0f && originalColor.B <= 1.0f

        val hsvColor = LinearRGBToHSV(originalColor)
        var pixelHue = hsvColor.R
        var pixelSaturation = hsvColor.G
        var pixelValue = hsvColor.B

        pixelValue *= AdjustBrightness

        if (!isNearlyEqual(AdjustBrightnessCurve, 1.0f) && AdjustBrightnessCurve != 0.0f) {
            pixelValue = pixelValue.pow(AdjustBrightnessCurve)
        }

        if (!isNearlyZero(AdjustBrightness)) {
            val invSatRaised = (1.0 - pixelSaturation).pow(5.0)
            val clampedVibrance = clamp(AdjustVibrance, 0.0f, 1.0f)
            val halfVibrance = clampedVibrance * 0.5f
            val satProduct = halfVibrance * invSatRaised.toFloat()
            pixelSaturation += satProduct
        }

        pixelSaturation *= AdjustSaturation

        pixelHue += AdjustHue

        pixelHue = fmod(pixelHue, 360.0f)
        if (pixelHue < 0.0f) pixelHue += 360.0f

        pixelSaturation = clamp(pixelSaturation, 0.0f, 1.0f)

        if (bShouldClampValue) {
            pixelValue = clamp(pixelValue, 0.0f, 1.0f)
        }

        val linearColor = HSVToLinearRGB(pixelHue, pixelSaturation, pixelValue)
        val newAlpha = lerp(AdjustMinAlpha, AdjustMaxAlpha, originalColor.A)
        return FLinearColor(linearColor.R, linearColor.G, linearColor.B, newAlpha)
    }

    private fun LinearRGBToHSV(inColor: FLinearColor): FLinearColor {
        val r = inColor.R
        val g = inColor.G
        val b = inColor.B

        val maxV = maxOf(r, g, b)
        val minV = minOf(r, g, b)
        val delta = maxV - minV

        var h: Float
        if (delta == 0.0f) {
            h = 0.0f
        } else if (maxV == r) {
            h = (g - b) / delta
        } else if (maxV == g) {
            h = (b - r) / delta + 2.0f
        } else {
            h = (r - g) / delta + 4.0f
        }
        h *= 60.0f
        if (h < 0.0f) h += 360.0f

        val s = if (maxV > 0.0f) delta / maxV else 0.0f
        return FLinearColor(h, s, maxV, inColor.A)
    }

    private fun HSVToLinearRGB(h: Float, s: Float, v: Float): FLinearColor {
        var hh = (h / 60.0f) % 6.0f
        val i = hh.toInt()
        val f = hh - i
        val p = v * (1.0f - s)
        val q = v * (1.0f - (s * f))
        val t = v * (1.0f - (s * (1.0f - f)))
        return when (i) {
            0 -> FLinearColor(v, t, p, 1.0f)
            1 -> FLinearColor(q, v, p, 1.0f)
            2 -> FLinearColor(p, v, t, 1.0f)
            3 -> FLinearColor(p, q, v, 1.0f)
            4 -> FLinearColor(t, p, v, 1.0f)
            else -> FLinearColor(v, p, q, 1.0f)
        }
    }

    private fun isNearlyEqual(a: Float, b: Float, errorTolerance: Float = 1e-4f): Boolean = Math.abs(a - b) <= errorTolerance
    private fun isNearlyZero(a: Float, errorTolerance: Float = 1e-4f): Boolean = Math.abs(a) <= errorTolerance
    private fun clamp(v: Float, min: Float, max: Float): Float = when {
        v < min -> min
        v > max -> max
        else -> v
    }
    private fun fmod(a: Float, b: Float): Float {
        val r = a % b
        return if (r < 0f) r + b else r
    }
    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
}
//@parity:off EXC-002
