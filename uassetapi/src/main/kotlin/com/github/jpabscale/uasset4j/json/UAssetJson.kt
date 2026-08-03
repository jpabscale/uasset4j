// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UAsset.cs (SerializeJson/DeserializeJson JSON plumbing) +
//        UAssetAPI/JSON/UAssetContractResolver.cs
package com.github.jpabscale.uasset4j.json

import com.github.jpabscale.uasset4j.BitArray
import com.github.jpabscale.uasset4j.CustomSerializationFlags
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.EPackageFlags
import com.github.jpabscale.uasset4j.FAssetRegistryRecord
import com.github.jpabscale.uasset4j.FEngineVersion
import com.github.jpabscale.uasset4j.FGenerationInfo
import com.github.jpabscale.uasset4j.Import
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.exporttypes.EClassSerializationControlExtension
import com.github.jpabscale.uasset4j.exporttypes.Export
import com.github.jpabscale.uasset4j.exporttypes.UDataTable
import com.github.jpabscale.uasset4j.propertytypes.objects.EPropertyTagExtension
import com.github.jpabscale.uasset4j.propertytypes.objects.EPropertyTagFlags
import com.github.jpabscale.uasset4j.propertytypes.objects.FSoftObjectPath
import com.github.jpabscale.uasset4j.propertytypes.objects.MapPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.unrealtypes.EObjectDataResourceVersion
import com.github.jpabscale.uasset4j.unrealtypes.EObjectFlags
import com.github.jpabscale.uasset4j.unrealtypes.EClassFlags
import com.github.jpabscale.uasset4j.unrealtypes.EFunctionFlags
import com.github.jpabscale.uasset4j.unrealtypes.EObjectDataResourceFlags
import com.github.jpabscale.uasset4j.unrealtypes.EPropertyFlags
import com.github.jpabscale.uasset4j.propertytypes.objects.ETextFlag
import com.github.jpabscale.uasset4j.unrealtypes.FGatherableTextData
import com.github.jpabscale.uasset4j.unrealtypes.FGuid
import com.github.jpabscale.uasset4j.unrealtypes.FMetaData
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FObjectDataResource
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.FPropertyTypeName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.FWorldTileInfo
import com.github.jpabscale.uasset4j.unrealtypes.GameSpecificOverride
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.SerializableString
import com.fasterxml.jackson.core.io.CharacterEscapes
import com.fasterxml.jackson.core.io.SerializedString
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.util.TreeMap

/**
 * Newtonsoft escapes control characters in JSON strings as lowercase-hex `\u00xx`; Jackson 2.19
 * emits uppercase. This forces the lowercase form so string-embedded control bytes match byte-for-byte.
 */
class NewtonsoftCharacterEscapes : CharacterEscapes() {
    private val table: IntArray = CharacterEscapes.standardAsciiEscapesForJSON().also { t ->
        for (i in 0 until 0x20) {
            if (i != 0x0A && i != 0x0D && i != 0x09 && i != 0x08 && i != 0x0C && i != 0x22 && i != 0x5C) {
                t[i] = CharacterEscapes.ESCAPE_CUSTOM
            }
        }
    }

    override fun getEscapeCodesForAscii(): IntArray = table

    override fun getEscapeSequence(ch: Int): SerializableString? {
        if (ch < 0x20) {
            return SerializedString(java.lang.String.format("\\u%04x", ch))
        }
        return null
    }
}

/**
 * The shared, centrally-configured Jackson [ObjectMapper] that reproduces UAssetAPI's Newtonsoft
 * JSON output byte-for-byte. All converters, mixins, and the member-ordering serializer modifier
 * live here; `UAsset.SerializeJson*` / `DeserializeJson*` are thin wrappers over it.
 */
object UAssetJson {
    val mapper: ObjectMapper = JsonMapperBuilder().build()

    @Suppress("UNCHECKED_CAST")
    private fun SimpleModule.addFlagsDeserializer(
        type: Class<*>,
        deser: JsonDeserializer<*>,
    ) {
        addDeserializer(type as Class<Any>, deser as JsonDeserializer<Any>)
    }

    @Suppress("UNCHECKED_CAST")
    private fun SimpleModule.addFlagsSerializer(
        type: Class<*>,
        ser: JsonSerializer<*>,
    ) {
        addSerializer(type as Class<Any>, ser as JsonSerializer<Any>)
    }

