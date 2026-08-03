// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/MainSerializer.cs
// NOTE: L3 core port. The property type registry is built explicitly (replacing the C# reflection scan),
// TypeToClass/Read/Write and the unversioned-header fragment packer are ported in full.
// ReadFProperty/WriteFProperty/ReadUProperty/WriteUProperty are wired to the FieldTypes classes.
package com.github.jpabscale.uasset4j

import com.github.jpabscale.uasset4j.fieldtypes.FArrayProperty
import com.github.jpabscale.uasset4j.fieldtypes.FBoolProperty
import com.github.jpabscale.uasset4j.fieldtypes.FByteProperty
import com.github.jpabscale.uasset4j.fieldtypes.FClassProperty
import com.github.jpabscale.uasset4j.fieldtypes.FDelegateProperty
import com.github.jpabscale.uasset4j.fieldtypes.FEnumProperty
import com.github.jpabscale.uasset4j.fieldtypes.FGenericProperty
import com.github.jpabscale.uasset4j.fieldtypes.FInterfaceProperty
import com.github.jpabscale.uasset4j.fieldtypes.FMapProperty
import com.github.jpabscale.uasset4j.fieldtypes.FMulticastDelegateProperty
import com.github.jpabscale.uasset4j.fieldtypes.FMulticastInlineDelegateProperty
import com.github.jpabscale.uasset4j.fieldtypes.FNumericProperty
import com.github.jpabscale.uasset4j.fieldtypes.FObjectProperty
import com.github.jpabscale.uasset4j.fieldtypes.FOptionalProperty
import com.github.jpabscale.uasset4j.fieldtypes.FProperty
import com.github.jpabscale.uasset4j.fieldtypes.FSetProperty
import com.github.jpabscale.uasset4j.fieldtypes.FSoftClassProperty
import com.github.jpabscale.uasset4j.fieldtypes.FSoftObjectProperty
import com.github.jpabscale.uasset4j.fieldtypes.FStructProperty
import com.github.jpabscale.uasset4j.fieldtypes.FWeakObjectProperty
import com.github.jpabscale.uasset4j.fieldtypes.UArrayProperty
import com.github.jpabscale.uasset4j.fieldtypes.UAssetClassProperty
import com.github.jpabscale.uasset4j.fieldtypes.UAssetObjectProperty
import com.github.jpabscale.uasset4j.fieldtypes.UBoolProperty
import com.github.jpabscale.uasset4j.fieldtypes.UByteProperty
import com.github.jpabscale.uasset4j.fieldtypes.UClassProperty
import com.github.jpabscale.uasset4j.fieldtypes.UDelegateProperty
import com.github.jpabscale.uasset4j.fieldtypes.UDoubleProperty
import com.github.jpabscale.uasset4j.fieldtypes.UEnumProperty
import com.github.jpabscale.uasset4j.fieldtypes.UFloatProperty
import com.github.jpabscale.uasset4j.fieldtypes.UGenericProperty
import com.github.jpabscale.uasset4j.fieldtypes.UIntProperty
import com.github.jpabscale.uasset4j.fieldtypes.UInterfaceProperty
import com.github.jpabscale.uasset4j.fieldtypes.ULazyObjectProperty
import com.github.jpabscale.uasset4j.fieldtypes.UMapProperty
import com.github.jpabscale.uasset4j.fieldtypes.UMulticastDelegateProperty
import com.github.jpabscale.uasset4j.fieldtypes.UMulticastInlineDelegateProperty
import com.github.jpabscale.uasset4j.fieldtypes.UMulticastSparseDelegateProperty
import com.github.jpabscale.uasset4j.fieldtypes.UNameProperty
import com.github.jpabscale.uasset4j.fieldtypes.UNumericProperty
import com.github.jpabscale.uasset4j.fieldtypes.UObjectProperty
import com.github.jpabscale.uasset4j.fieldtypes.UProperty
import com.github.jpabscale.uasset4j.fieldtypes.USetProperty
import com.github.jpabscale.uasset4j.fieldtypes.USoftClassProperty
import com.github.jpabscale.uasset4j.fieldtypes.USoftObjectProperty
import com.github.jpabscale.uasset4j.fieldtypes.UStrProperty
import com.github.jpabscale.uasset4j.fieldtypes.UStructProperty
import com.github.jpabscale.uasset4j.fieldtypes.UTextProperty
import com.github.jpabscale.uasset4j.fieldtypes.UWeakObjectProperty
import com.github.jpabscale.uasset4j.propertytypes.objects.AncestryInfo
import com.github.jpabscale.uasset4j.propertytypes.objects.ArrayPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.AssetObjectPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.BoolPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.BytePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.DelegatePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.DoublePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.EPropertyTagFlags
import com.github.jpabscale.uasset4j.propertytypes.objects.EnumPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.FieldPathPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.FloatPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.Int16PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.Int64PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.Int8PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.IntPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.InterfacePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.MapPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.MulticastDelegatePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.MulticastInlineDelegatePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.MulticastSparseDelegatePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.NamePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.ObjectPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.SetPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.SoftObjectPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.StrPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.TextPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.UInt16PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.UInt32PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.UInt64PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.UnknownPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.Utf8StrPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.WeakObjectPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.ClothLODDataCommonPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.ClothLODDataPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.ClothTetherDataPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.GameplayTagContainerPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.LevelSequenceObjectReferenceMapPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.RawStructPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.SoftAssetPathPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.SoftClassPathPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.SoftObjectPathPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.StringAssetReferencePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.StringClassReferencePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.core.ColorPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.core.DateTimePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.core.GuidPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.core.InstancedStructPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.core.TimespanPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.ColorMaterialInputPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.ExpressionInputPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.FontCharacterPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.KeyHandleMapPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.MaterialAttributesInputPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.MaterialOverrideNanitePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.NavAgentSelectorPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.PerPlatformBoolPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.PerPlatformFloatPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.PerPlatformFrameRatePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.PerPlatformIntPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.PerQualityLevelFloatPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.PerQualityLevelIntPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.RichCurveKeyPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.ScalarMaterialInputPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.SkeletalMeshAreaWeightedTriangleSamplerPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.SkeletalMeshSamplingLODBuiltDataPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.SkeletalMeshSamplingRegionBuiltDataPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.SmartNamePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.SplinePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.StringCurveKeyPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.Vector2MaterialInputPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.VectorMaterialInputPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.ViewTargetBlendParamsPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.WeightedRandomSamplerPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.Box2DPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.Box2fPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.BoxPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.IntPointPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.IntVector2PropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.IntVectorPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.LinearColorPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.MatrixPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.PlanePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.QuatPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.RotatorPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.TwoVectorsPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.Vector2DPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.Vector2fPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.Vector3fPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.Vector4PropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.Vector4fPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.VectorNetQuantize10PropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.VectorNetQuantize100PropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.VectorNetQuantizeNormalPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.VectorNetQuantizePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.VectorPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.FrameNumberPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.MovieSceneDoubleChannelPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.MovieSceneEvalTemplatePtrPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.MovieSceneEvaluationFieldEntityTreePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.MovieSceneEvaluationKeyPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.MovieSceneEventParametersPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.MovieSceneFloatChannelPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.MovieSceneFloatValuePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.MovieSceneFrameRangePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.MovieSceneGenerationLedgerPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.MovieSceneSegmentIdentifierPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.MovieSceneSegmentPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.MovieSceneSequenceIDPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.MovieSceneSequenceInstanceDataPtrPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.MovieSceneSubSectionFieldDataPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.MovieSceneSubSequenceTreePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.MovieSceneTemplatePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.MovieSceneTrackFieldDataPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.MovieSceneTrackIdentifierPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.MovieSceneTrackImplementationPtrPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.NameCurveKeyPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.SectionEvaluationDataTreePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.niagara.NiagaraDataChannelVariablePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.niagara.NiagaraDataInterfaceGPUParamInfoPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.niagara.NiagaraVariableBasePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.niagara.NiagaraVariablePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.niagara.NiagaraVariableWithOffsetPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.ranges.FloatRangePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.slate.DeprecateSlateVector2DPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.slate.FontDataPropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FPropertyTypeName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5
import com.github.jpabscale.uasset4j.unrealtypes.UniversalObjectLocatorFragmentPropertyData
import com.github.jpabscale.uasset4j.unversioned.FFragment
import com.github.jpabscale.uasset4j.unversioned.FUnversionedHeader
import com.github.jpabscale.uasset4j.unversioned.UsmapProperty
import com.github.jpabscale.uasset4j.unversioned.UsmapSchema
import com.github.jpabscale.uasset4j.unversioned.UsmapStructData
import java.util.BitSet
import kotlin.reflect.KClass

