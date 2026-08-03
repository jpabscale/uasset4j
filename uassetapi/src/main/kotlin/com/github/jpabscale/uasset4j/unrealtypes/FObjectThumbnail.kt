// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/FObjectThumbnail.cs
package com.github.jpabscale.uasset4j.unrealtypes

/** Unreal Object Thumbnail — thumbnail image data for an object. */
class FObjectThumbnail {
    /** Thumbnail Width. */
    var Width: Int = 0

    /** Thumbnail Height. */
    var Height: Int = 0

    /** Compressed image data bytes. */
    var CompressedImageData: ByteArray = ByteArray(0)

    /** Image data bytes (not serialized). */
    var ImageData: ByteArray? = null
}
