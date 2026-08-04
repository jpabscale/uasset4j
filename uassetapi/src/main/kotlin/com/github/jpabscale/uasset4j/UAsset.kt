// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UAsset.cs
// NOTE: Full statement-level port of the binary Read/Write engine. JSON methods (SerializeJson/
// DeserializeJson) are signatures only and throw NotImplementedError("UAsset JSON: L5"). The
// schema-pulling block in ConvertExportToChildExportAndRead references the L4 stub
// Usmap.GetSchemaFromStructExport. Kismet bytecode paths remain M6 stubs.
package com.github.jpabscale.uasset4j

import com.github.jpabscale.uasset4j.customversions.FAnimPhysObjectVersion
import com.github.jpabscale.uasset4j.customversions.FAssetRegistryVersion
import com.github.jpabscale.uasset4j.customversions.FFortniteMainBranchObjectVersion
import com.github.jpabscale.uasset4j.customversions.FFortniteReleaseBranchCustomObjectVersion
import com.github.jpabscale.uasset4j.customversions.FFrameworkObjectVersion
import com.github.jpabscale.uasset4j.customversions.FCoreObjectVersion
import com.github.jpabscale.uasset4j.customversions.FEditorObjectVersion
import com.github.jpabscale.uasset4j.customversions.FInstancedStructCustomVersion
import com.github.jpabscale.uasset4j.customversions.FNiagaraCustomVersion
import com.github.jpabscale.uasset4j.customversions.FNiagaraObjectVersion
import com.github.jpabscale.uasset4j.customversions.FReleaseObjectVersion
import com.github.jpabscale.uasset4j.customversions.FSequencerObjectVersion
import com.github.jpabscale.uasset4j.customversions.FUE5ReleaseStreamObjectVersion
import com.github.jpabscale.uasset4j.customversions.FUE5SpecialProjectStreamObjectVersion
import com.github.jpabscale.uasset4j.json.UAssetJson
import com.github.jpabscale.uasset4j.exporttypes.AssetImportDataExport
import com.github.jpabscale.uasset4j.exporttypes.ClassExport
import com.github.jpabscale.uasset4j.exporttypes.CurveTableExport
import com.github.jpabscale.uasset4j.exporttypes.DataTableExport
import com.github.jpabscale.uasset4j.exporttypes.EnumExport
import com.github.jpabscale.uasset4j.exporttypes.Export
import com.github.jpabscale.uasset4j.exporttypes.FunctionExport
import com.github.jpabscale.uasset4j.exporttypes.LevelExport
import com.github.jpabscale.uasset4j.exporttypes.MetaDataExport
import com.github.jpabscale.uasset4j.exporttypes.NormalExport
import com.github.jpabscale.uasset4j.exporttypes.PropertyExport
import com.github.jpabscale.uasset4j.exporttypes.RawExport
import com.github.jpabscale.uasset4j.exporttypes.SceneComponentExport
import com.github.jpabscale.uasset4j.exporttypes.StringTableExport
import com.github.jpabscale.uasset4j.exporttypes.StructExport
import com.github.jpabscale.uasset4j.exporttypes.UserDefinedStructExport
import com.github.jpabscale.uasset4j.fieldtypes.FMapProperty
import com.github.jpabscale.uasset4j.fieldtypes.FProperty
import com.github.jpabscale.uasset4j.fieldtypes.FStructProperty
import com.github.jpabscale.uasset4j.propertytypes.objects.AncestryInfo
import com.github.jpabscale.uasset4j.propertytypes.objects.FSoftObjectPath
import com.github.jpabscale.uasset4j.unrealtypes.EngineVersion
import com.github.jpabscale.uasset4j.unrealtypes.EObjectDataResourceFlags
import com.github.jpabscale.uasset4j.unrealtypes.EObjectDataResourceVersion
import com.github.jpabscale.uasset4j.unrealtypes.FGatherableTextData
import com.github.jpabscale.uasset4j.unrealtypes.FGuid
import com.github.jpabscale.uasset4j.unrealtypes.FMetaData
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FObjectDataResource
import com.github.jpabscale.uasset4j.unrealtypes.FObjectThumbnail
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.FTextSourceData
import com.github.jpabscale.uasset4j.unrealtypes.FTextSourceSiteContext
import com.github.jpabscale.uasset4j.unrealtypes.FWorldTileInfo
import com.github.jpabscale.uasset4j.unrealtypes.GameSpecificOverride as UEGameSpecificOverride
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion as UEObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5 as UEObjectVersionUE5
import com.github.jpabscale.uasset4j.unrealtypes.UE4VersionToObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.UE5VersionToObjectVersion
import com.github.jpabscale.uasset4j.unversioned.Usmap
import com.github.jpabscale.uasset4j.unversioned.UsmapEnum
import com.github.jpabscale.uasset4j.unversioned.UsmapSchema
import com.github.jpabscale.uasset4j.util.Out
import com.github.jpabscale.uasset4j.CustomSerializationFlags as UECustomSerializationFlags
import java.nio.file.Files
import java.nio.file.Paths
import java.util.TreeMap

enum class Formatting {
    None,
    Indented,
}

/** A type resource used by the import type hierarchy map. */
class FTypeResource {
    var TypeName: FName? = null
    var PackageName: FName? = null
    var ClassName: FName? = null
    var ClassPackageName: FName? = null

    fun Write(writer: AssetBinaryWriter) {
        writer.Write(TypeName)
        writer.Write(PackageName)
        writer.Write(ClassName)
        writer.Write(ClassPackageName)
    }

    constructor(reader: AssetBinaryReader) {
        TypeName = reader.ReadFName()
        PackageName = reader.ReadFName()
        ClassName = reader.ReadFName()
        ClassPackageName = reader.ReadFName()
    }

    constructor()
}

/** Hierarchical type information for an import. */
class FImportTypeHierarchy {
    var SuperTypes: Array<FTypeResource> = emptyArray()

    fun Write(writer: AssetBinaryWriter) {
        writer.WriteInt32(SuperTypes.size)
        for (superType in SuperTypes) superType.Write(writer)
    }

    constructor(reader: AssetBinaryReader) {
        SuperTypes = reader.ReadArray { FTypeResource(reader) }
    }

    constructor()
}

interface INameMap {
    fun GetNameMapIndexList(): List<FString>
    fun ClearNameIndexList()
    fun SetNameReference(index: Int, value: FString)
    fun GetNameReference(index: Int): FString
    fun ContainsNameReference(search: FString): Boolean
    fun SearchNameReference(search: FString): Int
    fun AddNameReference(name: FString, forceAddDuplicates: Boolean = false, skipFixes: Boolean = false): Int
    fun CanCreateDummies(): Boolean
}

class NameMapOutOfRangeException(requiredName: FString) :
    RuntimeException("Requested name \"${requiredName.Value}\" not found in name map")

class InvalidMappingsException(message: String = "Unversioned properties cannot be serialized without valid mappings") :
    RuntimeException(message)

class UnknownEngineVersionException(message: String) : RuntimeException(message)

@JvmInline
value class EPackageFlags(val value: Int) {
    fun HasFlag(flag: Int): Boolean = (value and flag) == flag

    companion object {
        const val PKG_None = 0x00000000
        const val PKG_NewlyCreated = 0x00000001
        const val PKG_ClientOptional = 0x00000002
        const val PKG_ServerSideOnly = 0x00000004
        const val PKG_CompiledIn = 0x00000010
        const val PKG_ForDiffing = 0x00000020
        const val PKG_EditorOnly = 0x00000040
        const val PKG_Developer = 0x00000080
        const val PKG_UncookedOnly = 0x00000100
        const val PKG_Cooked = 0x00000200
        const val PKG_ContainsNoAsset = 0x00000400
        const val PKG_UnversionedProperties = 0x00002000
        const val PKG_ContainsMapData = 0x00004000
        const val PKG_Compiling = 0x00010000
        const val PKG_ContainsMap = 0x00020000
        const val PKG_RequiresLocalizationGather = 0x00040000
        const val PKG_PlayInEditor = 0x00100000
        const val PKG_ContainsScript = 0x00200000
        const val PKG_DisallowExport = 0x00400000
        const val PKG_DynamicImports = 0x10000000
        const val PKG_RuntimeGenerated = 0x20000000
        const val PKG_ReloadingForCooker = 0x40000000
        const val PKG_FilterEditorOnly = 0x80000000.toInt()
    }
}

/** Holds basic Unreal version numbers. */
class FEngineVersion {
    var Major: Int = 0
    var Minor: Int = 0
    var Patch: Int = 0
    var Changelist: Long = 0
    var Branch: FString? = null

    fun Write(writer: UnrealBinaryWriter) {
        writer.WriteUInt16(Major)
        writer.WriteUInt16(Minor)
        writer.WriteUInt16(Patch)
        writer.WriteUInt32(Changelist)
        writer.Write(Branch)
    }

    constructor(reader: UnrealBinaryReader) {
        Major = reader.ReadUInt16()
        Minor = reader.ReadUInt16()
        Patch = reader.ReadUInt16()
        Changelist = reader.ReadUInt32()
        Branch = reader.ReadFString()
    }

    constructor(major: Int, minor: Int, patch: Int, changelist: Long, branch: FString) {
        Major = major
        Minor = minor
        Patch = patch
        Changelist = changelist
        Branch = branch
    }

    constructor()
}

/** Revision data for an Unreal package file. */
class FGenerationInfo {
    var ExportCount: Int
    var NameCount: Int

    constructor(exportCount: Int, nameCount: Int) {
        ExportCount = exportCount
        NameCount = nameCount
    }

    constructor() {
        ExportCount = 0
        NameCount = 0
    }
}

/** An asset registry record (uncooked assets only). */
class FAssetRegistryRecord {
    var Path: String? = null
    var ClassName: String? = null
    var TagMap: LinkedHashMap<String, String?> = linkedMapOf()
}

/** Kotlin replacement for System.Collections.BitArray. */
class BitArray {
    var Length: Int = 0
    var bits: java.util.BitSet = java.util.BitSet()

    fun get(index: Int): Boolean = bits.get(index)

    fun CopyTo(data: ByteArray) {
        val src = bits.toByteArray()
        src.copyInto(data, 0)
    }
}

open class UAsset() : INameMap {
    /** Agent string to provide context in serialized JSON. */
    var Info: String = "Serialized with UAssetAPI"

    /** The path of the file on disk that this asset represents. */
    var FilePath: String = ""

    /** Whether this asset is only being parsed to extract schemas for parsing a different asset. */
    var IsParsingToPullSchemas: Boolean = false

    /** The corresponding mapping data for the game that this asset is from. */
    var Mappings: Usmap? = null

    /** List of custom serialization flags, used to override certain optional behavior in how UAssetAPI serializes assets. */
    var CustomSerializationFlags: UECustomSerializationFlags = UECustomSerializationFlags.None

    /** Should the asset be split into separate .uasset, .uexp, and .ubulk files, as opposed to one single .uasset file? */
    var UseSeparateBulkDataFiles: Boolean = false

    /** Should this asset not serialize its engine and custom versions? */
    var IsUnversioned: Boolean = false

    /** The licensee file version. Used by some games to add their own Engine-level versioning. */
    var FileVersionLicenseeUE: Int = 0

    /** Enum for selecting game-specific overrides. */
    var GameSpecificOverride: UEGameSpecificOverride = UEGameSpecificOverride.None

    /** The object version of UE4 that will be used to parse this asset. */
    var ObjectVersion: UEObjectVersion = UEObjectVersion.UNKNOWN

    /** The object version of UE5 that will be used to parse this asset. Set to [UEObjectVersionUE5.UNKNOWN] for UE4 games. */
    var ObjectVersionUE5: UEObjectVersionUE5 = UEObjectVersionUE5.UNKNOWN

    /** All the custom versions stored in the archive. */
    var CustomVersionContainer: MutableList<CustomVersion>? = null

    /**
     * In MapProperties that have StructProperties as their keys or values, there is no universal, context-free way to
     * determine the type of the struct. To that end, this dictionary maps MapProperty names to the type of the structs
     * within them (pair of key struct type and value struct type) if they are not None-terminated property lists.
     */
    var MapStructTypeOverride: LinkedHashMap<String, Pair<FString?, FString?>> = linkedMapOf(
        "ColorDatabase" to (null to FString("LinearColor")),
        "PlayerCharacterIDs" to (FString("Guid") to null),
        "m_PerConditionValueToNodeMap" to (FString("Guid") to null),
        "BindingIdToReferences" to (FString("Guid") to null),
        "UserParameterRedirects" to (FString("NiagaraVariable") to FString("NiagaraVariable")),
        "Tracks" to (FString("MovieSceneTrackIdentifier") to null),
        "TrackReferenceCounts" to (FString("MovieSceneTrackIdentifier") to null),
        "SubSequences" to (FString("MovieSceneSequenceID") to null),
        "Hierarchy" to (FString("MovieSceneSequenceID") to null),
        "TrackSignatureToTrackIdentifier" to (FString("Guid") to FString("MovieSceneTrackIdentifier")),
        "SoftwareCursors" to (FString("Guid") to FString("SoftClassPath")),
        "ItemsToRefund" to (FString("Guid") to null),
        "PlayerCharacterIDMap" to (FString("Guid") to null),
        "RainChanceMinMaxPerWeatherState" to (null to FString("FloatRange")),
        "Assets" to (FString("Guid") to null),
        "PlanetOffsets" to (null to FString("Vector")),
    )

