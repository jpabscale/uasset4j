// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Engine/FWeightedRandomSampler.cs
package com.github.jpabscale.uasset4j.unrealtypes.objects.engine

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.StructAccessors

open class FWeightedRandomSampler {
    var Prob: Array<Float>
    var Alias: Array<Int>
    var TotalWeight: Float

    constructor() {
        Prob = emptyArray()
        Alias = emptyArray()
        TotalWeight = 0f
    }

    constructor(prob: Array<Float>, alias: Array<Int>, totalWeight: Float) {
        Prob = prob
        Alias = alias
        TotalWeight = totalWeight
    }

    constructor(reader: AssetBinaryReader) {
        Prob = reader.ReadArray(reader.ReadInt32()) { reader.ReadSingle() }
        Alias = reader.ReadArray(reader.ReadInt32()) { reader.ReadInt32() }
        TotalWeight = reader.ReadSingle()
    }

    fun Write(writer: AssetBinaryWriter): Int {
        val offset = writer.position

        writer.WriteInt32(Prob.size)
        for (p in Prob) {
            writer.WriteSingle(p)
        }

        writer.WriteInt32(Alias.size)
        for (a in Alias) {
            writer.WriteInt32(a)
        }

        writer.WriteSingle(TotalWeight)

        return writer.position - offset
    }

    fun clone(): FWeightedRandomSampler = FWeightedRandomSampler(Prob.copyOf(), Alias.copyOf(), TotalWeight)

    override fun toString(): String {
        var oup = "("
        oup += "("
        for (i in Prob.indices) {
            oup += Prob[i].toString() + ", "
        }
        oup = oup.dropLast(2) + ")"

        oup += "("
        for (i in Alias.indices) {
            oup += Alias[i].toString() + ", "
        }
        oup = oup.dropLast(2) + ")"

        oup += ", " + TotalWeight + ")"

        return oup
    }

    companion object {
        val accessors = StructAccessors(
            read = { r -> FWeightedRandomSampler(r) },
            fromString = { _, _ -> throw NotImplementedError() },
            write = { w, v -> v.Write(w) },
            defaultValue = { FWeightedRandomSampler() },
        )
    }
}

class FSkeletalMeshAreaWeightedTriangleSampler : FWeightedRandomSampler {
    constructor(reader: AssetBinaryReader) : super(reader)

    constructor()
}