    private fun JsonMapperBuilder(): com.fasterxml.jackson.databind.json.JsonMapper.Builder {
        val factory = com.fasterxml.jackson.core.JsonFactory().setCharacterEscapes(NewtonsoftCharacterEscapes())
        val module = SimpleModule("UAssetAPIJson").apply {
            addSerializer(FName::class.java, FNameJsonConverter())
            addDeserializer(FName::class.java, FNameJsonConverter.deserializer)
            addKeyDeserializer(
                FName::class.java,
                object : com.fasterxml.jackson.databind.KeyDeserializer() {
                    override fun deserializeKey(key: String, ctxt: DeserializationContext): FName {
                        val res = FName.DefineDummy(null, "temp", FNameToBeFilled.nextIndex())
                        FNameToBeFilled.current().add(res to key)
                        return res
                    }
                },
            )
            addKeyDeserializer(
                com.github.jpabscale.uasset4j.propertytypes.objects.FSoftObjectPath::class.java,
                object : com.fasterxml.jackson.databind.KeyDeserializer() {
                    override fun deserializeKey(key: String, ctxt: DeserializationContext): com.github.jpabscale.uasset4j.propertytypes.objects.FSoftObjectPath {
                        val inner = key.removePrefix("(").removeSuffix(")")
                        val parts = inner.split(", ", limit = 3)
                        fun name(s: String?): com.github.jpabscale.uasset4j.unrealtypes.FName? {
                            if (s.isNullOrEmpty() || s == "null") return null
                            val res = com.github.jpabscale.uasset4j.unrealtypes.FName.DefineDummy(null, "temp", FNameToBeFilled.nextIndex())
                            FNameToBeFilled.current().add(res to s)
                            return res
                        }
                        val sub = parts.getOrNull(2)
                        return com.github.jpabscale.uasset4j.propertytypes.objects.FSoftObjectPath(
                            name(parts.getOrNull(0)),
                            name(parts.getOrNull(1)),
                            if (sub.isNullOrEmpty() || sub == "null") null else com.github.jpabscale.uasset4j.unrealtypes.FString(sub),
                        )
                    }
                },
            )
            addSerializer(FString::class.java, FStringJsonConverter())
            addDeserializer(FString::class.java, FStringJsonConverter.deserializer)
            addSerializer(FPackageIndex::class.java, FPackageIndexJsonConverter())
            addDeserializer(FPackageIndex::class.java, FPackageIndexJsonConverter.deserializer)
            addKeyDeserializer(
                FPackageIndex::class.java,
                object : com.fasterxml.jackson.databind.KeyDeserializer() {
                    override fun deserializeKey(key: String, ctxt: DeserializationContext): FPackageIndex =
                        FPackageIndex(key.toIntOrNull() ?: 0)
                },
            )
            addSerializer(FGuid::class.java, GuidJsonConverter())
            addDeserializer(FGuid::class.java, GuidJsonConverter.deserializer)
            addSerializer(ByteArray::class.java, ByteArrayJsonConverter())
            addDeserializer(ByteArray::class.java, ByteArrayJsonConverter.deserializer)
            addSerializer(com.github.jpabscale.uasset4j.kismet.bytecode.expressions.ECastTokenValue::class.java, ECastTokenJsonConverter())
            addDeserializer(com.github.jpabscale.uasset4j.kismet.bytecode.expressions.ECastTokenValue::class.java, ECastTokenJsonConverter.deserializer)
            addSerializer(com.github.jpabscale.uasset4j.fieldtypes.EArrayDim::class.java, EArrayDimJsonConverter())
            addDeserializer(com.github.jpabscale.uasset4j.fieldtypes.EArrayDim::class.java, EArrayDimJsonConverter.deserializer)
            addSerializer(com.github.jpabscale.uasset4j.fieldtypes.ELifetimeCondition::class.java, ELifetimeConditionJsonConverter())
            addDeserializer(com.github.jpabscale.uasset4j.fieldtypes.ELifetimeCondition::class.java, ELifetimeConditionJsonConverter.deserializer)
            addSerializer(com.github.jpabscale.uasset4j.propertytypes.structs.core.Color::class.java, ColorJsonConverter())
            addDeserializer(com.github.jpabscale.uasset4j.propertytypes.structs.core.Color::class.java, ColorJsonConverter.deserializer)
            addSerializer(BitArray::class.java, BitArrayJsonConverter())
            addDeserializer(BitArray::class.java, BitArrayJsonConverter.deserializer)
            addSerializer(Float::class.java, FSignedZeroJsonConverter())
            addDeserializer(Float::class.java, FSignedZeroJsonConverter.deserializer)
            addSerializer(Double::class.java, FSignedZeroDoubleJsonConverter())
            addDeserializer(Double::class.java, FSignedZeroDoubleJsonConverter.deserializer)
            addSerializer(FPropertyTypeName::class.java, FPropertyTypeNameJsonConverter())
            addDeserializer(FPropertyTypeName::class.java, FPropertyTypeNameJsonConverter.deserializer)
            addSerializer(com.github.jpabscale.uasset4j.exporttypes.FStringTable::class.java, FStringTableJsonConverter())
            addDeserializer(com.github.jpabscale.uasset4j.exporttypes.FStringTable::class.java, FStringTableJsonConverter.deserializer)
            addSerializer(com.github.jpabscale.uasset4j.propertytypes.objects.FFormatArgumentValue::class.java, FFormatArgumentValueJsonSerializer())
            addDeserializer(com.github.jpabscale.uasset4j.propertytypes.objects.FFormatArgumentValue::class.java, FFormatArgumentValueJsonDeserializer())
            addSerializer(EPropertyTagFlags::class.java, FlagsConverters.propertyTagFlagsSerializer)
            addFlagsDeserializer(EPropertyTagFlags::class.java, FlagsConverters.propertyTagFlagsDeserializer)
            addSerializer(EPropertyTagExtension::class.java, FlagsConverters.propertyTagExtensionSerializer)
            addFlagsDeserializer(EPropertyTagExtension::class.java, FlagsConverters.propertyTagExtensionDeserializer)
            addSerializer(EClassSerializationControlExtension::class.java, FlagsConverters.classSerializationControlExtensionSerializer)
            addFlagsDeserializer(EClassSerializationControlExtension::class.java, FlagsConverters.classSerializationControlExtensionDeserializer)
            addSerializer(CustomSerializationFlags::class.java, FlagsConverters.customSerializationFlagsSerializer)
            addFlagsDeserializer(CustomSerializationFlags::class.java, FlagsConverters.customSerializationFlagsDeserializer)
            addSerializer(EPackageFlags::class.java, FlagsConverters.packageFlagsSerializer)
            addFlagsDeserializer(EPackageFlags::class.java, FlagsConverters.packageFlagsDeserializer)
            addSerializer(EObjectFlags::class.java, FlagsConverters.objectFlagsSerializer)
            addFlagsDeserializer(EObjectFlags::class.java, FlagsConverters.objectFlagsDeserializer)
            addSerializer(EClassFlags::class.java, FlagsConverters.classFlagsSerializer)
            addFlagsDeserializer(EClassFlags::class.java, FlagsConverters.classFlagsDeserializer)
            addSerializer(EPropertyFlags::class.java, FlagsConverters.propertyFlagsSerializer)
            addFlagsDeserializer(EPropertyFlags::class.java, FlagsConverters.propertyFlagsDeserializer)
            addSerializer(EFunctionFlags::class.java, FlagsConverters.functionFlagsSerializer)
            addFlagsDeserializer(EFunctionFlags::class.java, FlagsConverters.functionFlagsDeserializer)
            addSerializer(EObjectDataResourceFlags::class.java, FlagsConverters.objectDataResourceFlagsSerializer)
            addFlagsDeserializer(EObjectDataResourceFlags::class.java, FlagsConverters.objectDataResourceFlagsDeserializer)
            addSerializer(ETextFlag::class.java, FlagsConverters.textFlagSerializer)
            addFlagsDeserializer(ETextFlag::class.java, FlagsConverters.textFlagDeserializer)
            addSerializer(UAsset::class.java, UAssetSerializer())
            addDeserializer(UAsset::class.java, UAssetDeserializer())
            setSerializerModifier(UAssetBeanSerializerModifier())
            setDeserializerModifier(UAssetBeanDeserializerModifier())
        }
        return com.fasterxml.jackson.databind.json.JsonMapper.builder(factory)
            .annotationIntrospector(UAssetAnnotationIntrospector())
            .addModule(KotlinModule.Builder().build())
            .addModule(module)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }

    private fun ObjectMapper.applyMixins(): ObjectMapper = apply {
        addMixIn(PropertyData::class.java, PropertyDataMixin::class.java)
        addMixIn(Export::class.java, ExportMixin::class.java)
        addMixIn(CustomVersion::class.java, CustomVersionMixin::class.java)
        addMixIn(Import::class.java, ImportMixin::class.java)
        addMixIn(FGenerationInfo::class.java, FGenerationInfoMixin::class.java)
        addMixIn(FEngineVersion::class.java, FEngineVersionMixin::class.java)
        addMixIn(UDataTable::class.java, UDataTableMixin::class.java)
        addMixIn(MapPropertyData::class.java, MapPropertyDataMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.exporttypes.ClassExport::class.java, ClassExportMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.fieldtypes.UField::class.java, FieldTypeInfoMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.fieldtypes.FField::class.java, FieldTypeInfoMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.fieldtypes.FBoolProperty::class.java, FBoolPropertyMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector::class.java, FVectorMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector2D::class.java, FVector2DMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector4::class.java, FVector4Mixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FQuat::class.java, FQuatMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FPlane::class.java, FPlaneMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FRotator::class.java, FRotatorMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FLinearColor::class.java, FLinearColorMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FIntVector::class.java, FieldTypeInfoMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FIntVector2::class.java, FieldTypeInfoMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FMatrix::class.java, FieldTypeInfoMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FTwoVectors::class.java, FieldTypeInfoMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector2f::class.java, FieldTypeInfoMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector3f::class.java, FieldTypeInfoMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector4f::class.java, FieldTypeInfoMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.unrealtypes.objects.engine.FRichCurveKey::class.java, FRichCurveKeyMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.propertytypes.structs.engine.FNavAgentSelector::class.java, FNavAgentSelectorMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.unrealtypes.objects.engine.FSkeletalMeshSamplingRegionBuiltData::class.java, FSkeletalMeshSamplingRegionBuiltDataMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.propertytypes.structs.engine.FStringCurveKey::class.java, FStringCurveKeyMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.unrealtypes.objects.engine.FWeightedRandomSampler::class.java, FWeightedRandomSamplerMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.propertytypes.objects.FSoftObjectPath::class.java, FSoftObjectPathMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.propertytypes.objects.FTopLevelAssetPath::class.java, FTopLevelAssetPathMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.unrealtypes.FPropertyTypeNameNode::class.java, FPropertyTypeNameNodeMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.unrealtypes.FObjectDataResource::class.java, FObjectDataResourceMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression::class.java, KismetExpressionMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.kismet.bytecode.KismetPropertyPointer::class.java, KismetPropertyPointerMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.kismet.bytecode.FScriptText::class.java, FScriptTextMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.propertytypes.objects.FFieldPath::class.java, FFieldPathMixin::class.java)
        addMixIn(com.github.jpabscale.uasset4j.propertytypes.structs.movies.FMovieSceneEventParameters::class.java, FMovieSceneEventParametersMixin::class.java)
    }