    /**
     * In engine versions before VER_UE4_INNER_ARRAY_TAG_INFO: in ArrayProperties that have StructProperties as their
     * keys or values, there is no universal, context-free way to determine the type of the struct. To that end, this
     * dictionary maps ArrayProperty names to the type of the structs within them.
     */
    var ArrayStructTypeOverride: LinkedHashMap<String, FString> = linkedMapOf(
        "Keys" to FString("RichCurveKey"),
    )

    private var _packageFlags: EPackageFlags = EPackageFlags(0)
    var PackageFlags: EPackageFlags
        get() = _packageFlags
        set(value) {
            _packageFlags = value
            _hasUnversionedPropertiesCacheDirty = true
        }
    private var _hasUnversionedPropertiesCacheDirty: Boolean = true
    private var _hasUnversionedPropertiesCache: Boolean = false

    /** Whether or not this asset uses unversioned properties. */
    val HasUnversionedProperties: Boolean
        get() {
            if (_hasUnversionedPropertiesCacheDirty) {
                _hasUnversionedPropertiesCache = PackageFlags.HasFlag(EPackageFlags.PKG_UnversionedProperties)
                _hasUnversionedPropertiesCacheDirty = false
            }
            return _hasUnversionedPropertiesCache
        }

    /** Whether or not this asset has the PKG_FilterEditorOnly flag. */
    val IsFilterEditorOnly: Boolean get() = PackageFlags.HasFlag(EPackageFlags.PKG_FilterEditorOnly)

    internal val IsPreDependencyFormat: Boolean
        get() = IsFilterEditorOnly || ObjectVersion < UEObjectVersion.VER_UE4_ASSETREGISTRY_DEPENDENCYFLAGS

    internal var IsSerializationTime: Boolean = false

    /** Internal list of name map entries. Do not directly add values to here under any circumstances; use [AddNameReference] instead. */
    @JvmField
    var NameMapIndexList: MutableList<FString> = mutableListOf()

    /** Internal lookup for name map entries. Do not directly add values to here under any circumstances; use [AddNameReference] instead. */
    internal var NameMapLookup: MutableMap<String, Int> = mutableMapOf()

    /** List of SoftObjectPath contained in this package. */
    var SoftObjectPathList: MutableList<FSoftObjectPath>? = null

    /** Map of the gatherable text data. */
    var GatherableTextData: MutableList<FGatherableTextData>? = null

    /** List of object exports. UAssetAPI used to call these "categories." */
    var Exports: MutableList<Export> = mutableListOf()

    /** List of Searchable Names, by object containing them. Sorted to keep order consistent. */
    var SearchableNames: TreeMap<FPackageIndex, MutableList<FName>>? = null

    /** Map of hierarchical type information for FObjectImport Struct entries in the package. */
    var ImportTypeHierarchies: LinkedHashMap<FPackageIndex, FImportTypeHierarchy>? = null

    /** MetaData for the editor. */
    var MetaData: FMetaData? = null

    /** Map of object full names to the thumbnails. */
    var Thumbnails: LinkedHashMap<String, FObjectThumbnail>? = null

    /** Tile information used by WorldComposition. */
    var WorldTileInfo: FWorldTileInfo? = null

    /** The number of null bytes appended to the end of the package header (.uasset file). */
    var AppendedNullBytes: Int = 0

    /** Whether name hashes are expected to be serialized (lazily discovered during parsing). */
    var WillSerializeNameHashes: Boolean? = null

    /** The package file version number when this package was saved. */
    var LegacyFileVersion: Int = 0

    internal val CustomVersionSerializationFormat: ECustomVersionSerializationFormat
        get() {
            if (LegacyFileVersion > -3) return ECustomVersionSerializationFormat.Enums
            if (LegacyFileVersion > -6) return ECustomVersionSerializationFormat.Guids
            return ECustomVersionSerializationFormat.Optimized
        }

    /** The version to use for serializing data resources. */
    var DataResourceVersion: EObjectDataResourceVersion = EObjectDataResourceVersion.Invalid

    /** List of serialized UObject binary/bulk data resources. */
    var DataResources: MutableList<FObjectDataResource>? = null

    /** Whether or not this asset is loaded with the Event Driven Loader. */
    var UsesEventDrivenLoader: Boolean = false

    /** List of object imports. UAssetAPI used to call these "links." */
    var Imports: MutableList<Import> = mutableListOf()

    /** List of dependency lists for each export. */
    var DependsMap: MutableList<IntArray>? = null

    /** List of packages that are soft referenced by this package. */
    var SoftPackageReferenceList: MutableList<FString>? = null

    /** Offset to dependencies. This only appears in uncooked assets. */
    var AssetRegistryDependencyDataOffset: Long = -1

    /** Asset registry data. */
    var AssetRegistryRecords: MutableList<FAssetRegistryRecord>? = null

    /** Bits indicating if imports used in game are contained in import map. This only appears in uncooked assets. */
    var ImportBits: BitArray? = null

    /** Bits indicating if soft packages used in game are contained in soft package reference list. This only appears in uncooked assets. */
    var SoftPackageBits: BitArray? = null

    /** Extra package dependencies (collected build dependencies). */
    var ExtraPackageDependencies: Array<Pair<FName, Long>>? = null

    /** Any bulk data that is not stored in the export map. */
    var BulkData: ByteArray? = null

    var AdditionalFiles: ByteArray? = null

    var Trailer: ByteArray? = null

    /** Data about previous versions of this package. */
    var Generations: MutableList<FGenerationInfo> = mutableListOf()

    /** Current ID for this package. Effectively unused. */
    var PackageGuid: FGuid = FGuid(0u, 0u, 0u, 0u)

    /** Current persistent ID for this package. */
    var PersistentGuid: FGuid = FGuid(0u, 0u, 0u, 0u)

    /** Engine version this package was saved with. */
    var RecordedEngineVersion: FEngineVersion = FEngineVersion()

    /** Engine version this package is compatible with. */
    var RecordedCompatibleWithEngineVersion: FEngineVersion = FEngineVersion()

    /** Streaming install ChunkIDs. */
    var ChunkIDs: IntArray? = null

    /** Value that is used by the Unreal Engine to determine if the package was saved by Epic, a licensee, modder, etc. */
    var PackageSource: Long = 0

    /** In UE4: "FolderName"; In UE5: "PackageName". */
    var FolderName: FString? = null

    /** A map of name map entries to hashes to use when serializing instead of the default engine hash algorithm. */
    var OverrideNameMapHashes: LinkedHashMap<FString, Long>? = null

    /** "TotalHeaderSize" in UE4. */
    internal var SectionSixOffset: Int = 0

    /** Number of names used in this package. */
    internal var NameCount: Int = 0

    /** Location into the file on disk for the name data. */
    internal var NameOffset: Int = 0

    /** Localization ID of this package. */
    var LocalizationId: FString? = null

    internal var SoftObjectPathsCount: Int = 0
    internal var SoftObjectPathsOffset: Int = 0

    /** Number of gatherable text data items in this package. */
    internal var GatherableTextDataCount: Int = 0

    /** Location into the file on disk for the gatherable text data items. */
    internal var GatherableTextDataOffset: Int = 0

    /** Location into the file on disk for the MetaData data. */
    internal var MetaDataOffset: Int = 0

    /** Number of exports contained in this package. */
    internal var ExportCount: Int = 0

    /** Location into the file on disk for the "Export Details" data. */
    internal var ExportOffset: Int = 0

    /** Number of imports contained in this package. */
    internal var ImportCount: Int = 0

    /** Location into the file on disk for the ImportMap data. */
    internal var ImportOffset: Int = 0

    internal var CellExportCount: Int = 0
    internal var CellExportOffset: Int = 0
    internal var CellImportCount: Int = 0
    internal var CellImportOffset: Int = 0

    /** Location into the file on disk for the DependsMap data. */
    internal var DependsOffset: Int = 0

    internal var SoftPackageReferencesCount: Int = 0
    internal var SoftPackageReferencesOffset: Int = 0

    internal var SearchableNamesOffset: Int = 0
    internal var ThumbnailTableOffset: Int = 0

    var ImportTypeHierarchiesCount: Int = 0
    var ImportTypeHierarchiesOffset: Int = 0

    /** Hash of the Package's bytes when it was saved to disk. */
    internal var SavedHash: ByteArray? = null

    /** Should be zero. */
    internal var CompressionFlags: Long = 0

    /** List of additional packages that are needed to be cooked for this package. */
    internal var AdditionalPackagesToCook: MutableList<FString> = mutableListOf()

    /** Location into the file on disk for the asset registry tag data. */
    internal var AssetRegistryDataOffset: Int = 0

    /** Offset to the location in the file where the bulkdata starts. */
    internal var BulkDataStartOffset: Long = 0

    /** Offset to the location in the file where the FWorldTileInfo data start. */
    internal var WorldTileInfoDataOffset: Int = 0

    /** Number of preload dependencies contained in this package. */
    internal var PreloadDependencyCount: Int = 0

    /** Location into the file on disk for the preload dependency data. */
    internal var PreloadDependencyOffset: Int = 0

    internal var NamesReferencedFromExportDataCount: Int = 0
    internal var PayloadTocOffset: Long = -1
    internal var DataResourceOffset: Int = -1

    internal var doWeHaveAssetRegistryData: Boolean = true
    internal var doWeHaveWorldTileInfo: Boolean = true

    internal var haveWeLoadedDependencies: Boolean = false

    private var _cachedEngineVersion: EngineVersion = EngineVersion.UNKNOWN
    private var _cachedEngineVersionDirty: Boolean = true

    private var _internalAssetPath: String? = null
    internal var InternalAssetPath: String?
        get() {
            if (_internalAssetPath != null) return _internalAssetPath
            val folderName = this.FolderName?.Value
            if (folderName != null && folderName != "None") return folderName
            return null
        }
        set(value) {
            _internalAssetPath = value
        }

    internal var hasFoundParentClassExportName: Boolean = false
    internal var parentClassExportNameCache: FName? = null
    internal var parentClassExportName2Cache: FName? = null

    /** Set of assets that failed to be accessed when pulling schemas from another asset. */
    val OtherAssetsFailedToAccess: MutableSet<FName> = mutableSetOf()

    internal fun FixNameMapLookupIfNeeded() {
        if (NameMapIndexList.size > 0 && NameMapLookup.size == 0) {
            for (i in NameMapIndexList.indices) {
                NameMapLookup[NameMapIndexList[i].Value!!] = i
            }
        }
    }

    /** Returns the name map as a read-only list of FStrings. */
    override fun GetNameMapIndexList(): List<FString> {
        FixNameMapLookupIfNeeded()
        return NameMapIndexList
    }

    /** Clears the name map. This method should be used with extreme caution, as it may break unparsed references to the name map. */
    override fun ClearNameIndexList() {
        NameMapIndexList = mutableListOf()
        NameMapLookup = mutableMapOf()
    }

    /** Replaces a value in the name map at a particular index. */
    override fun SetNameReference(index: Int, value: FString) {
        FixNameMapLookupIfNeeded()
        NameMapIndexList[index] = value
        NameMapLookup[value.Value!!] = index
    }

    /** Gets a value in the name map at a particular index. */
    override fun GetNameReference(index: Int): FString {
        FixNameMapLookupIfNeeded()
        if (index < 0) return FString((-index).toString())
        if (index >= NameMapIndexList.size) return FString(index.toString())
        return NameMapIndexList[index]
    }

    /** Gets a value in the name map at a particular index, but with the index zero being treated as if it is not valid. */
    fun GetNameReferenceWithoutZero(index: Int): FString {
        FixNameMapLookupIfNeeded()
        if (index <= 0) return FString((-index).toString())
        if (index >= NameMapIndexList.size) return FString(index.toString())
        return NameMapIndexList[index]
    }

    /** Checks whether or not the value exists in the name map. */
    override fun ContainsNameReference(search: FString): Boolean {
        FixNameMapLookupIfNeeded()
        return NameMapLookup.containsKey(search.Value)
    }

    /** Searches the name map for a particular value. */
    override fun SearchNameReference(search: FString): Int {
        if (ContainsNameReference(search)) return NameMapLookup[search.Value]!!
        throw NameMapOutOfRangeException(search)
    }

    /** Adds a new value to the name map. */
    override fun AddNameReference(name: FString, forceAddDuplicates: Boolean, skipFixes: Boolean): Int {
        FixNameMapLookupIfNeeded()

        if (!forceAddDuplicates) {
            if (name.Value == null) throw IllegalArgumentException("Cannot add a null FString to the name map")
            if (name.Value == "") throw IllegalArgumentException("Cannot add an empty FString to the name map")
            if (ContainsNameReference(name)) return SearchNameReference(name)
        }

        if (IsSerializationTime) throw IllegalStateException("Attempt to add name \"$name\" to name map during serialization time")
        NameMapIndexList.add(name)
        NameMapLookup[name.Value!!] = NameMapIndexList.size - 1
        if (!skipFixes) NamesReferencedFromExportDataCount = NameMapIndexList.size
        return NameMapIndexList.size - 1
    }

    /** Whether or not we can create dummies in this name map. */
    override fun CanCreateDummies(): Boolean {
        if (IsSerializationTime) return true
        return !this.CustomSerializationFlags.HasFlag(UECustomSerializationFlags.NoDummies)
    }

    /** Creates a byte array from an asset path (concatenating the .uexp file if present). */
    fun PathToStream(p: String, loadUEXP: Boolean = true): ByteArray {
        val origStream = java.io.File(p).readBytes()
        val completeStream = java.io.ByteArrayOutputStream()
        completeStream.write(origStream)

        if (loadUEXP) {
            UseSeparateBulkDataFiles = false
            try {
                val targetFile = p.substringBeforeLast('.', p) + ".uexp"
                if (java.io.File(targetFile).exists()) {
                    completeStream.write(java.io.File(targetFile).readBytes())
                    UseSeparateBulkDataFiles = true
                }
            } catch (e: java.io.FileNotFoundException) {
            }
        }

        return completeStream.toByteArray()
    }

