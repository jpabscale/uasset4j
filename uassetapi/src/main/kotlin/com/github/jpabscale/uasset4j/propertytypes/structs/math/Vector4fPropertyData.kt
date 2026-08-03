// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Math/Vector4fPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.math

import com.github.jpabscale.uasset4j.propertytypes.objects.BasePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector4f

class Vector4fPropertyData : BasePropertyData<FVector4f> {
    constructor(name: FName?) : super(FVector4f.accessors) {
        Name = name
    }

    constructor() : super(FVector4f.accessors)

    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = Vector4fPropertyData()

    companion object {
        private val CurrentPropertyType = FString("Vector4f")
    }
}
