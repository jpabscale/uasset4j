// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Math/Vector2fPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.math

import com.github.jpabscale.uasset4j.propertytypes.objects.BasePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector2f

class Vector2fPropertyData : BasePropertyData<FVector2f> {
    constructor(name: FName?) : super(FVector2f.accessors) {
        Name = name
    }

    constructor() : super(FVector2f.accessors)

    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = Vector2fPropertyData()

    companion object {
        private val CurrentPropertyType = FString("Vector2f")
    }
}