    /** Creates a BinaryReader from an asset path. */
    fun PathToReader(p: String, loadUEXP: Boolean = true): AssetBinaryReader {
        return AssetBinaryReader(PathToStream(p, loadUEXP), this, loadUEXP)
    }

    /** Gets or sets the export associated with the specified key. */
    open operator fun get(key: FName): Export? {
        for (i in Exports.indices) {
            if (Exports[i].ObjectName == key) return Exports[i]
        }
        return null
    }

    open operator fun set(key: FName, value: Export) {
        value.ObjectName = key
        for (i in Exports.indices) {
            if (Exports[i].ObjectName == key) {
                Exports[i] = value
                return
            }
        }

        Exports.add(value)
    }

    /** Gets or sets the export associated with the specified key. */
    open operator fun get(key: String): Export? {
        val fName = FName.FromString(this, key) ?: return null
        return this[fName]
    }

    open operator fun set(key: String, value: Export) {
        val fName = FName.FromString(this, key) ?: return
        this[fName] = value
    }

    /** Searches for and returns this asset's ClassExport, if one exists. */
    fun GetClassExport(): ClassExport? {
        for (cat in Exports) {
            if (cat is ClassExport) return cat
        }
        return null
    }

    /** Resolves the ancestry of all properties present in this asset. */
    open fun ResolveAncestries() {
        if (WorldTileInfo != null) WorldTileInfo!!.ResolveAncestries(this, AncestryInfo())
        for (i in Exports.indices) Exports[i].ResolveAncestries(this, AncestryInfo())
    }

    /** Attempt to find another asset on disk given an asset path (starting with /Game/ or within a plugin). */
    open fun FindAssetOnDiskFromPath(path: String): String? {
        if (!path.startsWith("/") || path.startsWith("/Script")) return null
        val firstIdxWithoutSlash = path.indexOf('/') + 1
        val secondIdxWithoutSlash = path.indexOf('/', firstIdxWithoutSlash) + 1
        val pathPrefixPart = path.substring(firstIdxWithoutSlash, secondIdxWithoutSlash - 1)
        val pathNoPrefix = path.substring(secondIdxWithoutSlash) + ".uasset"

        var mappedPathOnDisk = ""
        var foundMappedPath = false

        val desiredPathRelativeToContent: String
        when (pathPrefixPart) {
            "Game" -> desiredPathRelativeToContent = UAPUtils.FixDirectorySeparatorsForDisk(pathNoPrefix)
            else -> desiredPathRelativeToContent = ".." + java.io.File.separator + "Plugins" + java.io.File.separator + pathPrefixPart + java.io.File.separator + "Content" + java.io.File.separator + UAPUtils.FixDirectorySeparatorsForDisk(pathNoPrefix)
        }

        val contentPart = java.io.File.separator + "Content"
        val pluginsPart = java.io.File.separator + "Plugins"
        if (!FilePath.isNullOrEmpty()) {
            val fixedFilePath = UAPUtils.FixDirectorySeparatorsForDisk(FilePath)
            val contentIndex = fixedFilePath.lastIndexOf(contentPart)
            val pluginsIndex = fixedFilePath.lastIndexOf(pluginsPart)

            var contentDir: String? = null
            if (!foundMappedPath && contentIndex > 0) {
                contentDir = fixedFilePath.substring(0, contentIndex + contentPart.length)
            }

            if (!foundMappedPath && pluginsIndex > 0) {
                contentDir = fixedFilePath.substring(0, pluginsIndex + pluginsPart.length) + java.io.File.separator + ".." + java.io.File.separator + "Content"
            }

            if (contentDir != null) {
                mappedPathOnDisk = Paths.get(contentDir, desiredPathRelativeToContent).toString()
                foundMappedPath = java.io.File(mappedPathOnDisk).exists()
            }

            if (!foundMappedPath) {
                mappedPathOnDisk = java.io.File(java.io.File(FilePath).parentFile?.path ?: "", java.io.File(pathNoPrefix).name).path
                foundMappedPath = java.io.File(mappedPathOnDisk).exists()
            }
        }

        return if (foundMappedPath) mappedPathOnDisk else null
    }

    /** Sets the version of the Unreal Engine to use in serialization. */
    fun SetEngineVersion(newVersion: EngineVersion) {
        if (newVersion == EngineVersion.UNKNOWN) return
        val bridgeVer = UE4VersionToObjectVersion.entries.firstOrNull { it.name == newVersion.name }
            ?: throw IllegalStateException("Invalid engine version specified")
        ObjectVersion = UEObjectVersion.entries.firstOrNull { it.value == bridgeVer.value } ?: UEObjectVersion.UNKNOWN

        val bridgeVer2 = UE5VersionToObjectVersion.entries.firstOrNull { it.name == newVersion.name }
        if (bridgeVer2 != null) ObjectVersionUE5 = UEObjectVersionUE5.entries.firstOrNull { it.value == bridgeVer2.value } ?: UEObjectVersionUE5.UNKNOWN

        CustomVersionContainer = GetDefaultCustomVersionContainer(newVersion)
    }

    /** Estimates the retail version of the Unreal Engine based on the object and custom versions. */
    fun GetEngineVersion(): EngineVersion {
        if (IsSerializationTime) {
            if (_cachedEngineVersionDirty) {
                _cachedEngineVersionDirty = false
                _cachedEngineVersion = Companion.GetEngineVersion(ObjectVersion, ObjectVersionUE5, CustomVersionContainer)
            }
            return _cachedEngineVersion
        }

        _cachedEngineVersionDirty = true
        return Companion.GetEngineVersion(ObjectVersion, ObjectVersionUE5, CustomVersionContainer)
    }

    /** Fetches the version of a custom version in this asset. */
    fun GetCustomVersion(key: FGuid): Int {
        val container = CustomVersionContainer ?: return -1
        for (custVer in container) {
            if (custVer.Key == key) {
                return custVer.Version
            }
        }

        return -1
    }

    /** Fetches the version of a custom version in this asset. */
    fun GetCustomVersion(friendlyName: String): Int {
        val container = CustomVersionContainer ?: return -1
        for (custVer in container) {
            if (custVer.FriendlyName == friendlyName) {
                return custVer.Version
            }
        }

        return -1
    }

    /** Fetches a custom version's enum value based off of its type. */
    inline fun <reified T : Enum<T>> GetCustomVersion(): Int {
        val container = CustomVersionContainer ?: return -1
        val friendlyName = T::class.java.simpleName
        for (custVer in container) {
            if (custVer.FriendlyName == friendlyName) {
                return custVer.Version
            }
        }

        return -1
    }

    /** Reads an export from disk. */
    fun ParseExport(reader: AssetBinaryReader, i: Int, read: Boolean = true) {
        reader.position = Exports[i].SerialOffset.toInt()
        ConvertExportToChildExportAndRead(reader, i, read)
    }

    fun ConvertExportToChildExportAndRead(reader: AssetBinaryReader, i: Int, read: Boolean = true) {
        try {
            val nextStarting: Long = if ((Exports.size - 1) > i) {
                Exports[i + 1].SerialOffset
            } else {
                (reader.Asset as UAsset).BulkDataStartOffset
            }

            val exportClassTypeName = Exports[i].GetExportClassType()!!
            val exportClassType = exportClassTypeName.Value!!.Value!!
            when (exportClassType) {
                "Level" -> Exports[i] = Exports[i].ConvertToChildExport<LevelExport>()
                "Enum", "UserDefinedEnum" -> Exports[i] = Exports[i].ConvertToChildExport<EnumExport>()
                "Function" -> Exports[i] = Exports[i].ConvertToChildExport<FunctionExport>()
                "UserDefinedStruct" -> Exports[i] = Exports[i].ConvertToChildExport<UserDefinedStructExport>()
                "MetaData" -> Exports[i] = Exports[i].ConvertToChildExport<MetaDataExport>()
                "AssetImportData" -> Exports[i] = Exports[i].ConvertToChildExport<AssetImportDataExport>()
                else -> {
                    when {
                        exportClassType.endsWith("DataTable") -> Exports[i] = Exports[i].ConvertToChildExport<DataTableExport>()
                        exportClassType.endsWith("CurveTable") -> Exports[i] = Exports[i].ConvertToChildExport<CurveTableExport>()
                        exportClassType.endsWith("StringTable") -> Exports[i] = Exports[i].ConvertToChildExport<StringTableExport>()
                        exportClassType.endsWith("BlueprintGeneratedClass") -> Exports[i] = Exports[i].ConvertToChildExport<ClassExport>()
                        exportClassType == "ScriptStruct" -> Exports[i] = Exports[i].ConvertToChildExport<StructExport>()
                        exportClassType == "SplineComponent" -> Exports[i] = Exports[i].ConvertToChildExport<SceneComponentExport>()
                        MainSerializer.PropertyTypeRegistry.containsKey(exportClassType) || MainSerializer.AdditionalPropertyRegistry.contains(exportClassType) ->
                            Exports[i] = Exports[i].ConvertToChildExport<PropertyExport>()
                        else -> Exports[i] = Exports[i].ConvertToChildExport<NormalExport>()
                    }
                }
            }

            if (read) Exports[i].Read(reader, nextStarting.toInt())

            // if we got a StructExport, let's modify mappings/MapStructTypeOverride if we can
            if (read && Exports[i] is StructExport && Exports[i] !is FunctionExport) {
                val fetchedStructExp = Exports[i] as StructExport
                if (fetchedStructExp.LoadedProperties != null) {
                    for (entry in fetchedStructExp.LoadedProperties) {
                        if (entry is FMapProperty) {
                            val fMapEntry = entry
                            var keyOverride: FString? = null
                            var valueOverride: FString? = null
                            if (fMapEntry.KeyProp is FStructProperty) {
                                val keyPropStruc = fMapEntry.KeyProp as FStructProperty
                                if (keyPropStruc.Struct.IsImport()) keyOverride = keyPropStruc.Struct.ToImport(this)?.ObjectName?.Value
                            }
                            if (fMapEntry.ValueProp is FStructProperty) {
                                val valuePropStruc = fMapEntry.ValueProp as FStructProperty
                                if (valuePropStruc.Struct.IsImport()) valueOverride = valuePropStruc.Struct.ToImport(this)?.ObjectName?.Value
                            }

                            MapStructTypeOverride[fMapEntry.Name?.Value?.Value ?: ""] = keyOverride to valueOverride
                        }
                    }
                }

                // add schema if possible
                if (Mappings?.Schemas != null && fetchedStructExp.ObjectName?.toString() != null) {
                    var outer: String? = null
                    if (fetchedStructExp.OuterIndex?.IsImport() == true) outer = fetchedStructExp.OuterIndex!!.ToImport(this)?.ObjectName?.toString()
                    if (fetchedStructExp.OuterIndex?.IsExport() == true) outer = fetchedStructExp.OuterIndex!!.ToExport(this)?.ObjectName?.toString()

                    val newSchema = Usmap.GetSchemaFromStructExport(fetchedStructExp, Mappings?.AreFNamesCaseInsensitive ?: true)
                    if (newSchema != null) {
                        newSchema.ModulePath = InternalAssetPath
                        Mappings!!.Schemas.put(fetchedStructExp.ObjectName.toString(), newSchema)
                        if (!newSchema.ModulePath.isNullOrEmpty()) Mappings!!.Schemas.put(newSchema.ModulePath + "." + (if (outer.isNullOrEmpty()) "" else outer + ".") + fetchedStructExp.ObjectName.toString(), newSchema)
                    }
                }
            }

            // if we got an enum, let's add to mappings enum map if we can
            if (read && Exports[i] is EnumExport) {
                val fetchedEnumExp = Exports[i] as EnumExport
                val enumName = fetchedEnumExp.ObjectName?.toString()
                if (Mappings?.EnumMap != null && enumName != null) {
                    val newEnum = UsmapEnum(enumName, LinkedHashMap())
                    for (entry in fetchedEnumExp.Enum!!.Names) {
                        newEnum.Values[entry.second] = entry.first.toString()
                    }
                    Mappings!!.EnumMap.put(enumName, newEnum)
                }
            }

            if (read) {
                val extrasLen = nextStarting - reader.position.toLong()
                if (extrasLen < 0) {
                    throw FormatException("Invalid padding at end of export " + (i + 1) + ": " + extrasLen + " bytes")
                } else {
                    Exports[i].Extras = reader.ReadBytes(extrasLen.toInt())
                }

                Exports[i].alreadySerialized = true
            }
        } catch (ex: Exception) {
            if (read) reader.position = Exports[i].SerialOffset.toInt()
            Exports[i] = Exports[i].ConvertToChildExport<RawExport>()
            if (read) (Exports[i] as RawExport).Data = reader.ReadBytes(Exports[i].SerialSize.toInt())
        }
    }

    /** Checks whether or not this asset maintains binary equality when serialized. */
    fun VerifyBinaryEquality(): Boolean {
        val f = PathToStream(FilePath)
        val newDataStream = WriteData()

        if (f.size != newDataStream.size) return false
        return f.contentEquals(newDataStream)
    }

