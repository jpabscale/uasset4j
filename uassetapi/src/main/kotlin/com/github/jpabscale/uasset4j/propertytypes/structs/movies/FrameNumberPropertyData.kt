// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Movies/FrameNumberPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.movies

import com.github.jpabscale.uasset4j.propertytypes.objects.BasePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class FrameNumberPropertyData() : BasePropertyData<FFrameNumber>(FFrameNumber.accessors) {
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = FrameNumberPropertyData()

    constructor(name: FName?) : this() {
        this.Name = name
    }

    companion object {
        private val CurrentPropertyType = FString("FrameNumber")
    }
}
