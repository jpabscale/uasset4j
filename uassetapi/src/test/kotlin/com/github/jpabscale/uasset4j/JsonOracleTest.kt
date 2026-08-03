// Copyright (c) 2026 jpabscale — original tests (not part of the UAssetAPI port)
package com.github.jpabscale.uasset4j

import com.github.jpabscale.uasset4j.unrealtypes.EngineVersion
import com.github.jpabscale.uasset4j.unversioned.Usmap
import com.github.jpabscale.uasset4j.util.Out
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * Acceptance test: the JSON layer must reproduce the C# UAssetAPI oracle byte-for-byte.
 *
 * The oracle at src/test/resources/oracle/soa/DT_ArmorLevels.json is the output of automod's built
 * UAssetAPI/UAssetCLI (pinned commit 33ef77e). This test loads the matching .uasset/.uexp through
 * the Kotlin binary reader, runs SerializeJson(true), and compares the two byte-for-byte.
 */
class JsonOracleTest {

    private val soaDir: Path = Path.of(System.getProperty("json.oracle.dir") ?: "src/test/resources/oracle/soa")

    @Test
    fun serializeMatchesOracleByteForByte() {
        val assetPath = soaDir.resolve("DT_ArmorLevels.uasset")
        val uexpPath = soaDir.resolve("DT_ArmorLevels.uexp")
        val oraclePath = soaDir.resolve("DT_ArmorLevels.json")
        val usmapPath = soaDir.resolve("SandsOfAura_1.01.25.usmap")
        assumeTrue(
            Files.exists(assetPath) && Files.exists(uexpPath) && Files.exists(oraclePath) && Files.exists(usmapPath),
            "oracle fixtures missing in $soaDir",
        )

        val usmap = Usmap(usmapPath.toString())
        // The C# UAssetCLI parses with CustomSerializationFlags.SkipPreloadDependencyLoading set
        // (see UAssetCLI/Program.cs), which the oracle JSON reflects in "CustomSerializationFlags".
        val asset = UAsset(
            assetPath.toString(),
            true,
            EngineVersion.VER_UE4_25,
            usmap,
            CustomSerializationFlags(CustomSerializationFlags.SkipPreloadDependencyLoading.value),
        )
        val actual = asset.SerializeJson(true)
        val expected = oraclePath.toFile().readText()

        if (expected != actual) {
            val report = firstDiff(expected, actual)
            throw AssertionError("JSON diverges from oracle:\n$report")
        }
    }

    @Test
    fun deserializeJsonRoundTrips() {
        val oraclePath = soaDir.resolve("DT_ArmorLevels.json")
        assumeTrue(Files.exists(oraclePath), "oracle fixture missing in $soaDir")

        val asset = UAsset.DeserializeJson(oraclePath.toFile().readText())
        val reSerialized = asset.SerializeJson(true)
        assertEquals(oraclePath.toFile().readText(), reSerialized, "fromjson -> tojson did not reproduce the oracle JSON")
    }

    @Test
    fun deserializeJsonReproducesAssetBytes() {
        val oraclePath = soaDir.resolve("DT_ArmorLevels.json")
        val assetPath = soaDir.resolve("DT_ArmorLevels.uasset")
        val uexpPath = soaDir.resolve("DT_ArmorLevels.uexp")
        val usmapPath = soaDir.resolve("SandsOfAura_1.01.25.usmap")
        assumeTrue(
            Files.exists(oraclePath) && Files.exists(assetPath) && Files.exists(uexpPath) && Files.exists(usmapPath),
            "oracle fixtures missing in $soaDir",
        )

        val asset = UAsset.DeserializeJson(oraclePath.toFile().readText())
        // The C# UAssetCLI assigns the mappings (and re-applies the skip-preload flag) after
        // DeserializeJson, before writing; mirror that here (UAssetCLI/Program.cs).
        asset.Mappings = Usmap(usmapPath.toString())
        asset.CustomSerializationFlags = CustomSerializationFlags(CustomSerializationFlags.SkipPreloadDependencyLoading.value)
        val uasset = Out<ByteArray?>()
        val uexp = Out<ByteArray?>()
        asset.Write(uasset, uexp)
        assertEquals(assetPath.toFile().readBytes().toList(), uasset.value!!.toList(), "rewritten .uasset bytes differ")
        assertEquals(uexpPath.toFile().readBytes().toList(), uexp.value!!.toList(), "rewritten .uexp bytes differ")
    }

    /** Locates the first byte where [a] and [b] differ and shows local context. */
    private fun firstDiff(a: String, b: String): String {
        if (a.length != b.length) {
            return "length differs: expected ${a.length}, actual ${b.length}"
        }
        var i = 0
        while (i < a.length) {
            if (a[i] != b[i]) break
            i++
        }
        val from = (i - 80).coerceAtLeast(0)
        val to = (i + 80).coerceAtMost(a.length)
        return "first divergence at char $i:\n" +
            "--- expected ---\n${a.substring(from, to)}\n" +
            "--- actual ---\n${b.substring(from, to)}"
    }
}
