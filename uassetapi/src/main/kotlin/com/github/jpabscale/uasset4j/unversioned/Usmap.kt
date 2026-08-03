// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Unversioned/Usmap.cs
// NOTE: M2 — usmap binary parsing. Jmap (ReadJMAP), the JSON converters, and the
// StructExport/PropertyExport-based schema converters are deferred (M2b/M4).
package com.github.jpabscale.uasset4j.unversioned

import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.FormatException
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.UAPUtils
import com.github.jpabscale.uasset4j.UsmapBinaryReader
import com.github.jpabscale.uasset4j.customversions.FCoreObjectVersion
import com.github.jpabscale.uasset4j.customversions.FFrameworkObjectVersion
import com.github.jpabscale.uasset4j.exporttypes.EnumExport
import com.github.jpabscale.uasset4j.exporttypes.Export
import com.github.jpabscale.uasset4j.exporttypes.FunctionExport
import com.github.jpabscale.uasset4j.exporttypes.PropertyExport
import com.github.jpabscale.uasset4j.exporttypes.StructExport
import com.github.jpabscale.uasset4j.fieldtypes.FArrayProperty
import com.github.jpabscale.uasset4j.fieldtypes.FByteProperty
import com.github.jpabscale.uasset4j.fieldtypes.FEnumProperty
import com.github.jpabscale.uasset4j.fieldtypes.FMapProperty
import com.github.jpabscale.uasset4j.fieldtypes.FProperty
import com.github.jpabscale.uasset4j.fieldtypes.FSetProperty
import com.github.jpabscale.uasset4j.fieldtypes.FStructProperty
import com.github.jpabscale.uasset4j.fieldtypes.UArrayProperty
import com.github.jpabscale.uasset4j.fieldtypes.UByteProperty
import com.github.jpabscale.uasset4j.fieldtypes.UEnumProperty
import com.github.jpabscale.uasset4j.fieldtypes.UMapProperty
import com.github.jpabscale.uasset4j.fieldtypes.UProperty
import com.github.jpabscale.uasset4j.fieldtypes.USetProperty
import com.github.jpabscale.uasset4j.fieldtypes.UStructProperty
import com.github.jpabscale.uasset4j.propertytypes.objects.AncestryInfo
import com.github.jpabscale.uasset4j.unrealtypes.EngineVersion
import com.github.jpabscale.uasset4j.unrealtypes.EPropertyFlags
import com.github.jpabscale.uasset4j.unrealtypes.FGuid
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5
import com.github.jpabscale.uasset4j.unrealtypes.UE4VersionToObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.UE5VersionToObjectVersion
import com.github.jpabscale.uasset4j.util.Out
import com.github.jpabscale.uasset4j.util.Zstd
import java.nio.file.Files
import java.nio.file.Path

enum class UsmapVersion {
    Initial,
    PackageVersioning,
    LongFName,
    LargeEnums,
    ExplicitEnumValues,
    LatestPlusOne,
    Latest;
}

enum class UsmapExtensionLayoutVersion {
    Initial,
}

enum class UsmapStructKind {
    None,
    UScriptStruct,
    UClass,
}

enum class UsmapCompressionMethod {
    None,
    Oodle,
    Brotli,
    ZStandard,
    Unknown,
}

enum class UsmapPropertyType {
    ByteProperty,
    BoolProperty,
    IntProperty,
    FloatProperty,
    ObjectProperty,
    NameProperty,
    DelegateProperty,
    DoubleProperty,
    ArrayProperty,
    StructProperty,
    StrProperty,
    TextProperty,
    InterfaceProperty,
    MulticastDelegateProperty,
    WeakObjectProperty,
    LazyObjectProperty,
    AssetObjectProperty,
    SoftObjectProperty,
    UInt64Property,
    UInt32Property,
    UInt16Property,
    Int64Property,
    Int16Property,
    Int8Property,
    MapProperty,
    SetProperty,
    EnumProperty,
    FieldPathProperty,
    OptionalProperty,
    Utf8StrProperty,
    AnsiStrProperty,
    Unknown,
}

open class UsmapPropertyData(var Type: UsmapPropertyType = UsmapPropertyType.Unknown) {
    override fun toString(): String = Type.name
}

class UsmapMapData : UsmapPropertyData {
    var InnerType: UsmapPropertyData? = null
    var ValueType: UsmapPropertyData? = null

    constructor() : super(UsmapPropertyType.Unknown) {
        Type = UsmapPropertyType.MapProperty
    }

    override fun toString(): String = "${super.toString()}<${InnerType!!}, ${ValueType!!}>"
}

class UsmapArrayData(type: UsmapPropertyType) : UsmapPropertyData(type) {
    var InnerType: UsmapPropertyData? = null

    override fun toString(): String = "${super.toString()}<${InnerType!!}>"
}

class UsmapStructData : UsmapPropertyData {
    var StructType: String? = null

    constructor(structType: String) : super(UsmapPropertyType.StructProperty) {
        StructType = structType
    }

    constructor() : super(UsmapPropertyType.StructProperty)

    override fun toString(): String = "${super.toString()}<${StructType ?: ""}>"
}

class UsmapEnumData : UsmapPropertyData {
    var InnerType: UsmapPropertyData? = null
    var Name: String? = null
    var Values: MutableList<String> = mutableListOf()

    constructor(name: String?, values: List<String>) : super(UsmapPropertyType.Unknown) {
        Name = name
        Values = values.toMutableList()
        Type = UsmapPropertyType.EnumProperty
    }

    constructor() : super(UsmapPropertyType.Unknown) {
        Type = UsmapPropertyType.EnumProperty
    }

    override fun toString(): String = "${super.toString()}<${Name ?: ""}<${InnerType!!}>>"
}

class UsmapProperty {
    var Name: String?
    var SchemaIndex: Int
    var ArrayIndex: Int
    var ArraySize: Int
    var PropertyData: UsmapPropertyData?
    var PropertyFlags: EPropertyFlags = EPropertyFlags.CPF_None

    constructor(name: String?, schemaIndex: Int, arrayIndex: Int, arraySize: Int, propertyData: UsmapPropertyData?) {
        Name = name
        SchemaIndex = schemaIndex
        ArrayIndex = arrayIndex
        ArraySize = arraySize
        PropertyData = propertyData
    }

    fun clone(): UsmapProperty = UsmapProperty(Name, SchemaIndex, ArrayIndex, ArraySize, PropertyData)

    override fun toString(): String = "${Name ?: ""} : $SchemaIndex : $ArrayIndex : $ArraySize : (${PropertyData!!})"
}

class UsmapSchema {
    var Name: String?
        get() {
            PopulateIfNeeded()
            return _Name
        }
        set(value) {
            _Name = value
        }
    private var _Name: String? = null

