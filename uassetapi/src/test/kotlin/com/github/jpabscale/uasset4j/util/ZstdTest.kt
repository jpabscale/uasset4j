// Copyright (c) 2026 jpabscale — original tests (not part of the UAssetAPI port)
package com.github.jpabscale.uasset4j.util

import com.github.luben.zstd.Zstd as ZstdJni
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Path
import java.util.Random
import kotlin.io.path.exists
import kotlin.io.path.readBytes

class ZstdTest {

    private val corpusDir: Path = Path.of(
        System.getProperty("testassets.dir") ?: "src/test/resources/testassets"
    )

    @Test
    fun roundTripVariousPayloads() {
        val rng = Random(42)
        val payloads = listOf(
            ByteArray(0),
            "hello".toByteArray(),
            ByteArray(1) { 0 },
            ByteArray(1) { 255.toByte() },
            ByteArray(100_000).also { rng.nextBytes(it) },
            ByteArray(200_000) { (it % 256).toByte() },
        )
        for (payload in payloads) {
            val compressed = ZstdJni.compress(payload)
            val out = Zstd.decompress(compressed, compressed.size, payload.size)
            assertArrayEquals(payload, out, "round-trip failed for ${payload.size} bytes")
        }
    }

    @Test
    fun matchesZstdJniOnRealUsmaps() {
        val fixtures = listOf(
            "TestUE5_4/Billiards/5.4.3-34507850+++UE5+Release-5.4-DeepSpace7.usmap",
            "TestUE5_4/BlankGame/BlankGame_Dumper-7.usmap",
            "TestUE5_4/JOY/5.4.3-34507850+++UE5+Release-5.4-JOY.usmap",
            "TestUE5_6/BpThirdPerson/ExplicitEnumValuesExample.usmap",
            "TestUE5_7/NanosWorld/NanosWorld.usmap",
        )
        for (rel in fixtures) {
            val p = corpusDir.resolve(rel)
            assumeTrue(p.exists(), "corpus fixture missing: $p")
            val file = p.readBytes()
            val header = parseUsmapHeader(file)
            assertEquals(3, header.method, "expected ZStandard-compressed fixture: $rel")

            val compressed = file.copyOfRange(header.payloadOffset, header.payloadOffset + header.compressedSize)
            val expected = ZstdJni.decompress(compressed, header.decompressedSize)
            assertEquals(header.decompressedSize, expected.size)
            val actual = Zstd.decompress(compressed, header.compressedSize, header.decompressedSize)
            assertArrayEquals(expected, actual, "qyntrax != zstd-jni on $rel")
        }
    }

    /** Minimal header parse mirroring Usmap.cs (UsmapVersion 3/4 fixtures carry a versioning section). */
    private fun parseUsmapHeader(file: ByteArray): UsmapHeader {
        val bb = ByteBuffer.wrap(file).order(ByteOrder.LITTLE_ENDIAN)
        val magic = bb.short.toInt() and 0xFFFF
        check(magic == 0x30C4) { "bad usmap magic 0x" + magic.toString(16) }
        val version = bb.get().toInt() and 0xFF
        if (version >= 1) {
            val hasVersioning = bb.int
            if (hasVersioning > 0) {
                bb.int // FileVersionUE4
                bb.int // FileVersionUE5
                repeat(bb.int) { bb.position(bb.position() + 20) } // numCustomVersions * (GUID 16 + int)
                bb.int // NetCL
            }
        }
        val method = bb.get().toInt() and 0xFF
        val compressedSize = bb.int.toLong() and 0xFFFFFFFFL
        val decompressedSize = bb.int.toLong() and 0xFFFFFFFFL
        return UsmapHeader(method, compressedSize.toInt(), decompressedSize.toInt(), bb.position())
    }

    private data class UsmapHeader(
        val method: Int,
        val compressedSize: Int,
        val decompressedSize: Int,
        val payloadOffset: Int,
    )
}