/** An entry in the property type registry. Contains the class Type used for standard and struct property serialization. */
class RegistryEntry {
    var PropertyType: KClass<out PropertyData>? = null
    var HasCustomStructSerialization: Boolean = false
    var Creator: (FName?) -> PropertyData = { throw NotImplementedError("RegistryEntry.Creator: L3") }

    constructor()
}

/** The main serializer for most property types in UAssetAPI. */
object MainSerializer {
    internal val AdditionalPropertyRegistry: Array<String> = arrayOf("ClassProperty", "SoftClassProperty", "AssetClassProperty")

    const val MaxSerializedArrayLength: Long = 1024L * 1024L

    private var _propertyTypeRegistry: MutableMap<String, RegistryEntry> = mutableMapOf()

    /** The property type registry. Maps serialized property names to their types. */
    internal var PropertyTypeRegistry: MutableMap<String, RegistryEntry>
        get() = _propertyTypeRegistry
        set(value) {
            _propertyTypeRegistry = value // I hope you know what you're doing!
        }

    private val allNonLetters = Regex("[^a-zA-Z]")

    /** Maps a serialized type name (with non-letters stripped) to its FField-derived class. */
    private val fPropertyRegistry: Map<String, KClass<out FProperty>> = mapOf(
        "EnumProperty" to FEnumProperty::class,
        "ArrayProperty" to FArrayProperty::class,
        "SetProperty" to FSetProperty::class,
        "ObjectProperty" to FObjectProperty::class,
        "SoftObjectProperty" to FSoftObjectProperty::class,
        "WeakObjectProperty" to FWeakObjectProperty::class,
        "ClassProperty" to FClassProperty::class,
        "SoftClassProperty" to FSoftClassProperty::class,
        "DelegateProperty" to FDelegateProperty::class,
        "MulticastDelegateProperty" to FMulticastDelegateProperty::class,
        "MulticastInlineDelegateProperty" to FMulticastInlineDelegateProperty::class,
        "InterfaceProperty" to FInterfaceProperty::class,
        "MapProperty" to FMapProperty::class,
        "BoolProperty" to FBoolProperty::class,
        "ByteProperty" to FByteProperty::class,
        "StructProperty" to FStructProperty::class,
        "NumericProperty" to FNumericProperty::class,
        "OptionalProperty" to FOptionalProperty::class,
    )