    var SuperType: String?
        get() {
            PopulateIfNeeded()
            return _SuperType
        }
        set(value) {
            _SuperType = value
        }
    private var _SuperType: String? = null

    var SuperTypeModulePath: String?
        get() {
            PopulateIfNeeded()
            return _SuperTypeModulePath
        }
        set(value) {
            _SuperTypeModulePath = value
        }
    private var _SuperTypeModulePath: String? = null

    var PropCount: Int
        get() {
            PopulateIfNeeded()
            return _PropCount
        }
        set(value) {
            _PropCount = value
        }
    private var _PropCount: Int = 0

    var ModulePath: String?
        get() {
            PopulateIfNeeded()
            return _ModulePath
        }
        set(value) {
            _ModulePath = value
        }
    private var _ModulePath: String? = null

    var StructKind: UsmapStructKind
        get() {
            PopulateIfNeeded()
            return _StructKind
        }
        set(value) {
            _StructKind = value
        }
    private var _StructKind: UsmapStructKind = UsmapStructKind.None

    var StructOrClassFlags: Int
        get() {
            PopulateIfNeeded()
            return _StructOrClassFlags
        }
        set(value) {
            _StructOrClassFlags = value
        }
    private var _StructOrClassFlags: Int = 0

    var FromAsset: Boolean = false

    internal var propertiesInternal: LinkedHashMap<Int, UsmapProperty> = LinkedHashMap()

    var Properties: LinkedHashMap<Int, UsmapProperty>
        get() {
            PopulateIfNeeded()
            return propertiesInternal
        }
        set(value) {
            propertiesInternal = value
        }

    private var PropertiesMap: MutableMap<Pair<String, Int>, UsmapProperty> = LinkedHashMap()
    private var CaseInsensitive: Boolean = false

    var JmapPath: String? = null
    var JmapOffset: Long = -1
    var JmapSize: Long = -1
    var IsPopulated: Boolean = false

    internal fun PopulateIfNeeded() {
        if (!IsPopulated && JmapOffset >= 0 && JmapSize >= 0 && JmapPath != null) {
            val jsonData = java.io.RandomAccessFile(JmapPath, "r").use { fs ->
                fs.seek(JmapOffset)
                val buffer = ByteArray(JmapSize.toInt())
                var total = 0
                while (total < buffer.size) {
                    val bytesRead = fs.read(buffer, total, buffer.size - total)
                    if (bytesRead < 0) break
                    total += bytesRead
                }
                buffer.copyOfRange(0, total)
            }
            PopulateIfNeeded(jsonData)
        }
    }

    internal fun PopulateIfNeeded(jsonData: ByteArray) {
        if (IsPopulated) return
        JmapHelper.ReadSchema(jsonData, this)
        IsPopulated = true
    }

    internal fun PopulateIfNeeded(jsonData: String) {
        if (IsPopulated) return
        JmapHelper.ReadSchema(jsonData, this)
        IsPopulated = true
    }

    internal fun PopulateIfNeeded(objectBase: JmapObjectBase?) {
        if (IsPopulated) return
        JmapHelper.ReadSchema(objectBase, this)
        IsPopulated = true
    }

    fun GetProperty(key: String, dupIndex: Int): UsmapProperty? {
        PopulateIfNeeded()
        val k = if (CaseInsensitive) key.lowercase() else key
        return PropertiesMap[k to dupIndex]
    }

    fun ConstructPropertiesMap(isCaseInsensitive: Boolean) {
        this.CaseInsensitive = isCaseInsensitive
        PropertiesMap = LinkedHashMap()
        for ((_, prop) in propertiesInternal) {
            val key = if (isCaseInsensitive) prop.Name!!.lowercase() else prop.Name!!
            PropertiesMap[key to prop.ArrayIndex] = prop
        }
    }

    constructor(name: String?, superType: String?, propCount: Int, props: LinkedHashMap<Int, UsmapProperty>, isCaseInsensitive: Boolean, superTypeModulePath: String?, fromAsset: Boolean = false) {
        this.Name = name
        this.SuperType = superType
        this.SuperTypeModulePath = superTypeModulePath
        this.PropCount = propCount
        propertiesInternal = props
        this.FromAsset = fromAsset

        ConstructPropertiesMap(isCaseInsensitive)

        IsPopulated = true
    }

    constructor()
}

class UsmapEnum {
    var Name: String?
        get() {
            PopulateIfNeeded()
            return _Name
        }
        set(value) {
            _Name = value
        }
    private var _Name: String? = null

    var ModulePath: String?
        get() {
            PopulateIfNeeded()
            return _ModulePath
        }
        set(value) {
            _ModulePath = value
        }
    private var _ModulePath: String? = null

    var EnumFlags: Int
        get() {
            PopulateIfNeeded()
            return _EnumFlags
        }
        set(value) {
            _EnumFlags = value
        }
    private var _EnumFlags: Int = 0

    internal var _Values: LinkedHashMap<Long, String?> = LinkedHashMap()

    var Values: LinkedHashMap<Long, String?>
        get() {
            PopulateIfNeeded()
            return _Values
        }
        set(value) {
            _Values = value
        }

    var JmapPath: String? = null
    var JmapOffset: Long = -1
    var JmapSize: Long = -1
    var IsPopulated: Boolean = false

    internal fun PopulateIfNeeded() {
        if (!IsPopulated && JmapOffset >= 0 && JmapSize >= 0 && JmapPath != null) {
            val jsonData = java.io.RandomAccessFile(JmapPath, "r").use { fs ->
                fs.seek(JmapOffset)
                val buffer = ByteArray(JmapSize.toInt())
                var total = 0
                while (total < buffer.size) {
                    val bytesRead = fs.read(buffer, total, buffer.size - total)
                    if (bytesRead < 0) break
                    total += bytesRead
                }
                buffer.copyOfRange(0, total)
            }
            PopulateIfNeeded(jsonData)
        }
    }

    internal fun PopulateIfNeeded(jsonData: ByteArray) {
        if (IsPopulated) return
        JmapHelper.ReadEnum(jsonData, this)
        IsPopulated = true
    }

    internal fun PopulateIfNeeded(jsonData: String) {
        if (IsPopulated) return
        JmapHelper.ReadEnum(jsonData, this)
        IsPopulated = true
    }

    internal fun PopulateIfNeeded(objectBase: JmapObjectBase?) {
        if (IsPopulated) return
        JmapHelper.ReadEnum(objectBase, this)
        IsPopulated = true
    }

    constructor(name: String?, values: LinkedHashMap<Long, String?>) {
        this.Name = name
        this.Values = values
        IsPopulated = true
    }

