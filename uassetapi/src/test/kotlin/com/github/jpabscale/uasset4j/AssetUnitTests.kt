// Copyright (c) 2026 jpabscale — JUnit 5 port of UAssetAPI.Tests/AssetUnitTests.cs (pinned 33ef77e)
// NOTE: The C# tests write their scratch outputs (MODIFIED.uasset, raw.json, *.bak) into the
// TestAssets folder. The JVM port keeps the corpus read-only and redirects every output write to
// build/test-scratch (documented in docs/mapping.md); reads still use src/test/resources/testassets.
package com.github.jpabscale.uasset4j

import com.github.jpabscale.uasset4j.exporttypes.DataTableExport
import com.github.jpabscale.uasset4j.exporttypes.Export
import com.github.jpabscale.uasset4j.exporttypes.FunctionExport
import com.github.jpabscale.uasset4j.exporttypes.NormalExport
import com.github.jpabscale.uasset4j.exporttypes.RawExport
import com.github.jpabscale.uasset4j.propertytypes.objects.BoolPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.EnumPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.FSoftObjectPath
import com.github.jpabscale.uasset4j.propertytypes.objects.FTopLevelAssetPath
import com.github.jpabscale.uasset4j.propertytypes.objects.FloatPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.MapPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.ObjectPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.propertytypes.objects.UnknownPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.LinearColorPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.VectorPropertyData
import com.github.jpabscale.uasset4j.unrealtypes.EngineVersion
import com.github.jpabscale.uasset4j.unrealtypes.EObjectFlags
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.GameSpecificOverride
import com.github.jpabscale.uasset4j.unversioned.Usmap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * Mirrors C# UAssetAPI.Tests/AssetUnitTests.cs one statement at a time (see docs/mapping.md).
 */
class AssetUnitTests {
    private val TestFolder: String = Path.of(System.getProperty("testassets.dir") ?: "src/test/resources/testassets").toString()
    private val scratchRoot: Path = Path.of(System.getProperty("test.scratch.dir") ?: "build/test-scratch").toAbsolutePath()

    private fun scratch(vararg parts: String): String {
        val res = scratchRoot.resolve(parts.joinToString("/"))
        res.parent.toFile().mkdirs()
        return res.toString()
    }

    private fun assumeCorpus() {
        assumeTrue(Files.exists(Path.of(TestFolder)), "testassets corpus missing; run from the uassetapi module dir or set -Dtestassets.dir")
    }

    /** Copies [rel] (and its sibling .uexp, if any) into the scratch dir, returning the scratch path. */
    private fun copyToScratch(rel: String): String {
        val src = File(TestFolder, rel)
        val dst = scratch(rel)
        src.copyTo(File(dst), overwrite = true)
        val uexpSrc = File(TestFolder, rel.substringBeforeLast('.') + ".uexp")
        if (uexpSrc.exists()) uexpSrc.copyTo(File(dst.substringBeforeLast('.') + ".uexp"), overwrite = true)
        return dst
    }

    /// <summary>
    /// Checks if two files have the same binary data.
    /// </summary>
    fun VerifyBinaryEquality(file1: String, file2: String) {
        if (file1 == file2) return

        val f1 = File(file1).readBytes()
        val f2 = File(file2).readBytes()
        assertTrue(f1.contentEquals(f2))
    }

    /// <summary>
    /// Asserts that all exports in an asset have parsed correctly.
    /// </summary>
    fun AssertAllExportsParsedCorrectly(tester: UAsset) {
        for (testExport in tester.Exports) {
            assertFalse(testExport is RawExport, "Export '${testExport.ObjectName}' in '${tester.FilePath}' was not parsed correctly (RawExport)")
            if (testExport is FunctionExport) {
                assertNotNull(testExport.ScriptBytecode, "FunctionExport '${testExport.ObjectName}' in '${tester.FilePath}' has null ScriptBytecode (failed to parse Kismet bytecode)")
            }
        }
    }

    /// <summary>
    /// Retrieves all the test assets in a particular folder.
    /// </summary>
    fun GetAllTestAssets(folder: String): Array<String> {
        val root = File(folder)
        val allFilesToTest = mutableListOf<String>()
        root.walkTopDown().filter { it.isFile && it.extension.equals("uasset", true) }.forEach { allFilesToTest.add(it.path) }
        root.walkTopDown().filter { it.isFile && it.extension.equals("umap", true) }.forEach { allFilesToTest.add(it.path) }
        return allFilesToTest.toTypedArray()
    }