    /** Maps a serialized type name (with non-letters stripped) to its UField-derived class. */
    private val uPropertyRegistry: Map<String, KClass<out UProperty>> = mapOf(
        "EnumProperty" to UEnumProperty::class,
        "ArrayProperty" to UArrayProperty::class,
        "SetProperty" to USetProperty::class,
        "ObjectProperty" to UObjectProperty::class,
        "WeakObjectProperty" to UWeakObjectProperty::class,
        "SoftObjectProperty" to USoftObjectProperty::class,
        "LazyObjectProperty" to ULazyObjectProperty::class,
        "AssetObjectProperty" to UAssetObjectProperty::class,
        "ClassProperty" to UClassProperty::class,
        "AssetClassProperty" to UAssetClassProperty::class,
        "SoftClassProperty" to USoftClassProperty::class,
        "DelegateProperty" to UDelegateProperty::class,
        "MulticastDelegateProperty" to UMulticastDelegateProperty::class,
        "MulticastInlineDelegateProperty" to UMulticastInlineDelegateProperty::class,
        "MulticastSparseDelegateProperty" to UMulticastSparseDelegateProperty::class,
        "InterfaceProperty" to UInterfaceProperty::class,
        "MapProperty" to UMapProperty::class,
        "BoolProperty" to UBoolProperty::class,
        "ByteProperty" to UByteProperty::class,
        "StructProperty" to UStructProperty::class,
        "NameProperty" to UNameProperty::class,
        "StrProperty" to UStrProperty::class,
        "TextProperty" to UTextProperty::class,
        "NumericProperty" to UNumericProperty::class,
        "DoubleProperty" to UDoubleProperty::class,
        "FloatProperty" to UFloatProperty::class,
        "IntProperty" to UIntProperty::class,
    )

    init {
        InitializePropertyTypeRegistry()
    }

    /** Registers a concrete [PropertyData] subclass, keyed by the FString its [PropertyData.PropertyType] reports. */
    private fun Register(creator: (FName?) -> PropertyData) {
        val testInstance = creator(null)
        val returnedPropType = testInstance.PropertyType ?: return
        val returnedPropTypeValue = returnedPropType.Value ?: return
        if (!testInstance.ShouldBeRegistered) return

        val res = RegistryEntry()
        res.PropertyType = testInstance::class
        res.HasCustomStructSerialization = testInstance.HasCustomStructSerialization
        res.Creator = creator

        if (_propertyTypeRegistry.containsKey(returnedPropTypeValue)) {
            throw IllegalStateException(
                "Different child classes of PropertyData with the same PropertyType field exist: " +
                    "${testInstance::class.qualifiedName} and ${_propertyTypeRegistry[returnedPropTypeValue]?.PropertyType?.qualifiedName}",
            )
        }
        _propertyTypeRegistry[returnedPropTypeValue] = res
    }