    constructor()
}

class Usmap {
    /** The path of the file on disk. */
    var FilePath: String = ""

    var Version: UsmapVersion = UsmapVersion.Initial
    var FileVersionUE4: ObjectVersion = ObjectVersion.UNKNOWN
    var FileVersionUE5: ObjectVersionUE5 = ObjectVersionUE5.UNKNOWN
    var CustomVersionContainer: MutableList<CustomVersion>? = null
    var NetCL: Long = 0

    var AreFNamesCaseInsensitive: Boolean = true

    var SkipBlueprintSchemas: Boolean = false

    var NameMap: MutableList<String?> = mutableListOf()
    var EnumMap: CIMap<UsmapEnum> = CIMap(true)
    var Schemas: CIMap<UsmapSchema> = CIMap(true)

    var FailedExtensions: MutableList<String> = mutableListOf()

    /** Cache of asset paths whose schemas have already been pulled into this mapping. */
    var PathsAlreadyProcessedForSchemas: MutableMap<String, Byte> = HashMap()

    fun PathToReader(path: String): UsmapBinaryReader {
        return UsmapBinaryReader(Files.readAllBytes(Path.of(path)), this)
    }

    fun ReadHeader(compressedReader: UsmapBinaryReader): UsmapBinaryReader {
        compressedReader.position = 0
        val fileSignature = compressedReader.ReadUInt16()
        if (fileSignature != USMAP_MAGIC) throw FormatException(".usmap: File signature mismatch")

        val rawVersion = compressedReader.ReadByte()
        if (rawVersion > UsmapVersion.Latest.ordinal) {
            throw FormatException(".usmap: Unknown file version $rawVersion")
        }
        Version = UsmapVersion.entries[rawVersion]

        if (Version >= UsmapVersion.PackageVersioning) {
            val bHasVersioning = compressedReader.ReadInt32() > 0
            if (bHasVersioning) {
                val rawUE4 = compressedReader.ReadInt32()
                FileVersionUE4 = ObjectVersion.entries.firstOrNull { it.value == rawUE4 }
                    ?: ObjectVersion.UNKNOWN
                val rawUE5 = compressedReader.ReadInt32()
                FileVersionUE5 = ObjectVersionUE5.entries.firstOrNull { it.value == rawUE5 }
                    ?: ObjectVersionUE5.UNKNOWN

                CustomVersionContainer = mutableListOf()
                val numCustomVersions = compressedReader.ReadInt32()
                for (i in 0 until numCustomVersions) {
                    val customVersionID = FGuid.fromBytes(compressedReader.ReadBytes(16))
                    val customVersionNumber = compressedReader.ReadInt32()
                    CustomVersionContainer!!.add(CustomVersion(customVersionID, customVersionNumber))
                }

                NetCL = compressedReader.ReadUInt32()
            }
        }

        val methodByte = compressedReader.ReadByte()
        val compressionMethod = if (methodByte == 0xFF) {
            UsmapCompressionMethod.Unknown
        } else {
            UsmapCompressionMethod.entries.getOrElse(methodByte) { UsmapCompressionMethod.Unknown }
        }

        val compressedSize = compressedReader.ReadUInt32()
        val decompressedSize = compressedReader.ReadUInt32()

        return when (compressionMethod) {
            UsmapCompressionMethod.None -> {
                if (compressedSize != decompressedSize) {
                    throw FormatException(".usmap: Compressed size must be equal to decompressed size")
                }
                compressedReader
            }
            UsmapCompressionMethod.ZStandard -> {
                val dat = Zstd.decompress(
                    compressedReader.ReadBytes(compressedSize.toInt()),
                    compressedSize.toInt(),
                    decompressedSize.toInt(),
                )
                UsmapBinaryReader(dat, this)
            }
            else -> throw NotImplementedError(".usmap: Compression method $compressionMethod is unimplemented")
        }
    }

    fun InitPropData(type: UsmapPropertyType): UsmapPropertyData {
        return when (type) {
            UsmapPropertyType.EnumProperty -> UsmapEnumData()
            UsmapPropertyType.StructProperty -> UsmapStructData()
            UsmapPropertyType.SetProperty, UsmapPropertyType.ArrayProperty, UsmapPropertyType.OptionalProperty ->
                UsmapArrayData(type)
            UsmapPropertyType.MapProperty -> UsmapMapData()
            else -> UsmapPropertyData(type)
        }
    }

    fun DeserializePropData(reader: UsmapBinaryReader): UsmapPropertyData {
        val typeByte = reader.ReadByte()
        val res = InitPropData(UsmapPropertyType.entries.getOrElse(typeByte) { UsmapPropertyType.Unknown })
        when (res.Type) {
            UsmapPropertyType.EnumProperty -> {
                (res as UsmapEnumData).InnerType = DeserializePropData(reader)
                res.Name = reader.ReadName()
            }
            UsmapPropertyType.StructProperty -> {
                (res as UsmapStructData).StructType = reader.ReadName()
            }
            UsmapPropertyType.SetProperty, UsmapPropertyType.ArrayProperty, UsmapPropertyType.OptionalProperty -> {
                (res as UsmapArrayData).InnerType = DeserializePropData(reader)
            }
            UsmapPropertyType.MapProperty -> {
                (res as UsmapMapData).InnerType = DeserializePropData(reader)
                res.ValueType = DeserializePropData(reader)
            }
            else -> {}
        }
        return res
    }