    /// <summary>
    /// Tests <see cref="FSoftObjectPath"/> equality functionality including IEquatable implementation.
    /// </summary>
    @Test
    fun TestFSoftObjectPathEquality() {
        assumeCorpus()
        // Create a dummy asset for FName construction
        val dummyAsset = UAsset(Path.of(TestFolder, "TestManyAssets", "Astroneer", "Augment_BroadBrush.uasset").toString(), EngineVersion.VER_UE4_23)

        // Create test instances
        val packageName1 = FName(dummyAsset, "TestPackage")
        val assetName1 = FName(dummyAsset, "TestAsset")
        val subPath1 = FString("SubPath1")

        val packageName2 = FName(dummyAsset, "TestPackage")
        val assetName2 = FName(dummyAsset, "TestAsset")
        val subPath2 = FString("SubPath1")

        val packageName3 = FName(dummyAsset, "DifferentPackage")
        val assetName3 = FName(dummyAsset, "DifferentAsset")
        val subPath3 = FString("SubPath2")

        val path1 = FSoftObjectPath(packageName1, assetName1, subPath1)
        val path2 = FSoftObjectPath(packageName2, assetName2, subPath2) // Same values
        val path3 = FSoftObjectPath(packageName3, assetName3, subPath3) // Different values
        val path4 = FSoftObjectPath(packageName1, assetName1, null) // Null subpath

        // Test IEquatable<FSoftObjectPath>.Equals
        assertTrue(path1.equals(path2), "Equal paths should return true with typed Equals")
        assertTrue(path2.equals(path1), "Equal paths should return true with typed Equals (symmetry)")
        assertFalse(path1.equals(path3), "Different paths should return false with typed Equals")
        assertFalse(path1.equals(path4), "Paths with different subpaths should return false with typed Equals")

        // Test object.Equals
        assertTrue(path1.equals(path2 as Any), "Equal paths should return true with object Equals")
        assertFalse(path1.equals(path3 as Any), "Different paths should return false with object Equals")
        assertFalse(path1.equals(null), "Path should not equal null")
        assertFalse(path1.equals("string"), "Path should not equal different type")

        // Test == operator
        assertTrue(path1 == path2, "Equal paths should return true with == operator")
        assertFalse(path1 == path3, "Different paths should return false with == operator")

        // Test != operator
        assertFalse(path1 != path2, "Equal paths should return false with != operator")
        assertTrue(path1 != path3, "Different paths should return true with != operator")

        // Test GetHashCode consistency
        assertTrue(path1.hashCode() == path2.hashCode(), "Equal paths should have equal hash codes")

        // Test with null values
        val pathWithNullPackage = FSoftObjectPath(FTopLevelAssetPath(null, assetName1), subPath1)
        val pathWithNullAsset = FSoftObjectPath(FTopLevelAssetPath(packageName1, null), subPath1)
        val pathWithNullSubPath = FSoftObjectPath(packageName1, assetName1, null)

        assertFalse(path1.equals(pathWithNullPackage), "Paths with null package should not equal non-null")
        assertFalse(path1.equals(pathWithNullAsset), "Paths with null asset should not equal non-null")
        assertFalse(path1.equals(pathWithNullSubPath), "Paths with null subpath should not equal non-null")

        // Test null equality
        val pathAllNull = FSoftObjectPath(FTopLevelAssetPath(null, null), null)
        val pathAllNull2 = FSoftObjectPath(FTopLevelAssetPath(null, null), null)
        assertTrue(pathAllNull.equals(pathAllNull2), "Paths with all null values should be equal")

        // Test partial equality scenarios
        val pathSamePackageAsset = FSoftObjectPath(FTopLevelAssetPath(packageName1, assetName1), subPath3)
        assertFalse(path1.equals(pathSamePackageAsset), "Paths with same package/asset but different subpath should not be equal")

        val pathSamePackageSubPath = FSoftObjectPath(FTopLevelAssetPath(packageName1, assetName3), subPath1)
        assertFalse(path1.equals(pathSamePackageSubPath), "Paths with same package/subpath but different asset should not be equal")

        val pathSameAssetSubPath = FSoftObjectPath(FTopLevelAssetPath(packageName3, assetName1), subPath1)
        assertFalse(path1.equals(pathSameAssetSubPath), "Paths with same asset/subpath but different package should not be equal")
    }

    /// <summary>
    /// Tests <see cref="FName.ToString"/> and <see cref="FName.FromString"/>.
    /// </summary>
    @Test
    fun TestNameConstruction() {
        assumeCorpus()
        val dummyAsset = UAsset(Path.of(TestFolder, "TestManyAssets", "Astroneer", "Augment_BroadBrush.uasset").toString(), EngineVersion.VER_UE4_23)

        var test = FName.FromString(dummyAsset, "HelloWorld_0")!!
        assertTrue(test.Value?.Value == "HelloWorld" && test.Number == 1)
        assertTrue(test.toString() == "HelloWorld_0")

        test = FName.FromString(dummyAsset, "5_72")!!
        assertTrue(test.Value?.Value == "5" && test.Number == 73)
        assertTrue(test.toString() == "5_72")

        test = FName.FromString(dummyAsset, "_3")!!
        assertTrue(test.Value?.Value == "_3" && test.Number == 0)
        assertTrue(test.toString() == "_3")

        test = FName.FromString(dummyAsset, "hi_")!!
        assertTrue(test.Value?.Value == "hi_" && test.Number == 0)
        assertTrue(test.toString() == "hi_")

        test = FName.FromString(dummyAsset, "hi_01")!!
        assertTrue(test.Value?.Value == "hi_01" && test.Number == 0)
        assertTrue(test.toString() == "hi_01")

        test = FName.FromString(dummyAsset, "hi_10")!!
        assertTrue(test.Value?.Value == "hi" && test.Number == 11)
        assertTrue(test.toString() == "hi_10")

        test = FName.FromString(dummyAsset, "blah")!!
        assertTrue(test.Value?.Value == "blah" && test.Number == 0)
        assertTrue(test.toString() == "blah")

        test = FName(dummyAsset, "HelloWorld", 2)
        assertTrue(test.toString() == "HelloWorld_1")

        test = FName(dummyAsset, "HelloWorld", 0)
        assertTrue(test.toString() == "HelloWorld")
    }

    /// <summary>
    /// Tests modifying values within the class default object of an asset.
    /// Binary equality is expected.
    /// </summary>
    @Test
    fun TestCDOModification() {
        assumeCorpus()
        val tester = UAsset(Path.of(TestFolder, "TestManyAssets", "Astroneer", "Augment_BroadBrush.uasset").toString(), EngineVersion.VER_UE4_23)
        assertTrue(tester.VerifyBinaryEquality())

        var cdoExport: NormalExport? = null
        for (testExport in tester.Exports) {
            if (testExport.ObjectFlags.hasFlag(EObjectFlags.RF_ClassDefaultObject.value)) {
                cdoExport = testExport as NormalExport
                break
            }
        }
        assertNotNull(cdoExport)

        cdoExport!!["PickupActor"] = ObjectPropertyData().apply { Value = FPackageIndex.FromRawIndex(0) }

        assertTrue(cdoExport["PickupActor"] is ObjectPropertyData)
        assertTrue((cdoExport["PickupActor"] as ObjectPropertyData).Value!!.Index == 0)
    }

