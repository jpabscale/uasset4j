// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Math/IntVector2PropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.math

import com.github.jpabscale.uasset4j.propertytypes.objects.BasePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FIntVector2

class IntVector2PropertyData : BasePropertyData<FIntVector2> {
    constructor(name: FName?) : super(FIntVector2.accessors) {
        Name = name
    }

    constructor() : super(FIntVector2.accessors)

    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = IntVector2PropertyData()

    companion object {
        private val CurrentPropertyType = FString("IntVector2")
    }
}