    fun ReadUSMAP(compressedReader: UsmapBinaryReader) {
        val reader = ReadHeader(compressedReader)

        NameMap = mutableListOf()
        val numNames = reader.ReadInt32()
        for (i in 0 until numNames) {
            val fixedLength = if (Version >= UsmapVersion.LongFName) reader.ReadInt16().toInt() else reader.ReadByte()
            NameMap.add(reader.ReadString(fixedLength))
        }

        EnumMap = CIMap(AreFNamesCaseInsensitive)
        val numEnums = reader.ReadInt32()
        val enumIndexMap = arrayOfNulls<UsmapEnum>(numEnums)
        for (i in 0 until numEnums) {
            val enumName = reader.ReadName()

            val newEnum = UsmapEnum(enumName, LinkedHashMap())
            val numEnumEntries = if (Version >= UsmapVersion.LargeEnums) reader.ReadInt16().toInt() else reader.ReadByte()

            if (Version >= UsmapVersion.ExplicitEnumValues) {
                for (j in 0 until numEnumEntries) {
                    val value = reader.ReadInt64()
                    val Name = reader.ReadName()
                    newEnum.Values[value] = Name!!
                }
            } else {
                for (j in 0 until numEnumEntries) {
                    newEnum.Values[j.toLong()] = reader.ReadName()
                }
            }

            if (!EnumMap.contains(enumName!!)) {
                enumIndexMap[i] = newEnum
                EnumMap.put(enumName, newEnum)
            }
        }

        Schemas = CIMap(AreFNamesCaseInsensitive)
        val numSchema = reader.ReadInt32()
        val schemaIndexMap = arrayOfNulls<UsmapSchema>(numSchema)
        for (i in 0 until numSchema) {
            val schemaName = reader.ReadName()
            val schemaSuperName = reader.ReadName()
            val numProps = reader.ReadUInt16()
            val serializablePropCount = reader.ReadUInt16()
            val props = LinkedHashMap<Int, UsmapProperty>()
            for (j in 0 until serializablePropCount) {
                val schemaIdx = reader.ReadUInt16()
                val ArraySize = reader.ReadByte()
                val Name = reader.ReadName()

                val currProp = UsmapProperty(Name, schemaIdx, 0, ArraySize, null)
                currProp.PropertyData = DeserializePropData(reader)
                for (k in 0 until ArraySize) {
                    val cln = currProp.clone()
                    cln.SchemaIndex = schemaIdx + k
                    cln.ArrayIndex = k
                    props[schemaIdx + k] = cln
                }
            }

            val newSchema = UsmapSchema(schemaName, schemaSuperName, numProps, props, AreFNamesCaseInsensitive, null)
            schemaIndexMap[i] = newSchema

            if (SkipBlueprintSchemas && schemaName!!.length >= 2 && schemaName.endsWith("_C")) continue
            if (SkipBlueprintSchemas && schemaName == "AnimBlueprintGeneratedConstantData") continue
            if (SkipBlueprintSchemas && schemaName == "AnimBlueprintGeneratedMutableData") continue

            Schemas.put(schemaName!!, newSchema)
        }

        fun readExtension(extId: String, extLeng: Long) {
            val endPos = reader.position + extLeng

            when (extId) {
                "PPTH" -> {
                    val ppthVer = reader.ReadByte()
                    if (ppthVer > 0) return

                    val ppthNumEnums = reader.ReadInt32()
                    for (i in 0 until ppthNumEnums) {
                        enumIndexMap[i]!!.ModulePath = reader.ReadName()
                    }
                    val ppthNumSchemas = reader.ReadInt32()
                    for (i in 0 until ppthNumSchemas) {
                        schemaIndexMap[i]!!.ModulePath = reader.ReadName()
                        Schemas.put(
                            "${schemaIndexMap[i]!!.ModulePath ?: ""}.${schemaIndexMap[i]!!.Name ?: ""}",
                            schemaIndexMap[i]!!,
                        )
                    }

                    if (reader.position.toLong() != endPos) throw FormatException("Failed to parse extension $extId: ended at ${reader.position}, expected $endPos")
                }
                "EATR" -> {
                    val eatrVer = reader.ReadByte()
                    if (eatrVer > 0) return

                    val eatrNumEnums = reader.ReadInt32()
                    for (i in 0 until eatrNumEnums) {
                        enumIndexMap[i]!!.EnumFlags = reader.ReadInt32()
                    }
                    val eatrNumSchemas = reader.ReadInt32()
                    for (i in 0 until eatrNumSchemas) {
                        val kindByte = reader.ReadByte()
                        schemaIndexMap[i]!!.StructKind =
                            UsmapStructKind.entries.getOrElse(kindByte) { UsmapStructKind.None }
                        schemaIndexMap[i]!!.StructOrClassFlags = reader.ReadInt32()
                        val eatrNumProps = reader.ReadInt32()
                        for (j in 0 until eatrNumProps) {
                            val flgs = EPropertyFlags(reader.ReadUInt64())
                            if (j < schemaIndexMap[i]!!.Properties.size) {
                                schemaIndexMap[i]!!.Properties[j]!!.PropertyFlags = flgs
                            }
                        }
                    }

                    if (reader.position.toLong() != endPos) throw FormatException("Failed to parse extension $extId: ended at ${reader.position}, expected $endPos")
                }
                "ENVP" -> {
                    val envpVer = reader.ReadByte()
                    if (envpVer > 0) return

                    val envpNumEnums = reader.ReadInt32()
                    for (i in 0 until envpNumEnums) {
                        enumIndexMap[i]!!.Values.clear()
                        val envpNumEnumEntries = reader.ReadInt32()
                        for (j in 0 until envpNumEnumEntries) {
                            val envpEntryVal = reader.ReadName()
                            val envpEntryKey = reader.ReadInt64()
                            enumIndexMap[i]!!.Values[envpEntryKey] = envpEntryVal
                        }
                    }

                    if (reader.position.toLong() != endPos) throw FormatException("Failed to parse extension $extId: ended at ${reader.position}, expected $endPos")
                }
                "MODL" -> {
                    val numModulePaths = reader.ReadUInt16()
                    val modulePaths = arrayOfNulls<String>(numModulePaths)
                    for (i in 0 until numModulePaths) modulePaths[i] = reader.ReadString()
                    for (i in schemaIndexMap.indices) {
                        val modulePathIndex = if (numModulePaths > 255) reader.ReadUInt16() else reader.ReadByte()
                        schemaIndexMap[i]!!.ModulePath = modulePaths[modulePathIndex]
                    }

                    if (reader.position.toLong() != endPos) throw FormatException("Failed to parse extension $extId: ended at ${reader.position}, expected $endPos")
                }
                else -> {}
            }

            reader.position = endPos.toInt()
        }

        FailedExtensions = mutableListOf()
        if (reader.length > reader.position) {
            val usmapExtensionsMagic = reader.ReadUInt32()
            if (usmapExtensionsMagic == 0x54584543L) { // "CEXT"
                val layoutByte = reader.ReadByte()
                val layoutVer = UsmapExtensionLayoutVersion.entries.getOrElse(layoutByte) {
                    throw IllegalStateException("Unknown extension layout version $layoutByte")
                }
                when (layoutVer) {
                    UsmapExtensionLayoutVersion.Initial -> {
                        val numExtensions = reader.ReadInt32()
                        for (i in 0 until numExtensions) {
                            val extId = reader.ReadString(4)!!
                            val extLeng = reader.ReadUInt32()
                            val endPos = reader.position + extLeng
                            try {
                                readExtension(extId, extLeng)
                            } catch (e: Exception) {
                                FailedExtensions.add(extId)
                                reader.position = endPos.toInt()
                            }
                        }
                    }
                }
            } else if (usmapExtensionsMagic == 1L) { // legacy
                readExtension("MODL", (reader.length - reader.position).toLong())
            }
        }
    }