    /// <summary>
    /// MapProperties contain no easy way to determine the type of structs within them.
    /// For C++ classes, it is impossible without access to the headers, but for blueprint classes, the correct serialization is contained within the UClass.
    /// In this test, we take an asset with custom struct serialization in a map and extract data from the ClassExport in order to determine the correct serialization for the structs.
    /// Binary equality is expected.
    /// </summary>
    @Test
    fun TestCustomSerializationStructsInMap() {
        assumeCorpus()
        val tester = UAsset(Path.of(TestFolder, "TestCustomSerializationStructsInMap", "wtf.uasset").toString(), EngineVersion.VER_UE4_25)
        assertTrue(tester.VerifyBinaryEquality())

        // Get the map property in export 2
        val exportTwo = FPackageIndex.FromRawIndex(2).ToExport(tester)
        assertTrue(exportTwo is NormalExport)

        val exportTwoNormal = exportTwo as NormalExport

        val mapPropertyName = FName.FromString(tester, "KekWait")!!
        val testMap = exportTwoNormal[mapPropertyName] as? MapPropertyData
        assertNotNull(testMap)
        assertTrue(testMap === exportTwoNormal[mapPropertyName.Value!!.Value!!])

        // Get the first entry of the map
        val entryKey = testMap?.Value?.keys?.elementAt(0) as? StructPropertyData
        val entryValue = testMap?.Value?.values?.elementAt(0) as? StructPropertyData
        assertNotNull(entryKey?.Value?.get(0))
        assertNotNull(entryValue?.Value?.get(0))

        // Check that the properties are correct
        assertTrue(entryKey!!.Value!![0] is VectorPropertyData)
        assertTrue(entryValue!!.Value!![0] is LinearColorPropertyData)
    }

    /// <summary>
    /// In this test, we examine a cooked asset that has been modified by an external tool.
    /// As a result of external modification, the asset now has new name map entries whose hashes were left empty.
    /// Binary equality is expected. Expected behavior is for UAssetAPI to detect this and override its normal hash algorithm.
    /// </summary>
    @Test
    fun TestImproperNameMapHashes() {
        assumeCorpus()
        val tester = UAsset(Path.of(TestFolder, "TestImproperNameMapHashes", "OC_Gatling_DamageB_B.uasset").toString(), EngineVersion.VER_UE4_25)
        assertTrue(tester.VerifyBinaryEquality())

        val testingEntries = LinkedHashMap<String, Boolean>()
        testingEntries["/Game/WeaponsNTools/GatlingGun/Overclocks/OC_BonusesAndPenalties/OC_Bonus_MovmentBonus_150p"] = false
        testingEntries["/Game/WeaponsNTools/GatlingGun/Overclocks/OC_BonusesAndPenalties/OC_Bonus_MovmentBonus_150p.OC_Bonus_MovmentBonus_150p"] = false

        for (overrideHashes in tester.OverrideNameMapHashes!!) {
            if (testingEntries.containsKey(overrideHashes.key.Value!!)) {
                assertTrue(overrideHashes.value == 0L)
                testingEntries[overrideHashes.key.Value!!] = true
            }
        }

        for (testingEntry in testingEntries) {
            assertTrue(testingEntry.value)
        }
    }

    /// <summary>
    /// In this test, we examine a cooked asset that has been modified by an external tool.
    /// As a result of external modification, two identical entries now exist in the name map, which never occurs in assets cooked by the Unreal Engine.
    /// Binary equality is not expected, but the asset must successfully parse anyways.
    /// </summary>
    @Test
    fun TestDuplicateNameMapEntries() {
        assumeCorpus()
        val tester = UAsset(Path.of(TestFolder, "TestDuplicateNameMapEntries", "BIOME_AzureWeald.uasset").toString(), EngineVersion.VER_UE4_25)

        // Make sure a duplicate entry actually exists
        var duplicatesExist = false
        val enumeratedEntries = HashMap<String, Boolean>()
        for (entry in tester.GetNameMapIndexList()) {
            if (enumeratedEntries.containsKey(entry.Value!!) && enumeratedEntries[entry.Value]!!) {
                duplicatesExist = true
                break
            }
            enumeratedEntries[entry.Value!!] = true
        }
        assertTrue(duplicatesExist)

        // Make sure all exports parsed correctly
        AssertAllExportsParsedCorrectly(tester)
    }

    /// <summary>
    /// In this test, we have an asset with a few properties that UAssetAPI has no serialization for. (The properties do not actually exist in the engine itself, so this is expected behavior.)
    /// UAssetAPI must fallback to UnknownPropertyType to parse the asset correctly and maintain binary equality.
    /// </summary>
    @Test
    fun TestUnknownProperties() {
        assumeCorpus()
        val tester = UAsset(Path.of(TestFolder, "TestUnknownProperties", "BP_DetPack_Charge.uasset").toString(), EngineVersion.VER_UE4_25)
        assertTrue(tester.VerifyBinaryEquality())
        AssertAllExportsParsedCorrectly(tester)

        // Check that only the expected unknown properties are present
        val newUnknownProperties = LinkedHashMap<String, Boolean>()
        newUnknownProperties["GarbagePropty"] = false
        newUnknownProperties["EvenMoreGarbageTestingPropertyy"] = false

        for (testExport in tester.Exports) {
            if (testExport is NormalExport) {
                for (prop in testExport.Data!!) {
                    if (prop is UnknownPropertyData) {
                        val serializingType = prop.SerializingPropertyType.Value
                        assertNotNull(serializingType)
                        assertTrue(newUnknownProperties.containsKey(serializingType!!))
                        newUnknownProperties[serializingType] = true
                    }
                }
            }
        }

        for (entry in newUnknownProperties) {
            assertTrue(entry.value)
        }
    }

    private fun TestManyAssetsSubsection(game: String, version: EngineVersion, mappings: Usmap? = null, gsOverride: GameSpecificOverride = GameSpecificOverride.None) {
        val dir = File(TestFolder, "TestManyAssets/$game")
        assumeTrue(dir.isDirectory, "fixture dir missing: ${dir.path}")
        val allTestingAssets = GetAllTestAssets(dir.path)
        for (assetPath in allTestingAssets) {
            println(assetPath)
            val tester = UAsset(assetPath, version, mappings, CustomSerializationFlags.None, gsOverride)
            assertTrue(tester.VerifyBinaryEquality())
            AssertAllExportsParsedCorrectly(tester)
            println(tester.GetEngineVersion())
        }
    }

