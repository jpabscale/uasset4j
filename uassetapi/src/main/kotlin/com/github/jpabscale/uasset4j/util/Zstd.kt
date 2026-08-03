// Copyright (c) 2026 jpabscale — original code (not part of the UAssetAPI port)
package com.github.jpabscale.uasset4j.util

import com.qyntrax.unzstd.ZstdDecompressor

/**
 * Sole zstd entry point for the port. Mirrors the single C# usage
 * (`new ZstdSharp.Decompressor().Unwrap(src, decompressedSize)` in Usmap.cs).
 *
 * Implementation: qyntrax-unzstd (pure JVM, no Unsafe/foreign/JNI), so it works on a plain JVM and
 * under GraalVM native-image with zero config. Swapping to another implementation (aircompressor,
 * zstd-jni) is a one-function change.
 */
object Zstd {
    fun decompress(src: ByteArray, compressedSize: Int, decompressedSize: Int): ByteArray {
        val out = ByteArray(decompressedSize)
        ZstdDecompressor().decompress(src, 0, compressedSize, out, 0, decompressedSize)
        return out
    }
}
