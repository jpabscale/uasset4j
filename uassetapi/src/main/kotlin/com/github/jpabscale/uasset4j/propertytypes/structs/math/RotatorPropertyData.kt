// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Math/RotatorPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.math

import com.github.jpabscale.uasset4j.propertytypes.objects.BasePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FRotator

class RotatorPropertyData : BasePropertyData<FRotator> {
    constructor(name: FName?) : super(FRotator.accessors) {
        Name = name
    }

    constructor() : super(FRotator.accessors)

    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = RotatorPropertyData()

    companion object {
        private val CurrentPropertyType = FString("Rotator")
    }
}
