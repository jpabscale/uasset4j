// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Core/Math/FMatrix.cs
package com.github.jpabscale.uasset4j.unrealtypes.objects.core.math

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.StructAccessors

class FMatrix {
    var XPlane: FPlane
    var YPlane: FPlane
    var ZPlane: FPlane
    var WPlane: FPlane

    constructor(xPlane: FPlane, yPlane: FPlane, zPlane: FPlane, wPlane: FPlane) {
        XPlane = xPlane
        YPlane = yPlane
        ZPlane = zPlane
        WPlane = wPlane
    }

    constructor(reader: AssetBinaryReader) {
        XPlane = FPlane(reader)
        YPlane = FPlane(reader)
        ZPlane = FPlane(reader)
        WPlane = FPlane(reader)
    }

    constructor() {
        XPlane = FPlane()
        YPlane = FPlane()
        ZPlane = FPlane()
        WPlane = FPlane()
    }

    fun Write(writer: AssetBinaryWriter): Int {
        var size = XPlane.Write(writer)
        size += YPlane.Write(writer)
        size += ZPlane.Write(writer)
        size += WPlane.Write(writer)
        return size
    }

    companion object {
        val accessors = StructAccessors(
            read = { r -> FMatrix(r) },
            fromString = { d, a -> FromString(d, a) },
            write = { w, v -> v.Write(w) },
            defaultValue = { FMatrix() },
        )

        fun FromString(d: Array<String>, asset: UAsset): FMatrix {
            throw NotImplementedError()
        }
    }
}
