// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/FUniversalObjectLocatorFragment.cs
package com.github.jpabscale.uasset4j.unrealtypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.FormatException
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData

class UniversalObjectLocatorFragmentPropertyData : StructPropertyData {
    var FragmentTypeID: FName? = null

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        FragmentTypeID = reader.ReadFName()
        if (FragmentTypeID!!.Value?.Value == "None") return

        val structType = FragmentTypeRegistry[FragmentTypeID!!.Value?.Value]
        if (structType != null) {
            StructType = FName.DefineDummy(reader.Asset, structType)
        } else {
            throw FormatException("Unknown FragmentTypeID : $FragmentTypeID")
        }

        super.Read(reader, includeHeader, 1, leng2, PropertySerializationContext.StructFallback)
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        writer.Write(FragmentTypeID)
        var res = 8
        if (FragmentTypeID!!.Value?.Value == "None") return res
        res += super.Write(writer, includeHeader, PropertySerializationContext.StructFallback)
        return res
    }

    constructor(name: FName?, forcedType: FName?) : super(name, forcedType)
    constructor(name: FName?) : super(name)
    constructor() : super()

    private companion object {
        val FragmentTypeRegistry: LinkedHashMap<String, String> = linkedMapOf(
            "actor" to "DirectPathObjectLocator",
            "animinst" to "AnimInstanceLocatorFragment",
            "subobj" to "SubObjectLocator",
            "ls_lazy_obj_ptr" to "LegacyLazyObjectPtrFragment",
        )
        val CurrentPropertyType = FString("UniversalObjectLocatorFragment")
    }
}