    init {
        mapper.applyMixins()
    }

    /** Serializes [value] with (or without) the pretty-printed 2-space indentation. */
    fun write(value: Any?, formatted: Boolean): String {
        if (value == null) return "null"
        return if (formatted) mapper.writer(prettyPrinter).writeValueAsString(value)
        else mapper.writeValueAsString(value)
    }

    /**
     * Newtonsoft's Formatting.Indented: 2-space indent, each array element on its own line, and a
     * bare `": "` field separator (Jackson's default printer puts a space before the colon and
     * keeps arrays inline).
     */
    val prettyPrinter: com.fasterxml.jackson.core.util.DefaultPrettyPrinter by lazy {
        UAssetPrettyPrinter()
    }

    /** Deserializes a [UAsset] from [json]. */
    fun read(json: String): UAsset {
        FNameToBeFilled.clear()
        return mapper.readValue(json, UAsset::class.java)
    }

    fun read(json: java.io.InputStream): UAsset {
        FNameToBeFilled.clear()
        return mapper.readValue(json, UAsset::class.java)
    }

    fun <T : Any> readGeneric(json: String, target: Class<T>): T {
        FNameToBeFilled.clear()
        return mapper.readValue(json, target)
    }

    /**
     * Resolves every deferred dummy FName recorded during deserialization against [asset]'s name
     * map (mirrors the ToBeFilled loop in UAsset.DeserializeJson).
     */
    fun resolveNames(asset: UAsset?) {
        for ((fname, str) in FNameToBeFilled.current()) {
            fname.Asset = asset
            if (asset != null && FName.IsFromStringValid(asset, str)) {
                val dummy = FName.FromString(asset, str)
                if (dummy != null) {
                    fname.Value = dummy.Value
                    fname.Number = dummy.Number
                } else {
                    fname.DummyValue = FString.FromString(str)
                    fname.Number = 0
                }
            } else {
                fname.DummyValue = FString.FromString(str)
                fname.Number = 0
            }
        }
        FNameToBeFilled.clear()
    }
}

/** Newtonsoft-Indented-compatible pretty printer: 2-space indent + `": "` separators. */
private class UAssetPrettyPrinter : com.fasterxml.jackson.core.util.DefaultPrettyPrinter() {
    init {
        _objectIndenter = com.fasterxml.jackson.core.util.DefaultIndenter.SYSTEM_LINEFEED_INSTANCE
        _arrayIndenter = com.fasterxml.jackson.core.util.DefaultIndenter.SYSTEM_LINEFEED_INSTANCE
    }