    /** Initializes the property type registry. */
    private fun InitializePropertyTypeRegistry() {
        if (_propertyTypeRegistry.isNotEmpty()) return

        Register { name -> ArrayPropertyData(name) }
        Register { name -> AssetObjectPropertyData(name) }
        Register { name -> BoolPropertyData(name) }
        Register { name -> Box2DPropertyData(name) }
        Register { name -> Box2fPropertyData(name) }
        Register { name -> BoxPropertyData(name) }
        Register { name -> BytePropertyData(name) }
        Register { name -> ClothLODDataCommonPropertyData(name) }
        Register { name -> ClothLODDataPropertyData(name) }
        Register { name -> ClothTetherDataPropertyData(name) }
        Register { name -> ColorMaterialInputPropertyData(name) }
        Register { name -> ColorPropertyData(name) }
        Register { name -> DateTimePropertyData(name) }
        Register { name -> DelegatePropertyData(name) }
        Register { name -> DeprecateSlateVector2DPropertyData(name) }
        Register { name -> DoublePropertyData(name) }
        Register { name -> EnumPropertyData(name) }
        Register { name -> ExpressionInputPropertyData(name) }
        Register { name -> FieldPathPropertyData(name) }
        Register { name -> FloatPropertyData(name) }
        Register { name -> FloatRangePropertyData(name) }
        Register { name -> FontCharacterPropertyData(name) }
        Register { name -> FontDataPropertyData(name) }
        Register { name -> FrameNumberPropertyData(name) }
        Register { name -> GameplayTagContainerPropertyData(name) }
        Register { name -> GuidPropertyData(name) }
        Register { name -> InstancedStructPropertyData(name) }
        Register { name -> Int16PropertyData(name) }
        Register { name -> Int64PropertyData(name) }
        Register { name -> Int8PropertyData(name) }
        Register { name -> IntPointPropertyData(name) }
        Register { name -> IntPropertyData(name) }
        Register { name -> IntVector2PropertyData(name) }
        Register { name -> IntVectorPropertyData(name) }
        Register { name -> InterfacePropertyData(name) }
        Register { name -> KeyHandleMapPropertyData(name) }
        Register { name -> LevelSequenceObjectReferenceMapPropertyData(name) }
        Register { name -> LinearColorPropertyData(name) }
        Register { name -> MapPropertyData(name) }
        Register { name -> MaterialAttributesInputPropertyData(name) }
        Register { name -> MaterialOverrideNanitePropertyData(name) }
        Register { name -> MatrixPropertyData(name) }
        Register { name -> MovieSceneDoubleChannelPropertyData(name) }
        Register { name -> MovieSceneEvalTemplatePtrPropertyData(name) }
        Register { name -> MovieSceneEvaluationFieldEntityTreePropertyData(name) }
        Register { name -> MovieSceneEvaluationKeyPropertyData(name) }
        Register { name -> MovieSceneEventParametersPropertyData(name) }
        Register { name -> MovieSceneFloatChannelPropertyData(name) }
        Register { name -> MovieSceneFloatValuePropertyData(name) }
        Register { name -> MovieSceneFrameRangePropertyData(name) }
        Register { name -> MovieSceneGenerationLedgerPropertyData(name) }
        Register { name -> MovieSceneSegmentIdentifierPropertyData(name) }
        Register { name -> MovieSceneSegmentPropertyData(name) }
        Register { name -> MovieSceneSequenceIDPropertyData(name) }
        Register { name -> MovieSceneSequenceInstanceDataPtrPropertyData(name) }
        Register { name -> MovieSceneSubSectionFieldDataPropertyData(name) }
        Register { name -> MovieSceneSubSequenceTreePropertyData(name) }
        Register { name -> MovieSceneTemplatePropertyData(name) }
        Register { name -> MovieSceneTrackFieldDataPropertyData(name) }
        Register { name -> MovieSceneTrackIdentifierPropertyData(name) }
        Register { name -> MovieSceneTrackImplementationPtrPropertyData(name) }
        Register { name -> MulticastDelegatePropertyData(name) }
        Register { name -> MulticastInlineDelegatePropertyData(name) }
        Register { name -> MulticastSparseDelegatePropertyData(name) }
        Register { name -> NameCurveKeyPropertyData(name) }
        Register { name -> NamePropertyData(name) }
        Register { name -> NavAgentSelectorPropertyData(name) }
        Register { name -> NiagaraDataChannelVariablePropertyData(name) }
        Register { name -> NiagaraDataInterfaceGPUParamInfoPropertyData(name) }
        Register { name -> NiagaraVariableBasePropertyData(name) }
        Register { name -> NiagaraVariablePropertyData(name) }
        Register { name -> NiagaraVariableWithOffsetPropertyData(name) }
        Register { name -> ObjectPropertyData(name) }
        Register { name -> PerPlatformBoolPropertyData(name) }
        Register { name -> PerPlatformFloatPropertyData(name) }
        Register { name -> PerPlatformFrameRatePropertyData(name) }
        Register { name -> PerPlatformIntPropertyData(name) }
        Register { name -> PerQualityLevelFloatPropertyData(name) }
        Register { name -> PerQualityLevelIntPropertyData(name) }
        Register { name -> PlanePropertyData(name) }
        Register { name -> QuatPropertyData(name) }
        Register { name -> RawStructPropertyData(name) }
        Register { name -> RichCurveKeyPropertyData(name) }
        Register { name -> RotatorPropertyData(name) }
        Register { name -> ScalarMaterialInputPropertyData(name) }
        Register { name -> SectionEvaluationDataTreePropertyData(name) }
        Register { name -> SetPropertyData(name) }
        Register { name -> SkeletalMeshAreaWeightedTriangleSamplerPropertyData(name) }
        Register { name -> SkeletalMeshSamplingLODBuiltDataPropertyData(name) }
        Register { name -> SkeletalMeshSamplingRegionBuiltDataPropertyData(name) }
        Register { name -> SmartNamePropertyData(name) }
        Register { name -> SoftObjectPropertyData(name) }
        Register { name -> SoftObjectPathPropertyData(name) }
        Register { name -> SoftClassPathPropertyData(name) }
        Register { name -> SoftAssetPathPropertyData(name) }
        Register { name -> StringAssetReferencePropertyData(name) }
        Register { name -> StringClassReferencePropertyData(name) }
        Register { name -> SplinePropertyData(name) }
        Register { name -> StrPropertyData(name) }
        Register { name -> StringCurveKeyPropertyData(name) }
        Register { name -> StructPropertyData(name) }
        Register { name -> TextPropertyData(name) }
        Register { name -> TimespanPropertyData(name) }
        Register { name -> TwoVectorsPropertyData(name) }
        Register { name -> UInt16PropertyData(name) }
        Register { name -> UInt32PropertyData(name) }
        Register { name -> UInt64PropertyData(name) }
        Register { name -> UnknownPropertyData(name) }
        Register { name -> UniversalObjectLocatorFragmentPropertyData(name) }
        Register { name -> Utf8StrPropertyData(name) }
        Register { name -> Vector2DPropertyData(name) }
        Register { name -> Vector2MaterialInputPropertyData(name) }
        Register { name -> Vector2fPropertyData(name) }
        Register { name -> Vector3fPropertyData(name) }
        Register { name -> Vector4PropertyData(name) }
        Register { name -> Vector4fPropertyData(name) }
        Register { name -> VectorMaterialInputPropertyData(name) }
        Register { name -> VectorNetQuantize10PropertyData(name) }
        Register { name -> VectorNetQuantize100PropertyData(name) }
        Register { name -> VectorNetQuantizeNormalPropertyData(name) }
        Register { name -> VectorNetQuantizePropertyData(name) }
        Register { name -> VectorPropertyData(name) }
        Register { name -> ViewTargetBlendParamsPropertyData(name) }
        Register { name -> WeakObjectPropertyData(name) }
        Register { name -> WeightedRandomSamplerPropertyData(name) }
    }

