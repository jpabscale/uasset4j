// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/ExportTypes/Export.cs
package com.github.jpabscale.uasset4j.exporttypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.EPackageFlags
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.AncestryInfo
import com.github.jpabscale.uasset4j.unrealtypes.EObjectFlags
import com.github.jpabscale.uasset4j.unrealtypes.FGuid
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5
import com.github.jpabscale.uasset4j.util.Out

/** Enum used to determine whether an export should be loaded or not on the client/server. Not actually a bitflag. */
enum class EExportFilterFlags(val value: Byte) {
    None(0),
    NotForClient(1),
    NotForServer(2);
}

/** Enum flags that indicate that additional data may be serialized prior to actual tagged property serialization. */
@JvmInline
value class EClassSerializationControlExtension(val value: Byte) {
    fun HasFlag(flag: Byte): Boolean = (value.toInt() and flag.toInt()) == flag.toInt()

    companion object {
        const val NoExtension: Byte = 0x00
        const val ReserveForFutureUse: Byte = 0x01
        const val OverridableSerializationInformation: Byte = 0x02
    }
}

/**
 * UObject resource type for objects that are contained within this package and can be referenced by other packages.
 */
abstract class Export {
    /** The name of the UObject represented by this resource. */
    var ObjectName: FName? = null
    /** Location of the resource for this resource's Outer (import/other export). 0 = this resource is a top-level UPackage. */
    var OuterIndex: FPackageIndex? = null
    /** Location of this export's class (import/other export). 0 = this export is a UClass. */
    var ClassIndex: FPackageIndex? = null
    /** Location of this export's parent class (import/other export). 0 = this export is not derived from UStruct. */
    var SuperIndex: FPackageIndex? = null
    /** Location of this export's template (import/other export). 0 = there is some problem. */
    var TemplateIndex: FPackageIndex? = null
    /** The object flags for the UObject represented by this resource. */
    var ObjectFlags: EObjectFlags = EObjectFlags(0L)
    /** The number of bytes to serialize when saving/loading this export's UObject. */
    var SerialSize: Long = 0
    /** The location (into the FLinker's underlying file reader archive) of the beginning of the data for this export's UObject. Used for verification only. */
    var SerialOffset: Long = 0

    /**
     * The location (relative to SerialOffset) of the beginning of the portion of this export's data that is serialized using tagged property serialization.
     * Serialized into packages using tagged property serialization as of [ObjectVersionUE5.SCRIPT_SERIALIZATION_OFFSET] (5.4).
     */
    var ScriptSerializationStartOffset: Long = 0
    /**
     * The location (relative to SerialOffset) of the end of the portion of this export's data that is serialized using tagged property serialization.
     * Serialized into packages using tagged property serialization as of [ObjectVersionUE5.SCRIPT_SERIALIZATION_OFFSET] (5.4).
     */
    var ScriptSerializationEndOffset: Long = 0

    /** Was this export forced into the export table via OBJECTMARK_ForceTagExp? */
    var bForcedExport: Boolean = false
    /** Should this export not be loaded on clients? */
    var bNotForClient: Boolean = false
    /** Should this export not be loaded on servers? */
    var bNotForServer: Boolean = false
    /** If this object is a top level package, this is the GUID for the original package file. Deprecated. */
    var PackageGuid: FGuid = FGuid(0u, 0u, 0u, 0u)
    /** Whether this object is an inherited instance. */
    var IsInheritedInstance: Boolean = false
    /** If this export is a top-level package, this is the flags for the original package. */
    var PackageFlags: EPackageFlags = EPackageFlags(0)
    /** Should this export be always loaded in editor game? */
    var bNotAlwaysLoadedForEditorGame: Boolean = false
    /** Is this export an asset? */
    var bIsAsset: Boolean = false
    /** Whether a public hash should be generated for this export. */
    var GeneratePublicHash: Boolean = false

    /**
     * The export table must serialize as a fixed size, this is used to index into a long list, which is later loaded into the array. -1 means dependencies are not present. These are contiguous blocks, so CreateBeforeSerializationDependencies starts at FirstExportDependencyOffset + SerializationBeforeSerializationDependencies.
     */
    internal var FirstExportDependencyOffset: Int = 0
    internal var SerializationBeforeSerializationDependenciesSize: Int = 0
    internal var CreateBeforeSerializationDependenciesSize: Int = 0
    internal var SerializationBeforeCreateDependenciesSize: Int = 0
    internal var CreateBeforeCreateDependenciesSize: Int = 0

