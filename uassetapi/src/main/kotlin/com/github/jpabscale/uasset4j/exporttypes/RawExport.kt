// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/ExportTypes/RawExport.cs
package com.github.jpabscale.uasset4j.exporttypes

import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset

/**
 * An export that could not be properly parsed by UAssetAPI, and is instead represented as an array of bytes as a fallback.
 */
class RawExport : Export {
    var Data: ByteArray? = null

    constructor(superExport: Export) {
        Asset = superExport.Asset
        Extras = superExport.Extras
    }

    constructor(data: ByteArray?, asset: UAsset?, extras: ByteArray?) : super(asset, extras) {
        Data = data
    }

    constructor() : super()

    override fun Write(writer: AssetBinaryWriter) {
        writer.WriteBytes(Data!!)
    }
}