    /**
     * Generates an unversioned header based on a list of properties, and sorts the list in the correct order to be serialized.
     */
    fun GenerateUnversionedHeader(data: MutableList<PropertyData>, parentName: FName?, parentModulePath: FName?, asset: UAsset): FUnversionedHeader? {
        val sortedProps = mutableListOf<PropertyData>()
        if (!asset.HasUnversionedProperties) return null
        if (asset.Mappings == null) return null

        var firstNumAll = Int.MAX_VALUE
        var lastNumAll = Int.MIN_VALUE
        val propertiesToTouch = mutableSetOf<Int>()
        val propMap = HashMap<Int, PropertyData>()
        val zeroProps = mutableSetOf<Int>()
        for (entry in data) {
            val tryGet = asset.Mappings!!.TryGetProperty(entry.Name, entry.Ancestry, entry.ArrayIndex, asset)
            if (tryGet == null) {
                throw FormatException("No valid property \"" + entry.Name.toString() + "\" in class " + entry.Ancestry.Parent.toString())
            }
            val idx = tryGet.second
            propMap[idx] = entry
            if (entry.CanBeZero(asset) && entry.IsZero) zeroProps.add(idx)

            if (idx < firstNumAll) firstNumAll = idx
            if (idx > lastNumAll) lastNumAll = idx
            propertiesToTouch.add(idx)
        }

        var lastNumBefore = -1
        val allFrags = mutableListOf<FFragment>()
        if (propertiesToTouch.size > 0) {
            while (true) {
                val fragmentHasAnyZeros = mutableSetOf<Int>()

                var firstNum = lastNumBefore + 1
                while (!propertiesToTouch.contains(firstNum) && firstNum <= lastNumAll) firstNum++
                if (firstNum > lastNumAll) break

                var lastNum = firstNum
                while (propertiesToTouch.contains(lastNum)) {
                    if (zeroProps.contains(lastNum)) {
                        val valueNum = lastNum - firstNum
                        fragmentHasAnyZeros.add(valueNum / FFragment.ValueMax)
                    }
                    sortedProps.add(propMap[lastNum]!!)

                    lastNum++
                }
                lastNum--

                val newFrag = FFragment.GetFromBounds(lastNumBefore, firstNum, lastNum, fragmentHasAnyZeros.contains(0), false)

                while (newFrag.SkipNum > FFragment.SkipMax) {
                    allFrags.add(FFragment(FFragment.SkipMax, 0, false, false))
                    newFrag.SkipNum -= FFragment.SkipMax
                }
                var fragIdx = 0
                while (newFrag.ValueNum > FFragment.ValueMax) {
                    allFrags.add(FFragment(newFrag.SkipNum, FFragment.ValueMax, false, fragmentHasAnyZeros.contains(fragIdx), firstNum + FFragment.ValueMax * fragIdx))
                    newFrag.ValueNum -= FFragment.ValueMax
                    newFrag.FirstNum += FFragment.ValueMax
                    newFrag.SkipNum = 0
                    fragIdx += 1
                    newFrag.bHasAnyZeroes = fragmentHasAnyZeros.contains(fragIdx)
                }

                allFrags.add(newFrag)
                lastNumBefore = lastNum
            }
            allFrags[allFrags.size - 1].bIsLast = true
        } else {
            // add "blank" fragment
            val highestSchema = parentName?.toString()

            // i doubt that this is true, empirically tested; need more data
            var numSkip: Int
            if (asset.ObjectVersion >= ObjectVersion.VER_UE4_CORRECT_LICENSEE_FLAG) {
                numSkip = minOf(asset.Mappings!!.GetAllProperties(highestSchema ?: "", parentModulePath?.toString(), asset).size, FFragment.SkipMax)
            } else {
                numSkip = if (asset.Mappings!!.Schemas.get(highestSchema!!)!!.Properties.isEmpty()) {
                    0
                } else {
                    minOf(asset.Mappings!!.GetAllProperties(highestSchema, parentModulePath?.toString(), asset).size, FFragment.SkipMax)
                }
            }
            allFrags.add(FFragment(numSkip, 0, true, false))
        }

        // generate zero mask
        var bHasNonZeroValues = false
        val zeroMaskList = mutableListOf<Boolean>()
        for (frag in allFrags) {
            if (frag.bHasAnyZeroes) {
                for (i in 0 until frag.ValueNum) {
                    val isZero = zeroProps.contains(frag.FirstNum + i)
                    if (!isZero) bHasNonZeroValues = true
                    zeroMaskList.add(isZero)
                }
            }
        }
        val zeroMask = BitSet()
        for (i in zeroMaskList.indices) if (zeroMaskList[i]) zeroMask.set(i)

        val res = FUnversionedHeader()
        res.Fragments = allFrags
        res.ZeroMask = zeroMask
        res.ZeroMaskLength = zeroMaskList.size
        res.bHasNonZeroValues = bHasNonZeroValues
        if (res.Fragments.isNotEmpty()) {
            res.CurrentFragment = 0
            res.UnversionedPropertyIndex = res.Fragments[0].FirstNum
        }

        data.clear()
        data.addAll(sortedProps)
        return res
    }

