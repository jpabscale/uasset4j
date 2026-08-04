// Reimplemented from CUE4Parse (Apache-2.0) — Copyright (c) FabianFG and contributors
// Source: CUE4Parse/UE4/Assets/Exports/Texture/UCurveLinearColorAtlas.cs
//
// CUE4Parse defines this as `UCurveLinearColorAtlas : UTexture2D` (no custom serialization).
// uasset4j reads the tagged-property portion of a texture export through the generic
// `NormalExport` path, so there is nothing extra to parse. This class exists so callers can
// recognize the asset type by class name.
//@parity:on EXC-002
package com.github.jpabscale.uasset4j.curves

import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData

/** A curve linear-color atlas asset. Mirrors CUE4Parse's `UCurveLinearColorAtlas`. */
class UCurveLinearColorAtlas {
    /** The serialized texture properties (from the owning NormalExport's `Data`). */
    var Properties: MutableList<PropertyData> = mutableListOf()

    /** The curves owned by this atlas (from `GradientCurves`), if any. */
    var GradientCurves: MutableList<FRichCurve> = mutableListOf()

    constructor()

    constructor(properties: MutableList<PropertyData>) {
        Properties = properties
        for (p in properties) {
            if (p is StructPropertyData && p.Name?.Value?.Value == "GradientCurves") {
                // GradientCurves is a TArray<FRichCurve> in UCurveLinearColorAtlas; read the
                // FRichCurve elements via the struct-fallback path.
                val value = p.Value
                if (value != null) {
                    for (element in value) {
                        if (element is StructPropertyData) GradientCurves.add(FRichCurve.fromStruct(element))
                    }
                }
            }
        }
    }
}
//@parity:off EXC-002
