// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/ExportTypes/FunctionExport.cs
package com.github.jpabscale.uasset4j.exporttypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.unrealtypes.EFunctionFlags

/**
 * Export data for a blueprint function.
 */
class FunctionExport : StructExport {
    var FunctionFlags: EFunctionFlags = EFunctionFlags(EFunctionFlags.FUNC_None)

    constructor(superExport: Export) : super(superExport) {
        Asset = superExport.Asset
        Extras = superExport.Extras
    }

    constructor(asset: UAsset?, extras: ByteArray?) : super(asset, extras)

    constructor() : super()

    override fun Read(reader: AssetBinaryReader, nextStarting: Int) {
        super.Read(reader, nextStarting)
        FunctionFlags = EFunctionFlags(reader.ReadUInt32().toInt())
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        writer.WriteUInt32(FunctionFlags.value.toLong() and 0xFFFFFFFFL)
    }
}
