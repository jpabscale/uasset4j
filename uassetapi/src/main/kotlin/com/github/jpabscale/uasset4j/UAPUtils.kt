// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UAPUtils.cs
@file:OptIn(kotlin.ExperimentalUnsignedTypes::class)

// NOTE: M1 subset — GUID helpers and arithmetic utilities only. Reflection/JSON-dependent members
// (APIVersion, FindAllInstances, GetOrderedFields, SerializeJson, SortByDependencies, ...) land
// alongside their consumers in M3/M4.
package com.github.jpabscale.uasset4j

import com.github.jpabscale.uasset4j.unrealtypes.FGuid

object UAPUtils {
    /** Current version of UAssetAPI, matching the pinned C# build's AssemblyInformationalVersion. */
    const val APIVersion = "2.0.0"

    /** The git commit associated with this build of UAssetAPI. */
    const val CurrentCommit = "33ef77e"

    /** Display agent for UAssetAPI (mirrors UAPUtils.DisplayVersion in the C# port). */
    val DisplayVersion: String
        get() = "UAssetAPI v" + APIVersion + (if (CurrentCommit.isEmpty()) "" else " (" + CurrentCommit + ")")

    fun GUID(value1: UInt, value2: UInt, value3: UInt, value4: UInt): FGuid =
        FGuid.fromUnsignedInts(value1, value2, value3, value4)

    fun interpretAsGuidAndConvertToUnsignedInts(value: String): UIntArray =
        ConvertToGUID(value.trim()).toUnsignedInts()

    // adapted from UAssetGUI - see NOTICE.md
    fun ConvertStringToByteArray(val_: String): ByteArray {
        if (val_.isBlank()) return ByteArray(0)
        val rawStringArr = val_.split(' ')
        return ByteArray(rawStringArr.size) { rawStringArr[it].toInt(16).toByte() }
    }

    fun ConvertToGUID(guidString: String): FGuid {
        val validBraceFormat =
            guidString.isNotEmpty() &&
                guidString[0] == '{' &&
                guidString.length > 37 &&
                guidString[9] == '-' &&
                guidString[14] == '-' &&
                guidString[19] == '-' &&
                guidString[24] == '-' &&
                guidString[37] == '}'
        if (!validBraceFormat) {
            return parseStandardGuid(guidString)
        }

        val byteText = guidString.substring(29, 37) + guidString.substring(20, 24) + guidString.substring(25, 29) +
            guidString.substring(10, 14) + guidString.substring(15, 19) + guidString.substring(1, 9)
        val byteArr = ConvertHexStringToByteArray(byteText)
        byteArr.reverse()
        return FGuid.fromBytes(byteArr)
    }

    private fun parseStandardGuid(guidString: String): FGuid {
        val s = guidString.replace("{", "").replace("}", "").trim()
        // Accept "8-4-4-4-12" or 32 hex digits.
        val normalized = if (s.contains('-')) s else {
            if (s.length == 32) {
                buildString {
                    append(s, 0, 8); append('-'); append(s, 8, 12); append('-')
                    append(s, 12, 16); append('-'); append(s, 16, 20); append('-'); append(s, 20, 32)
                }
            } else s
        }
        return try {
            FGuid.fromUuid(java.util.UUID.fromString(normalized))
        } catch (_: IllegalArgumentException) {
            FGuid(0u, 0u, 0u, 0u)
        }
    }

    fun ConvertToString(`val`: FGuid): String = `val`.toPrettyString()

    fun ConvertHexStringToByteArray(hexString: String): ByteArray {
        val cleaned = hexString.replace(" ", "").replace("-", "")
        return ByteArray(cleaned.length / 2) { cleaned.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
    }

    fun AlignPadding(pos: Long, align: Int): Long {
        val remainder = pos % align
        return pos + if (remainder == 0L) 0 else align - remainder
    }

    fun AlignPadding(pos: Int, align: Int): Int = AlignPadding(pos.toLong(), align).toInt()

    fun DivideAndRoundUp(a: Int, b: Int): Int = (a + b - 1) / b

    fun <T : Comparable<T>> Clamp(val_: T, min: T, max: T): T {
        if (val_.compareTo(min) < 0) return min
        else if (val_.compareTo(max) > 0) return max
        else return val_
    }

    fun FixDirectorySeparatorsForDisk(path: String): String {
        return path.replace('/', java.io.File.separatorChar).replace('\\', java.io.File.separatorChar)
    }

    fun <T> SortByDependencies(allExports: List<T>, dependencies: Map<T, List<T>>): List<T> {
        val sortedSoFar = mutableListOf<T>()
        val visited = mutableSetOf<T>()

        for (item in allExports) SortByDependenciesVisit(item, visited, sortedSoFar, dependencies)

        return sortedSoFar
    }

    private fun <T> SortByDependenciesVisit(item: T, visited: MutableSet<T>, sortedSoFar: MutableList<T>, dependencies: Map<T, List<T>>) {
        if (!visited.contains(item)) {
            visited.add(item)
            if (dependencies.containsKey(item)) {
                for (dependency in dependencies[item]!!) SortByDependenciesVisit(dependency, visited, sortedSoFar, dependencies)
            }
            sortedSoFar.add(item)
        } else {
            if (!sortedSoFar.contains(item)) throw FormatException("Cyclic dependency exists in preload dependency graph")
        }
    }
}
