// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/ExportTypes/PropertyExport.cs
package com.github.jpabscale.uasset4j.exporttypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.MainSerializer
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.fieldtypes.UProperty

/**
 * Export data for a [UProperty].
 */
class PropertyExport : NormalExport {
    var Property: UProperty? = null

    constructor(superExport: Export) : super(superExport)

    constructor(asset: UAsset?, extras: ByteArray?) : super(asset, extras)

    constructor() : super()

    override fun Read(reader: AssetBinaryReader, nextStarting: Int) {
        super.Read(reader, nextStarting)

        val exportClassType = this.GetExportClassType()
        Property = MainSerializer.ReadUProperty(reader, exportClassType)
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)

        MainSerializer.WriteUProperty(Property!!, writer)
    }
}
