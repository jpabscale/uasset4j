// Reimplemented from CUE4Parse (Apache-2.0) — Copyright (c) FabianFG and contributors
// Source: CUE4Parse/UE4/Objects/Engine/Curves/UCurveVector.cs
//@parity:on EXC-002
package com.github.jpabscale.uasset4j.curves

import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData

/**
 * A vector curve asset (`UCurveVector`), holding three `FRichCurve`s (`X`, `Y`, `Z`).
 * Mirrors CUE4Parse's `UCurveVector`. The curves are property-serialized and read via the
 * struct-fallback path.
 */
class UCurveVector {
    var FloatCurves: Array<FRichCurve> = arrayOf(FRichCurve(), FRichCurve(), FRichCurve())

    constructor()

    constructor(properties: MutableList<PropertyData>) {
        for (p in properties) {
            if (p is StructPropertyData && p.Name?.Value?.Value == "FloatCurves") {
                val value = p.Value
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

    fun Eval(inTime: Float, inDefaultValue: Float = 0f): Triple<Float, Float, Float> =
        Triple(
            FloatCurves.getOrElse(0) { FRichCurve() }.Eval(inTime, inDefaultValue),
            FloatCurves.getOrElse(1) { FRichCurve() }.Eval(inTime, inDefaultValue),
            FloatCurves.getOrElse(2) { FRichCurve() }.Eval(inTime, inDefaultValue),
        )
}
//@parity:off EXC-002