    /** Finds the class path and export name of the SuperStruct of this asset, if it exists. */
    open fun GetParentClass(parentClassPath: Out<FName?>, parentClassExportName: Out<FName?>) {
        parentClassPath.value = null
        parentClassExportName.value = null

        val bgcCat = GetClassExport()
        if (bgcCat == null) return
        if (bgcCat.SuperStruct == null) return

        val parentClassLink = bgcCat.SuperStruct!!.ToImport(this)
        if (parentClassLink == null) return
        if (parentClassLink.OuterIndex!!.Index >= 0) return

        parentClassExportName.value = parentClassLink.ObjectName
        parentClassPath.value = parentClassLink.OuterIndex!!.ToImport(this)!!.ObjectName
    }

    internal open fun GetParentClassExportName(modulePath: Out<FName?>): FName? {
        if (!hasFoundParentClassExportName) {
            hasFoundParentClassExportName = true
            val path = Out<FName?>()
            val name = Out<FName?>()
            GetParentClass(path, name)
            parentClassExportName2Cache = path.value
            parentClassExportNameCache = name.value
        }

        modulePath.value = parentClassExportName2Cache
        return parentClassExportNameCache
    }

    /** Adds a new import to the import map. */
    fun AddImport(li: Import): FPackageIndex {
        Imports.add(li)
        return FPackageIndex.FromImport(Imports.size - 1)
    }

    /** Searches for an import in the import map based off of certain parameters. */
    fun SearchForImport(classPackage: FName?, className: FName?, outerIndex: FPackageIndex?, objectName: FName?): Int {
        var currentPos = 0
        for (i in Imports.indices) {
            currentPos--
            if (classPackage == Imports[i].ClassPackage
                && className == Imports[i].ClassName
                && outerIndex == Imports[i].OuterIndex
                && objectName == Imports[i].ObjectName
            ) {
                return currentPos
            }
        }

        return 0
    }

    /** Searches for an import in the import map based off of certain parameters. */
    fun SearchForImport(classPackage: FName?, className: FName?, objectName: FName?): Int {
        var currentPos = 0
        for (i in Imports.indices) {
            currentPos--
            if (classPackage == Imports[i].ClassPackage
                && className == Imports[i].ClassName
                && objectName == Imports[i].ObjectName
            ) {
                return currentPos
            }
        }

        return 0
    }

    /** Searches for an import in the import map based off of certain parameters. */
    fun SearchForImport(objectName: FName?): Int {
        var currentPos = 0
        for (i in Imports.indices) {
            currentPos--
            if (objectName == Imports[i].ObjectName) return currentPos
        }

        return 0
    }

    fun PullSchemasFromAnotherAsset(path: FName?): Boolean {
        if (CustomSerializationFlags.HasFlag(UECustomSerializationFlags.SkipPreloadDependencyLoading)) return false

        if (Mappings?.Schemas == null) return false
        if (path?.Value?.Value == null) return false
        if (!path.Value!!.Value!!.startsWith("/") || path.Value!!.Value!!.startsWith("/Script")) return false
        val assetPath = path.toString()
        val pathOnDisk = FindAssetOnDiskFromPath(assetPath)
        if (pathOnDisk == null) {
            OtherAssetsFailedToAccess.add(path)
            return false
        }

        // basic circular referencing guard
        if (Mappings!!.PathsAlreadyProcessedForSchemas.containsKey(assetPath)) {
            return false
        }

        var success = false
        try {
            Mappings!!.PathsAlreadyProcessedForSchemas[assetPath] = 1

            // initial read to just fetch the FolderName
            val otherAsset = UAsset(this.ObjectVersion, this.ObjectVersionUE5, this.CustomVersionContainer?.map { it.clone() }?.toMutableList(), this.Mappings)
            val otherReader = otherAsset.PathToReader(pathOnDisk)
            otherAsset.CustomSerializationFlags = UECustomSerializationFlags(
                UECustomSerializationFlags.SkipLoadingExports.value or UECustomSerializationFlags.SkipPreloadDependencyLoading.value,
            )
            otherAsset.FilePath = pathOnDisk
            otherAsset.GameSpecificOverride = GameSpecificOverride
            otherAsset.IsParsingToPullSchemas = true
            otherAsset.Read(otherReader)

            // second read to get schemas
            otherAsset.InternalAssetPath = if (otherAsset.FolderName != null && otherAsset.FolderName.toString() != "None") otherAsset.FolderName.toString() else assetPath
            otherAsset.CustomSerializationFlags = UECustomSerializationFlags.None
            otherReader.position = 0
            otherAsset.Read(otherReader)

            for (entry in otherAsset.OtherAssetsFailedToAccess) {
                OtherAssetsFailedToAccess.add(entry)
            }
        } catch (e: Exception) {
            success = false
        }

        return success
    }

    private fun LoadDependencies(): MutableMap<Int, MutableList<Int>> {
        haveWeLoadedDependencies = true

        val depsMap = LinkedHashMap<Int, MutableList<Int>>()
        for (i in Exports.indices) {
            val newExport = Exports[i]
            val deps = mutableListOf<FPackageIndex>()
            deps.addAll(newExport.SerializationBeforeSerializationDependencies)
            deps.addAll(newExport.SerializationBeforeCreateDependencies)

            depsMap[i + 1] = mutableListOf()
            for (dep in deps) {
                if (dep.IsImport()) {
                    val imp = dep.ToImport(this)
                    if (imp?.OuterIndex?.IsImport() == true) {
                        val outerIndex1 = imp.OuterIndex?.ToImport(this)
                        val sourcePath = outerIndex1?.ObjectName
                        if (sourcePath?.toString()?.startsWith('/') == true) {
                            this.PullSchemasFromAnotherAsset(sourcePath)
                        } else if (outerIndex1?.OuterIndex?.IsImport() == true) {
                            val outerIndex2 = outerIndex1.OuterIndex!!.ToImport(this)
                            if (outerIndex2?.ObjectName?.toString()?.startsWith('/') == true) {
                                this.PullSchemasFromAnotherAsset(outerIndex2.ObjectName)
                            }
                        }
                    }
                }

                if (dep.IsExport()) {
                    depsMap[i + 1]!!.add(dep.Index)
                }
            }
        }
        return depsMap
    }

    /** Reads the initial portion of the asset (everything before the name map). */
    private fun ReadHeader(reader: AssetBinaryReader) {
        reader.position = 0
        val fileSignature = reader.ReadUInt32()
        if (fileSignature != UASSET_MAGIC) throw FormatException("File signature mismatch")

        LegacyFileVersion = reader.ReadInt32()
        if (LegacyFileVersion != -4) {
            reader.ReadInt32()
        }

        val rawFileVersionUE4 = reader.ReadInt32()
        val fileVersionUE4 = UEObjectVersion.entries.firstOrNull { it.value == rawFileVersionUE4 } ?: UEObjectVersion.UNKNOWN
        if (fileVersionUE4 > UEObjectVersion.UNKNOWN) {
            IsUnversioned = false
            ObjectVersion = fileVersionUE4
        } else {
            IsUnversioned = true
            if (Mappings != null && Mappings!!.FileVersionUE4.value > 0) ObjectVersion = Mappings!!.FileVersionUE4
            if (ObjectVersion == UEObjectVersion.UNKNOWN) throw UnknownEngineVersionException("Cannot begin serialization of an unversioned asset before an object version is manually specified")
        }

        if (LegacyFileVersion <= -8) {
            val rawFileVersionUE5 = reader.ReadInt32()
            val fileVersionUE5 = UEObjectVersionUE5.entries.firstOrNull { it.value == rawFileVersionUE5 } ?: UEObjectVersionUE5.UNKNOWN
            if (fileVersionUE5 > UEObjectVersionUE5.UNKNOWN) ObjectVersionUE5 = fileVersionUE5
        }
        if (ObjectVersionUE5 == UEObjectVersionUE5.UNKNOWN && Mappings != null && Mappings!!.FileVersionUE5.value > 0) ObjectVersionUE5 = Mappings!!.FileVersionUE5

        // if wasn't unversioned, we'll ignore the current custom version container and just read it from disk
        if (!IsUnversioned) {
            CustomVersionContainer = null
        }

        FileVersionLicenseeUE = reader.ReadInt32()

        if (ObjectVersionUE5 >= UEObjectVersionUE5.PACKAGE_SAVED_HASH) {
            SavedHash = reader.ReadBytes(20)
            SectionSixOffset = reader.ReadInt32()
        }

        // Custom versions container
        if (LegacyFileVersion <= -2) {
            CustomVersionContainer = reader.ReadCustomVersionContainer(CustomVersionSerializationFormat, CustomVersionContainer, Mappings)
        }

        if (ObjectVersionUE5 < UEObjectVersionUE5.PACKAGE_SAVED_HASH) {
            SectionSixOffset = reader.ReadInt32()
        }

        FolderName = reader.ReadFString()
        PackageFlags = EPackageFlags(reader.ReadUInt32().toInt())
        NameCount = reader.ReadInt32()
        NameOffset = reader.ReadInt32()

        if (ObjectVersionUE5 >= UEObjectVersionUE5.ADD_SOFTOBJECTPATH_LIST) {
            SoftObjectPathsCount = reader.ReadInt32()
            SoftObjectPathsOffset = reader.ReadInt32()
        }

        if (!IsFilterEditorOnly && ObjectVersion >= UEObjectVersion.VER_UE4_ADDED_PACKAGE_SUMMARY_LOCALIZATION_ID) {
            LocalizationId = reader.ReadFString()
        }

        if (ObjectVersion >= UEObjectVersion.VER_UE4_SERIALIZE_TEXT_IN_PACKAGES) {
            GatherableTextDataCount = reader.ReadInt32()
            GatherableTextDataOffset = reader.ReadInt32()
        }

        ExportCount = reader.ReadInt32()
        ExportOffset = reader.ReadInt32()
        ImportCount = reader.ReadInt32()
        ImportOffset = reader.ReadInt32()

        if (ObjectVersionUE5 >= UEObjectVersionUE5.VERSE_CELLS) {
            CellExportCount = reader.ReadInt32()
            CellExportOffset = reader.ReadInt32()
            CellImportCount = reader.ReadInt32()
            CellImportOffset = reader.ReadInt32()
        }

        if (ObjectVersionUE5 >= UEObjectVersionUE5.METADATA_SERIALIZATION_OFFSET) {
            MetaDataOffset = reader.ReadInt32()
        }

        DependsOffset = reader.ReadInt32()
        if (ObjectVersion >= UEObjectVersion.VER_UE4_ADD_STRING_ASSET_REFERENCES_MAP) {
            SoftPackageReferencesCount = reader.ReadInt32()
            SoftPackageReferencesOffset = reader.ReadInt32()
        }
        if (ObjectVersion >= UEObjectVersion.VER_UE4_ADDED_SEARCHABLE_NAMES) {
            SearchableNamesOffset = reader.ReadInt32()
        }
        ThumbnailTableOffset = reader.ReadInt32()

        if (ObjectVersionUE5 >= UEObjectVersionUE5.IMPORT_TYPE_HIERARCHIES) {
            ImportTypeHierarchiesCount = reader.ReadInt32()
            ImportTypeHierarchiesOffset = reader.ReadInt32()
        }

        if (ObjectVersionUE5 < UEObjectVersionUE5.PACKAGE_SAVED_HASH) {
            PackageGuid = reader.ReadGuid()
        }

        if (!IsFilterEditorOnly) {
            PersistentGuid = if (ObjectVersion >= UEObjectVersion.VER_UE4_ADDED_PACKAGE_OWNER) {
                reader.ReadGuid()
            } else {
                PackageGuid
            }

            if (ObjectVersion >= UEObjectVersion.VER_UE4_ADDED_PACKAGE_OWNER &&
                ObjectVersion < UEObjectVersion.VER_UE4_NON_OUTER_PACKAGE_IMPORT
            ) {
                reader.ReadBytes(16)
            }
        }
        Generations = mutableListOf()
        val generationCount = reader.ReadInt32()
        for (i in 0 until generationCount) {
            val genNumExports = reader.ReadInt32()
            val genNumNames = reader.ReadInt32()
            Generations.add(FGenerationInfo(genNumExports, genNumNames))
        }
        RecordedEngineVersion = if (ObjectVersion >= UEObjectVersion.VER_UE4_ENGINE_VERSION_OBJECT) {
            FEngineVersion(reader)
        } else {
            FEngineVersion(4, 0, 0, reader.ReadUInt32(), FString.FromString("")!!)
        }

        RecordedCompatibleWithEngineVersion = if (ObjectVersion >= UEObjectVersion.VER_UE4_PACKAGE_SUMMARY_HAS_COMPATIBLE_ENGINE_VERSION) {
            FEngineVersion(reader)
        } else {
            RecordedEngineVersion
        }

        CompressionFlags = reader.ReadUInt32()
        val numCompressedChunks = reader.ReadInt32()
        if (numCompressedChunks > 0) throw FormatException("Asset has package-level compression and is likely too old to be parsed")

        PackageSource = reader.ReadUInt32()

        AdditionalPackagesToCook = mutableListOf()
        val numAdditionalPackagesToCook = reader.ReadInt32()
        for (i in 0 until numAdditionalPackagesToCook) {
            AdditionalPackagesToCook.add(reader.ReadFString()!!)
        }

        if (LegacyFileVersion > -7) {
            val numTextureAllocations = reader.ReadInt32()
            if (numTextureAllocations > 0) throw FormatException("Asset has texture allocation info and is likely too old to be parsed")
        }

        AssetRegistryDataOffset = reader.ReadInt32()
        BulkDataStartOffset = reader.ReadInt64()

        if (ObjectVersion >= UEObjectVersion.VER_UE4_WORLD_LEVEL_INFO) {
            WorldTileInfoDataOffset = reader.ReadInt32()
        }

        if (ObjectVersion >= UEObjectVersion.VER_UE4_CHANGED_CHUNKID_TO_BE_AN_ARRAY_OF_CHUNKIDS) {
            val numChunkIDs = reader.ReadInt32()
            ChunkIDs = IntArray(numChunkIDs) { reader.ReadInt32() }
        } else if (ObjectVersion >= UEObjectVersion.VER_UE4_ADDED_CHUNKID_TO_ASSETDATA_AND_UPACKAGE) {
            ChunkIDs = intArrayOf(reader.ReadInt32())
        }

        if (ObjectVersion >= UEObjectVersion.VER_UE4_PRELOAD_DEPENDENCIES_IN_COOKED_EXPORTS) {
            PreloadDependencyCount = reader.ReadInt32()
            PreloadDependencyOffset = reader.ReadInt32()
        }

        // ue5 stuff
        NamesReferencedFromExportDataCount = if (ObjectVersionUE5 >= UEObjectVersionUE5.NAMES_REFERENCED_FROM_EXPORT_DATA) reader.ReadInt32() else NameCount
        PayloadTocOffset = if (ObjectVersionUE5 >= UEObjectVersionUE5.PAYLOAD_TOC) reader.ReadInt64() else -1
        DataResourceOffset = if (ObjectVersionUE5 >= UEObjectVersionUE5.DATA_RESOURCES) reader.ReadInt32() else -1
    }

