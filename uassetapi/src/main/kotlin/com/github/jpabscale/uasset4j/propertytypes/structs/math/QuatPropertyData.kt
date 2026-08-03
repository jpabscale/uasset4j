// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Math/QuatPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.math

import com.github.jpabscale.uasset4j.propertytypes.objects.BasePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FQuat

class QuatPropertyData : BasePropertyData<FQuat> {
    constructor(name: FName?) : super(FQuat.accessors) {
        Name = name
    }

    constructor() : super(FQuat.accessors)

    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = QuatPropertyData()

    companion object {
        private val CurrentPropertyType = FString("Quat")
    }
}
