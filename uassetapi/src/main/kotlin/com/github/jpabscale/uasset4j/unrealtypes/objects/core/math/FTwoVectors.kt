// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Core/Math/FTwoVectors.cs
package com.github.jpabscale.uasset4j.unrealtypes.objects.core.math

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.StructAccessors

class FTwoVectors {
    var V1: FVector
    var V2: FVector

    constructor(v1: FVector, v2: FVector) {
        V1 = v1
        V2 = v2
    }

    constructor(reader: AssetBinaryReader) {
        V1 = FVector(reader)
        V2 = FVector(reader)
    }

    constructor() {
        V1 = FVector()
        V2 = FVector()
    }

    fun Write(writer: AssetBinaryWriter): Int {
        var size = V1.Write(writer)
        size += V2.Write(writer)
        return size
    }

    override fun toString(): String = "($V1, $V2)"

    companion object {
        val accessors = StructAccessors(
            read = { r -> FTwoVectors(r) },
            fromString = { d, a -> FromString(d, a) },
            write = { w, v -> v.Write(w) },
            defaultValue = { FTwoVectors() },
        )

        fun FromString(d: Array<String>, asset: UAsset): FTwoVectors {
            throw NotImplementedError()
        }
    }
}
