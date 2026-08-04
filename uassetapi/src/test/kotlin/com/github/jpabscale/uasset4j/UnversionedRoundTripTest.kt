// Copyright (c) 2026 jpabscale — original tests (not part of the UAssetAPI port)
package com.github.jpabscale.uasset4j

import com.github.jpabscale.uasset4j.unrealtypes.EngineVersion
import com.github.jpabscale.uasset4j.unversioned.Usmap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * Round-trip tests for unversioned (usmap-schema) assets from the UAssetAPI test corpus.
 *
 * The corpus is not committed; CI downloads it from the pinned UAssetAPI SHA (see
 * .github/workflows/ci.yml) into `src/test/resources/testassets`. These tests skip via
 * `assumeTrue` when the corpus or its usmaps are absent, mirroring [JsonOracleTest].
 *
 * Covered behaviour (EXC-003):
 *  - JSON round-trip with Mappings re-attached after `DeserializeJson` writes byte-identical .uexp.
 *  - Writing unversioned properties while Mappings is null throws, instead of silently emitting a
 *    corrupt header (the C# oracle writes no header at all in that case).
 */
class UnversionedRoundTripTest {

    private val corpusDir: Path = Path.of(System.getProperty("unversioned.corpus.dir") ?: "src/test/resources/testassets")

    private data class Fixture(val rel: String, val engine: EngineVersion)

    /** Unversioned corpus assets verified to round-trip byte-identically with Mappings re-attached. */
    private val fixtures = listOf(
        Fixture("TestManyAssets/LiesOfP/SkillInfo.uasset", EngineVersion.VER_UE4_26),
        Fixture("TestManyAssets/LiesOfP/ItemInfo.uasset", EngineVersion.VER_UE4_26),
        Fixture("TestManyAssets/LiesOfP/SkillHitInfo.uasset", EngineVersion.VER_UE4_26),
        Fixture("TestUE5_1/UnderlyingEnumTypes/NewDataTable.uasset", EngineVersion.VER_UE5_1),
        Fixture("TestUE5_3/Engine/DefaultRecorderBoneCompression.uasset", EngineVersion.VER_UE5_1),
        Fixture("TestUE5_3/RON/AmmoDataTable.uasset", EngineVersion.VER_UE5_1),
        Fixture("TestUE5_5/BlankGame/BP_Test.uasset", EngineVersion.VER_UE5_1),
        Fixture("TestUE5_5/BlankGame/WBP_Hello.uasset", EngineVersion.VER_UE5_1),
        Fixture("TestUE5_5/BlankGame/M_Cube_1.uasset", EngineVersion.VER_UE5_1),
        Fixture("TestJson/MGA_HeavyWeapon_Parent.uasset", EngineVersion.VER_UE4_26),
    )

    /** Nearest `.usmap` walking up the tree, matching tools/sweep.py's find_usmap. */
    private fun findUsmap(asset: Path): Path? {
        var d = asset.parent
        while (d != null) {
            val found = d.toFile().listFiles()?.filter { it.extension == "usmap" }?.sortedBy { it.name }?.firstOrNull()
            if (found != null) return found.toPath()
            d = d.parent
        }
        return null
    }

    @Test
    fun jsonRoundTripWithMappingsIsByteIdentical() {
        for (fixture in fixtures) {
            val asset = corpusDir.resolve(fixture.rel)
            val uexp = asset.resolveSibling(asset.fileName.toString().removeSuffix(".uasset") + ".uexp")
            val usmap = findUsmap(asset)
            assumeTrue(
                Files.exists(asset) && Files.exists(uexp) && usmap != null,
                "corpus fixture missing: ${fixture.rel} (run the CI corpus fetch)",
            )

            val orig = UAsset(asset.toString(), true, fixture.engine, Usmap(usmap!!.toString()))
            val jsonStr = orig.SerializeJson(isFormatted = false)

            // DeserializeJson does not carry Mappings (usmap schema); the caller re-attaches it.
            val restored = UAsset.DeserializeJson(jsonStr)
            restored.Mappings = Usmap(usmap.toString())

            val outDir = Files.createTempDirectory("uasset4j-unv-rt")
            val outBase = outDir.resolve(asset.fileName.toString())
            restored.Write(outBase.toString())

            val outUexp = outDir.resolve(asset.fileName.toString().removeSuffix(".uasset") + ".uexp")
            assertEquals(
                Files.readAllBytes(uexp).toList(),
                Files.readAllBytes(outUexp).toList(),
                "rewritten .uexp bytes differ for ${fixture.rel}",
            )
            outDir.toFile().deleteRecursively()
        }
    }

    @Test
    fun writeWithoutMappingsThrows() {
        val fixture = fixtures.first()
        val asset = corpusDir.resolve(fixture.rel)
        val usmap = findUsmap(asset)
        assumeTrue(Files.exists(asset) && usmap != null, "corpus fixture missing: ${fixture.rel}")

        val orig = UAsset(asset.toString(), true, fixture.engine, Usmap(usmap!!.toString()))
        assumeTrue(orig.HasUnversionedProperties, "expected ${fixture.rel} to use unversioned properties")

        val restored = UAsset.DeserializeJson(orig.SerializeJson(isFormatted = false))
        // Mappings deliberately NOT re-attached — this must fail loudly, not emit a corrupt header.

        val outDir = Files.createTempDirectory("uasset4j-unv-nomap")
        val out = outDir.resolve(asset.fileName.toString())
        val ex = assertThrows(IllegalStateException::class.java) {
            restored.Write(out.toString())
        }
        assertEquals(true, ex.message?.contains("mappings") ?: false, "error should mention mappings, got: ${ex.message}")
        outDir.toFile().deleteRecursively()
    }
}