    /**
     * Initializes the correct PropertyData class based off of serialized name, type, etc.
     */
    fun TypeToClass(
        type: FName?,
        name: FName?,
        ancestry: AncestryInfo?,
        parentName: FName?,
        parentModulePath: FName?,
        asset: UAsset,
        reader: AssetBinaryReader? = null,
        leng: Int = 0,
        propertyTagFlags: EPropertyTagFlags = EPropertyTagFlags(EPropertyTagFlags.None),
        ArrayIndex: Int = 0,
        includeHeader: Boolean = true,
        isZero: Boolean = false,
        propertyTypeName: FPropertyTypeName? = null,
    ): PropertyData? {
        val startingOffset = if (reader != null) reader.position.toLong() else 0L

        if (type?.Value?.Value == "None") return null

        val typeVal = type?.Value?.Value
        var data: PropertyData = if (typeVal != null && PropertyTypeRegistry.containsKey(typeVal)) {
            PropertyTypeRegistry[typeVal]!!.Creator(name)
        } else {
            if (leng > 0) {
                val unknown = UnknownPropertyData(name)
                unknown.SetSerializingPropertyType(type!!.Value!!)
                unknown
            } else {
                if (reader == null) throw FormatException("Unknown property type: " + type.toString() + " (on " + name.toString() + ")")
                throw FormatException("Unknown property type: " + type.toString() + " (on " + name.toString() + " at " + reader.position + ")")
            }
        }

        data.IsZero = isZero
        data.PropertyTagFlags = propertyTagFlags
        data.Ancestry.Initialize(ancestry, parentName, parentModulePath)
        data.ArrayIndex = ArrayIndex
        data.PropertyTypeName = propertyTypeName
        if (reader != null && !isZero) {
            val posBefore = reader.position
            try {
                data.Read(reader, includeHeader, leng.toLong())
            } catch (e: Exception) {
                // if asset is unversioned, bubble the error up to make the whole export fail
                if (data is StructPropertyData && !reader.Asset!!.HasUnversionedProperties) {
                    reader.position = posBefore
                    data = RawStructPropertyData(name)
                    data.PropertyTagFlags = propertyTagFlags
                    data.Ancestry.Initialize(ancestry, parentName, parentModulePath)
                    data.ArrayIndex = ArrayIndex
                    data.PropertyTypeName = propertyTypeName
                    data.Read(reader, includeHeader, leng.toLong())
                } else {
                    throw e
                }
            }
            if (data.Offset == 0L) data.Offset = startingOffset // fallback
        } else if (reader != null && isZero) {
            data.InitializeZero(reader)
        }

        return data
    }

