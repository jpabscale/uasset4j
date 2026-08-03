// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Engine/SkeletalMeshSamplingLODBuiltDataPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.engine

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.AncestryInfo
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class SkeletalMeshSamplingLODBuiltDataPropertyData : PropertyData {
    var Value: SkeletalMeshAreaWeightedTriangleSamplerPropertyData?
        get() = GetObject<SkeletalMeshAreaWeightedTriangleSamplerPropertyData>()
        set(v) = SetObject(v)

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        Value = SkeletalMeshAreaWeightedTriangleSamplerPropertyData(FName.DefineDummy(reader.Asset, "AreaWeightedTriangleSampler"))
        Value!!.Ancestry.Initialize(Ancestry, Name)
        Value!!.Read(reader, false, 0)
    }

    override fun ResolveAncestries(asset: UAsset, ancestrySoFar: AncestryInfo) {
        val ancestryNew = ancestrySoFar.clone()
        ancestryNew.SetAsParent(Name)

        if (Value == null) Value = SkeletalMeshAreaWeightedTriangleSamplerPropertyData()
        Value!!.ResolveAncestries(asset, ancestryNew)
        super.ResolveAncestries(asset, ancestrySoFar)
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        if (Value == null) Value = SkeletalMeshAreaWeightedTriangleSamplerPropertyData()
        return Value!!.Write(writer, false)
    }

    override fun toString(): String = Value.toString()

    override fun CreateClone(): PropertyData = SkeletalMeshSamplingLODBuiltDataPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("SkeletalMeshSamplingLODBuiltData")
    }
}
