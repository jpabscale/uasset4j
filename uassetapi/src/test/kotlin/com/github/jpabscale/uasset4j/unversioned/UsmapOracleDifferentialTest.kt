// Copyright (c) 2026 jpabscale — original tests (not part of the UAssetAPI port)
package com.github.jpabscale.uasset4j.unversioned

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

/**
 * Differential test vs the C# UAssetAPI oracle (automod's built UAssetAPI.dll at tools/UAssetCLI).
 *
 * The golden `<name>.dump` files are canonical structure dumps produced by `usmapdump` (a small C#
 * tool in /tmp/opencode/usmapdump) on the pinned UAssetAPI. The Kotlin parse must reproduce the
 * dump byte-for-byte. Both the usmap fixtures and the dumps are game-derived, so they stay local
 * (gitignored); the test skips when they are absent.
 */
class UsmapOracleDifferentialTest {

    private val corpusDir: Path = Path.of(System.getProperty("testassets.dir") ?: "src/test/resources/testassets")
    private val oracleDir: Path = Path.of("src/test/resources/oracle")

    private val cases = listOf(
        "RealGames/StellarBlade_1.4.1.usmap" to "SB.dump",
        "RealGames/SandsOfAura_1.01.25.usmap" to "SoA.dump",
        "TestUE5_4/Billiards/5.4.3-34507850+++UE5+Release-5.4-DeepSpace7.usmap" to "TestUE5_4_Billiards.dump",
        "TestUE5_4/BlankGame/BlankGame_Dumper-7.usmap" to "TestUE5_4_BlankGame.dump",
        "TestUE5_4/JOY/5.4.3-34507850+++UE5+Release-5.4-JOY.usmap" to "TestUE5_4_JOY.dump",
        "TestUE5_6/BpThirdPerson/ExplicitEnumValuesExample.usmap" to "TestUE5_6_ExplicitEnumValues.dump",
        "TestUE5_7/NanosWorld/NanosWorld.usmap" to "TestUE5_7_NanosWorld.dump",
        "TestUE5_1/UnderlyingEnumTypes/UnderlyingEnumTypes.usmap" to "TestUE5_1_UnderlyingEnumTypes.dump",
        "TestJson/Outriders.usmap" to "TestJson_Outriders.dump",
    )

    @Test
    fun differentialAgainstCSharpOracle() {
        for ((usmapRel, dumpRel) in cases) {
            val usmapPath = corpusDir.resolve(usmapRel)
            val goldenPath = oracleDir.resolve(dumpRel)
            assumeTrue(
                usmapPath.exists() && goldenPath.exists(),
                "oracle fixtures missing: $usmapPath / $goldenPath",
            )

            val usmap = Usmap(usmapPath.toString())
            assertEquals(goldenPath.readText(), dumpToString(usmap), "usmap dump mismatch: $usmapRel")
        }
    }

    private fun dumpToString(usmap: Usmap): String = buildString {
        appendLine("VERSION ${usmap.Version.name}")
        appendLine("FILEVERSIONUE4 ${usmap.FileVersionUE4.value}")
        appendLine("FILEVERSIONUE5 ${usmap.FileVersionUE5.value}")
        appendLine("NETCL ${usmap.NetCL}")

        val cvc = usmap.CustomVersionContainer
        appendLine("CUSTOMVERSIONS ${cvc?.size ?: 0}")
        if (cvc != null) {
            for (cv in cvc) {
                appendLine("  ${cv.Key} ${cv.Version}")
            }
        }

        appendLine("NAMES ${usmap.NameMap.size}")
        for (i in usmap.NameMap.indices) {
            appendLine("  $i ${usmap.NameMap[i] ?: ""}")
        }

        appendLine("ENUMS ${usmap.EnumMap.size}")
        for (name in usmap.EnumMap.keys.sorted()) {
            val e = usmap.EnumMap.get(name)!!
            appendLine("  $name ${e.Values.size}")
            for ((k, v) in e.Values.toSortedMap()) {
                appendLine("    $k ${v ?: ""}")
            }
        }

        appendLine("SCHEMAS ${usmap.Schemas.size}")
        for (name in usmap.Schemas.keys.sorted()) {
            val s = usmap.Schemas.get(name)!!
            appendLine("  $name|${s.SuperType ?: ""}|${s.PropCount}|${s.ModulePath ?: ""}|${s.StructKind.name}|${s.StructOrClassFlags}")
            for (idx in s.Properties.keys.sorted()) {
                val p = s.Properties[idx]!!
                appendLine("    $idx|${p.Name ?: ""}|${p.ArraySize}|${p.ArrayIndex}|${p.PropertyData?.toString() ?: "null"}")
            }
        }
    }
}
