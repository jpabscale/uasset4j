// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/JSON/UAssetContractResolver.cs (TypeNameHandling.Objects parity)
package com.github.jpabscale.uasset4j.json

import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.FEngineVersion
import com.github.jpabscale.uasset4j.FGenerationInfo
import com.github.jpabscale.uasset4j.Import
import com.github.jpabscale.uasset4j.exporttypes.Export
import com.github.jpabscale.uasset4j.exporttypes.UDataTable
import com.github.jpabscale.uasset4j.propertytypes.objects.MapPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver

/**
 * Newtonsoft's TypeNameHandling.Objects emits a `"$type"` property on every POCO. In Jackson this
 * is `@JsonTypeInfo` with a custom [TypeIdResolver]. These mixins apply it to the polymorphic
 * hierarchies (PropertyData, Export) and to the standalone POCOs that UAsset serializes, so every
 * emitted object carries `"$type": "<Namespace>.<Class>, UAssetAPI"` and every incoming `$type` is
 * resolved back to the right class.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface PropertyDataMixin

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface ExportMixin

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface CustomVersionMixin

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface ImportMixin

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FGenerationInfoMixin

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FEngineVersionMixin

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface UDataTableMixin

/**
 * MapPropertyData.Value is a C# `TMap<PropertyData, PropertyData>` serialized by TMapJsonConverter
 * as an ordered array of [key, value] pairs.
 */
internal interface MapPropertyDataMixin {
    @get:JsonSerialize(using = TMapJsonSerializer::class)
    @get:JsonDeserialize(using = TMapJsonDeserializer::class)
    val Value: Map<*, *>
}

/** ClassExport.FuncMap is a C# `TMap<FName, FPackageIndex>` serialized as [key, value] pairs. */
internal interface ClassExportMixin {
    @get:JsonSerialize(using = TMapJsonSerializer::class)
    @get:JsonDeserialize(using = TMapJsonDeserializer::class)
    val FuncMap: Map<*, *>?
}

/**
 * UField and FProperty are serialized with `$type` under TypeNameHandling.Objects (e.g.
 * "UAssetAPI.FieldTypes.UField, UAssetAPI", "UAssetAPI.FieldTypes.FObjectProperty, UAssetAPI").
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FieldTypeInfoMixin {
    /** FField.MetaDataMap is a C# `TMap<FName, FString>` serialized as [key, value] pairs. */
    @get:JsonSerialize(using = TMapJsonSerializer::class)
    @get:JsonDeserialize(using = TMapJsonDeserializer::class)
    val MetaDataMap: Map<*, *>?
}

/** FVector: `$type` + X/Y/Z doubles; the Float getters are [JsonIgnore] in C#. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FVectorMixin {
    @get:JsonIgnore
    val XFloat: Float
    @get:JsonIgnore
    val YFloat: Float
    @get:JsonIgnore
    val ZFloat: Float
}

/** FVector2D: `$type` + X/Y doubles; the Float getters are [JsonIgnore] in C#. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FVector2DMixin {
    @get:JsonIgnore
    val XFloat: Float
    @get:JsonIgnore
    val YFloat: Float
}

/** FVector4: `$type` + X/Y/Z/W doubles; the Float getters are [JsonIgnore] in C#. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FVector4Mixin {
    @get:JsonIgnore
    val XFloat: Float
    @get:JsonIgnore
    val YFloat: Float
    @get:JsonIgnore
    val ZFloat: Float
    @get:JsonIgnore
    val WFloat: Float
}

/** FQuat: `$type` + X/Y/Z/W doubles; the Float getters are [JsonIgnore] in C#. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FQuatMixin {
    @get:JsonIgnore
    val XFloat: Float
    @get:JsonIgnore
    val YFloat: Float
    @get:JsonIgnore
    val ZFloat: Float
    @get:JsonIgnore
    val WFloat: Float
}

/** FPlane: `$type` + X/Y/Z/W doubles; the Float getters are [JsonIgnore] in C#. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FPlaneMixin {
    @get:JsonIgnore
    val XFloat: Float
    @get:JsonIgnore
    val YFloat: Float
    @get:JsonIgnore
    val ZFloat: Float
    @get:JsonIgnore
    val WFloat: Float
}

/** FRotator: `$type` + Pitch/Yaw/Roll doubles; the Float getters are [JsonIgnore] in C#. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FRotatorMixin {
    @get:JsonIgnore
    val PitchFloat: Float
    @get:JsonIgnore
    val YawFloat: Float
    @get:JsonIgnore
    val RollFloat: Float
}

/** FLinearColor: `$type` + R/G/B/A float fields. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FLinearColorMixin

/** FRichCurveKey: `$type` + the curve key fields. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FRichCurveKeyMixin

/** FNavAgentSelector: `$type` + PackedBits. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FNavAgentSelectorMixin

/** FSkeletalMeshSamplingRegionBuiltData: `$type` + the public arrays (AreaWeightedSampler is private in C#). */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FSkeletalMeshSamplingRegionBuiltDataMixin {
    @get:JsonIgnore
    val AreaWeightedSampler: Any?
}

/** FStringCurveKey: `$type` + Time/Value. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FStringCurveKeyMixin

/** FWeightedRandomSampler: `$type` + Prob/Alias/TotalWeight. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FWeightedRandomSamplerMixin

/** FBoolProperty's byte fields are C# `byte` (0-255), serialized unsigned. */
internal interface FBoolPropertyMixin {
    @get:JsonSerialize(using = UnsignedByteJsonConverter::class)
    val FieldSize: Byte
    @get:JsonSerialize(using = UnsignedByteJsonConverter::class)
    val ByteOffset: Byte
    @get:JsonSerialize(using = UnsignedByteJsonConverter::class)
    val ByteMask: Byte
    @get:JsonSerialize(using = UnsignedByteJsonConverter::class)
    val FieldMask: Byte
}

/** FSoftObjectPath: `$type` + AssetPath/SubPathString (emitted under TypeNameHandling.Objects). */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FSoftObjectPathMixin

/** FTopLevelAssetPath: `$type` + PackageName/AssetName. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FTopLevelAssetPathMixin

/** FPropertyTypeNameNode: `$type` + Name/InnerCount. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FPropertyTypeNameNodeMixin

/** FObjectDataResource: `$type` + the bulk-data resource fields. */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FObjectDataResourceMixin

/**
 * Kismet bytecode expressions (C# `[JsonObject(MemberSerialization.OptIn)]`) emit `$type` under
 * TypeNameHandling.Objects; the bean modifier filters each EX_* class to its `[JsonProperty]` members.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface KismetExpressionMixin

/** KismetPropertyPointer: `$type` + the Old/New members (ShouldSerialize-gated). */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface KismetPropertyPointerMixin

/** FScriptText: `$type` + the text literal members (C# `[JsonObject(MemberSerialization.OptIn)]`). */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FScriptTextMixin

/** FFieldPath: `$type` + Path/ResolvedOwner (emitted under TypeNameHandling.Objects). */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FFieldPathMixin

/** FMovieSceneEventParameters: `$type` + StructType/StructBytes (emitted under TypeNameHandling.Objects). */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "\$type", visible = true)
@JsonTypeIdResolver(UAssetTypeIdResolver::class)
internal interface FMovieSceneEventParametersMixin