    private fun ReadBitArray(reader: AssetBinaryReader): BitArray {
        val bitCount = reader.ReadInt32()
        val length = ComputeBitArrayDataLenth(bitCount)
        val data = reader.ReadBytes(length)
        val res = BitArray()
        res.bits = java.util.BitSet.valueOf(data)
        res.Length = bitCount
        return res
    }

    private fun BitsToNumWords(bitCount: Int): Int {
        return kotlin.math.ceil(bitCount / 32.0).toInt()
    }

    private fun ComputeBitArrayDataLenth(bitCount: Int): Int {
        return Int.SIZE_BYTES * BitsToNumWords(bitCount)
    }

    /** Reads an asset into memory. */
    open fun Read(reader: AssetBinaryReader, manualSkips: IntArray? = null, forceReads: IntArray? = null) {
        reader.Asset = this
        hasFoundParentClassExportName = false

        // Header
        ReadHeader(reader)

        // Name map
        reader.position = NameOffset

        OverrideNameMapHashes = linkedMapOf()
        ClearNameIndexList()
        for (i in 0 until NameCount) {
            val hashes = Out<Long>()
            val nameInMap = reader.ReadNameMapString(hashes)!!
            val hashVal = hashes.value ?: 0L
            if (hashVal == 0L) {
                OverrideNameMapHashes!![nameInMap] = 0
            } else if (hashVal shr 16 == 0L && nameInMap.Value != null && nameInMap.Value == nameInMap.Value!!.lowercase()) {
                nameInMap.IsCasePreserving = false
            }
            AddNameReference(nameInMap, true, true)
        }

        SoftObjectPathList = null
        if (SoftObjectPathsOffset > 0) {
            reader.position = SoftObjectPathsOffset
            SoftObjectPathList = mutableListOf()
            for (i in 0 until SoftObjectPathsCount) {
                SoftObjectPathList!!.add(FSoftObjectPath(reader, false))
            }
        }

        // Gatherable text
        if (GatherableTextDataOffset > 0 && GatherableTextDataCount > 0) {
            reader.position = GatherableTextDataOffset

            GatherableTextData = mutableListOf()
            for (i in 0 until GatherableTextDataCount) {
                val namespaceName = reader.ReadFString()

                val sourceString = reader.ReadFString()
                val sourceStringMetaData = reader.ReadLocMetadataObject()
                val sourceData = FTextSourceData()
                sourceData.SourceString = sourceString
                sourceData.SourceStringMetaData = sourceStringMetaData

                val contexts = mutableListOf<FTextSourceSiteContext>()
                val contextsCount = reader.ReadInt32()
                for (j in 0 until contextsCount) {
                    val keyName = reader.ReadFString()
                    val siteDescription = reader.ReadFString()
                    val isEditorOnly = reader.ReadBooleanInt()
                    val isOptional = reader.ReadBooleanInt()
                    val infoMetaData = reader.ReadLocMetadataObject()
                    val keyMetaData = reader.ReadLocMetadataObject()

                    val context = FTextSourceSiteContext()
                    context.KeyName = keyName
                    context.SiteDescription = siteDescription
                    context.IsEditorOnly = isEditorOnly
                    context.IsOptional = isOptional
                    context.InfoMetaData = infoMetaData
                    context.KeyMetaData = keyMetaData
                    contexts.add(context)
                }

                val textData = FGatherableTextData()
                textData.NamespaceName = namespaceName
                textData.SourceData = sourceData
                textData.SourceSiteContexts = contexts
                GatherableTextData!!.add(textData)
            }
        }

        if (MetaDataOffset > 0) {
            MetaData = FMetaData(reader)
        }

        // Imports
        Imports = mutableListOf()
        if (ImportOffset > 0) {
            reader.position = ImportOffset
            for (i in 0 until ImportCount) {
                Imports.add(Import(reader))
            }
        }

        // Export details
        Exports = mutableListOf()
        val exportLoadOrder = mutableListOf<Int>()
        if (ExportOffset > 0) {
            reader.position = ExportOffset
            for (i in 0 until ExportCount) {
                val newExport: Export = RawExport(ByteArray(0), this, ByteArray(0))
                newExport.ReadExportMapEntry(reader)
                Exports.add(newExport)
            }
        }

        // DependsMap
        DependsMap = null
        if (DependsOffset > 0 || (ObjectVersion > UEObjectVersion.VER_UE4_PRELOAD_DEPENDENCIES_IN_COOKED_EXPORTS && ObjectVersion < UEObjectVersion.VER_UE4_64BIT_EXPORTMAP_SERIALSIZES)) {
            DependsMap = mutableListOf()
            if (DependsOffset > 0) reader.position = DependsOffset
            for (i in 0 until ExportCount) {
                val size = reader.ReadInt32()
                val data = IntArray(size) { reader.ReadInt32() }
                DependsMap!!.add(data)
            }
        }

        // SoftPackageReferenceList
        SoftPackageReferenceList = null
        if (SoftPackageReferencesOffset > 0) {
            reader.position = SoftPackageReferencesOffset
            SoftPackageReferenceList = mutableListOf()
            for (i in 0 until SoftPackageReferencesCount) {
                SoftPackageReferenceList!!.add(
                    if (ObjectVersion >= UEObjectVersion.VER_UE4_ADDED_SOFT_OBJECT_PATH) {
                        FString.FromString(reader.ReadFName().toString()) ?: FString(null)
                    } else {
                        reader.ReadFString() ?: FString(null)
                    },
                )
            }
        }

        if (AssetRegistryDataOffset > 0) {
            AssetRegistryDependencyDataOffset = -1
            reader.position = AssetRegistryDataOffset
            if (!IsPreDependencyFormat) {
                AssetRegistryDependencyDataOffset = reader.ReadInt64()
            }

            val numAssets = reader.ReadInt32()
            AssetRegistryRecords = mutableListOf()
            for (i in 0 until numAssets) {
                val record = FAssetRegistryRecord()

                record.Path = reader.ReadString()
                record.ClassName = reader.ReadString()

                val tagNum = reader.ReadInt32()
                record.TagMap = linkedMapOf()
                for (j in 0 until tagNum) {
                    val key = reader.ReadString()!!
                    val value = reader.ReadString()
                    record.TagMap[key] = value
                }
                AssetRegistryRecords!!.add(record)
            }

            if (!IsPreDependencyFormat) {
                ImportBits = ReadBitArray(reader)
                SoftPackageBits = ReadBitArray(reader)
                if (ObjectVersionUE5 >= UEObjectVersionUE5.ASSETREGISTRY_PACKAGEBUILDDEPENDENCIES) {
                    ExtraPackageDependencies = reader.ReadArray { reader.ReadFName() to reader.ReadUInt32() }
                }
            }
        } else {
            doWeHaveAssetRegistryData = false
        }

        AdditionalFiles = ByteArray(0)
        if (BulkDataStartOffset > 0 && reader.LoadUexp) {
            val before = reader.position.toLong()
            reader.position = BulkDataStartOffset.toInt()
            val hasPayload = PayloadTocOffset > 0
            val end = if (hasPayload) PayloadTocOffset else reader.length.toLong()
            AdditionalFiles = reader.ReadBytes((end - BulkDataStartOffset).toInt())
            if (hasPayload) {
                Trailer = reader.ReadBytes((reader.length.toLong() - reader.position.toLong()).toInt())
            }
            reader.position = before.toInt()
        }

        // WorldTileInfoDataOffset
        WorldTileInfo = null
        if (WorldTileInfoDataOffset > 0) {
            reader.position = WorldTileInfoDataOffset
            WorldTileInfo = FWorldTileInfo()
            WorldTileInfo!!.Read(reader, this)
        } else {
            doWeHaveWorldTileInfo = false
        }

        // PreloadDependencies
        if (PreloadDependencyOffset > 0) reader.position = PreloadDependencyOffset
        for (i in Exports.indices) {
            if (PreloadDependencyOffset <= 0) continue
            if (Exports[i].FirstExportDependencyOffset < 0) continue
            this.UsesEventDrivenLoader = true

            reader.position = PreloadDependencyOffset
            reader.position += Exports[i].FirstExportDependencyOffset * Int.SIZE_BYTES

            Exports[i].SerializationBeforeSerializationDependencies = MutableList(Exports[i].SerializationBeforeSerializationDependenciesSize) { FPackageIndex.FromRawIndex(reader.ReadInt32()) }
            Exports[i].CreateBeforeSerializationDependencies = MutableList(Exports[i].CreateBeforeSerializationDependenciesSize) { FPackageIndex.FromRawIndex(reader.ReadInt32()) }
            Exports[i].SerializationBeforeCreateDependencies = MutableList(Exports[i].SerializationBeforeCreateDependenciesSize) { FPackageIndex.FromRawIndex(reader.ReadInt32()) }
            Exports[i].CreateBeforeCreateDependencies = MutableList(Exports[i].CreateBeforeCreateDependenciesSize) { FPackageIndex.FromRawIndex(reader.ReadInt32()) }
        }

        // DataResources (5.3+)
        DataResources = null
        if (DataResourceOffset > 0) {
            DataResources = mutableListOf()
            reader.position = DataResourceOffset
            DataResourceVersion = EObjectDataResourceVersion.entries.getOrElse(reader.ReadUInt32().toInt()) { EObjectDataResourceVersion.Invalid }

            val count = reader.ReadInt32()
            for (i in 0 until count) {
                val flags = EObjectDataResourceFlags(reader.ReadUInt32().toInt())

                var cookedIndex = 0
                if (DataResourceVersion >= EObjectDataResourceVersion.AddedCookedIndex) {
                    cookedIndex = reader.ReadByte()
                }

                val serialOffset = reader.ReadInt64()
                val duplicateSerialOffset = reader.ReadInt64()
                val serialSize = reader.ReadInt64()
                val rawSize = reader.ReadInt64()
                val outerIndex = FPackageIndex.FromRawIndex(reader.ReadInt32())
                val legacyBulkDataFlags = reader.ReadUInt32()

                DataResources!!.add(FObjectDataResource(flags, serialOffset, duplicateSerialOffset, serialSize, rawSize, outerIndex, legacyBulkDataFlags, cookedIndex))
            }
        }

        // possible for some null bytes to exist at end of .uasset file as part of external conversion projects
        if (Exports.size > 0) {
            val offsetDiff = Exports[0].SerialOffset - reader.position.toLong()
            val paddingBytes = reader.ReadBytes(offsetDiff.toInt())
            for (byt in paddingBytes) {
                if (byt != 0.toByte()) throw FormatException("Encountered additional non-null data at end of legacy header data")
            }
            AppendedNullBytes = paddingBytes.size
        }

        if (reader.LoadUexp) {
            val skipLoadingExports = CustomSerializationFlags.HasFlag(UECustomSerializationFlags.SkipLoadingExports)
            val skipParsingExports = skipLoadingExports || CustomSerializationFlags.HasFlag(UECustomSerializationFlags.SkipParsingExports)

            // load dependencies, if needed and available
            val depsMap = LoadDependencies()
            exportLoadOrder.addAll(UAPUtils.SortByDependencies((1..Exports.size).toList(), depsMap))

            // Export data
            if (SectionSixOffset > 0 && Exports.size > 0) {
                for (exportIdx in exportLoadOrder) {
                    val i = exportIdx - 1

                    if (!skipLoadingExports) reader.position = Exports[i].SerialOffset.toInt()
                    if (skipParsingExports || skipLoadingExports || (manualSkips != null && manualSkips.contains(i) && (forceReads == null || !forceReads.contains(i)))) {
                        Exports[i] = Exports[i].ConvertToChildExport<RawExport>()
                        (Exports[i] as RawExport).Data = if (skipLoadingExports) ByteArray(0) else reader.ReadBytes(Exports[i].SerialSize.toInt())
                        continue
                    }

                    ConvertExportToChildExportAndRead(reader, i)
                }

                // catch any stragglers
                for (i in Exports.indices) {
                    if (Exports[i].alreadySerialized) continue

                    if (!skipLoadingExports) reader.position = Exports[i].SerialOffset.toInt()
                    if (skipParsingExports || skipLoadingExports || (manualSkips != null && manualSkips.contains(i) && (forceReads == null || !forceReads.contains(i)))) {
                        Exports[i] = Exports[i].ConvertToChildExport<RawExport>()
                        (Exports[i] as RawExport).Data = if (skipLoadingExports) ByteArray(0) else reader.ReadBytes(Exports[i].SerialSize.toInt())
                        continue
                    }

                    ConvertExportToChildExportAndRead(reader, i)
                }
            }
        } else {
            // skip loading dependencies & parsing export data if we don't load uexp/exports
            for (i in Exports.indices) {
                if (manualSkips != null && manualSkips.contains(i) && (forceReads == null || !forceReads.contains(i))) {
                    Exports[i] = Exports[i].ConvertToChildExport<RawExport>()
                    continue
                }

                ConvertExportToChildExportAndRead(reader, i, false)
            }
        }

        // Searchable names
        if (SearchableNamesOffset > 0) {
            SearchableNames = TreeMap()
            reader.position = SearchableNamesOffset
            val searchableNamesCount = reader.ReadInt32()

            for (i in 0 until searchableNamesCount) {
                val collectionIndex = reader.ReadInt32()
                val collectionCount = reader.ReadInt32()
                val searchableCollection = mutableListOf<FName>()
                for (j in 0 until collectionCount) {
                    val searchableName = reader.ReadFName()
                    searchableCollection.add(searchableName)
                }

                SearchableNames!![FPackageIndex.FromRawIndex(collectionIndex)] = searchableCollection
            }
        }

        if (ImportTypeHierarchiesOffset > 0) {
            reader.position = ImportTypeHierarchiesOffset
            ImportTypeHierarchies = reader.ReadMap(ImportTypeHierarchiesCount, { FPackageIndex(reader) }, { FImportTypeHierarchy(reader) })
        }

        // Thumbnails
        if (ThumbnailTableOffset > 0) {
            reader.position = ThumbnailTableOffset
            val thumbnailCount = reader.ReadInt32()
            val thumbnailOffsets = LinkedHashMap<String, Int>()
            for (i in 0 until thumbnailCount) {
                val objectShortClassName = reader.ReadFString()!!
                val objectPathWithoutPackageName = reader.ReadFString()!!
                val objectName = "${objectShortClassName} ${objectPathWithoutPackageName}"

                val fileOffset = reader.ReadInt32()

                thumbnailOffsets[objectName] = fileOffset
            }

            Thumbnails = linkedMapOf()
            for ((key, value) in thumbnailOffsets) {
                reader.position = value
                Thumbnails!![key] = reader.ReadObjectThumbnail()
            }
        }
    }