    override fun createInstance(): com.fasterxml.jackson.core.util.DefaultPrettyPrinter = UAssetPrettyPrinter()

    override fun writeObjectFieldValueSeparator(g: JsonGenerator) {
        g.writeRaw(": ")
    }

    override fun writeStartObject(g: JsonGenerator) {
        g.writeRaw('{')
        _nesting++
    }

    override fun writeEndObject(g: JsonGenerator, nrOfValues: Int) {
        if (nrOfValues > 0) {
            _objectIndenter.writeIndentation(g, _nesting - 1)
        }
        _nesting--
        g.writeRaw('}')
    }

    override fun writeStartArray(g: JsonGenerator) {
        g.writeRaw('[')
        _nesting++
    }

    override fun writeEndArray(g: JsonGenerator, nrOfValues: Int) {
        if (nrOfValues > 0) {
            _arrayIndenter.writeIndentation(g, _nesting - 1)
        }
        _nesting--
        g.writeRaw(']')
    }
}

/** UAsset serializer: emits `$type` + the 62 properties in the exact C# oracle order. */
class UAssetSerializer : StdSerializer<UAsset>(UAsset::class.java) {
    override fun serialize(value: UAsset, gen: JsonGenerator, provider: SerializerProvider) {
        val tf = provider.typeFactory
        gen.writeStartObject()
        gen.writeStringField("\$type", "UAssetAPI.UAsset, UAssetAPI")
        gen.writeObjectField("Info", value.Info)
        writeTyped(gen, provider, listType(tf, FString::class.java), "NameMap", value.NameMapIndexList)
        gen.writeObjectField("CustomSerializationFlags", value.CustomSerializationFlags)
        gen.writeObjectField("UseSeparateBulkDataFiles", value.UseSeparateBulkDataFiles)
        gen.writeObjectField("IsUnversioned", value.IsUnversioned)
        gen.writeObjectField("FileVersionLicenseeUE", value.FileVersionLicenseeUE)
        gen.writeObjectField("GameSpecificOverride", value.GameSpecificOverride)
        gen.writeObjectField("ObjectVersion", value.ObjectVersion)
        gen.writeObjectField("ObjectVersionUE5", value.ObjectVersionUE5)
        writeTyped(gen, provider, listType(tf, CustomVersion::class.java), "CustomVersionContainer", value.CustomVersionContainer)
        writeTyped(gen, provider, listType(tf, FSoftObjectPath::class.java), "SoftObjectPathList", value.SoftObjectPathList)
        writeTyped(gen, provider, listType(tf, FGatherableTextData::class.java), "GatherableTextData", value.GatherableTextData)
        writeTyped(gen, provider, listType(tf, Export::class.java), "Exports", value.Exports)
        gen.writeObjectField("SearchableNames", value.SearchableNames)
        writeTMapField(gen, "ImportTypeHierarchies", value.ImportTypeHierarchies)
        gen.writeObjectField("MetaData", value.MetaData)
        gen.writeObjectField("Thumbnails", value.Thumbnails)
        gen.writeObjectField("WorldTileInfo", value.WorldTileInfo)
        gen.writeObjectField("AppendedNullBytes", value.AppendedNullBytes)
        gen.writeObjectField("OtherAssetsFailedToAccess", value.OtherAssetsFailedToAccess)
        gen.writeObjectField("LegacyFileVersion", value.LegacyFileVersion)
        gen.writeObjectField("DataResourceVersion", value.DataResourceVersion)
        writeTyped(gen, provider, listType(tf, FObjectDataResource::class.java), "DataResources", value.DataResources)
        gen.writeObjectField("UsesEventDrivenLoader", value.UsesEventDrivenLoader)
        gen.writeObjectField("WillSerializeNameHashes", value.WillSerializeNameHashes)
        writeTyped(gen, provider, listType(tf, Import::class.java), "Imports", value.Imports)
        gen.writeObjectField("DependsMap", value.DependsMap)
        writeTyped(gen, provider, listType(tf, FString::class.java), "SoftPackageReferenceList", value.SoftPackageReferenceList)
        gen.writeObjectField("AssetRegistryDependencyDataOffset", value.AssetRegistryDependencyDataOffset)
        writeTyped(gen, provider, listType(tf, FAssetRegistryRecord::class.java), "AssetRegistryRecords", value.AssetRegistryRecords)
        gen.writeObjectField("ImportBits", value.ImportBits)
        gen.writeObjectField("SoftPackageBits", value.SoftPackageBits)
        gen.writeObjectField("ExtraPackageDependencies", value.ExtraPackageDependencies)
        gen.writeObjectField("BulkData", value.BulkData)
        gen.writeObjectField("AdditionalFiles", value.AdditionalFiles)
        gen.writeObjectField("Trailer", value.Trailer)
        writeTyped(gen, provider, listType(tf, FGenerationInfo::class.java), "Generations", value.Generations)
        gen.writeObjectField("PackageGuid", value.PackageGuid)
        gen.writeObjectField("PersistentGuid", value.PersistentGuid)
        gen.writeObjectField("RecordedEngineVersion", value.RecordedEngineVersion)
        gen.writeObjectField("RecordedCompatibleWithEngineVersion", value.RecordedCompatibleWithEngineVersion)
        gen.writeObjectField("ChunkIDs", value.ChunkIDs)
        gen.writeObjectField("PackageSource", value.PackageSource)
        gen.writeObjectField("FolderName", value.FolderName)
        gen.writeObjectField("LocalizationId", value.LocalizationId)
        gen.writeObjectField("SoftObjectPathsCount", value.SoftObjectPathsCount)
        gen.writeObjectField("SoftObjectPathsOffset", value.SoftObjectPathsOffset)
        gen.writeObjectField("SearchableNamesOffset", value.SearchableNamesOffset)
        gen.writeObjectField("ThumbnailTableOffset", value.ThumbnailTableOffset)
        gen.writeObjectField("ImportTypeHierarchiesCount", value.ImportTypeHierarchiesCount)
        gen.writeObjectField("ImportTypeHierarchiesOffset", value.ImportTypeHierarchiesOffset)
        gen.writeObjectField("SavedHash", value.SavedHash)
        gen.writeObjectField("CompressionFlags", value.CompressionFlags)
        writeTyped(gen, provider, listType(tf, FString::class.java), "AdditionalPackagesToCook", value.AdditionalPackagesToCook)
        gen.writeObjectField("NamesReferencedFromExportDataCount", value.NamesReferencedFromExportDataCount)
        gen.writeObjectField("PayloadTocOffset", value.PayloadTocOffset)
        gen.writeObjectField("DataResourceOffset", value.DataResourceOffset)
        gen.writeObjectField("doWeHaveAssetRegistryData", value.doWeHaveAssetRegistryData)
        gen.writeObjectField("doWeHaveWorldTileInfo", value.doWeHaveWorldTileInfo)
        gen.writeObjectField("PackageFlags", value.PackageFlags)
        gen.writeObjectField("HasUnversionedProperties", value.HasUnversionedProperties)
        gen.writeObjectField("IsFilterEditorOnly", value.IsFilterEditorOnly)
        gen.writeEndObject()
    }