    /**
     * Reads a property into memory.
     */
    fun Read(reader: AssetBinaryReader, ancestry: AncestryInfo?, parentName: FName?, parentModulePath: FName?, header: FUnversionedHeader?, includeHeader: Boolean): PropertyData? {
        val asset = reader.Asset ?: throw IllegalStateException("MainSerializer.Read requires an asset")
        val startingOffset = reader.position.toLong()
        var name: FName? = null
        var type: FName? = null
        var leng = 0
        var propertyTagFlags = EPropertyTagFlags(EPropertyTagFlags.None)
        var typeName: FPropertyTypeName? = null
        var ArrayIndex = 0
        var structType: String? = null
        var isZero = false

        if (asset.HasUnversionedProperties) {
            if (asset.Mappings == null) {
                throw IllegalStateException("Unversioned asset requires mappings to be loaded")
            }

            var relevantSchema = asset.Mappings!!.GetSchemaFromName(parentName?.toString(), asset, parentModulePath?.toString())
            val h = header ?: throw IllegalStateException("MainSerializer.Read requires an unversioned header")

            while (h.UnversionedPropertyIndex > h.Fragments[h.CurrentFragment].LastNum) {
                if (h.Fragments[h.CurrentFragment].bIsLast) return null
                h.CurrentFragment++
                h.UnversionedPropertyIndex = h.Fragments[h.CurrentFragment].FirstNum
            }

            var practicingUnversionedPropertyIndex = h.UnversionedPropertyIndex
            while (practicingUnversionedPropertyIndex >= relevantSchema!!.PropCount) {
                practicingUnversionedPropertyIndex -= relevantSchema.PropCount

                val schemas = asset.Mappings!!.Schemas
                val superType = relevantSchema.SuperType
                val superTypeModulePath = relevantSchema.SuperTypeModulePath
                relevantSchema = when {
                    superType != null && superTypeModulePath != null && schemas.contains(superTypeModulePath + "." + superType) ->
                        schemas.get(superTypeModulePath + "." + superType)
                    superType != null && schemas.contains(superType) && relevantSchema.Name != superType ->
                        schemas.get(superType)
                    else -> null
                }

                if (relevantSchema == null) {
                    throw FormatException("Failed to find a valid property for schema index " + h.UnversionedPropertyIndex + " in the class " + parentName.toString())
                }
            }
            val relevantProperty = relevantSchema.Properties[practicingUnversionedPropertyIndex]!!
            h.UnversionedPropertyIndex += 1

            name = FName.DefineDummy(asset, relevantProperty.Name ?: "")
            type = FName.DefineDummy(asset, relevantProperty.PropertyData!!.Type.name)
            leng = 1 // not serialized
            ArrayIndex = relevantProperty.ArrayIndex
            if (relevantProperty.PropertyData is UsmapStructData) {
                structType = (relevantProperty.PropertyData as UsmapStructData).StructType
            }

            // check if property is zero
            if (h.Fragments[h.CurrentFragment].bHasAnyZeroes) {
                isZero = if (h.ZeroMaskIndex >= h.ZeroMaskLength) false else h.ZeroMask.get(h.ZeroMaskIndex)
                h.ZeroMaskIndex++
            }
        } else if (asset.ObjectVersionUE5 >= ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
            name = reader.ReadFName()
            if (name.Value?.Value == "None") return null

            typeName = FPropertyTypeName(reader)
            type = typeName.GetName()
            leng = reader.ReadInt32()
            propertyTagFlags = EPropertyTagFlags(reader.ReadByte())

            if (propertyTagFlags.HasFlag(EPropertyTagFlags.HasArrayIndex)) {
                ArrayIndex = reader.ReadInt32()
            }
        } else {
            name = reader.ReadFName()
            if (name.Value?.Value == "None") return null

            type = reader.ReadFName()

            leng = reader.ReadInt32()
            ArrayIndex = reader.ReadInt32()
        }

        val result = TypeToClass(type, name, ancestry, parentName, parentModulePath, asset, reader, leng, propertyTagFlags, ArrayIndex, includeHeader, isZero, typeName)!!
        if (structType != null && result is StructPropertyData) result.StructType = FName.DefineDummy(asset, structType)
        result.Offset = startingOffset
        return result
    }

