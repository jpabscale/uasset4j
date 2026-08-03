// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Engine/FSkeletalMeshSamplingRegionBuiltData.cs
package com.github.jpabscale.uasset4j.unrealtypes.objects.engine

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.customversions.FNiagaraObjectVersion
import com.github.jpabscale.uasset4j.propertytypes.objects.StructAccessors

class FSkeletalMeshSamplingRegionBuiltData {
    var TriangleIndices: Array<Int>
    var Vertices: Array<Int>
    var BoneIndices: Array<Int>
    var AreaWeightedSampler: FSkeletalMeshAreaWeightedTriangleSampler

    constructor() {
        TriangleIndices = emptyArray()
        Vertices = emptyArray()
        BoneIndices = emptyArray()
        AreaWeightedSampler = FSkeletalMeshAreaWeightedTriangleSampler()
    }

    constructor(reader: AssetBinaryReader) {
        TriangleIndices = reader.ReadArray(reader.ReadInt32()) { reader.ReadInt32() }
        BoneIndices = reader.ReadArray(reader.ReadInt32()) { reader.ReadInt32() }

        AreaWeightedSampler = FSkeletalMeshAreaWeightedTriangleSampler(reader)

        if (reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FNiagaraObjectVersion")) >=
            FNiagaraObjectVersion.SkeletalMeshVertexSampling.ordinal
        ) {
            Vertices = reader.ReadArray(reader.ReadInt32()) { reader.ReadInt32() }
        } else {
            Vertices = emptyArray()
        }
    }

    fun Write(writer: AssetBinaryWriter): Int {
        val offset = writer.position

        if (TriangleIndices.isEmpty()) TriangleIndices = emptyArray()
        writer.WriteInt32(TriangleIndices.size)
        for (t in TriangleIndices) {
            writer.WriteInt32(t)
        }

        if (BoneIndices.isEmpty()) BoneIndices = emptyArray()
        writer.WriteInt32(BoneIndices.size)
        for (b in BoneIndices) {
            writer.WriteInt32(b)
        }

        AreaWeightedSampler.Write(writer)

        if (writer.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FNiagaraObjectVersion")) >=
            FNiagaraObjectVersion.SkeletalMeshVertexSampling.ordinal
        ) {
            if (Vertices.isEmpty()) Vertices = emptyArray()
            writer.WriteInt32(Vertices.size)
            for (v in Vertices) {
                writer.WriteInt32(v)
            }
        }

        return writer.position - offset
    }

    companion object {
        val accessors = StructAccessors(
            read = { r -> FSkeletalMeshSamplingRegionBuiltData(r) },
            fromString = { _, _ -> throw NotImplementedError() },
            write = { w, v -> v.Write(w) },
            defaultValue = { FSkeletalMeshSamplingRegionBuiltData() },
        )
    }
}