    private fun TestSubsection(game: String, mainFolder: String, subFolder: String, version: EngineVersion, mappings: Usmap? = null, gsOverride: GameSpecificOverride = GameSpecificOverride.None) {
        val dir = File(mainFolder, "$subFolder/$game")
        assumeTrue(dir.isDirectory, "fixture dir missing: ${dir.path}")
        val allTestingAssets = GetAllTestAssets(dir.path)
        for (assetPath in allTestingAssets) {
            println(assetPath)
            val tester = UAsset(assetPath, version, mappings, CustomSerializationFlags.None, gsOverride)
            assertTrue(tester.VerifyBinaryEquality())
            AssertAllExportsParsedCorrectly(tester)
            println(tester.GetEngineVersion())
        }
    }

    /// <summary>
    /// Tests the GUID/string conversion operations to ensure that they match the Unreal implementation.
    /// </summary>
    @Test
    fun TestGUIDs() {
        val input = "{CF873D05-4977-597A-F120-7F9F90B1ED09}"
        val test = UAPUtils.ConvertToGUID(input)
        assertTrue(UAPUtils.ConvertToString(test) == input)
        assertTrue(test.toByteArray().contentEquals(UAPUtils.ConvertHexStringToByteArray("05 3D 87 CF 7A 59 77 49 9F 7F 20 F1 09 ED B1 90")))
    }

    /// <summary>
    /// In this test, we examine a variety of assets from different games and ensure that they parse correctly and maintain binary equality.
    /// </summary>
    @Test
    fun TestManyAssets() {
        assumeCorpus()
        TestManyAssetsSubsection("Biodigital", EngineVersion.VER_UE4_14)
        TestManyAssetsSubsection("SnakePass", EngineVersion.VER_UE4_14)
        TestManyAssetsSubsection("Tekken", EngineVersion.VER_UE4_14)
        TestManyAssetsSubsection("MidAir", EngineVersion.VER_UE4_17)
        TestManyAssetsSubsection("MutantYearZero", EngineVersion.VER_UE4_17)
        TestManyAssetsSubsection("Bloodstained", EngineVersion.VER_UE4_18)
        TestManyAssetsSubsection("BurningDaylight", EngineVersion.VER_UE4_18)
        TestManyAssetsSubsection("CodeVein", EngineVersion.VER_UE4_18)
        TestManyAssetsSubsection("Liminal", EngineVersion.VER_UE4_18)
        TestManyAssetsSubsection("ToTheCore", EngineVersion.VER_UE4_18)
        TestManyAssetsSubsection("TheBeastInside", EngineVersion.VER_UE4_19)
        TestManyAssetsSubsection("TheOccupation", EngineVersion.VER_UE4_19)
        TestManyAssetsSubsection("Astroneer", EngineVersion.VER_UE4_23)
        TestManyAssetsSubsection("StarlitSeason", EngineVersion.VER_UE4_24)
        TestManyAssetsSubsection("MISC_426", EngineVersion.VER_UE4_26)
        TestManyAssetsSubsection("VERSIONED", EngineVersion.UNKNOWN)

        // traditional, NOT zen/io store. includes unversioned properties
        TestManyAssetsSubsection("LiesOfP", EngineVersion.VER_UE4_27, Usmap(Path.of(TestFolder, "TestManyAssets", "LiesOfP", "LiesOfP.usmap").toString()))
        TestManyAssetsSubsection("Palia", EngineVersion.VER_UE5_1, Usmap(Path.of(TestFolder, "TestManyAssets", "Palia", "Palia.usmap").toString()))
        TestManyAssetsSubsection("F1Manager2023", EngineVersion.VER_UE5_1, Usmap(Path.of(TestFolder, "TestManyAssets", "F1Manager2023", "F1Manager2023.usmap").toString()))
        TestManyAssetsSubsection("Palworld", EngineVersion.VER_UE5_1, Usmap(Path.of(TestFolder, "TestManyAssets", "Palworld", "Palworld.usmap").toString()))
        TestManyAssetsSubsection("Clay", EngineVersion.VER_UE5_1, Usmap(Path.of(TestFolder, "TestManyAssets", "Clay", "Clay.usmap").toString()))
        TestManyAssetsSubsection("SN2", EngineVersion.VER_UE5_6, Usmap(Path.of(TestFolder, "TestManyAssets", "SN2", "SN2.usmap").toString())) // BP_SN2PlayerController tests plugin asset fetch
        TestManyAssetsSubsection("Bellwright", EngineVersion.VER_UE5_6, Usmap(Path.of(TestFolder, "TestManyAssets", "Bellwright", "Bellwright.usmap").toString())) // FInstancedStruct
    }

    /// <summary>
    /// Tests assets using different jmap files for mappings.
    /// </summary>
    @Test
    fun TestJMAP() {
        assumeCorpus()
        TestManyAssetsSubsection("Clay", EngineVersion.VER_UE5_1, Usmap(Path.of(TestFolder, "TestManyAssets", "Clay", "Clay_unminified.jmap").toString()))
        TestManyAssetsSubsection("Clay", EngineVersion.VER_UE5_1, Usmap(Path.of(TestFolder, "TestManyAssets", "Clay", "Clay_minified.jmap").toString()))
        TestManyAssetsSubsection("Clay", EngineVersion.VER_UE5_1, Usmap(Path.of(TestFolder, "TestManyAssets", "Clay", "Clay.jmap.gz").toString()))
        TestManyAssetsSubsection("SN2", EngineVersion.UNKNOWN, Usmap(Path.of(TestFolder, "TestManyAssets", "SN2", "SN2.jmap.gz").toString())) // test of jmap version auto-fill
    }

