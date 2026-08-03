// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Engine/SkeletalMeshSamplingRegionBuiltDataPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.engine

import com.github.jpabscale.uasset4j.propertytypes.objects.BasePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.objects.engine.FSkeletalMeshSamplingRegionBuiltData

class SkeletalMeshSamplingRegionBuiltDataPropertyData : BasePropertyData<FSkeletalMeshSamplingRegionBuiltData> {
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = SkeletalMeshSamplingRegionBuiltDataPropertyData()

    constructor(name: FName?) : super(FSkeletalMeshSamplingRegionBuiltData.accessors, name)
    constructor() : super(FSkeletalMeshSamplingRegionBuiltData.accessors)

    companion object {
        private val CurrentPropertyType = FString("SkeletalMeshSamplingRegionBuiltData")
    }
}
