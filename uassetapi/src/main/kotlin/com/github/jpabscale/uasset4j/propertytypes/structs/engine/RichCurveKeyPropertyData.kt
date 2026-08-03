// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Engine/RichCurveKeyPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.engine

import com.github.jpabscale.uasset4j.propertytypes.objects.BasePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.objects.engine.FRichCurveKey

class RichCurveKeyPropertyData : BasePropertyData<FRichCurveKey> {
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = RichCurveKeyPropertyData()

    constructor(name: FName?) : super(FRichCurveKey.accessors, name)
    constructor() : super(FRichCurveKey.accessors)

    companion object {
        private val CurrentPropertyType = FString("RichCurveKey")
    }
}
