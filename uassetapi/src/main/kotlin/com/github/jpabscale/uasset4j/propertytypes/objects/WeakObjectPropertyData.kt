// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/WeakObjectPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.FString

class WeakObjectPropertyData : ObjectPropertyData {
    override val PropertyType: FString get() = CurrentPropertyType
    override val DefaultValue: Any get() = FPackageIndex.FromRawIndex(0)

    override fun CreateClone(): PropertyData = WeakObjectPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("WeakObjectProperty")
    }
}
