// Reimplemented from CUE4Parse (Apache-2.0) — Copyright (c) FabianFG and contributors
// Source: CUE4Parse/UE4/Assets/Exports/Engine/UCurveTable.cs + ECurveTableMode.cs
//
// The RowMap values are property-serialized structs read through the same tagged-property /
// unversioned-header path as `FStructFallback` (see CurveTableExport).
//@parity:on EXC-002
package com.github.jpabscale.uasset4j.curves

import com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FName

/** Determines which curve type is stored in a UCurveTable's rows. */
enum class ECurveTableMode {
    Empty,
    SimpleCurves,
    RichCurves,
}

/**
 * A curve table asset. Mirrors CUE4Parse's `UCurveTable`. Rows are keyed by name; each value is
 * a property-serialized `SimpleCurve` or `RichCurve` struct (per [CurveTableMode]).
 */
class UCurveTable {
    var CurveTableMode: ECurveTableMode = ECurveTableMode.Empty
    var RowMap: LinkedHashMap<FName, StructPropertyData> = LinkedHashMap()

    constructor()

    constructor(curveTableMode: ECurveTableMode, rowMap: LinkedHashMap<FName, StructPropertyData>) {
        CurveTableMode = curveTableMode
        RowMap = rowMap
    }
}
//@parity:off EXC-002