    var SerializationBeforeSerializationDependencies: MutableList<FPackageIndex> = mutableListOf()
    var CreateBeforeSerializationDependencies: MutableList<FPackageIndex> = mutableListOf()
    var SerializationBeforeCreateDependencies: MutableList<FPackageIndex> = mutableListOf()
    var CreateBeforeCreateDependencies: MutableList<FPackageIndex> = mutableListOf()

    /** Miscellaneous, unparsed export data, stored as a byte array. */
    var Extras: ByteArray? = null

    /** The asset that this export is parsed with. */
    var Asset: UAsset? = null

    internal var alreadySerialized: Boolean = false

    constructor(asset: UAsset?, extras: ByteArray?) {
        Asset = asset
        Extras = extras
    }

    constructor()

    open fun Read(reader: AssetBinaryReader, nextStarting: Int = 0) {
    }

    /** Resolves the ancestry of all child properties of this export. */
    open fun ResolveAncestries(asset: UAsset, ancestrySoFar: AncestryInfo) {
    }

    open fun Write(writer: AssetBinaryWriter) {
    }

    private fun ReadBit(reader: AssetBinaryReader): Boolean = reader.ReadBooleanInt()

    private fun WriteBit(writer: AssetBinaryWriter, b: Boolean) {
        writer.WriteBooleanInt(b)
    }

    fun ReadExportMapEntry(reader: AssetBinaryReader) {
        Asset = reader.Asset

        this.ClassIndex = FPackageIndex(reader.ReadInt32())
        this.SuperIndex = FPackageIndex(reader.ReadInt32())
        if (Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_TemplateIndex_IN_COOKED_EXPORTS) {
            this.TemplateIndex = FPackageIndex(reader.ReadInt32())
        }
        this.OuterIndex = FPackageIndex(reader.ReadInt32())
        this.ObjectName = reader.ReadFName()
        this.ObjectFlags = EObjectFlags(reader.ReadUInt32())
        if (Asset!!.ObjectVersion < ObjectVersion.VER_UE4_64BIT_EXPORTMAP_SERIALSIZES) {
            this.SerialSize = reader.ReadInt32().toLong()
            this.SerialOffset = reader.ReadInt32().toLong()
        } else {
            this.SerialSize = reader.ReadInt64()
            this.SerialOffset = reader.ReadInt64()
        }
        this.bForcedExport = ReadBit(reader)
        this.bNotForClient = ReadBit(reader)
        this.bNotForServer = ReadBit(reader)
        if (Asset!!.ObjectVersionUE5 < ObjectVersionUE5.REMOVE_OBJECT_EXPORT_PACKAGE_GUID) this.PackageGuid = reader.ReadGuid()
        if (Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.TRACK_OBJECT_EXPORT_IS_INHERITED) this.IsInheritedInstance = ReadBit(reader)
        this.PackageFlags = EPackageFlags(reader.ReadUInt32().toInt())
        if (Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_LOAD_FOR_EDITOR_GAME) {
            this.bNotAlwaysLoadedForEditorGame = ReadBit(reader)
        }
        if (Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_COOKED_ASSETS_IN_EDITOR_SUPPORT) {
            this.bIsAsset = ReadBit(reader)
        }
        if (Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.OPTIONAL_RESOURCES) {
            this.GeneratePublicHash = ReadBit(reader)
        }
        if (Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_PRELOAD_DEPENDENCIES_IN_COOKED_EXPORTS) {
            this.FirstExportDependencyOffset = reader.ReadInt32()
            this.SerializationBeforeSerializationDependenciesSize = reader.ReadInt32()
            this.CreateBeforeSerializationDependenciesSize = reader.ReadInt32()
            this.SerializationBeforeCreateDependenciesSize = reader.ReadInt32()
            this.CreateBeforeCreateDependenciesSize = reader.ReadInt32()
        }
        if (!Asset!!.HasUnversionedProperties && Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.SCRIPT_SERIALIZATION_OFFSET) {
            this.ScriptSerializationStartOffset = reader.ReadInt64()
            this.ScriptSerializationEndOffset = reader.ReadInt64()
        }
    }

