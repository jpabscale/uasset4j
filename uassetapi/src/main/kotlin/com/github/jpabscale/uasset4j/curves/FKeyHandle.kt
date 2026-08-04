// Reimplemented from CUE4Parse (Apache-2.0) — Copyright (c) FabianFG and contributors
// Source: CUE4Parse/UE4/Objects/Engine/Curves/FKeyHandle.cs
//@parity:on EXC-002
package com.github.jpabscale.uasset4j.curves

/** Key handle used by FRichCurve key lookup tables. */
class FKeyHandle {
    var Index: UInt = 0u

    constructor()

    constructor(index: UInt) {
        Index = index
    }
}
//@parity:off EXC-002