    /// <summary>
    /// In this test, we examine and modify a DataTable to ensure that it parses correctly and maintains binary equality.
    /// </summary>
    @Test
    fun TestDataTables() {
        assumeCorpus()
        val assetPath = Path.of(TestFolder, "TestManyAssets", "Bloodstained", "PB_DT_RandomizerRoomCheck.uasset").toString()
        val tester = UAsset(assetPath, EngineVersion.VER_UE4_18)
        assertTrue(tester.VerifyBinaryEquality())
        AssertAllExportsParsedCorrectly(tester)
        assertTrue(tester.Exports.size == 1)

        val ourDataTableExport = tester.Exports[0] as? DataTableExport
        val ourTable = ourDataTableExport?.Table
        assertNotNull(ourTable)

        // Check out the first entry to make sure it's parsing alright, and flip all the flags for later testing
        val firstEntry = ourTable!!.Data[0]

        var didFindTestName = false
        for (i in firstEntry.Value!!.indices) {
            val propData = firstEntry.Value!![i]
            println("$i: ${propData.Name}, ${propData.PropertyType}")
            if (propData.Name == FName(tester, "AcceleratorANDDoubleJump")) didFindTestName = true
            if (propData is BoolPropertyData) propData.Value = !(propData.Value ?: false)
        }
        assertTrue(didFindTestName)

        // Save the modified table
        val modifiedPath = scratch("TestDataTables", "MODIFIED.uasset")
        tester.Write(modifiedPath)

        // Load the modified table back in and make sure we're good
        val tester2 = UAsset(modifiedPath, EngineVersion.VER_UE4_18)
        assertTrue(tester2.VerifyBinaryEquality())
        AssertAllExportsParsedCorrectly(tester2)
        assertTrue(tester2.Exports.size == 1)

        // Flip the flags back to what they originally were
        var firstEntry2 = (tester2.Exports[0] as? DataTableExport)?.Table?.Data?.get(0)
        assertNotNull(firstEntry2)
        for (i in firstEntry2!!.Value!!.indices) {
            if (firstEntry2.Value!![i] is BoolPropertyData) (firstEntry2.Value!![i] as BoolPropertyData).Value = !((firstEntry2.Value!![i] as BoolPropertyData).Value ?: false)
        }

        // Save and check that it's binary equal to what we originally had
        tester2.Write(tester2.FilePath)
        assertTrue(File(assetPath).readBytes().contentEquals(File(modifiedPath).readBytes()))
    }

    private fun TestJsonOnFile(file: String, version: EngineVersion, subFolder: String = "TestJson", mappingsFile: String? = null) {
        assumeCorpus()
        val subFolderFile = File(TestFolder, subFolder)
        val input = File(subFolderFile, file)
        assumeTrue(input.exists(), "fixture missing: ${input.path}")
        if (mappingsFile != null) assumeTrue(File(subFolderFile, mappingsFile).exists(), "fixture missing: ${File(subFolderFile, mappingsFile).path}")

        val mappings = if (mappingsFile.isNullOrEmpty()) null else Usmap(Path.of(subFolderFile.path, mappingsFile).toString())

        println(file)
        val tester = UAsset(input.path, version, mappings)
        assertTrue(tester.VerifyBinaryEquality())
        AssertAllExportsParsedCorrectly(tester)

        val jsonSerializedAsset = tester.SerializeJson()
        val rawJson = scratch(subFolder, "raw.json")
        File(rawJson).writeText(jsonSerializedAsset)

        val tester2 = UAsset.DeserializeJson(File(rawJson).readText())
        tester2.Mappings = mappings
        val modifiedPath = scratch(subFolder, "MODIFIED.uasset")
        tester2.Write(modifiedPath)

        // For the assets we're testing binary equality is maintained and can be used as a metric of success, but binary equality is not guaranteed for all assets
        assertTrue(input.readBytes().contentEquals(File(modifiedPath).readBytes()))
    }

    /// <summary>
    /// In this test, we serialize some assets to JSON and back to test if the JSON serialization system is functional.
    /// </summary>
    @Test
    fun TestJson() {
        assumeCorpus()
        TestJsonOnFile("PB_DT_RandomizerRoomCheck.uasset", EngineVersion.VER_UE4_18, "TestManyAssets/Bloodstained")
        TestJsonOnFile("m02VIL_004_Gimmick.umap", EngineVersion.VER_UE4_18, "TestManyAssets/Bloodstained")
        TestJsonOnFile("Staging_T2.umap", EngineVersion.VER_UE4_23, "TestManyAssets/Astroneer")
        TestJsonOnFile("Items.uasset", EngineVersion.VER_UE4_23) // string table
        TestJsonOnFile("ABP_SMG_A.uasset", EngineVersion.VER_UE4_25)
        TestJsonOnFile("WPN_LockOnRifle.uasset", EngineVersion.VER_UE4_25)
        TestJsonOnFile("Map_FrontEnd_Hotel_LS_Night.umap", EngineVersion.VER_UE4_27)
        TestJsonOnFile("AssetDatabase_AutoGenerated.uasset", EngineVersion.VER_UE4_27)
        TestJsonOnFile("RaceSimDataAsset.uasset", EngineVersion.VER_UE4_27)
        TestJsonOnFile("TurboAcres_Environment.uasset", EngineVersion.VER_UE4_27)
        TestJsonOnFile("MGA_HeavyWeapon_Parent.uasset", EngineVersion.VER_UE4_25, "TestJson", "Outriders.usmap")
        TestJsonOnFile("Atlas_6x4_Semi.uasset", EngineVersion.VER_UE5_5, "TestJson", "MotorTown.usmap")

        val path5_7 = "TestEditorUE5_7/Blueprints"
        TestJsonOnFile("BP_FirstPersonCameraManager.uasset", EngineVersion.VER_UE5_7, path5_7)
        TestJsonOnFile("BP_FirstPersonCharacter.uasset", EngineVersion.VER_UE5_7, path5_7)
        TestJsonOnFile("BP_FirstPersonGameMode.uasset", EngineVersion.VER_UE5_7, path5_7)
        TestJsonOnFile("BP_FirstPersonPlayerController.uasset", EngineVersion.VER_UE5_7, path5_7)
        TestJsonOnFile("BP_FP_CameraManager.uasset", EngineVersion.VER_UE5_7, path5_7)
    }