    /**
     * Reads an FProperty into memory. Primarily used as a part of StructExport serialization.
     */
    fun ReadFProperty(reader: AssetBinaryReader): FProperty {
        val serializedType = reader.ReadFName()
        val requestedType = fPropertyRegistry[allNonLetters.replace(serializedType.Value?.Value ?: "", "")]
        val res = if (requestedType != null) requestedType.java.getDeclaredConstructor().newInstance() else FGenericProperty()
        res.SerializedType = serializedType
        res.Read(reader)
        return res
    }

    /**
     * Serializes an FProperty from memory.
     */
    fun WriteFProperty(prop: FProperty, writer: AssetBinaryWriter) {
        writer.Write(prop.SerializedType)
        prop.Write(writer)
    }

    /**
     * Reads a UProperty into memory. Primarily used as a part of PropertyExport serialization.
     */
    fun ReadUProperty(reader: AssetBinaryReader, serializedType: FName?): UProperty {
        return ReadUProperty(reader, uPropertyRegistry[allNonLetters.replace(serializedType?.Value?.Value ?: "", "")])
    }

    /**
     * Reads a UProperty into memory. Primarily used as a part of PropertyExport serialization.
     */
    fun ReadUProperty(reader: AssetBinaryReader, requestedType: KClass<out UProperty>?): UProperty {
        val res = if (requestedType != null) requestedType.java.getDeclaredConstructor().newInstance() else UGenericProperty()
        res.Read(reader)
        return res
    }

    /**
     * Reads a UProperty into memory. Primarily used as a part of PropertyExport serialization.
     */
    inline fun <reified T : UProperty> ReadUProperty(reader: AssetBinaryReader): T {
        @Suppress("UNCHECKED_CAST")
        return ReadUProperty(reader, T::class) as T
    }

    /**
     * Serializes a UProperty from memory.
     */
    fun WriteUProperty(prop: UProperty, writer: AssetBinaryWriter) {
        prop.Write(writer)
    }

    /**
     * Serializes a property from memory.
     */
    fun Write(property: PropertyData, writer: AssetBinaryWriter, includeHeader: Boolean): Int {
        property.Offset = writer.position.toLong()

        val asset = writer.Asset ?: throw IllegalStateException("MainSerializer.Write requires an asset")
        if (asset.HasUnversionedProperties) {
            if (!property.IsZero || !property.CanBeZero(asset)) property.Write(writer, includeHeader)
            return -1 // length is not serialized
        } else if (asset.ObjectVersionUE5 >= ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
            writer.Write(property.Name)
            if (property is UnknownPropertyData) {
                writer.Write(FName(asset, property.SerializingPropertyType))
                writer.WriteInt32(0)
            } else {
                property.PropertyTypeName!!.Write(writer)
            }

            // update flags appropriately
            if (property is BoolPropertyData) {
                if (property.Value == true) {
                    property.PropertyTagFlags = EPropertyTagFlags(property.PropertyTagFlags.value or EPropertyTagFlags.BoolTrue)
                } else {
                    property.PropertyTagFlags = EPropertyTagFlags(property.PropertyTagFlags.value and EPropertyTagFlags.BoolTrue.inv())
                }
            }

            if (property.ArrayIndex != 0) {
                property.PropertyTagFlags = EPropertyTagFlags(property.PropertyTagFlags.value or EPropertyTagFlags.HasArrayIndex)
            } else {
                property.PropertyTagFlags = EPropertyTagFlags(property.PropertyTagFlags.value and EPropertyTagFlags.HasArrayIndex.inv())
            }

            if (property.PropertyGuid != null) {
                property.PropertyTagFlags = EPropertyTagFlags(property.PropertyTagFlags.value or EPropertyTagFlags.HasPropertyGuid)
            } else {
                property.PropertyTagFlags = EPropertyTagFlags(property.PropertyTagFlags.value and EPropertyTagFlags.HasPropertyGuid.inv())
            }

            val oldLoc = writer.position
            writer.WriteInt32(0) // initial length
            writer.WriteByte(property.PropertyTagFlags.value)
            if (property.ArrayIndex != 0) writer.WriteInt32(property.ArrayIndex)
            if (property.PropertyGuid != null) writer.WriteGuid(property.PropertyGuid!!)
            val realLength = property.Write(writer, includeHeader)
            val newLoc = writer.position

            writer.position = oldLoc
            writer.WriteInt32(realLength)
            writer.position = newLoc
            return oldLoc
        } else {
            writer.Write(property.Name)
            if (property is UnknownPropertyData) {
                writer.Write(FName(asset, property.SerializingPropertyType))
            } else if (property is RawStructPropertyData) {
                writer.Write(FName(asset, FString.FromString("StructProperty")))
            } else {
                writer.Write(FName(asset, property.PropertyType))
            }
            val oldLoc = writer.position
            writer.WriteInt32(0) // initial length
            writer.WriteInt32(property.ArrayIndex)
            val realLength = property.Write(writer, includeHeader)
            val newLoc = writer.position

            writer.position = oldLoc
            writer.WriteInt32(realLength)
            writer.position = newLoc
            return oldLoc
        }
    }
}