    fun WriteExportMapEntry(writer: AssetBinaryWriter) {
        Asset = writer.Asset

        writer.WriteInt32(ClassIndex?.Index ?: 0)
        writer.WriteInt32(SuperIndex?.Index ?: 0)
        if (Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_TemplateIndex_IN_COOKED_EXPORTS) {
            writer.WriteInt32(TemplateIndex?.Index ?: 0)
        }
        writer.WriteInt32(OuterIndex?.Index ?: 0)
        writer.Write(ObjectName)
        writer.WriteUInt32(ObjectFlags.value)
        if (Asset!!.ObjectVersion < ObjectVersion.VER_UE4_64BIT_EXPORTMAP_SERIALSIZES) {
            writer.WriteInt32(SerialSize.toInt())
            writer.WriteInt32(SerialOffset.toInt())
        } else {
            writer.WriteInt64(SerialSize)
            writer.WriteInt64(SerialOffset)
        }
        WriteBit(writer, bForcedExport)
        WriteBit(writer, bNotForClient)
        WriteBit(writer, bNotForServer)
        if (Asset!!.ObjectVersionUE5 < ObjectVersionUE5.REMOVE_OBJECT_EXPORT_PACKAGE_GUID) writer.WriteGuid(PackageGuid)
        if (Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.TRACK_OBJECT_EXPORT_IS_INHERITED) WriteBit(writer, IsInheritedInstance)
        writer.WriteUInt32(PackageFlags.value.toLong() and 0xFFFFFFFFL)
        if (Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_LOAD_FOR_EDITOR_GAME) {
            WriteBit(writer, bNotAlwaysLoadedForEditorGame)
        }
        if (Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_COOKED_ASSETS_IN_EDITOR_SUPPORT) {
            WriteBit(writer, bIsAsset)
        }
        if (Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.OPTIONAL_RESOURCES) {
            WriteBit(writer, GeneratePublicHash)
        }
        if (Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_PRELOAD_DEPENDENCIES_IN_COOKED_EXPORTS) {
            writer.WriteInt32(FirstExportDependencyOffset)
            writer.WriteInt32(SerializationBeforeSerializationDependenciesSize)
            writer.WriteInt32(CreateBeforeSerializationDependenciesSize)
            writer.WriteInt32(SerializationBeforeCreateDependenciesSize)
            writer.WriteInt32(CreateBeforeCreateDependenciesSize)
        }
        if (!Asset!!.HasUnversionedProperties && Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.SCRIPT_SERIALIZATION_OFFSET) {
            writer.WriteInt64(ScriptSerializationStartOffset)
            writer.WriteInt64(ScriptSerializationEndOffset)
        }
    }

    fun GetExportClassType(): FName? {
        return if (this.ClassIndex!!.IsImport()) this.ClassIndex!!.ToImport(Asset!!)!!.ObjectName
        else FName.DefineDummy(Asset, this.ClassIndex!!.Index.toString())
    }

    fun GetClassTypeForAncestry(asset: UAsset?, modulePath: Out<FName?>): FName? {
        var a = asset
        if (a == null) a = Asset
        return GetClassTypeForAncestry(this.ClassIndex!!, a!!, modulePath)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (fieldName in GetAllObjectExportFields(Asset ?: UAsset())) {
            val value = getFieldValue(fieldName)
            sb.appendLine(fieldName + ": " + (value ?: "(null)"))
        }
        return sb.toString()
    }

    private fun getFieldValue(fieldName: String): Any? = when (fieldName) {
        "ObjectName" -> ObjectName
        "OuterIndex" -> OuterIndex
        "ClassIndex" -> ClassIndex
        "SuperIndex" -> SuperIndex
        "TemplateIndex" -> TemplateIndex
        "ObjectFlags" -> ObjectFlags
        "SerialSize" -> SerialSize
        "SerialOffset" -> SerialOffset
        "ScriptSerializationStartOffset" -> ScriptSerializationStartOffset
        "ScriptSerializationEndOffset" -> ScriptSerializationEndOffset
        "bForcedExport" -> bForcedExport
        "bNotForClient" -> bNotForClient
        "bNotForServer" -> bNotForServer
        "PackageGuid" -> PackageGuid
        "IsInheritedInstance" -> IsInheritedInstance
        "PackageFlags" -> PackageFlags
        "bNotAlwaysLoadedForEditorGame" -> bNotAlwaysLoadedForEditorGame
        "bIsAsset" -> bIsAsset
        "GeneratePublicHash" -> GeneratePublicHash
        "SerializationBeforeSerializationDependencies" -> SerializationBeforeSerializationDependencies
        "CreateBeforeSerializationDependencies" -> CreateBeforeSerializationDependencies
        "SerializationBeforeCreateDependencies" -> SerializationBeforeCreateDependencies
        "CreateBeforeCreateDependencies" -> CreateBeforeCreateDependencies
        else -> null
    }