    /// <summary>
    /// In this test, we add a new property called "CoolProperty" in the tests assembly to test whether or not PropertyData-inheriting classes in dependent assemblies are registered by UAssetAPI.
    /// The JVM port has no assembly scanning; the test registers the custom property explicitly into the (shared) property registry before parsing.
    /// </summary>
    @Test
    fun TestCustomProperty() {
        assumeCorpus()
        // UAssetAPI.C# registers PropertyData subclasses from dependent assemblies via reflection; the
        // JVM port's MainSerializer builds a static registry, so register the test property by hand.
        MainSerializer.PropertyTypeRegistry["CoolProperty"] = RegistryEntry().apply {
            PropertyType = CoolPropertyData::class
            HasCustomStructSerialization = false
            Creator = { name -> CoolPropertyData(name) }
        }

        val tester = UAsset(Path.of(TestFolder, "TestCustomProperty", "AlternateStartActor.uasset").toString(), EngineVersion.VER_UE4_23)
        assertTrue(tester.VerifyBinaryEquality())
        AssertAllExportsParsedCorrectly(tester)

        // Make sure that there are no unknown properties, and that there is at least one CoolProperty with a value of 72
        var hasCoolProperty = false
        for (testExport in tester.Exports) {
            if (testExport is NormalExport) {
                for (prop in testExport.Data!!) {
                    assertFalse(prop is UnknownPropertyData)
                    if (prop is CoolPropertyData) {
                        hasCoolProperty = true
                        assertTrue(prop.Value == 72)
                    }
                }
            }
        }
        assertTrue(hasCoolProperty)
    }

    /// <summary>
    /// In this test, we verify that Ace Combat 7 decryption works.
    /// Binary equality is expected.
    /// </summary>
    @Test
    fun TestACE7() {
        assumeCorpus()
        val ace7Src = File(TestFolder, "TestACE7")
        assumeTrue(ace7Src.isDirectory, "fixture dir missing: ${ace7Src.path}")
        val ace7Dst = File(scratch("TestACE7")).apply { mkdirs() }

        // Create copies of original files (in the scratch dir, to keep the corpus read-only)
        for (f in ace7Src.listFiles()!!) {
            if (!f.isFile) continue
            f.copyTo(File(ace7Dst, f.name), overwrite = true)
            File(ace7Dst, f.name + ".bak").writeBytes(f.readBytes())
        }

        // Decrypt them
        val decrypter = AC7Decrypt()
        decrypter.Decrypt(File(ace7Dst, "plwp_6aam_a0.uasset").path, File(ace7Dst, "plwp_6aam_a0.uasset").path)
        decrypter.Decrypt(File(ace7Dst, "ex02_IGC_03_Subtitle.uasset").path, File(ace7Dst, "ex02_IGC_03_Subtitle.uasset").path)

        // Verify the files can be parsed
        var tester = UAsset(File(ace7Dst, "plwp_6aam_a0.uasset").path, EngineVersion.VER_UE4_18)
        assertTrue(tester.VerifyBinaryEquality())
        AssertAllExportsParsedCorrectly(tester)

        tester = UAsset(File(ace7Dst, "ex02_IGC_03_Subtitle.uasset").path, EngineVersion.VER_UE4_18)
        assertTrue(tester.VerifyBinaryEquality())
        AssertAllExportsParsedCorrectly(tester)

        // Encrypt them
        decrypter.Encrypt(File(ace7Dst, "plwp_6aam_a0.uasset").path, File(ace7Dst, "plwp_6aam_a0.uasset").path)
        decrypter.Encrypt(File(ace7Dst, "ex02_IGC_03_Subtitle.uasset").path, File(ace7Dst, "ex02_IGC_03_Subtitle.uasset").path)

        // Verify binary equality
        for (path in ace7Dst.listFiles()!!.filter { it.name.endsWith(".bak") }) {
            VerifyBinaryEquality(path.path, path.path.dropLast(4))
        }
    }

    /// <summary>
    /// In this test, we verify that material assets parses correctly and maintains binary equality.
    /// Binary equality is expected.
    /// </summary>
    @Test
    fun TestMaterials() {
        assumeCorpus()
        // Verify the files can be parsed
        var tester = UAsset(Path.of(TestFolder, "TestMaterials", "M_COM_DetailMaster_B.uasset").toString(), EngineVersion.VER_UE4_18)
        assertTrue(tester.VerifyBinaryEquality())
        AssertAllExportsParsedCorrectly(tester)

        tester = UAsset(Path.of(TestFolder, "TestMaterials", "as_mt_base.uasset").toString(), EngineVersion.VER_UE4_20)
        assertTrue(tester.VerifyBinaryEquality())
        AssertAllExportsParsedCorrectly(tester)
    }

    /// <summary>
    /// In this test, we are trying to read a source asset.
    /// Binary equality is expected.
    /// </summary>
    @Test
    fun TestEditorAssets() {
        assumeCorpus()
        val soundClass = UAsset(Path.of(TestFolder, "TestEditorAssets", "TestSoundClass.uasset").toString(), EngineVersion.VER_UE4_27)
        assertTrue(soundClass.VerifyBinaryEquality())
        AssertAllExportsParsedCorrectly(soundClass)

        val material = UAsset(Path.of(TestFolder, "TestEditorAssets", "TestMaterial.uasset").toString(), EngineVersion.VER_UE4_27)
        assertTrue(material.VerifyBinaryEquality())
        AssertAllExportsParsedCorrectly(material)

        val blueprint = UAsset(Path.of(TestFolder, "TestEditorAssets", "TestActorBP.uasset").toString(), EngineVersion.VER_UE4_27)
        assertTrue(blueprint.VerifyBinaryEquality())
        AssertAllExportsParsedCorrectly(blueprint)
    }

