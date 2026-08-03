// Copyright (c) 2026 jpabscale — original code (not part of the UAssetAPI port)
package com.github.jpabscale.uasset4j.uassetcli

import com.github.jpabscale.uasset4j.CustomSerializationFlags
import com.github.jpabscale.uasset4j.Formatting
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.unrealtypes.EngineVersion
import com.github.jpabscale.uasset4j.unversioned.Usmap
import java.nio.file.Files
import java.nio.file.Path

fun main(args: Array<String>) {
    if (args.isNotEmpty()) {
        when (args[0].lowercase()) {
            // tojson <source> <destination> <engine version> [mappings name]
            "tojson" -> {
                UAGConfig.LoadMappings()

                if (args.size < 4) {
                    printUsage()
                    return
                }
                var selectedMappings: Usmap? = null
                if (args.size >= 5) {
                    selectedMappings = UAGConfig.TryGetMappings(args[4])
                    if (selectedMappings == null) {
                        try {
                            selectedMappings = Usmap(args[4])
                        } catch (e: Exception) {
                        }
                    }
                }

                val selectedVer = parseEngineVersion(args[3])

                val jsonSerializedAsset = UAsset(args[1], true, selectedVer, selectedMappings, CustomSerializationFlags.SkipPreloadDependencyLoading)
                    .SerializeJson(Formatting.Indented)
                Files.writeString(Path.of(args[2]), jsonSerializedAsset)
                return
            }
            // fromjson <source> <destination> [mappings name]
            "fromjson" -> {
                UAGConfig.LoadMappings()

                if (args.size < 3) {
                    printUsage()
                    return
                }
                var selectedMappings: Usmap? = null
                if (args.size >= 4) {
                    selectedMappings = UAGConfig.TryGetMappings(args[3])
                    if (selectedMappings == null) {
                        try {
                            selectedMappings = Usmap(args[3])
                        } catch (e: Exception) {
                        }
                    }
                }

                val jsonDeserializedAsset = Path.of(args[1]).toFile().inputStream().buffered().use { UAsset.DeserializeJson(it) }
                jsonDeserializedAsset.Mappings = selectedMappings
                jsonDeserializedAsset.CustomSerializationFlags = CustomSerializationFlags(
                    jsonDeserializedAsset.CustomSerializationFlags.value or CustomSerializationFlags.SkipPreloadDependencyLoading.value
                )
                jsonDeserializedAsset.FilePath = args[1]
                jsonDeserializedAsset.Write(args[2])
                return
            }
        }
    }

    printUsage()
}

/** Mirrors Program.cs engine-version resolution: int "23" -> VER_UE4_23, "4.26" -> VER_UE4_26, else enum name; unknown -> UNKNOWN (never throws). */
fun parseEngineVersion(raw: String): EngineVersion {
    val parsedInt = raw.toIntOrNull()
    if (parsedInt != null) {
        val ordinal = EngineVersion.VER_UE4_0.ordinal + parsedInt
        if (ordinal in EngineVersion.entries.indices) return EngineVersion.entries[ordinal]
        return EngineVersion.UNKNOWN
    }
    val candidate = if (raw.contains('.')) "VER_UE" + raw.replace('.', '_') else raw
    return EngineVersion.entries.firstOrNull { it.name.equals(candidate, true) } ?: EngineVersion.UNKNOWN
}

private fun printUsage() {
    println("Usage: UAssetCLI [ fromjson <source> <destination> [mappings name]")
    println("                 | tojson <source> <destination> <engine version> [mappings name] ]")
}
