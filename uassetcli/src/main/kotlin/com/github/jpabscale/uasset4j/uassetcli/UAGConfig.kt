// Copyright (c) 2026 jpabscale — original code (not part of the UAssetAPI port)
package com.github.jpabscale.uasset4j.uassetcli

import com.github.jpabscale.uasset4j.unversioned.Usmap
import java.io.File

object UAGConfig {
    val ConfigFolder: File
        get() = File(localAppData(), "UAssetGUI")
    val MappingsFolder: File
        get() = File(ConfigFolder, "Mappings")

    val AllMappings: LinkedHashMap<String, String> = linkedMapOf()

    fun LoadMappings() {
        AllMappings.clear()
        val mappingsFolder = MappingsFolder
        if (!mappingsFolder.isDirectory) return
        mappingsFolder.listFiles { f -> f.isFile && f.name.endsWith(".usmap") }
            ?.forEach { f -> AllMappings[f.nameWithoutExtension] = f.path }
    }

    fun TryGetMappings(name: String): Usmap? {
        val mappingPath = AllMappings[name] ?: return null
        return try {
            Usmap(mappingPath)
        } catch (e: Exception) {
            null
        }
    }

    private fun localAppData(): String {
        System.getenv("LOCALAPPDATA")?.let { return it }
        return System.getenv("USERPROFILE")?.let { "$it${File.separator}AppData${File.separator}Local" } ?: ""
    }
}