    private fun listType(tf: com.fasterxml.jackson.databind.type.TypeFactory, elem: Class<*>): JavaType =
        tf.constructCollectionType(List::class.java, elem)

    private fun writeTyped(
        gen: JsonGenerator,
        provider: SerializerProvider,
        type: JavaType,
        name: String,
        value: Any?,
    ) {
        gen.writeFieldName(name)
        if (value == null) {
            gen.writeNull()
            return
        }
        provider.findTypedValueSerializer(type, true, null).serialize(value, gen, provider)
    }

    private fun writeTMapField(
        gen: JsonGenerator,
        name: String,
        map: LinkedHashMap<com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex, com.github.jpabscale.uasset4j.FImportTypeHierarchy>?,
    ) {
        gen.writeFieldName(name)
        if (map == null) {
            gen.writeNull()
            return
        }
        gen.writeStartArray()
        for ((k, v) in map) {
            gen.writeStartArray()
            gen.writeObject(k)
            gen.writeObject(v)
            gen.writeEndArray()
        }
        gen.writeEndArray()
    }
}

/** UAsset deserializer: reads `$type` + all 62 properties back into a [UAsset]. */
class UAssetDeserializer : StdDeserializer<UAsset>(UAsset::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): UAsset {
        val root = p.readValueAsTree<JsonNode>()
        val asset = UAsset()
        val codec = p.codec

        fun <T> read(cls: Class<T>, node: JsonNode?): T? =
            if (node == null || node.isNull) null else codec.treeToValue(node, cls)

        fun readList(cls: Class<*>, node: JsonNode?): MutableList<Any>? =
            if (node == null || node.isNull) null else {
                val jt = ctxt.typeFactory.constructCollectionType(List::class.java, ctxt.constructType(cls))
                ((codec as ObjectMapper).convertValue(node, jt) as? List<Any>)?.toMutableList()
            }

