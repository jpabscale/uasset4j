// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Engine/WeightedRandomSamplerPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.engine

import com.github.jpabscale.uasset4j.propertytypes.objects.BasePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.objects.engine.FWeightedRandomSampler

open class WeightedRandomSamplerPropertyData : BasePropertyData<FWeightedRandomSampler> {
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = WeightedRandomSamplerPropertyData()

    override fun CloneInto(res: PropertyData) {
        super.CloneInto(res)
        (res as WeightedRandomSamplerPropertyData).Value = Value?.clone()
    }

    constructor(name: FName?) : super(FWeightedRandomSampler.accessors, name)
    constructor() : super(FWeightedRandomSampler.accessors)

    companion object {
        private val CurrentPropertyType = FString("WeightedRandomSampler")
    }
}

class SkeletalMeshAreaWeightedTriangleSamplerPropertyData : WeightedRandomSamplerPropertyData {
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = SkeletalMeshAreaWeightedTriangleSamplerPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("SkeletalMeshAreaWeightedTriangleSampler")
    }
}