    /// <summary>
    /// In this test, we test several traditional assets specifically from Unreal Engine 5.3 games.
    /// Binary equality is expected.
    /// </summary>
    @Test
    fun TestTraditionalUE5_3() {
        assumeCorpus()
        TestSubsection("Engine", TestFolder, "TestUE5_3", EngineVersion.VER_UE5_3, Usmap(Path.of(TestFolder, "TestUE5_3", "Engine", "Engine.usmap").toString()))
        TestSubsection("RON", TestFolder, "TestUE5_3", EngineVersion.VER_UE5_3, Usmap(Path.of(TestFolder, "TestUE5_3", "RON", "ReadyOrNot.usmap").toString()))
    }

    /// <summary>
    /// In this test, we test several traditional assets specifically from Unreal Engine 5.4 games.
    /// Binary equality is expected.
    /// </summary>
    @Test
    fun TestTraditionalUE5_4() {
        assumeCorpus()
        TestSubsection("BlankGame", TestFolder, "TestUE5_4", EngineVersion.VER_UE5_4, Usmap(Path.of(TestFolder, "TestUE5_4", "BlankGame", "BlankGame_Dumper-7.usmap").toString()))
        TestSubsection("Bellwright", TestFolder, "TestUE5_4", EngineVersion.VER_UE5_4, Usmap(Path.of(TestFolder, "TestUE5_4", "Bellwright", "Bellwright.usmap").toString()))
        TestSubsection("TheForeverWinter", TestFolder, "TestUE5_4", EngineVersion.VER_UE5_4, Usmap(Path.of(TestFolder, "TestUE5_4", "TheForeverWinter", "TheForeverWinter.usmap").toString()))
        TestSubsection("Billiards", TestFolder, "TestUE5_4", EngineVersion.VER_UE5_4, Usmap(Path.of(TestFolder, "TestUE5_4", "Billiards", "5.4.3-34507850+++UE5+Release-5.4-DeepSpace7.usmap").toString()))
        TestSubsection("JOY", TestFolder, "TestUE5_4", EngineVersion.VER_UE5_4, Usmap(Path.of(TestFolder, "TestUE5_4", "JOY", "5.4.3-34507850+++UE5+Release-5.4-JOY.usmap").toString()))
    }

    /// <summary>
    /// In this test, we test several traditional assets specifically from Unreal Engine 5.5 games.
    /// Binary equality is expected.
    /// </summary>
    @Test
    fun TestTraditionalUE5_5() {
        assumeCorpus()
        TestSubsection("BlankGame", TestFolder, "TestUE5_5", EngineVersion.VER_UE5_5, Usmap(Path.of(TestFolder, "TestUE5_5", "BlankGame", "BlankUE5_5.usmap").toString()))
    }

    /// <summary>
    /// In this test, we test several traditional assets specifically from Unreal Engine 5.6 games.
    /// Binary equality is expected.
    /// </summary>
    @Test
    fun TestTraditionalUE5_6() {
        assumeCorpus()
        TestSubsection("BpThirdPerson", TestFolder, "TestUE5_6", EngineVersion.VER_UE5_6, Usmap(Path.of(TestFolder, "TestUE5_6", "BpThirdPerson", "ExplicitEnumValuesExample.usmap").toString()))
    }

    /// <summary>
    /// In this test, we test several traditional assets specifically from Unreal Engine 5.7 games.
    /// Binary equality is expected.
    /// </summary>
    @Test
    fun TestTraditionalUE5_7() {
        assumeCorpus()
        TestSubsection("NanosWorld", TestFolder, "TestUE5_7", EngineVersion.VER_UE5_7, Usmap(Path.of(TestFolder, "TestUE5_7", "NanosWorld", "NanosWorld.usmap").toString()))
        TestSubsection("FarFarWest", TestFolder, "TestUE5_7", EngineVersion.VER_UE5_7, Usmap(Path.of(TestFolder, "TestUE5_7", "FarFarWest", "FarFarWest.usmap").toString()))
    }

    /// <summary>
    /// In this test, we test several editor assets specifically from Unreal Engine 5.7 games.
    /// Binary equality is expected.
    /// </summary>
    @Test
    fun TestEditorUE5_7() {
        assumeCorpus()
        TestSubsection("Blueprints", TestFolder, "TestEditorUE5_7", EngineVersion.VER_UE5_7)
    }

    /// <summary>
    /// In this test, we test the Clone function, along with indexers for assets and exports.
    /// </summary>
    @Test
    fun TestClone() {
        assumeCorpus()
        val mappings = Usmap(Path.of(TestFolder, "TestUE5_3", "RON", "ReadyOrNot.usmap").toString())

        // Work on a scratch copy so the corpus asset is not rewritten in place (C# writes into TestFolder)
        val blueprintPath = copyToScratch("TestUE5_3/RON/AmmoDataTable.uasset")

        // clone everything and check for binary equality
        val blueprint = UAsset(blueprintPath, EngineVersion.VER_UE5_3, mappings)
        for (i in blueprint.Exports.indices) {
            val curExp = blueprint.Exports[i]
            if (curExp is NormalExport) {
                for (j in curExp.Data!!.indices) {
                    curExp.Data!![j] = curExp.Data!![j].clone()
                }
            }
            if (curExp is DataTableExport) {
                for (j in curExp.Table!!.Data.indices) {
                    curExp.Table!!.Data[j] = curExp.Table!!.Data[j].clone() as StructPropertyData
                }
            }
        }
        assertTrue(blueprint.VerifyBinaryEquality())

        // some basic tests with the indexers
        val exp = blueprint["AmmoDataTable"] as DataTableExport
        val struc = exp["556x45JHP"] as StructPropertyData
        val nuevo = struc.clone() as StructPropertyData
        nuevo["Damage"] = FloatPropertyData().apply { Value = 60f }
        exp["556x45JHP_MODIFIED"] = nuevo

        // save, read again, and verify
        blueprint.Write(blueprint.FilePath)

        val blueprint2 = UAsset(blueprint.FilePath, EngineVersion.VER_UE5_3, mappings)
        assertTrue(blueprint2.VerifyBinaryEquality())

        val exp2 = blueprint["AmmoDataTable"] as DataTableExport
        val struc2 = exp["556x45JHP"] as StructPropertyData
        val struc2_2 = exp["556x45JHP_MODIFIED"] as StructPropertyData
        assertTrue(struc2["Damage"] is FloatPropertyData && (struc2["Damage"] as FloatPropertyData).Value == 30f)
        assertTrue(struc2_2["Damage"] is FloatPropertyData && (struc2_2["Damage"] as FloatPropertyData).Value == 60f)
    }

