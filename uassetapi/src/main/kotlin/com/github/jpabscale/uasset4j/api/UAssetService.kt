// Copyright (c) 2026 jpabscale — original code (not part of the UAssetAPI port)
package com.github.jpabscale.uasset4j.api

import com.github.jpabscale.uasset4j.CustomSerializationFlags
import com.github.jpabscale.uasset4j.Formatting
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.unrealtypes.EngineVersion
import com.github.jpabscale.uasset4j.unversioned.Usmap
import java.nio.file.Files
import java.nio.file.Path

object UAssetService {
    @JvmStatic
    @JvmOverloads
    fun toJson(src: Path, engineVersion: EngineVersion, mappingsName: String? = null): String =
        UAsset(src.toString(), true, engineVersion, resolveMappings(mappingsName), CustomSerializationFlags.SkipPreloadDependencyLoading)
            .SerializeJson(Formatting.Indented)

    @JvmStatic
    @JvmOverloads
    fun fromJson(json: String, mappingsName: String? = null): UAsset {
        val asset = UAsset.DeserializeJson(json)
        asset.Mappings = resolveMappings(mappingsName)
        asset.CustomSerializationFlags = CustomSerializationFlags(
            asset.CustomSerializationFlags.value or CustomSerializationFlags.SkipPreloadDependencyLoading.value
        )
        asset.FilePath = "JSON"
        return asset
    }

    @JvmStatic
    @JvmOverloads
    fun roundTrip(src: Path, engineVersion: EngineVersion, mappingsName: String? = null): ByteArray {
        val asset = fromJson(toJson(src, engineVersion, mappingsName), mappingsName)
        val tempUasset = Files.createTempFile("uassetapi-roundtrip", ".uasset")
        val tempUexp = Path.of(tempUasset.toString().substringBeforeLast('.') + ".uexp")
        try {
            asset.Write(tempUasset.toString())
            val bytes = Files.readAllBytes(tempUasset)
            return if (Files.exists(tempUexp)) bytes + Files.readAllBytes(tempUexp) else bytes
        } finally {
            Files.deleteIfExists(tempUasset)
            Files.deleteIfExists(tempUexp)
        }
    }

    @JvmStatic
    @JvmOverloads
    fun load(src: Path, engineVersion: EngineVersion, mappingsName: String? = null): UAsset =
        UAsset(src.toString(), true, engineVersion, resolveMappings(mappingsName), CustomSerializationFlags.SkipPreloadDependencyLoading)

    @JvmStatic
    fun write(asset: UAsset, dst: Path) {
        asset.Write(dst.toString())
    }

    private fun resolveMappings(mappingsPath: String?): Usmap? {
        if (mappingsPath == null) return null
        val path = Path.of(mappingsPath)
        if (!Files.isRegularFile(path)) {
            throw IllegalArgumentException(
                "Mappings path does not exist: $mappingsPath"
            )
        }
        return try {
            Usmap(path.toString())
        } catch (e: Exception) {
            throw IllegalArgumentException(
                "Failed to load mappings from $mappingsPath: ${e.message}", e
            )
        }
    }
}
