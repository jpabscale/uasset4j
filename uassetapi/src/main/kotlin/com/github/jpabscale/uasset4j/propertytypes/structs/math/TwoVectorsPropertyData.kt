// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Math/TwoVectorsPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.math

import com.github.jpabscale.uasset4j.propertytypes.objects.BasePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FTwoVectors

class TwoVectorsPropertyData : BasePropertyData<FTwoVectors> {
    constructor(name: FName?) : super(FTwoVectors.accessors) {
        Name = name
    }

    constructor() : super(FTwoVectors.accessors)

    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = TwoVectorsPropertyData()

    companion object {
        private val CurrentPropertyType = FString("TwoVectors")
    }
}