    /// <summary>
    /// In this test, we save and load a .pak file to verify functionality of the repak interop.
    /// The C# test drives the external repak tool via PakBuilder/PakReader, which has no JVM port.
    /// </summary>
    @Disabled("Requires the external repak tool (PakBuilder/PakReader interop); not portable to the JVM port")
    @Test
    fun TestRepak() {
        // C#: builds output2.pak via repak, reads it back, and prints each file's contents
    }

    /// <summary>
    /// In this test, we parse a .usmap containing an OptionalProperty (as currently produced by Dumper-7) to verify compatibility.
    /// </summary>
    @Test
    fun TestUsmapWithOptionalProperty() {
        assumeCorpus()
        val usmap = Usmap(Path.of(TestFolder, "TestUE5_4", "BlankGame", "BlankGame_Dumper-7.usmap").toString())
        assertEquals(31948, usmap.NameMap.size)
        assertEquals(1565, usmap.EnumMap.size)
        assertEquals(7657, usmap.Schemas.size)
    }

    /// <summary>
    /// In this test, we parse a .usmap with explicit enum values.
    /// </summary>
    @Test
    fun TestUsmapWithExplicitEnumValues() {
        assumeCorpus()
        val usmap = Usmap(Path.of(TestFolder, "TestUE5_6", "BpThirdPerson", "ExplicitEnumValuesExample.usmap").toString())
        assertEquals(36767, usmap.NameMap.size)
        assertEquals(1739, usmap.EnumMap.size)
        assertEquals(9230, usmap.Schemas.size)
    }

    /// <summary>
    /// In this test, we do tests for various underlying enum types within a DataTable row to ensure that it parses correctly and maintains binary equality.
    /// </summary>
    @Test
    fun TestUnderlyingEnumTypes() {
        assumeCorpus()
        val usmap = Usmap(Path.of(TestFolder, "TestUE5_1", "UnderlyingEnumTypes", "UnderlyingEnumTypes.usmap").toString())
        val assetPath = Path.of(TestFolder, "TestUE5_1", "UnderlyingEnumTypes", "NewDataTable.uasset").toString()
        val tester = UAsset(assetPath, EngineVersion.VER_UE5_1, usmap)
        assertTrue(tester.VerifyBinaryEquality())
        AssertAllExportsParsedCorrectly(tester)
        assertTrue(tester.Exports.size == 1)

        val ourDataTableExport = tester.Exports[0] as? DataTableExport
        val ourTable = ourDataTableExport?.Table
        assertNotNull(ourTable)

        // Check out the first entry to make sure it's parsing alright
        var firstEntry = ourTable!!.Data[1]
        for (i in firstEntry.Value!!.indices) {
            val propData = firstEntry.Value!![i]
            if (propData is EnumPropertyData && propData.InnerType?.Value?.Value != "Int64Property") propData.Value = FName.DefineDummy(tester, "Two")
            if (propData is EnumPropertyData && propData.InnerType?.Value?.Value == "Int64Property") propData.Value = FName.DefineDummy(tester, "None")
        }

        // Save the modified table
        val modifiedPath = scratch("TestUnderlyingEnumTypes", "MODIFIED.uasset")
        tester.Write(modifiedPath)

        // Load the modified table back in and make sure we're good
        val tester2 = UAsset(modifiedPath, EngineVersion.VER_UE5_1, usmap)
        assertTrue(tester2.VerifyBinaryEquality())
        AssertAllExportsParsedCorrectly(tester2)
        assertTrue(tester2.Exports.size == 1)

        firstEntry = (tester2.Exports[0] as? DataTableExport)?.Table?.Data?.get(1)!!
        assertNotNull(firstEntry)
        println("${"#".padStart(2)} ${"Name".padEnd(20)} ${"Type".padEnd(15)} ${"Sub Type".padEnd(15)} ${"Value".padEnd(10)} Offset")
        println("-------------------------------------------------------------------------")
        for (i in firstEntry.Value!!.indices) {
            val propData = firstEntry.Value!![i]
            if (propData is EnumPropertyData) {
                println("$i: ${propData.Name.toString().padEnd(20)} ${propData.PropertyType.toString().padEnd(15)} ${(propData.InnerType?.Value?.Value ?: "").padEnd(15)} ${(propData.RawValue ?: "").toString().padEnd(10)} ${propData.Offset}")
            } else {
                println("$i: ${propData.Name.toString().padEnd(20)} ${propData.PropertyType.toString().padEnd(15)} ${"None".padEnd(15)} ${(propData.RawValue ?: "").toString().padEnd(10)} ${propData.Offset}")
            }
        }

        // Save and check that it's binary equal to what we originally had
        tester2.Write(tester2.FilePath)
        assertTrue(File(assetPath).readBytes().contentEquals(File(modifiedPath).readBytes()))
    }

    /// <summary>
    /// In this test, we trace reads through a debug wrapper stream.
    /// The C# test is compiled behind #if DEBUGTRACING and drives internal Trace.LoggingAspect debug hooks.
    /// </summary>
    @Disabled("C# guards TestTracing behind #if DEBUGTRACING and uses internal Trace.LoggingAspect debug hooks; not portable to the JVM port")
    @Test
    fun TestTracing() {
        // C# (DEBUGTRACING builds only): wraps M_COM_DetailMaster_B.uasset in a TraceStream and parses it
    }
}

/// <summary>
/// JVM stand-in for the C# tests assembly's CoolPropertyData (registered into MainSerializer's
/// property registry by TestCustomProperty, mirroring UAssetAPI's reflection-based assembly scan).
/// </summary>
class CoolPropertyData : PropertyData {
    var Value: Int = 0

    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = reader.ReadByte()
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        writer.WriteByte(Value)
        return 1
    }

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("CoolProperty")
    }
}