    fun GetSchemaFromName(nm: String?, asset: UAsset? = null, modulePath: String? = null, throwExceptions: Boolean = true): UsmapSchema? {
        if (nm.isNullOrEmpty()) return null

        val withModulePath = if (modulePath != null) "$modulePath.$nm" else null

        val withoutModulePathComponents = nm.split(".")
        val withoutModulePath = if (withoutModulePathComponents.size > 1) withoutModulePathComponents.last() else null

        var relevantSchema: UsmapSchema? = null
        if (withModulePath != null && Schemas.contains(withModulePath)) {
            relevantSchema = Schemas.get(withModulePath)
        } else if (Schemas.contains(nm)) {
            relevantSchema = Schemas.get(nm)
        } else if (withoutModulePath != null && Schemas.contains(withoutModulePath)) {
            relevantSchema = Schemas.get(withoutModulePath)
        } else {
            relevantSchema = GetSchemaFromStructExport(nm, asset)
        }
        if (throwExceptions && relevantSchema == null) throw FormatException("Failed to find a valid schema for parent name $nm")
        return relevantSchema
    }

    fun GetAllProperties(schemaName: String, modulePath: String? = null, asset: UAsset? = null): MutableList<UsmapProperty> {
        val res = mutableListOf<UsmapProperty>()
        var relevantSchema = GetSchemaFromName(schemaName, asset, modulePath)
        while (relevantSchema != null) {
            res.addAll(relevantSchema.Properties.values)
            relevantSchema = GetSchemaFromName(relevantSchema.SuperType, asset, relevantSchema.SuperTypeModulePath, false)
        }
        return res
    }

    fun ReadJMAP(path: String, lazyRead: Boolean) {
        val jsonData: ByteArray
        val effectiveLazyRead: Boolean
        when {
            path.endsWith(".jmap") -> {
                jsonData = Files.readAllBytes(Path.of(path))
                effectiveLazyRead = lazyRead
            }
            path.endsWith(".jmap.gz") -> {
                jsonData = java.io.FileInputStream(path).use { fs ->
                    java.util.zip.GZIPInputStream(fs).use { gzip ->
                        gzip.readBytes()
                    }
                }
                effectiveLazyRead = false
            }
            else -> throw IllegalStateException("Unable to determine appropriate jmap compression algorithm for file name ${Path.of(path).fileName}")
        }
        ReadJMAPUncompressed(jsonData, effectiveLazyRead, path)
    }