    /** Serializes the initial portion of the asset from memory. */
    private fun MakeHeader(): ByteArray {
        val writer = AssetBinaryWriter(this)

        writer.WriteUInt32(UASSET_MAGIC)
        writer.WriteInt32(LegacyFileVersion)
        if (LegacyFileVersion != 4) {
            writer.WriteInt32(if (IsUnversioned) 0 else 864)
        }

        if (IsUnversioned) {
            writer.WriteInt32(0)
        } else {
            writer.WriteInt32(ObjectVersion.value)
        }

        if (LegacyFileVersion <= -8) {
            if (IsUnversioned) {
                writer.WriteInt32(0)
            } else {
                writer.WriteInt32(ObjectVersionUE5.value)
            }
        }

        writer.WriteInt32(FileVersionLicenseeUE)

        if (ObjectVersionUE5 >= UEObjectVersionUE5.PACKAGE_SAVED_HASH) {
            writer.WriteBytes(SavedHash ?: ByteArray(20))
            writer.WriteInt32(SectionSixOffset)
        }

        if (LegacyFileVersion <= -2) {
            if (IsUnversioned) {
                writer.WriteInt32(0)
            } else {
                writer.WriteCustomVersionContainer(CustomVersionSerializationFormat, CustomVersionContainer)
            }
        }

        if (ObjectVersionUE5 < UEObjectVersionUE5.PACKAGE_SAVED_HASH) {
            writer.WriteInt32(SectionSixOffset)
        }

        writer.Write(FolderName)
        writer.WriteUInt32(PackageFlags.value.toLong() and 0xFFFFFFFFL)
        writer.WriteInt32(NameCount)
        writer.WriteInt32(NameOffset)
        if (ObjectVersionUE5 >= UEObjectVersionUE5.ADD_SOFTOBJECTPATH_LIST) {
            writer.WriteInt32(SoftObjectPathsCount)
            writer.WriteInt32(SoftObjectPathsOffset)
        }
        if (!IsFilterEditorOnly && ObjectVersion >= UEObjectVersion.VER_UE4_ADDED_PACKAGE_SUMMARY_LOCALIZATION_ID) {
            writer.Write(LocalizationId)
        }
        if (ObjectVersion >= UEObjectVersion.VER_UE4_SERIALIZE_TEXT_IN_PACKAGES) {
            writer.WriteInt32(GatherableTextDataCount)
            writer.WriteInt32(GatherableTextDataOffset)
        }
        writer.WriteInt32(ExportCount)
        writer.WriteInt32(ExportOffset)
        writer.WriteInt32(ImportCount)
        writer.WriteInt32(ImportOffset)

        if (ObjectVersionUE5 >= UEObjectVersionUE5.VERSE_CELLS) {
            writer.WriteInt32(CellExportCount)
            writer.WriteInt32(CellExportOffset)
            writer.WriteInt32(CellImportCount)
            writer.WriteInt32(CellImportOffset)
        }

        if (ObjectVersionUE5 >= UEObjectVersionUE5.METADATA_SERIALIZATION_OFFSET) {
            writer.WriteInt32(MetaDataOffset)
        }

        writer.WriteInt32(DependsOffset)
        if (ObjectVersion >= UEObjectVersion.VER_UE4_ADD_STRING_ASSET_REFERENCES_MAP) {
            writer.WriteInt32(SoftPackageReferencesCount)
            writer.WriteInt32(SoftPackageReferencesOffset)
        }
        if (ObjectVersion >= UEObjectVersion.VER_UE4_ADDED_SEARCHABLE_NAMES) {
            writer.WriteInt32(SearchableNamesOffset)
        }

        writer.WriteInt32(ThumbnailTableOffset)

        if (ObjectVersionUE5 >= UEObjectVersionUE5.IMPORT_TYPE_HIERARCHIES) {
            writer.WriteInt32(ImportTypeHierarchiesCount)
            writer.WriteInt32(ImportTypeHierarchiesOffset)
        }

        if (ObjectVersionUE5 < UEObjectVersionUE5.PACKAGE_SAVED_HASH) {
            writer.WriteGuid(PackageGuid)
        }

        if (!IsFilterEditorOnly) {
            if (ObjectVersion >= UEObjectVersion.VER_UE4_ADDED_PACKAGE_OWNER) {
                writer.WriteGuid(PersistentGuid)
            }

            if (ObjectVersion >= UEObjectVersion.VER_UE4_ADDED_PACKAGE_OWNER &&
                ObjectVersion < UEObjectVersion.VER_UE4_NON_OUTER_PACKAGE_IMPORT
            ) {
                writer.WriteBytes(ByteArray(16))
            }
        }

        writer.WriteInt32(Generations.size)
        for (i in Generations.indices) {
            Generations[i].ExportCount = ExportCount
            Generations[i].NameCount = NameCount
            writer.WriteInt32(Generations[i].ExportCount)
            writer.WriteInt32(Generations[i].NameCount)
        }

        if (ObjectVersion >= UEObjectVersion.VER_UE4_ENGINE_VERSION_OBJECT) {
            RecordedEngineVersion.Write(writer)
        } else {
            writer.WriteUInt32(RecordedEngineVersion.Changelist)
        }

        if (ObjectVersion >= UEObjectVersion.VER_UE4_PACKAGE_SUMMARY_HAS_COMPATIBLE_ENGINE_VERSION) {
            RecordedCompatibleWithEngineVersion.Write(writer)
        }

        writer.WriteUInt32(CompressionFlags)
        writer.WriteInt32(0) // numCompressedChunks
        writer.WriteUInt32(PackageSource)
        writer.WriteInt32(AdditionalPackagesToCook.size)
        for (i in AdditionalPackagesToCook.indices) {
            writer.Write(AdditionalPackagesToCook[i])
        }

        if (LegacyFileVersion > -7) {
            writer.WriteInt32(0) // numTextureAllocations
        }

        writer.WriteInt32(AssetRegistryDataOffset)
        writer.WriteInt64(BulkDataStartOffset)

        if (ObjectVersion >= UEObjectVersion.VER_UE4_WORLD_LEVEL_INFO) {
            writer.WriteInt32(WorldTileInfoDataOffset)
        }

        if (ObjectVersion >= UEObjectVersion.VER_UE4_CHANGED_CHUNKID_TO_BE_AN_ARRAY_OF_CHUNKIDS) {
            writer.WriteInt32(ChunkIDs?.size ?: 0)
            for (i in 0 until (ChunkIDs?.size ?: 0)) {
                writer.WriteInt32(ChunkIDs!![i])
            }
        } else if (ObjectVersion >= UEObjectVersion.VER_UE4_ADDED_CHUNKID_TO_ASSETDATA_AND_UPACKAGE) {
            writer.WriteInt32(ChunkIDs!![0])
        }

        if (ObjectVersion >= UEObjectVersion.VER_UE4_PRELOAD_DEPENDENCIES_IN_COOKED_EXPORTS) {
            writer.WriteInt32(PreloadDependencyCount)
            writer.WriteInt32(PreloadDependencyOffset)
        }

        // ue5 stuff
        if (ObjectVersionUE5 >= UEObjectVersionUE5.NAMES_REFERENCED_FROM_EXPORT_DATA) {
            writer.WriteInt32(NamesReferencedFromExportDataCount)
        }

        if (ObjectVersionUE5 >= UEObjectVersionUE5.PAYLOAD_TOC) {
            writer.WriteInt64(PayloadTocOffset)
        }

        if (ObjectVersionUE5 >= UEObjectVersionUE5.DATA_RESOURCES) {
            writer.WriteInt32(DataResourceOffset)
        }

        return writer.toByteArray()
    }

