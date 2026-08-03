// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Engine/NavAgentSelectorPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.engine

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.BasePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.StructAccessors
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class FNavAgentSelector {
    var PackedBits: Long = 0

    constructor(packedBits: Long) {
        PackedBits = packedBits
    }

    constructor(reader: AssetBinaryReader) {
        PackedBits = reader.ReadUInt32()
    }

    fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteUInt32(PackedBits)
        return 4
    }

    override fun toString(): String = PackedBits.toString()

    companion object {
        val accessors = StructAccessors(
            read = { r -> FNavAgentSelector(r) },
            fromString = { d, _ -> FNavAgentSelector(d[0].toULongOrNull()?.toLong() ?: 0L) },
            write = { w, v -> v.Write(w) },
            defaultValue = { FNavAgentSelector(0L) },
        )
    }
}

class NavAgentSelectorPropertyData : BasePropertyData<FNavAgentSelector> {
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = NavAgentSelectorPropertyData()

    constructor(name: FName?) : super(FNavAgentSelector.accessors, name)
    constructor() : super(FNavAgentSelector.accessors)

    companion object {
        private val CurrentPropertyType = FString("NavAgentSelector")
    }
}