    private fun CopyBaseFieldsTo(target: Export) {
        target.ObjectName = this.ObjectName
        target.OuterIndex = this.OuterIndex
        target.ClassIndex = this.ClassIndex
        target.SuperIndex = this.SuperIndex
        target.TemplateIndex = this.TemplateIndex
        target.ObjectFlags = this.ObjectFlags
        target.SerialSize = this.SerialSize
        target.SerialOffset = this.SerialOffset
        target.ScriptSerializationStartOffset = this.ScriptSerializationStartOffset
        target.ScriptSerializationEndOffset = this.ScriptSerializationEndOffset
        target.bForcedExport = this.bForcedExport
        target.bNotForClient = this.bNotForClient
        target.bNotForServer = this.bNotForServer
        target.PackageGuid = this.PackageGuid
        target.IsInheritedInstance = this.IsInheritedInstance
        target.PackageFlags = this.PackageFlags
        target.bNotAlwaysLoadedForEditorGame = this.bNotAlwaysLoadedForEditorGame
        target.bIsAsset = this.bIsAsset
        target.GeneratePublicHash = this.GeneratePublicHash
        target.FirstExportDependencyOffset = this.FirstExportDependencyOffset
        target.SerializationBeforeSerializationDependenciesSize = this.SerializationBeforeSerializationDependenciesSize
        target.CreateBeforeSerializationDependenciesSize = this.CreateBeforeSerializationDependenciesSize
        target.SerializationBeforeCreateDependenciesSize = this.SerializationBeforeCreateDependenciesSize
        target.CreateBeforeCreateDependenciesSize = this.CreateBeforeCreateDependenciesSize
        target.SerializationBeforeSerializationDependencies = this.SerializationBeforeSerializationDependencies
        target.CreateBeforeSerializationDependencies = this.CreateBeforeSerializationDependencies
        target.SerializationBeforeCreateDependencies = this.SerializationBeforeCreateDependencies
        target.CreateBeforeCreateDependencies = this.CreateBeforeCreateDependencies
        target.Extras = this.Extras
        target.Asset = this.Asset
        target.alreadySerialized = this.alreadySerialized
    }

    open fun clone(): Any {
        val res = this::class.java.getDeclaredConstructor().newInstance() as Export
        CopyBaseFieldsTo(res)
        res.SerializationBeforeSerializationDependencies = this.SerializationBeforeSerializationDependencies.toMutableList()
        res.CreateBeforeSerializationDependencies = this.CreateBeforeSerializationDependencies.toMutableList()
        res.SerializationBeforeCreateDependencies = this.SerializationBeforeCreateDependencies.toMutableList()
        res.CreateBeforeCreateDependencies = this.CreateBeforeCreateDependencies.toMutableList()
        res.Extras = this.Extras?.copyOf()
        res.PackageGuid = FGuid.fromBytes(this.PackageGuid.toByteArray())
        return res
    }

    fun ConvertToChildExportInternal(type: Class<out Export>): Export {
        val res = type.getDeclaredConstructor().newInstance()
        CopyBaseFieldsTo(res)
        res.Asset = this.Asset
        res.Extras = this.Extras
        return res
    }

    /** Creates a child export instance with the same export details as the current export. */
    inline fun <reified T : Export> ConvertToChildExport(): T {
        @Suppress("UNCHECKED_CAST")
        return ConvertToChildExportInternal(T::class.java) as T
    }

    companion object {
        fun GetExportMapEntrySize(asset: UAsset): Long {
            val testWriter = AssetBinaryWriter(asset)
            object : Export() {}.WriteExportMapEntry(testWriter)
            return testWriter.position.toLong()
        }

        fun GetClassTypeForAncestry(classIndex: FPackageIndex, asset: UAsset, modulePath: Out<FName?>): FName? {
            modulePath.value = null
            if (classIndex.IsNull()) return null
            if (classIndex.IsExport()) return classIndex.ToExport(asset)?.ObjectName

            val imp = classIndex.ToImport(asset)
            if (imp != null && imp.OuterIndex != null && imp.OuterIndex!!.IsImport()) modulePath.value = imp.OuterIndex!!.ToImport(asset)!!.ObjectName
            return imp?.ObjectName
        }

        /** All object export fields, ordered by their serialization index. */
        fun GetAllObjectExportFields(asset: UAsset): Array<String> = orderedFieldNames

        fun GetAllFieldNames(asset: UAsset): Array<String> = orderedFieldNames

        private val orderedFieldNames: Array<String> = arrayOf(
            "ObjectName",
            "OuterIndex",
            "ClassIndex",
            "SuperIndex",
            "TemplateIndex",
            "ObjectFlags",
            "SerialSize",
            "SerialOffset",
            "ScriptSerializationStartOffset",
            "ScriptSerializationEndOffset",
            "bForcedExport",
            "bNotForClient",
            "bNotForServer",
            "PackageGuid",
            "IsInheritedInstance",
            "PackageFlags",
            "bNotAlwaysLoadedForEditorGame",
            "bIsAsset",
            "GeneratePublicHash",
            "SerializationBeforeSerializationDependencies",
            "CreateBeforeSerializationDependencies",
            "SerializationBeforeCreateDependencies",
            "CreateBeforeCreateDependencies",
        )
    }
}