    /** Serializes an asset from memory. */
    open fun WriteData(): ByteArray {
        IsSerializationTime = true

        try {
            // resolve ancestries
            ResolveAncestries()

            // load deps if needed (i.e. asset was loaded from json)
            if (!haveWeLoadedDependencies) LoadDependencies()

            val writer = AssetBinaryWriter(this)

            // Header
            writer.position = 0
            writer.WriteBytes(MakeHeader())

            // Name map
            this.NameOffset = writer.position
            this.NameCount = this.NameMapIndexList.size
            for (i in this.NameMapIndexList.indices) {
                val disableCasePreservingHash = !NameMapIndexList[i].IsCasePreserving && GetCustomVersion<FReleaseObjectVersion>() < FReleaseObjectVersion.PropertiesSerializeRepCondition.ordinal
                writer.Write(if (disableCasePreservingHash) CRCGenerator.ToLower(NameMapIndexList[i], false) else NameMapIndexList[i])

                if (WillSerializeNameHashes == true || (WillSerializeNameHashes == null && ObjectVersion >= UEObjectVersion.VER_UE4_NAME_HASHES_SERIALIZED)) {
                    if (OverrideNameMapHashes != null && OverrideNameMapHashes!!.containsKey(NameMapIndexList[i])) {
                        writer.WriteUInt32(OverrideNameMapHashes!![NameMapIndexList[i]]!!)
                    } else {
                        writer.WriteUInt32(CRCGenerator.GenerateHash(NameMapIndexList[i], disableCasePreservingHash, writer.Asset!!.GetEngineVersion() == EngineVersion.VER_UE4_20))
                    }
                }
            }

            // soft object paths
            if (SoftObjectPathList != null) {
                this.SoftObjectPathsOffset = writer.position
                this.SoftObjectPathsCount = SoftObjectPathList!!.size

                for (i in SoftObjectPathList!!.indices) {
                    SoftObjectPathList!![i].Write(writer, false)
                }
            } else {
                this.SoftObjectPathsOffset = 0
            }

            // Gatherable text
            if (!IsFilterEditorOnly && GatherableTextData != null) {
                GatherableTextDataOffset = writer.position
                GatherableTextDataCount = GatherableTextData!!.size

                for (gatherableTextData in GatherableTextData!!) {
                    writer.Write(gatherableTextData.NamespaceName)

                    writer.Write(gatherableTextData.SourceData!!.SourceString)
                    writer.Write(gatherableTextData.SourceData!!.SourceStringMetaData!!)

                    writer.WriteInt32(gatherableTextData.SourceSiteContexts.size)
                    for (context in gatherableTextData.SourceSiteContexts) {
                        writer.Write(context.KeyName)
                        writer.Write(context.SiteDescription)
                        writer.WriteBooleanInt(context.IsEditorOnly)
                        writer.WriteBooleanInt(context.IsOptional)
                        writer.Write(context.InfoMetaData!!)
                        writer.Write(context.KeyMetaData!!)
                    }
                }
            }

            if (MetaData != null) {
                MetaDataOffset = writer.position
                MetaData!!.Write(writer)
            }

            // Imports
            if (this.Imports.size > 0) {
                this.ImportOffset = writer.position
                this.ImportCount = this.Imports.size
                for (i in this.Imports.indices) {
                    writer.Write(this.Imports[i].ClassPackage)
                    writer.Write(this.Imports[i].ClassName)
                    writer.WriteInt32(this.Imports[i].OuterIndex?.Index ?: 0)
                    writer.Write(this.Imports[i].ObjectName)
                    if (writer.Asset!!.ObjectVersion >= UEObjectVersion.VER_UE4_NON_OUTER_PACKAGE_IMPORT && !writer.Asset!!.IsFilterEditorOnly) {
                        writer.Write(this.Imports[i].PackageName)
                    }
                    if (writer.Asset!!.ObjectVersionUE5 >= UEObjectVersionUE5.OPTIONAL_RESOURCES) writer.WriteBooleanInt(this.Imports[i].bImportOptional)
                }
            } else {
                this.ImportOffset = 0
            }

            // Export details
            if (this.Exports.size > 0) {
                this.ExportOffset = writer.position
                this.ExportCount = this.Exports.size
                for (i in this.Exports.indices) {
                    val us = this.Exports[i]
                    us.WriteExportMapEntry(writer)
                }
            } else {
                this.ExportOffset = 0
            }

            // for binary equality after json conversion
            if (ObjectVersionUE5 >= UEObjectVersionUE5.VERSE_CELLS) {
                CellImportOffset = writer.position
                CellExportOffset = writer.position
            }

            // DependsMap
            if (DependsMap != null) {
                this.DependsOffset = if (ObjectVersion > UEObjectVersion.VER_UE4_PRELOAD_DEPENDENCIES_IN_COOKED_EXPORTS && ObjectVersion < UEObjectVersion.VER_UE4_64BIT_EXPORTMAP_SERIALSIZES) 0 else writer.position
                for (i in this.Exports.indices) {
                    if (i >= this.DependsMap!!.size) this.DependsMap!!.add(IntArray(0))

                    val currentData = this.DependsMap!![i]
                    writer.WriteInt32(currentData.size)
                    for (j in currentData.indices) {
                        writer.WriteInt32(currentData[j])
                    }
                }
            } else {
                this.DependsOffset = 0
                writer.WriteInt32(0)
            }

            // SoftPackageReferenceList
            if (SoftPackageReferenceList != null) {
                this.SoftPackageReferencesOffset = writer.position
                this.SoftPackageReferencesCount = this.SoftPackageReferenceList!!.size
                for (i in this.SoftPackageReferenceList!!.indices) {
                    if (ObjectVersion >= UEObjectVersion.VER_UE4_ADDED_SOFT_OBJECT_PATH) {
                        writer.Write(FName.FromString(this, this.SoftPackageReferenceList!![i].Value!!)!!)
                    } else {
                        writer.Write(this.SoftPackageReferenceList!![i])
                    }
                }
            } else {
                this.SoftPackageReferencesOffset = 0
            }

            if (!IsFilterEditorOnly && SearchableNames != null) {
                SearchableNamesOffset = writer.position

                writer.WriteInt32(SearchableNames!!.size)

                for (i in 0 until SearchableNames!!.size) {
                    val searchableNamesCollectionPair = SearchableNames!!.entries.elementAt(i)
                    val searchableNamesCollectionIndex = searchableNamesCollectionPair.key
                    val searchableNamesCollectionContent = searchableNamesCollectionPair.value

                    writer.WriteInt32(searchableNamesCollectionIndex.Index)
                    writer.WriteInt32(searchableNamesCollectionContent.size)

                    for (j in searchableNamesCollectionContent.indices) {
                        writer.Write(searchableNamesCollectionContent[j])
                    }
                }
            } else {
                SearchableNamesOffset = 0
            }

            if (ImportTypeHierarchies != null) {
                ImportTypeHierarchiesOffset = writer.position
                ImportTypeHierarchiesCount = ImportTypeHierarchies!!.size

                for (kvp in ImportTypeHierarchies!!) {
                    kvp.key.Write(writer)
                    kvp.value.Write(writer)
                }
            } else {
                ImportTypeHierarchiesOffset = 0
                ImportTypeHierarchiesCount = 0
            }

            if (!IsFilterEditorOnly && Thumbnails != null) {
                val thumbnailOffsets = mutableListOf<Pair<String, Int>>()
                for (kv in Thumbnails!!) {
                    val offset = writer.position
                    writer.Write(kv.value)
                    thumbnailOffsets.add(kv.key to offset)
                }

                ThumbnailTableOffset = writer.position

                writer.WriteInt32(Thumbnails!!.size)
                for (thumbnail in thumbnailOffsets) {
                    val firstSpaceIdx = thumbnail.first.indexOf(' ')
                    if (firstSpaceIdx == -1 || firstSpaceIdx == 0) {
                        throw IllegalStateException("Invalid thumbnail object name: \"${thumbnail.first}\"")
                    }

                    val objectClassName = FString(thumbnail.first.substring(0, firstSpaceIdx))
                    val objectPath = thumbnail.first.substring(firstSpaceIdx + 1)

                    val objectPathWithoutPackageName = FString(objectPath.substring(objectPath.indexOf('.') + 1))

                    writer.Write(objectClassName)
                    writer.Write(objectPathWithoutPackageName)
                    writer.WriteInt32(thumbnail.second)
                }
            } else {
                ThumbnailTableOffset = 0
            }

            // AssetRegistryData
            if (this.doWeHaveAssetRegistryData) {
                this.AssetRegistryDataOffset = writer.position

                if (!IsPreDependencyFormat) {
                    writer.WriteInt64(AssetRegistryDependencyDataOffset)
                }

                writer.WriteInt32(AssetRegistryRecords!!.size)
                for (record in AssetRegistryRecords!!) {
                    writer.Write(FString(record.Path))
                    writer.Write(FString(record.ClassName))

                    writer.WriteInt32(record.TagMap.size)
                    for (pair in record.TagMap) {
                        writer.Write(FString(pair.key))
                        writer.Write(FString(pair.value))
                    }
                }

                if (!IsPreDependencyFormat) {
                    AssetRegistryDependencyDataOffset = writer.position.toLong()
                    writer.position = AssetRegistryDataOffset
                    writer.WriteInt64(AssetRegistryDependencyDataOffset)
                    writer.position = AssetRegistryDependencyDataOffset.toInt()

                    WriteBitArray(writer, ImportBits)
                    WriteBitArray(writer, SoftPackageBits)

                    if (ObjectVersionUE5 >= UEObjectVersionUE5.ASSETREGISTRY_PACKAGEBUILDDEPENDENCIES) {
                        if (ExtraPackageDependencies == null) writer.WriteInt32(0)
                        else {
                            writer.WriteInt32(ExtraPackageDependencies!!.size)
                            for (kvp in ExtraPackageDependencies!!) {
                                writer.Write(kvp.first)
                                writer.WriteUInt32(kvp.second)
                            }
                        }
                    }
                }
            } else {
                this.AssetRegistryDataOffset = 0
            }

            // WorldTileInfo
            if (this.doWeHaveWorldTileInfo) {
                this.WorldTileInfoDataOffset = writer.position
                WorldTileInfo!!.Write(writer, this)
            } else {
                this.WorldTileInfoDataOffset = 0
            }

            // PreloadDependencies
            this.PreloadDependencyOffset = writer.position
            if (this.UseSeparateBulkDataFiles) this.UsesEventDrivenLoader = true
            if (this.UsesEventDrivenLoader) {
                this.PreloadDependencyCount = 0
                for (i in this.Exports.indices) {
                    Exports[i].FirstExportDependencyOffset = this.PreloadDependencyCount

                    Exports[i].SerializationBeforeSerializationDependenciesSize = Exports[i].SerializationBeforeSerializationDependencies.size
                    for (j in 0 until Exports[i].SerializationBeforeSerializationDependenciesSize) writer.WriteInt32(Exports[i].SerializationBeforeSerializationDependencies[j].Index)

                    Exports[i].CreateBeforeSerializationDependenciesSize = Exports[i].CreateBeforeSerializationDependencies.size
                    for (j in 0 until Exports[i].CreateBeforeSerializationDependenciesSize) writer.WriteInt32(Exports[i].CreateBeforeSerializationDependencies[j].Index)

                    Exports[i].SerializationBeforeCreateDependenciesSize = Exports[i].SerializationBeforeCreateDependencies.size
                    for (j in 0 until Exports[i].SerializationBeforeCreateDependenciesSize) writer.WriteInt32(Exports[i].SerializationBeforeCreateDependencies[j].Index)

                    Exports[i].CreateBeforeCreateDependenciesSize = Exports[i].CreateBeforeCreateDependencies.size
                    for (j in 0 until Exports[i].CreateBeforeCreateDependenciesSize) writer.WriteInt32(Exports[i].CreateBeforeCreateDependencies[j].Index)

                    this.PreloadDependencyCount +=
                        Exports[i].SerializationBeforeSerializationDependencies.size +
                        Exports[i].CreateBeforeSerializationDependencies.size +
                        Exports[i].SerializationBeforeCreateDependencies.size +
                        Exports[i].CreateBeforeCreateDependencies.size

                    if (Exports[i].FirstExportDependencyOffset == this.PreloadDependencyCount) Exports[i].FirstExportDependencyOffset = -1
                }
            } else {
                this.PreloadDependencyCount = -1
                for (i in this.Exports.indices) Exports[i].FirstExportDependencyOffset = -1
            }

            // DataResources (5.3+)
            if (DataResources != null) {
                this.DataResourceOffset = writer.position
                writer.WriteUInt32(DataResourceVersion.value.toLong())
                writer.WriteInt32(DataResources!!.size)

                for (i in DataResources!!.indices) {
                    val dataResource = DataResources!![i]
                    writer.WriteUInt32(dataResource.Flags.value.toLong() and 0xFFFFFFFFL)

                    if (DataResourceVersion >= EObjectDataResourceVersion.AddedCookedIndex) {
                        writer.WriteByte(dataResource.CookedIndex)
                    }

                    writer.WriteInt64(dataResource.SerialOffset)
                    writer.WriteInt64(dataResource.DuplicateSerialOffset)
                    writer.WriteInt64(dataResource.SerialSize)
                    writer.WriteInt64(dataResource.RawSize)
                    writer.WriteInt32(dataResource.OuterIndex?.Index ?: 0)
                    writer.WriteUInt32(dataResource.LegacyBulkDataFlags)
                }
            }

            if (AppendedNullBytes > 0) {
                writer.WriteBytes(ByteArray(AppendedNullBytes))
            }

            // Export data
            this.SectionSixOffset = writer.position
            val categoryStarts = LongArray(this.Exports.size)
            if (this.Exports.size > 0) {
                for (i in this.Exports.indices) {
                    categoryStarts[i] = writer.position.toLong()
                    val us = this.Exports[i]
                    us.Write(writer)
                    writer.WriteBytes(us.Extras ?: ByteArray(0))
                }
            }

            this.BulkDataStartOffset = writer.position.toLong()
            writer.WriteBytes(AdditionalFiles ?: ByteArray(0))
            if (PayloadTocOffset > 0) {
                this.PayloadTocOffset = writer.position.toLong()
                writer.WriteBytes(Trailer ?: ByteArray(0))
            }

            val end = writer.position

            // Rewrite Section 3
            if (this.Exports.size > 0) {
                writer.position = this.ExportOffset
                for (i in this.Exports.indices) {
                    val us = this.Exports[i]

                    val nextStarting: Long = if ((Exports.size - 1) > i) {
                        categoryStarts[i + 1]
                    } else {
                        this.BulkDataStartOffset
                    }

                    us.SerialOffset = categoryStarts[i]
                    us.SerialSize = nextStarting - categoryStarts[i]

                    us.WriteExportMapEntry(writer)
                }
            }

            // Rewrite header
            writer.position = 0
            writer.WriteBytes(MakeHeader())

            writer.position = end
            return writer.toByteArray()
        } finally {
            IsSerializationTime = false
            GetEngineVersion()
        }
    }

    private fun WriteBitArray(writer: AssetBinaryWriter, bitArray: BitArray?) {
        val count = bitArray?.Length ?: 0
        writer.WriteInt32(count)
        if (count > 0) {
            val data = ByteArray(ComputeBitArrayDataLenth(count))
            bitArray!!.CopyTo(data)
            writer.WriteBytes(data)
        }
    }

    /** Serializes and writes an asset to two split streams (.uasset and .uexp) from memory. */
    open fun Write(uassetStream: Out<ByteArray?>, uexpStream: Out<ByteArray?>) {
        if (ObjectVersion == UEObjectVersion.UNKNOWN) throw UnknownEngineVersionException("Cannot begin serialization before an object version is specified")

        val newData = WriteData()

        if (this.UseSeparateBulkDataFiles && this.Exports.size > 0) {
            val breakingOffPoint = this.Exports[0].SerialOffset.toInt()
            uassetStream.value = newData.copyOfRange(0, breakingOffPoint)
            uexpStream.value = newData.copyOfRange(breakingOffPoint, newData.size)
        } else {
            uassetStream.value = newData
            uexpStream.value = null
        }
    }

    /** Serializes and writes an asset to disk from memory. */
    open fun Write(outputPath: String) {
        if (ObjectVersion == UEObjectVersion.UNKNOWN) throw UnknownEngineVersionException("Cannot begin serialization before an object version is specified")

        val newData = WriteData()

        if (this.UseSeparateBulkDataFiles && this.Exports.size > 0) {
            val breakingOffPoint = this.Exports[0].SerialOffset.toInt()
            java.io.File(outputPath).writeBytes(newData.copyOfRange(0, breakingOffPoint))
            java.io.File(outputPath.substringBeforeLast('.', outputPath) + ".uexp").writeBytes(newData.copyOfRange(breakingOffPoint, newData.size))
        } else {
            java.io.File(outputPath).writeBytes(newData)
        }
    }

    /** Serializes this asset as JSON. */
    fun SerializeJson(isFormatted: Boolean = false): String =
        SerializeJson(if (isFormatted) Formatting.Indented else Formatting.None)

