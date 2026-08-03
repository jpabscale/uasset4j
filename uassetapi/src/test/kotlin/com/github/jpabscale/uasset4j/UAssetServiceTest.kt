// Copyright (c) 2026 jpabscale — original tests (not part of the UAssetAPI port)
package com.github.jpabscale.uasset4j

import com.github.jpabscale.uasset4j.api.UAssetService
import com.github.jpabscale.uasset4j.unrealtypes.EngineVersion
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.GZIPOutputStream

/**
 * UAssetService requires the mappings argument to be a full, existing file path.
 * A bare name (e.g. the UAssetGUI style "StellarBlade_1.4.1") must fail loudly
 * rather than silently degrading to unversioned parsing without mappings.
 */
class UAssetServiceTest {

    private val soaDir: Path = Path.of(System.getProperty("json.oracle.dir") ?: "src/test/resources/oracle/soa")

    @Test
    fun resolveMappingsAcceptsFullPath() {
        val usmapPath = soaDir.resolve("SandsOfAura_1.01.25.usmap")
        org.junit.jupiter.api.Assumptions.assumeTrue(Files.exists(usmapPath), "oracle fixtures missing in $soaDir")

        val asset = UAssetService.load(
            soaDir.resolve("DT_ArmorLevels.uasset"),
            EngineVersion.VER_UE4_25,
            usmapPath.toString(),
        )
        assert(asset.Mappings != null) { "full path should load mappings" }
    }

    @Test
    fun resolveMappingsRejectsMissingPath() {
        assertThrows(IllegalArgumentException::class.java) {
            UAssetService.load(
                soaDir.resolve("DT_ArmorLevels.uasset"),
                EngineVersion.VER_UE4_25,
                "/nonexistent/SandsOfAura_1.01.25.usmap",
            )
        }
    }

    @Test
    fun resolveMappingsRejectsBareName() {
        assertThrows(IllegalArgumentException::class.java) {
            UAssetService.load(
                soaDir.resolve("DT_ArmorLevels.uasset"),
                EngineVersion.VER_UE4_25,
                "SandsOfAura_1.01.25",
            )
        }
    }

    /**
     * Approved parity exception EXC-001 (see docs/parity-exceptions.json): gzip-compressed
     * .usmap.gz mappings are a user-approved divergence. The C# oracle only recognizes .usmap,
     * .jmap and .jmap.gz; a .usmap.gz falls through to ReadUSMAP and throws FormatException.
     * uasset4j decompresses the gzip stream first.
     */
    @Test
    fun resolveMappingsAcceptsGzipUsmap() {
        val usmapPath = soaDir.resolve("SandsOfAura_1.01.25.usmap")
        org.junit.jupiter.api.Assumptions.assumeTrue(Files.exists(usmapPath), "oracle fixtures missing in $soaDir")

        val gz = Files.createTempFile("SandsOfAura_1.01.25", ".usmap.gz")
        try {
            GZIPOutputStream(Files.newOutputStream(gz)).use { gos ->
                Files.newInputStream(usmapPath).use { it.copyTo(gos) }
            }
            val asset = UAssetService.load(
                soaDir.resolve("DT_ArmorLevels.uasset"),
                EngineVersion.VER_UE4_25,
                gz.toString(),
            )
            assert(asset.Mappings != null) { ".usmap.gz should load mappings" }
        } finally {
            Files.deleteIfExists(gz)
        }
    }
}