    private fun ReadJMAPUncompressed(data: ByteArray, lazyRead: Boolean, path: String?) {
        EnumMap = CIMap(AreFNamesCaseInsensitive)
        Schemas = CIMap(AreFNamesCaseInsensitive)

        val rootOpen = JmapSkipWs(data, 0)
        if (rootOpen >= data.size || data[rootOpen].toInt().toChar() != '{') throw FormatException(".jmap: Invalid top-level JSON")

        for ((memberName, span) in JmapScanMembers(data, rootOpen)) {
            when (memberName) {
                "metadata" -> ReadJmapMetadata(data, span)
                "objects" -> {
                    for ((schemaName, objSpan) in JmapScanMembers(data, span.start)) {
                        val schemaNameNoPath: String
                        val modulePath: String?
                        if (schemaName.contains(".")) {
                            schemaNameNoPath = schemaName.substring(schemaName.lastIndexOf('.') + 1)
                            modulePath = schemaName.substring(0, schemaName.lastIndexOf('.'))
                        } else {
                            schemaNameNoPath = schemaName
                            modulePath = null
                        }

                        if (schemaName.contains(':') || schemaName.contains("Default__")) continue

                        val offset = objSpan.start.toLong()
                        val size = (objSpan.endExclusive - objSpan.start).toLong()

                        if (lazyRead) {
                            val newSchema = UsmapSchema()
                            newSchema.Name = schemaNameNoPath
                            newSchema.ModulePath = modulePath
                            newSchema.JmapPath = path
                            newSchema.JmapOffset = offset
                            newSchema.JmapSize = size
                            newSchema.IsPopulated = false

                            val newEnum = UsmapEnum()
                            newEnum.Name = schemaNameNoPath
                            newEnum.ModulePath = modulePath
                            newEnum.JmapPath = path
                            newEnum.JmapOffset = offset
                            newEnum.JmapSize = size
                            newEnum.IsPopulated = false

                            Schemas.put(schemaName, newSchema)
                            EnumMap.put(schemaName, newEnum)
                            Schemas.put(schemaNameNoPath, newSchema)
                            EnumMap.put(schemaNameNoPath, newEnum)
                        } else {
                            val objectJSON = String(data, objSpan.start, objSpan.endExclusive - objSpan.start, Charsets.UTF_8)
                            val objectBase = JmapHelper.GetObjectBase(objectJSON)
                            if (objectBase is JmapEnum) {
                                val newEnum = UsmapEnum()
                                newEnum.Name = schemaNameNoPath
                                newEnum.ModulePath = modulePath
                                newEnum.PopulateIfNeeded(objectBase)
                                EnumMap.put(schemaName, newEnum)
                                EnumMap.put(schemaNameNoPath, newEnum)
                            } else {
                                val newSchema = UsmapSchema()
                                newSchema.Name = schemaNameNoPath
                                newSchema.ModulePath = modulePath
                                newSchema.PopulateIfNeeded(objectBase)
                                Schemas.put(schemaName, newSchema)
                                Schemas.put(schemaNameNoPath, newSchema)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun ReadJmapMetadata(data: ByteArray, span: JmapSpan) {
        val node = JmapObjectMapper.readTree(String(data, span.start, span.endExclusive - span.start, Charsets.UTF_8))
        if (!node.isObject) return

        val engineVersionNode = node.get("engine_version")
        if (engineVersionNode == null || !engineVersionNode.isObject) return

        var major = -1
        var minor = -1
        engineVersionNode.get("major")?.let { if (it.isNumber) major = it.asInt() }
        engineVersionNode.get("minor")?.let { if (it.isNumber) minor = it.asInt() }

        if (major >= 0 && minor >= 0) {
            var newVersion = EngineVersion.UNKNOWN
            if (major == 4) {
                newVersion = EngineVersion.entries.getOrElse(EngineVersion.VER_UE4_0.ordinal + minor) { EngineVersion.UNKNOWN }
            } else if (major == 5) {
                newVersion = EngineVersion.entries.getOrElse(EngineVersion.VER_UE5_0.ordinal + minor) { EngineVersion.UNKNOWN }
            }

            if (newVersion != EngineVersion.UNKNOWN) {
                val bridgeVer = UE4VersionToObjectVersion.entries.firstOrNull { it.name.equals(newVersion.name, true) }
                if (bridgeVer != null) {
                    FileVersionUE4 = ObjectVersion.entries.firstOrNull { it.value == bridgeVer.value } ?: ObjectVersion.UNKNOWN

                    val bridgeVer2 = UE5VersionToObjectVersion.entries.firstOrNull { it.name.equals(newVersion.name, true) }
                    if (bridgeVer2 != null) {
                        FileVersionUE5 = ObjectVersionUE5.entries.firstOrNull { it.value == bridgeVer2.value } ?: ObjectVersionUE5.UNKNOWN
                    }

                    CustomVersionContainer = UAsset.GetDefaultCustomVersionContainer(newVersion)
                }
            }
        }
    }

    constructor(path: String) {
        this.FilePath = path
        if (path.endsWith(".jmap")) {
            ReadJMAP(path, true)
        } else if (path.endsWith(".jmap.gz")) {
            ReadJMAP(path, false)
        } else if (path.endsWith(".usmap.gz")) {
            //@parity:on EXC-001
            val dat = java.io.FileInputStream(path).use { fs ->
                java.util.zip.GZIPInputStream(fs).use { gzip ->
                    gzip.readBytes()
                }
            }
            ReadUSMAP(UsmapBinaryReader(dat, this))
            //@parity:off EXC-001
        } else {
            ReadUSMAP(PathToReader(path))
        }
    }

    constructor(reader: UsmapBinaryReader) {
        ReadUSMAP(reader)
    }

    constructor()

    private fun GetSchemaFromStructExport(nm: String?, asset: UAsset?): UsmapSchema? {
        if (nm == null) return null
        return Companion.GetSchemaFromStructExport(nm, asset!!)
    }

    fun TryGetProperty(propertyName: FName?, ancestry: AncestryInfo, dupIndex: Int, asset: UAsset): Pair<UsmapProperty?, Int>? {
        var idx = 0
        var schemaName = ancestry.Parent?.toString()
        var relevantSchema = GetSchemaFromName(schemaName, asset)
        while (schemaName != null && relevantSchema != null) {
            val prop = relevantSchema.GetProperty(propertyName?.toString() ?: "", dupIndex)
            if (prop != null) {
                idx += prop.SchemaIndex
                return Pair(prop, idx)
            }

            idx += relevantSchema.PropCount
            schemaName = relevantSchema.SuperType
            relevantSchema = GetSchemaFromName(schemaName, asset)
        }

        return null
    }

    fun TryGetPropertyData(propertyName: FName?, ancestry: AncestryInfo, asset: UAsset): UsmapPropertyData? {
        if (propertyName == null) return null

        if (propertyName.IsDummy && propertyName.Value?.Value?.toIntOrNull() != null) {
            // this is actually an array member; try to find its parent array
            val arrDat = TryGetPropertyData(ancestry.Parent, ancestry.CloneWithoutParent(), asset) as? UsmapArrayData
            if (arrDat != null && arrDat.InnerType != null) return arrDat.InnerType
        }

        var schemaName = ancestry.Parent?.Value?.Value
        var relevantSchema = GetSchemaFromName(schemaName, asset, null, false)
        while (schemaName != null && relevantSchema != null) {
            val propDat = propertyName.Value?.Value?.let { relevantSchema.GetProperty(it, 0) }?.PropertyData
            if (propDat != null) return propDat
            schemaName = relevantSchema.SuperType
            relevantSchema = GetSchemaFromName(schemaName, asset, null, false)
        }

        return null
    }

    companion object {
        const val USMAP_MAGIC = 0x30C4

        fun ConvertFPropertyToUsmapPropertyData(exp: StructExport, entry: FProperty): UsmapPropertyData {
            val typ = entry.GetUsmapPropertyType()
            val converted1: UsmapPropertyData?
            when (typ) {
                UsmapPropertyType.EnumProperty -> {
                    val enumIndex = (entry as FEnumProperty).Enum
                    val underlyingProp = (entry as FEnumProperty).UnderlyingProp
                    if (enumIndex.IsExport()) {
                        val exp2 = enumIndex.ToExport(exp.Asset!!) as EnumExport
                        val allNames = mutableListOf<String>()
                        for (cosa in exp2.Enum!!.Names) allNames.add(cosa.first.toString())
                        converted1 = UsmapEnumData(exp2.ObjectName.toString(), allNames).apply {
                            InnerType = ConvertFPropertyToUsmapPropertyData(exp, underlyingProp!!)
                        }
                    } else if (enumIndex.IsImport()) {
                        val enumName = enumIndex.ToImport(exp.Asset!!)?.ObjectName?.Value?.Value
                        val value: UsmapEnum? = if (enumName == null) null else exp.Asset!!.Mappings?.EnumMap?.get(enumName)
                        if (enumName.isNullOrEmpty() || value == null) {
                            if (!exp.Asset!!.HasUnversionedProperties) {
                                return UsmapEnumData(enumName, emptyList()).apply {
                                    InnerType = UsmapPropertyData(UsmapPropertyType.ByteProperty)
                                }
                            } else {
                                throw IllegalStateException("Attempt to index into non-existent enum " + enumName)
                            }
                        }
                        val allNames = mutableListOf<String>()
                        for (cosa in value.Values) allNames.add(cosa.toString())
                        converted1 = UsmapEnumData(enumName, allNames).apply {
                            InnerType = ConvertFPropertyToUsmapPropertyData(exp, underlyingProp!!)
                        }
                    } else {
                        converted1 = null
                    }
                }
                UsmapPropertyType.ByteProperty -> {
                    val enumIndex = (entry as FByteProperty).Enum
                    if (enumIndex.IsExport()) {
                        val exp2 = enumIndex.ToExport(exp.Asset!!) as EnumExport
                        val allNames = mutableListOf<String>()
                        for (cosa in exp2.Enum!!.Names) allNames.add(cosa.first.toString())
                        converted1 = UsmapEnumData(exp2.ObjectName.toString(), allNames).apply {
                            InnerType = UsmapPropertyData(UsmapPropertyType.ByteProperty)
                        }
                    } else if (enumIndex.IsImport()) {
                        val enumName = enumIndex.ToImport(exp.Asset!!)?.ObjectName?.Value?.Value
                        val value: UsmapEnum? = if (enumName == null) null else exp.Asset!!.Mappings?.EnumMap?.get(enumName)
                        if (enumName.isNullOrEmpty() || value == null) {
                            if (!exp.Asset!!.HasUnversionedProperties) {
                                return UsmapEnumData(enumName, emptyList()).apply {
                                    InnerType = UsmapPropertyData(UsmapPropertyType.ByteProperty)
                                }
                            } else {
                                throw IllegalStateException("Attempt to index into non-existent enum " + enumName)
                            }
                        }
                        val allNames = mutableListOf<String>()
                        for (cosa in value.Values) allNames.add(cosa.toString())
                        converted1 = UsmapEnumData(enumName, allNames).apply {
                            InnerType = UsmapPropertyData(UsmapPropertyType.ByteProperty)
                        }
                    } else {
                        converted1 = UsmapPropertyData(UsmapPropertyType.ByteProperty)
                    }
                }
                UsmapPropertyType.StructProperty -> {
                    val strucstr = Export.GetClassTypeForAncestry((entry as FStructProperty).Struct, exp.Asset!!, Out())
                    converted1 = UsmapStructData(strucstr.toString())
                }
                UsmapPropertyType.SetProperty ->
                    converted1 = UsmapArrayData(typ).apply {
                        InnerType = ConvertFPropertyToUsmapPropertyData(exp, (entry as FSetProperty).ElementProp!!)
                    }
                UsmapPropertyType.ArrayProperty ->
                    converted1 = UsmapArrayData(typ).apply {
                        InnerType = ConvertFPropertyToUsmapPropertyData(exp, (entry as FArrayProperty).Inner!!)
                    }
                UsmapPropertyType.MapProperty ->
                    converted1 = UsmapMapData().apply {
                        InnerType = ConvertFPropertyToUsmapPropertyData(exp, (entry as FMapProperty).KeyProp!!)
                        ValueType = ConvertFPropertyToUsmapPropertyData(exp, (entry as FMapProperty).ValueProp!!)
                    }
                else -> converted1 = UsmapPropertyData(typ)
            }
            return converted1!!
        }

        fun ConvertUPropertyToUsmapPropertyData(exp: PropertyExport): UsmapPropertyData {
            val asset = exp.Asset!!
            val typ = exp.Property!!.GetUsmapPropertyType()
            val converted: UsmapPropertyData?
            when (val p = exp.Property!!) {
                is UEnumProperty -> {
                    val enumIndex = p.Enum
                    val underlyingProp = p.UnderlyingProp
                    if (enumIndex.IsExport()) {
                        val exp2 = enumIndex.ToExport(asset) as EnumExport
                        val allNames = mutableListOf<String>()
                        for (cosa in exp2.Enum!!.Names) allNames.add(cosa.first.toString())
                        converted = UsmapEnumData(exp2.ObjectName.toString(), allNames).apply {
                            InnerType = ConvertUPropertyToUsmapPropertyData(underlyingProp.ToExport(asset) as PropertyExport)
                        }
                    } else if (enumIndex.IsImport()) {
                        val enumName = enumIndex.ToImport(asset)?.ObjectName?.Value?.Value
                        val value: UsmapEnum? = if (enumName == null) null else exp.Asset!!.Mappings?.EnumMap?.get(enumName)
                        if (enumName == null || value == null) {
                            if (!exp.Asset!!.HasUnversionedProperties) {
                                return UsmapEnumData(enumName, emptyList()).apply {
                                    InnerType = UsmapPropertyData(UsmapPropertyType.ByteProperty)
                                }
                            } else {
                                throw IllegalStateException("Attempt to index into non-existent enum " + enumName)
                            }
                        }
                        val allNames = mutableListOf<String>()
                        for (cosa in value.Values) allNames.add(cosa.toString())
                        converted = UsmapEnumData(enumName, allNames).apply {
                            InnerType = ConvertUPropertyToUsmapPropertyData(underlyingProp.ToExport(asset) as PropertyExport)
                        }
                    } else {
                        converted = null
                    }
                }
                is UByteProperty -> {
                    val enumIndex = p.Enum
                    if (enumIndex.IsExport()) {
                        val exp2 = enumIndex.ToExport(asset) as EnumExport
                        val allNames = mutableListOf<String>()
                        for (cosa in exp2.Enum!!.Names) allNames.add(cosa.first.toString())
                        converted = UsmapEnumData(exp2.ObjectName.toString(), allNames).apply {
                            InnerType = UsmapPropertyData(UsmapPropertyType.ByteProperty)
                        }
                    } else if (enumIndex.IsImport()) {
                        val enumName = enumIndex.ToImport(asset)?.ObjectName?.Value?.Value
                        val value: UsmapEnum? = if (enumName == null) null else exp.Asset!!.Mappings?.EnumMap?.get(enumName)
                        if (enumName == null || value == null) {
                            if (!exp.Asset!!.HasUnversionedProperties) {
                                return UsmapEnumData(enumName, emptyList()).apply {
                                    InnerType = UsmapPropertyData(UsmapPropertyType.ByteProperty)
                                }
                            } else {
                                throw IllegalStateException("Attempt to index into non-existent enum " + enumName)
                            }
                        }
                        val allNames = mutableListOf<String>()
                        for (cosa in value.Values) allNames.add(cosa.toString())
                        converted = UsmapEnumData(enumName, allNames).apply {
                            InnerType = UsmapPropertyData(UsmapPropertyType.ByteProperty)
                        }
                    } else {
                        converted = UsmapPropertyData(UsmapPropertyType.ByteProperty)
                    }
                }
                is UStructProperty -> {
                    val strucstr = Export.GetClassTypeForAncestry(p.Struct, asset, Out())
                    converted = UsmapStructData(strucstr.toString())
                }
                is UArrayProperty ->
                    converted = UsmapArrayData(UsmapPropertyType.ArrayProperty).apply {
                        InnerType = ConvertUPropertyToUsmapPropertyData(p.Inner.ToExport(asset) as PropertyExport)
                    }
                is USetProperty ->
                    converted = UsmapArrayData(UsmapPropertyType.SetProperty).apply {
                        InnerType = ConvertUPropertyToUsmapPropertyData(p.ElementProp.ToExport(asset) as PropertyExport)
                    }
                is UMapProperty ->
                    converted = UsmapMapData().apply {
                        InnerType = ConvertUPropertyToUsmapPropertyData(p.KeyProp.ToExport(asset) as PropertyExport)
                        ValueType = ConvertUPropertyToUsmapPropertyData(p.ValueProp.ToExport(asset) as PropertyExport)
                    }
                else -> converted = UsmapPropertyData(typ)
            }
            return converted!!
        }

        fun GetSchemaFromStructExport(exportName: String, asset: UAsset): UsmapSchema? {
            if (asset == null) throw IllegalStateException("Cannot evaluate struct export without package reference")
            for (exp in asset.Exports) {
                if (exp.ObjectName?.Value?.Value == exportName && exp is StructExport) return GetSchemaFromStructExport(exp, asset.Mappings?.AreFNamesCaseInsensitive ?: true)
            }
            return null
        }

        fun GetSchemaFromStructExport(exp: StructExport, isCaseInsensitive: Boolean): UsmapSchema {
            val res = LinkedHashMap<Int, UsmapProperty>()
            var idx = 0
            if (exp.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FCoreObjectVersion")) >= FCoreObjectVersion.FProperties.ordinal) {
                for (entry in exp.LoadedProperties!!) {
                    val converted = UsmapProperty(entry.Name!!.toString(), idx, 0, 1, ConvertFPropertyToUsmapPropertyData(exp, entry))
                    res[idx] = converted
                    idx++
                }
            } else {
                val childlist = mutableListOf<FPackageIndex>()
                if (exp.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFrameworkObjectVersion")) < FFrameworkObjectVersion.RemoveUField_Next.ordinal && exp.Children!!.isNotEmpty()) {
                    var next = exp.Children!!.first()
                    while (!next.IsNull()) {
                        childlist.add(next)
                        next = when (val expo = next.ToExport(exp.Asset!!)) {
                            is FunctionExport -> expo.Field?.Next ?: FPackageIndex()
                            is PropertyExport -> expo.Property?.Next ?: FPackageIndex()
                            else -> FPackageIndex()
                        }
                    }
                }
                val children = if (exp.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFrameworkObjectVersion")) >= FFrameworkObjectVersion.RemoveUField_Next.ordinal) exp.Children!! else childlist.toTypedArray()

                for (entry in children) {
                    if (entry.ToExport(exp.Asset!!) !is PropertyExport) continue

                    val converted = UsmapProperty((entry.ToExport(exp.Asset!!) as PropertyExport).ObjectName.toString(), idx, 0, 1, ConvertUPropertyToUsmapPropertyData(entry.ToExport(exp.Asset!!) as PropertyExport))
                    res[idx] = converted
                    idx++
                }
            }

            val ssName = if (exp.SuperStruct?.IsImport() == true) exp.SuperStruct!!.ToImport(exp.Asset!!)!!.ObjectName.toString() else null
            val ssPath = if (exp.SuperStruct?.IsImport() == true && exp.SuperStruct!!.ToImport(exp.Asset!!)!!.OuterIndex?.IsImport() == true) exp.SuperStruct!!.ToImport(exp.Asset!!)!!.OuterIndex!!.ToImport(exp.Asset!!)!!.ObjectName.toString() else null
            return UsmapSchema(exp.ObjectName.toString(), ssName, res.size, res, isCaseInsensitive, ssPath, true)
        }
    }
}

/** Byte span of a single JSON value within a .jmap file ([start] inclusive, [endExclusive] exclusive). */
private data class JmapSpan(val start: Int, val endExclusive: Int)

private val JmapObjectMapper = com.fasterxml.jackson.databind.ObjectMapper()

private fun JmapSkipWs(data: ByteArray, pos: Int): Int {
    var p = pos
    while (p < data.size) {
        val c = data[p].toInt().toChar()
        if (c != ' ' && c != '\t' && c != '\n' && c != '\r') break
        p++
    }
    return p
}

private fun JmapReadString(data: ByteArray, start: Int): Pair<String, Int> {
    var p = start + 1
    val sb = StringBuilder()
    while (p < data.size) {
        when (data[p].toInt().toChar()) {
            '"' -> return sb.toString() to (p + 1)
            '\\' -> {
                when (data[p + 1].toInt().toChar()) {
                    '"' -> {
                        sb.append('"'); p += 2
                    }
                    '\\' -> {
                        sb.append('\\'); p += 2
                    }
                    '/' -> {
                        sb.append('/'); p += 2
                    }
                    'b' -> {
                        sb.append('\b'); p += 2
                    }
                    'f' -> {
                        sb.append('\u000C'); p += 2
                    }
                    'n' -> {
                        sb.append('\n'); p += 2
                    }
                    'r' -> {
                        sb.append('\r'); p += 2
                    }
                    't' -> {
                        sb.append('\t'); p += 2
                    }
                    'u' -> {
                        val hex = String(data, p + 2, 4, Charsets.UTF_8)
                        sb.append(hex.toInt(16).toChar())
                        p += 6
                    }
                    else -> {
                        sb.append(data[p + 1].toInt().toChar()); p += 2
                    }
                }
            }
            else -> {
                sb.append(data[p].toInt().toChar()); p++
            }
        }
    }
    throw FormatException(".jmap: Unterminated JSON string")
}

private fun JmapFindMatchingClose(data: ByteArray, open: Int): Int {
    var depth = 0
    var p = open
    while (p < data.size) {
        when (data[p].toInt().toChar()) {
            '"' -> p = JmapReadString(data, p).second
            '{', '[' -> {
                depth++
                p++
            }
            '}', ']' -> {
                depth--
                p++
                if (depth == 0) return p
            }
            else -> p++
        }
    }
    throw FormatException(".jmap: Unterminated JSON object or array")
}

private fun JmapValueEnd(data: ByteArray, start: Int): Int {
    var p = JmapSkipWs(data, start)
    if (p >= data.size) throw FormatException(".jmap: Unexpected end of input")
    return when (data[p].toInt().toChar()) {
        '{', '[' -> JmapFindMatchingClose(data, p)
        '"' -> JmapReadString(data, p).second
        else -> {
            while (p < data.size) {
                val c = data[p].toInt().toChar()
                if (c == ',' || c == '}' || c == ']' || c == ' ' || c == '\t' || c == '\n' || c == '\r') break
                p++
            }
            p
        }
    }
}

private fun JmapScanMembers(data: ByteArray, openBrace: Int): List<Pair<String, JmapSpan>> {
    val res = mutableListOf<Pair<String, JmapSpan>>()
    var p = JmapSkipWs(data, openBrace + 1)
    if (p >= data.size) throw FormatException(".jmap: Unexpected end of object")
    if (data[p].toInt().toChar() == '}') return res
    while (true) {
        if (data[p].toInt().toChar() != '"') throw FormatException(".jmap: Expected JSON property name")
        val (name, afterName) = JmapReadString(data, p)
        p = JmapSkipWs(data, afterName)
        if (p >= data.size || data[p].toInt().toChar() != ':') throw FormatException(".jmap: Expected ':' after property name")
        p = JmapSkipWs(data, p + 1)
        val valueEnd = JmapValueEnd(data, p)
        res.add(name to JmapSpan(p, valueEnd))
        p = JmapSkipWs(data, valueEnd)
        if (p >= data.size) throw FormatException(".jmap: Unexpected end of object")
        when (data[p].toInt().toChar()) {
            ',' -> p = JmapSkipWs(data, p + 1)
            '}' -> break
            else -> throw FormatException(".jmap: Expected ',' or '}' after JSON value")
        }
    }
    return res
}