        root.get("Info")?.let { if (!it.isNull) asset.Info = it.asText() }
        readList(FString::class.java, root.get("NameMap"))?.let { asset.NameMapIndexList = it as MutableList<FString> }
        root.get("CustomSerializationFlags")?.let { if (!it.isNull) asset.CustomSerializationFlags = FlagsConverters.parseCustomSerializationFlags(it.asText()) }
        root.get("UseSeparateBulkDataFiles")?.let { if (!it.isNull) asset.UseSeparateBulkDataFiles = it.asBoolean() }
        root.get("IsUnversioned")?.let { if (!it.isNull) asset.IsUnversioned = it.asBoolean() }
        root.get("FileVersionLicenseeUE")?.let { if (!it.isNull) asset.FileVersionLicenseeUE = it.asInt() }
        read(GameSpecificOverride::class.java, root.get("GameSpecificOverride"))?.let { asset.GameSpecificOverride = it }
        read(ObjectVersion::class.java, root.get("ObjectVersion"))?.let { asset.ObjectVersion = it }
        read(ObjectVersionUE5::class.java, root.get("ObjectVersionUE5"))?.let { asset.ObjectVersionUE5 = it }
        readList(CustomVersion::class.java, root.get("CustomVersionContainer"))?.let { asset.CustomVersionContainer = it as MutableList<CustomVersion> }
        readList(FSoftObjectPath::class.java, root.get("SoftObjectPathList"))?.let { asset.SoftObjectPathList = it as MutableList<FSoftObjectPath> }
        readList(FGatherableTextData::class.java, root.get("GatherableTextData"))?.let { asset.GatherableTextData = it as MutableList<FGatherableTextData> }
        readList(Export::class.java, root.get("Exports"))?.let { asset.Exports = it as MutableList<Export> }
        root.get("SearchableNames")?.let { sn ->
            if (!sn.isNull) {
                val jt = ctxt.typeFactory.constructMapType(
                    TreeMap::class.java, ctxt.constructType(FPackageIndex::class.java),
                    ctxt.typeFactory.constructCollectionType(List::class.java, ctxt.constructType(FName::class.java)),
                )
                asset.SearchableNames = (codec as ObjectMapper).convertValue(sn, jt) as TreeMap<FPackageIndex, MutableList<FName>>
            }
        }
        root.get("ImportTypeHierarchies")?.let { if (!it.isNull) asset.ImportTypeHierarchies = readTMap(ctxt, p, it) }
        read(FMetaData::class.java, root.get("MetaData"))?.let { asset.MetaData = it }
        root.get("Thumbnails")?.let { th ->
            if (!th.isNull) {
                val jt = ctxt.typeFactory.constructMapType(
                    LinkedHashMap::class.java, ctxt.constructType(String::class.java),
                    ctxt.constructType(com.github.jpabscale.uasset4j.unrealtypes.FObjectThumbnail::class.java),
                )
                asset.Thumbnails = (codec as ObjectMapper).convertValue(th, jt) as LinkedHashMap<String, com.github.jpabscale.uasset4j.unrealtypes.FObjectThumbnail>
            }
        }
        read(FWorldTileInfo::class.java, root.get("WorldTileInfo"))?.let { asset.WorldTileInfo = it }
        root.get("AppendedNullBytes")?.let { if (!it.isNull) asset.AppendedNullBytes = it.asInt() }
        root.get("OtherAssetsFailedToAccess")?.let { oa ->
            if (oa.isArray) for (n in oa) read(FName::class.java, n)?.let { asset.OtherAssetsFailedToAccess.add(it) }
        }
        root.get("LegacyFileVersion")?.let { if (!it.isNull) asset.LegacyFileVersion = it.asInt() }
        read(EObjectDataResourceVersion::class.java, root.get("DataResourceVersion"))?.let { asset.DataResourceVersion = it }
        readList(FObjectDataResource::class.java, root.get("DataResources"))?.let { asset.DataResources = it as MutableList<FObjectDataResource> }
        root.get("UsesEventDrivenLoader")?.let { if (!it.isNull) asset.UsesEventDrivenLoader = it.asBoolean() }
        root.get("WillSerializeNameHashes")?.let { if (!it.isNull) asset.WillSerializeNameHashes = it.asBoolean() }
        readList(Import::class.java, root.get("Imports"))?.let { asset.Imports = it as MutableList<Import> }
        readList(IntArray::class.java, root.get("DependsMap"))?.let { asset.DependsMap = it as MutableList<IntArray> }
        readList(FString::class.java, root.get("SoftPackageReferenceList"))?.let { asset.SoftPackageReferenceList = it as MutableList<FString> }
        root.get("AssetRegistryDependencyDataOffset")?.let { if (!it.isNull) asset.AssetRegistryDependencyDataOffset = it.asLong() }
        readList(FAssetRegistryRecord::class.java, root.get("AssetRegistryRecords"))?.let { asset.AssetRegistryRecords = it as MutableList<FAssetRegistryRecord> }
        read(BitArray::class.java, root.get("ImportBits"))?.let { asset.ImportBits = it }
        read(BitArray::class.java, root.get("SoftPackageBits"))?.let { asset.SoftPackageBits = it }
        read(ByteArray::class.java, root.get("BulkData"))?.let { asset.BulkData = it }
        read(ByteArray::class.java, root.get("AdditionalFiles"))?.let { asset.AdditionalFiles = it }
        read(ByteArray::class.java, root.get("Trailer"))?.let { asset.Trailer = it }
        readList(FGenerationInfo::class.java, root.get("Generations"))?.let { asset.Generations = it as MutableList<FGenerationInfo> }
        read(FGuid::class.java, root.get("PackageGuid"))?.let { asset.PackageGuid = it }
        read(FGuid::class.java, root.get("PersistentGuid"))?.let { asset.PersistentGuid = it }
        read(FEngineVersion::class.java, root.get("RecordedEngineVersion"))?.let { asset.RecordedEngineVersion = it }
        read(FEngineVersion::class.java, root.get("RecordedCompatibleWithEngineVersion"))?.let { asset.RecordedCompatibleWithEngineVersion = it }
        root.get("ChunkIDs")?.let { if (!it.isNull) asset.ChunkIDs = codec.treeToValue(it, IntArray::class.java) }
        root.get("PackageSource")?.let { if (!it.isNull) asset.PackageSource = it.asLong() }
        read(FString::class.java, root.get("FolderName"))?.let { asset.FolderName = it }
        read(FString::class.java, root.get("LocalizationId"))?.let { asset.LocalizationId = it }
        root.get("SoftObjectPathsCount")?.let { if (!it.isNull) asset.SoftObjectPathsCount = it.asInt() }
        root.get("SoftObjectPathsOffset")?.let { if (!it.isNull) asset.SoftObjectPathsOffset = it.asInt() }
        root.get("SearchableNamesOffset")?.let { if (!it.isNull) asset.SearchableNamesOffset = it.asInt() }
        root.get("ThumbnailTableOffset")?.let { if (!it.isNull) asset.ThumbnailTableOffset = it.asInt() }
        root.get("ImportTypeHierarchiesCount")?.let { if (!it.isNull) asset.ImportTypeHierarchiesCount = it.asInt() }
        root.get("ImportTypeHierarchiesOffset")?.let { if (!it.isNull) asset.ImportTypeHierarchiesOffset = it.asInt() }
        read(ByteArray::class.java, root.get("SavedHash"))?.let { asset.SavedHash = it }
        root.get("CompressionFlags")?.let { if (!it.isNull) asset.CompressionFlags = it.asLong() }
        readList(FString::class.java, root.get("AdditionalPackagesToCook"))?.let { asset.AdditionalPackagesToCook = it as MutableList<FString> }
        root.get("NamesReferencedFromExportDataCount")?.let { if (!it.isNull) asset.NamesReferencedFromExportDataCount = it.asInt() }
        root.get("PayloadTocOffset")?.let { if (!it.isNull) asset.PayloadTocOffset = it.asLong() }
        root.get("DataResourceOffset")?.let { if (!it.isNull) asset.DataResourceOffset = it.asInt() }
        root.get("doWeHaveAssetRegistryData")?.let { if (!it.isNull) asset.doWeHaveAssetRegistryData = it.asBoolean() }
        root.get("doWeHaveWorldTileInfo")?.let { if (!it.isNull) asset.doWeHaveWorldTileInfo = it.asBoolean() }
        root.get("PackageFlags")?.let { if (!it.isNull) asset.PackageFlags = FlagsConverters.parsePackageFlags(it.asText()) }

        for (ex in asset.Exports) ex.Asset = asset
        return asset
    }

    private fun readTMap(
        ctxt: DeserializationContext,
        p: JsonParser,
        node: JsonNode,
    ): LinkedHashMap<FPackageIndex, com.github.jpabscale.uasset4j.FImportTypeHierarchy> {
        val res = LinkedHashMap<FPackageIndex, com.github.jpabscale.uasset4j.FImportTypeHierarchy>()
        val codec = p.codec
        for (pair in node) {
            val k = codec.treeToValue(pair.get(0), FPackageIndex::class.java)
            val v = codec.treeToValue(pair.get(1), com.github.jpabscale.uasset4j.FImportTypeHierarchy::class.java)
            if (k != null && v != null) res[k] = v
        }
        return res
    }
}