    /** Serializes this asset as JSON. */
    fun SerializeJson(jsonFormatting: Formatting): String {
        Info = "Serialized with " + UAPUtils.DisplayVersion
        return UAssetJson.write(this, jsonFormatting == Formatting.Indented)
    }

    /** Serializes an object as JSON. */
    fun SerializeJsonObject(value: Any?, isFormatted: Boolean = false): String =
        SerializeJsonObject(value, if (isFormatted) Formatting.Indented else Formatting.None)

    /** Serializes an object as JSON. */
    fun SerializeJsonObject(value: Any?, jsonFormatting: Formatting): String =
        UAssetJson.write(value, jsonFormatting == Formatting.Indented)

    /** Deserializes an object from JSON. */
    inline fun <reified T : Any> DeserializeJsonObject(json: String): T {
        val res = UAssetJson.readGeneric(json, T::class.java)
        UAssetJson.resolveNames(this)
        return res
    }

    constructor(path: String, engineVersion: EngineVersion = EngineVersion.UNKNOWN, mappings: Usmap? = null, customSerializationFlags: UECustomSerializationFlags = UECustomSerializationFlags.None, gsOverride: UEGameSpecificOverride = UEGameSpecificOverride.None) : this() {
        this.FilePath = path
        this.Mappings = mappings
        this.CustomSerializationFlags = customSerializationFlags
        this.GameSpecificOverride = gsOverride
        SetEngineVersion(engineVersion)

        Read(PathToReader(path))
    }

    constructor(path: String, loadUexp: Boolean, engineVersion: EngineVersion = EngineVersion.UNKNOWN, mappings: Usmap? = null, customSerializationFlags: UECustomSerializationFlags = UECustomSerializationFlags.None, gsOverride: UEGameSpecificOverride = UEGameSpecificOverride.None) : this() {
        this.FilePath = path
        this.Mappings = mappings
        this.CustomSerializationFlags = customSerializationFlags
        this.GameSpecificOverride = gsOverride
        SetEngineVersion(engineVersion)

        Read(PathToReader(path, loadUexp))
    }

    constructor(reader: AssetBinaryReader, engineVersion: EngineVersion = EngineVersion.UNKNOWN, mappings: Usmap? = null, useSeparateBulkDataFiles: Boolean = false, customSerializationFlags: UECustomSerializationFlags = UECustomSerializationFlags.None, gsOverride: UEGameSpecificOverride = UEGameSpecificOverride.None) : this() {
        this.Mappings = mappings
        this.CustomSerializationFlags = customSerializationFlags
        this.GameSpecificOverride = gsOverride
        UseSeparateBulkDataFiles = useSeparateBulkDataFiles
        SetEngineVersion(engineVersion)
        Read(reader)
    }

    constructor(engineVersion: EngineVersion, mappings: Usmap? = null, customSerializationFlags: UECustomSerializationFlags = UECustomSerializationFlags.None, gsOverride: UEGameSpecificOverride = UEGameSpecificOverride.None) : this() {
        this.Mappings = mappings
        this.CustomSerializationFlags = customSerializationFlags
        this.GameSpecificOverride = gsOverride
        SetEngineVersion(engineVersion)
    }

    constructor(path: String, objectVersion: UEObjectVersion, objectVersionUE5: UEObjectVersionUE5, customVersionContainer: List<CustomVersion>?, mappings: Usmap? = null, customSerializationFlags: UECustomSerializationFlags = UECustomSerializationFlags.None, gsOverride: UEGameSpecificOverride = UEGameSpecificOverride.None) : this() {
        this.FilePath = path
        this.Mappings = mappings
        this.CustomSerializationFlags = customSerializationFlags
        this.GameSpecificOverride = gsOverride
        ObjectVersion = objectVersion
        ObjectVersionUE5 = objectVersionUE5
        if (customVersionContainer != null) CustomVersionContainer = customVersionContainer.toMutableList()

        Read(PathToReader(path))
    }

    constructor(reader: AssetBinaryReader, objectVersion: UEObjectVersion, objectVersionUE5: UEObjectVersionUE5, customVersionContainer: List<CustomVersion>?, mappings: Usmap? = null, useSeparateBulkDataFiles: Boolean = false, customSerializationFlags: UECustomSerializationFlags = UECustomSerializationFlags.None, gsOverride: UEGameSpecificOverride = UEGameSpecificOverride.None) : this() {
        this.Mappings = mappings
        this.CustomSerializationFlags = customSerializationFlags
        this.GameSpecificOverride = gsOverride
        UseSeparateBulkDataFiles = useSeparateBulkDataFiles
        ObjectVersion = objectVersion
        ObjectVersionUE5 = objectVersionUE5
        if (customVersionContainer != null) CustomVersionContainer = customVersionContainer.toMutableList()

        Read(reader)
    }

    constructor(objectVersion: UEObjectVersion, objectVersionUE5: UEObjectVersionUE5, customVersionContainer: List<CustomVersion>?, mappings: Usmap? = null, customSerializationFlags: UECustomSerializationFlags = UECustomSerializationFlags.None, gsOverride: UEGameSpecificOverride = UEGameSpecificOverride.None) : this() {
        this.Mappings = mappings
        this.CustomSerializationFlags = customSerializationFlags
        this.GameSpecificOverride = gsOverride
        ObjectVersion = objectVersion
        ObjectVersionUE5 = objectVersionUE5
        if (customVersionContainer != null) CustomVersionContainer = customVersionContainer.toMutableList()
    }

    companion object {
        /** Magic number for the .uasset format. */
        const val UASSET_MAGIC: Long = 0x9E2A83C1L

        /** Magic number for Ace Combat 7 encrypted .uasset format. */
        const val ACE7_MAGIC: Long = 0x37454341L

        private val cachedCustomVersionReflectionData: MutableMap<String, EngineVersion> = HashMap()

        private val customVersionTypeRegistry: Map<String, Class<out Enum<*>>> = mapOf(
            "FFortniteMainBranchObjectVersion" to FFortniteMainBranchObjectVersion::class.java,
            "FFrameworkObjectVersion" to FFrameworkObjectVersion::class.java,
            "FCoreObjectVersion" to FCoreObjectVersion::class.java,
            "FEditorObjectVersion" to FEditorObjectVersion::class.java,
            "FAnimPhysObjectVersion" to FAnimPhysObjectVersion::class.java,
            "FReleaseObjectVersion" to FReleaseObjectVersion::class.java,
            "FAssetRegistryVersion" to FAssetRegistryVersion::class.java,
            "FSequencerObjectVersion" to FSequencerObjectVersion::class.java,
            "FFortniteReleaseBranchCustomObjectVersion" to FFortniteReleaseBranchCustomObjectVersion::class.java,
            "FUE5ReleaseStreamObjectVersion" to FUE5ReleaseStreamObjectVersion::class.java,
            "FNiagaraObjectVersion" to FNiagaraObjectVersion::class.java,
            "FNiagaraCustomVersion" to FNiagaraCustomVersion::class.java,
            "FUE5SpecialProjectStreamObjectVersion" to FUE5SpecialProjectStreamObjectVersion::class.java,
            "FInstancedStructCustomVersion" to FInstancedStructCustomVersion::class.java,
        )

        private fun customVersionValue(entry: Enum<*>): Int = entry.ordinal - (if (entry.name == "LatestVersion") 2 else 0)

        private fun introducedOf(entry: Enum<*>): EngineVersion {
            val field = entry.javaClass.getDeclaredField("introduced")
            field.isAccessible = true
            return field.get(entry) as EngineVersion
        }

        private fun engineVersionFromName(name: String): EngineVersion? {
            for (e in EngineVersion.entries) {
                if (e.name == name) return e
            }
            return null
        }

        private fun GetIntroducedFromCustomVersionValue(customVersionType: Class<out Enum<*>>, `val`: Int): EngineVersion {
            val constants = customVersionType.enumConstants ?: return EngineVersion.UNKNOWN
            for (entry in constants) {
                if (customVersionValue(entry) == `val`) {
                    return introducedOf(entry)
                }
            }
            return EngineVersion.UNKNOWN
        }

        /**
         * Estimates the retail version of the Unreal Engine based on the object and custom versions.
         */
        fun GetEngineVersion(objectVersion: UEObjectVersion, objectVersionUE5: UEObjectVersionUE5, customVersionContainer: List<CustomVersion>?): EngineVersion {
            // analyze all possible versions based off of the object version alone
            var allPossibleVersions = mutableListOf<EngineVersion>()
            var targetVer = objectVersionUE5.value
            while (allPossibleVersions.isEmpty() && targetVer >= UEObjectVersionUE5.INITIAL_VERSION.value) {
                allPossibleVersions = UE5VersionToObjectVersion.entries
                    .filter { it.value == targetVer }
                    .map { engineVersionFromName(it.name) }
                    .filterNotNull()
                    .toMutableList()
                targetVer -= 1
            }
            targetVer = objectVersion.value
            while (allPossibleVersions.isEmpty() && targetVer > UEObjectVersion.VER_UE4_OLDEST_LOADABLE_PACKAGE.value) {
                allPossibleVersions = UE4VersionToObjectVersion.entries
                    .filter { it.value == targetVer }
                    .map { engineVersionFromName(it.name) }
                    .filterNotNull()
                    .toMutableList()
                targetVer -= 1
            }

            if (allPossibleVersions.isEmpty()) return EngineVersion.UNKNOWN
            if (allPossibleVersions.size == 1 || customVersionContainer == null) return allPossibleVersions[0]

            // multiple possible versions; use custom versions to eliminate some
            var minIntroduced = EngineVersion.VER_UE4_OLDEST_LOADABLE_PACKAGE
            var maxIntroduced = EngineVersion.VER_UE4_AUTOMATIC_VERSION_PLUS_ONE
            for (entry in customVersionContainer) {
                if (entry.FriendlyName == null) continue
                val customVersionType = customVersionTypeRegistry[stripNonLetters(entry.FriendlyName!!)]
                if (customVersionType == null) continue
                val minIntroducedThis = GetIntroducedFromCustomVersionValue(customVersionType, entry.Version)
                val maxIntroducedThis = GetIntroducedFromCustomVersionValue(customVersionType, entry.Version + 1)

                if (minIntroducedThis != EngineVersion.UNKNOWN && minIntroducedThis > minIntroduced) minIntroduced = minIntroducedThis
                if (maxIntroducedThis != EngineVersion.UNKNOWN && maxIntroducedThis < maxIntroduced) maxIntroduced = maxIntroducedThis
            }

            val finalPossibleVersions = allPossibleVersions.filter { it >= minIntroduced && it < maxIntroduced }.sorted()
            if (finalPossibleVersions.isEmpty()) return allPossibleVersions[0]
            if (finalPossibleVersions.isNotEmpty()) return finalPossibleVersions[0]
            return EngineVersion.UNKNOWN
        }

        private val allNonLettersRegex = Regex("[^a-zA-Z]")

        private fun stripNonLetters(input: String): String = allNonLettersRegex.replace(input, "")

        private fun introducedFromName(typ: Class<out Enum<*>>, name: String): EngineVersion {
            val cacheKey = typ.toString() + name
            cachedCustomVersionReflectionData[cacheKey]?.let { return it }

            val constants = typ.enumConstants ?: return EngineVersion.UNKNOWN
            var res = EngineVersion.UNKNOWN
            for (entry in constants) {
                if (entry.name == name) {
                    res = introducedOf(entry)
                    break
                }
            }
            cachedCustomVersionReflectionData[cacheKey] = res
            return res
        }

        fun GuessCustomVersionFromTypeAndEngineVersion(chosenVersion: EngineVersion, typ: Class<out Enum<*>>): Int {
            val allValsRaw = typ.enumConstants ?: return -1

            val allVals = mutableListOf<String>()
            for (i in allValsRaw.indices) {
                if (allValsRaw[i].name != "VersionPlusOne" && allValsRaw[i].name != "LatestVersion") {
                    allVals.add(allValsRaw[i].name)
                }
            }

            for (i in allVals.indices.reversed()) {
                val `val` = allVals[i]

                val attributeIntroducedVersion = introducedFromName(typ, `val`)

                if (attributeIntroducedVersion != EngineVersion.UNKNOWN && chosenVersion >= attributeIntroducedVersion) return i
            }
            return -1
        }

        /** Fetches a list of all default custom versions for a specific Unreal version. */
        fun GetDefaultCustomVersionContainer(chosenVersion: EngineVersion): MutableList<CustomVersion> {
            val res = mutableListOf<CustomVersion>()
            for ((key, value) in CustomVersion.GuidToCustomVersionStringMap) {
                val customVersionType = customVersionTypeRegistry[value]
                if (customVersionType == null) continue
                val guessedCustomVersion = GuessCustomVersionFromTypeAndEngineVersion(chosenVersion, customVersionType)
                if (guessedCustomVersion < 0) continue
                res.add(CustomVersion(key, guessedCustomVersion))
            }
            return res
        }

        /** Reads an asset from serialized JSON and initializes a new instance of the [UAsset] class to store its data in memory. */
        fun DeserializeJson(json: String): UAsset {
            val res = UAssetJson.read(json)
            UAssetJson.resolveNames(res)
            for (ex in res.Exports) ex.Asset = res
            res.ResolveAncestries()
            return res
        }

        /** Reads an asset from serialized JSON and initializes a new instance of the [UAsset] class to store its data in memory. */
        fun DeserializeJson(stream: java.io.InputStream): UAsset {
            val res = UAssetJson.read(stream)
            UAssetJson.resolveNames(res)
            for (ex in res.Exports) ex.Asset = res
            res.ResolveAncestries()
            return res
        }
    }
}
