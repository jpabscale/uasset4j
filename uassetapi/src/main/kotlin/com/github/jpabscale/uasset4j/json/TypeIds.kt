// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/JSON/UAssetContractResolver.cs (type-name resolution portion)
package com.github.jpabscale.uasset4j.json

import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.FAssetRegistryRecord
import com.github.jpabscale.uasset4j.FEngineVersion
import com.github.jpabscale.uasset4j.FGenerationInfo
import com.github.jpabscale.uasset4j.FImportTypeHierarchy
import com.github.jpabscale.uasset4j.Import
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.unrealtypes.FGatherableTextData
import com.github.jpabscale.uasset4j.unrealtypes.FMetaData
import com.github.jpabscale.uasset4j.unrealtypes.FWorldTileInfo
import com.github.jpabscale.uasset4j.exporttypes.ActorComponentExport
import com.github.jpabscale.uasset4j.exporttypes.AssetImportDataExport
import com.github.jpabscale.uasset4j.exporttypes.ClassExport
import com.github.jpabscale.uasset4j.exporttypes.CurveTableExport
import com.github.jpabscale.uasset4j.exporttypes.DataTableExport
import com.github.jpabscale.uasset4j.exporttypes.EnumExport
import com.github.jpabscale.uasset4j.exporttypes.Export
import com.github.jpabscale.uasset4j.exporttypes.FieldExport
import com.github.jpabscale.uasset4j.exporttypes.FunctionExport
import com.github.jpabscale.uasset4j.exporttypes.LevelExport
import com.github.jpabscale.uasset4j.exporttypes.MetaDataExport
import com.github.jpabscale.uasset4j.exporttypes.NormalExport
import com.github.jpabscale.uasset4j.exporttypes.PropertyExport
import com.github.jpabscale.uasset4j.exporttypes.RawExport
import com.github.jpabscale.uasset4j.exporttypes.SceneComponentExport
import com.github.jpabscale.uasset4j.exporttypes.SerializedInterfaceReference
import com.github.jpabscale.uasset4j.exporttypes.StringTableExport
import com.github.jpabscale.uasset4j.exporttypes.StructExport
import com.github.jpabscale.uasset4j.exporttypes.UDataTable
import com.github.jpabscale.uasset4j.curves.UCurveTable
import com.github.jpabscale.uasset4j.exporttypes.UserDefinedStructExport
import com.github.jpabscale.uasset4j.propertytypes.objects.ArrayPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.AssetObjectPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.BoolPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.BytePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.DelegatePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.DoublePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.EnumPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.FDelegate
import com.github.jpabscale.uasset4j.propertytypes.objects.FieldPathPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.FloatPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.Int16PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.Int64PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.Int8PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.InterfacePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.IntPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.MapPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.MulticastDelegatePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.MulticastInlineDelegatePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.MulticastSparseDelegatePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.NamePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.ObjectPropertyData
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
import com.github.jpabscale.uasset4j.propertytypes.structs.ClothLODDataPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.ClothLODDataCommonPropertyData
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
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.FontCharacterPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.ColorMaterialInputPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.ExpressionInputPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.KeyHandleMapPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.MaterialAttributesInputPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.MaterialInputPropertyData
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
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.UniqueNetIdReplPropertyData
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
import com.github.jpabscale.uasset4j.propertytypes.structs.math.Vector4fPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.Vector4PropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.VectorPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.VectorNetQuantize100PropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.VectorNetQuantize10PropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.VectorNetQuantizeNormalPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.VectorNetQuantizePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.FMovieSceneEventParameters
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
import com.github.jpabscale.uasset4j.propertytypes.structs.niagara.NiagaraDataInterfaceGPUParamInfoPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.niagara.NiagaraDataChannelVariablePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.niagara.NiagaraVariableBasePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.niagara.NiagaraVariablePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.niagara.NiagaraVariableWithOffsetPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.ranges.FloatRangePropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.slate.DeprecateSlateVector2DPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.slate.FontDataPropertyData
import com.github.jpabscale.uasset4j.kismet.bytecode.FScriptText
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpressionGeneric
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetPropertyPointer
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_AddMulticastDelegate
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ArrayConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ArrayGetByRef
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Assert
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_BindDelegate
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_BitFieldConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Breakpoint
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ByteConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_CallMath
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_CallMulticastDelegate
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_CastBase
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ClassContext
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ClassSparseDataVariable
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ClearMulticastDelegate
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ComputedJump
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Context
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Context_FailSilent
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_CrossInterfaceCast
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_DefaultVariable
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_DeprecatedOp4A
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_DoubleConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_DynamicCast
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_EndArray
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_EndArrayConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_EndFunctionParms
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_EndMap
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_EndMapConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_EndOfScript
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_EndParmValue
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_EndSet
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_EndSetConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_EndStructConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_False
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_FieldPathConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_FinalFunction
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_FloatConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_InstanceDelegate
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_InstanceVariable
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_InstrumentationEvent
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Int64Const
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_IntConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_IntConstByte
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_InterfaceContext
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_InterfaceToObjCast
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_IntOne
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_IntZero
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Jump
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_JumpIfNot
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Let
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_LetBase
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_LetBool
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_LetDelegate
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_LetMulticastDelegate
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_LetObj
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_LetValueOnPersistentFrame
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_LetWeakObjPtr
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_LocalFinalFunction
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_LocalOutVariable
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_LocalVariable
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_LocalVirtualFunction
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_MapConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_MetaCast
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_NameConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_NoInterface
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_NoObject
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Nothing
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_NothingInt32
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ObjectConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ObjToInterfaceCast
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_PopExecutionFlow
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_PopExecutionFlowIfNot
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_PrimitiveCast
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_PropertyConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_PushExecutionFlow
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_RemoveMulticastDelegate
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Return
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_RotationConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Self
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_SetArray
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_SetConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_SetMap
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_SetSet
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Skip
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_SkipOffsetConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_SoftObjectConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_StringConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_StructConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_StructMemberContext
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_SwitchValue
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.FKismetSwitchCase
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_TextConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Tracepoint
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_TransformConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_True
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_UInt64Const
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_UnicodeStringConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_VariableBase
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Vector3fConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_VectorConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_VirtualFunction
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_WireTracepoint
import com.github.jpabscale.uasset4j.propertytypes.objects.FFieldPath
import com.github.jpabscale.uasset4j.unrealtypes.FObjectThumbnail

/**
 * Maps every JSON-serializable Kotlin class to the C# type string that Newtonsoft's
 * TypeNameHandling.Objects emits ("<Namespace>.<Class>, UAssetAPI") and back.
 *
 * The C# namespaces are flat: all of PropertyTypes/Structs/ subfolders collapse to
 * `UAssetAPI.PropertyTypes.Structs`, and all of UnrealTypes/Objects/ to
 * `UAssetAPI.PropertyTypes.Objects`.
 */
object UAssetTypeIds {
    private fun KClassId(kclass: kotlin.reflect.KClass<*>): String {
        val name = kclass.simpleName ?: return ""
        val fqn = kclass.java.name
        return when {
            fqn.startsWith("com.github.jpabscale.uasset4j.propertytypes.objects.") -> "UAssetAPI.PropertyTypes.Objects.$name, UAssetAPI"
            fqn.startsWith("com.github.jpabscale.uasset4j.propertytypes.structs.") -> "UAssetAPI.PropertyTypes.Structs.$name, UAssetAPI"
            fqn.startsWith("com.github.jpabscale.uasset4j.exporttypes.") -> "UAssetAPI.ExportTypes.$name, UAssetAPI"
            fqn.startsWith("com.github.jpabscale.uasset4j.fieldtypes.") -> "UAssetAPI.FieldTypes.$name, UAssetAPI"
            fqn.startsWith("com.github.jpabscale.uasset4j.kismet.bytecode.expressions.") -> "UAssetAPI.Kismet.Bytecode.Expressions.$name, UAssetAPI"
            fqn.startsWith("com.github.jpabscale.uasset4j.kismet.bytecode.") -> "UAssetAPI.Kismet.Bytecode.$name, UAssetAPI"
            fqn.startsWith("com.github.jpabscale.uasset4j.unrealtypes.") -> "UAssetAPI.UnrealTypes.$name, UAssetAPI"
            fqn.startsWith("com.github.jpabscale.uasset4j.") -> "UAssetAPI.$name, UAssetAPI"
            else -> ""
        }
    }

    private val propertyClasses = listOf(
        ArrayPropertyData::class, AssetObjectPropertyData::class, BoolPropertyData::class,
        BytePropertyData::class, DelegatePropertyData::class, DoublePropertyData::class,
        EnumPropertyData::class, FieldPathPropertyData::class, FloatPropertyData::class,
        Int16PropertyData::class, Int64PropertyData::class, Int8PropertyData::class,
        InterfacePropertyData::class, IntPropertyData::class, MapPropertyData::class,
        MulticastDelegatePropertyData::class, MulticastInlineDelegatePropertyData::class,
        MulticastSparseDelegatePropertyData::class, NamePropertyData::class,
        ObjectPropertyData::class, SetPropertyData::class, SoftObjectPropertyData::class,
        StrPropertyData::class, TextPropertyData::class, UInt16PropertyData::class,
        UInt32PropertyData::class, UInt64PropertyData::class, UnknownPropertyData::class,
        Utf8StrPropertyData::class, WeakObjectPropertyData::class,
        StructPropertyData::class, RawStructPropertyData::class, ClothLODDataPropertyData::class,
        ClothLODDataCommonPropertyData::class, ClothTetherDataPropertyData::class,
        GameplayTagContainerPropertyData::class, LevelSequenceObjectReferenceMapPropertyData::class,
        SoftObjectPathPropertyData::class, SoftClassPathPropertyData::class,
        SoftAssetPathPropertyData::class, StringAssetReferencePropertyData::class,
        StringClassReferencePropertyData::class,
        ColorPropertyData::class, DateTimePropertyData::class, GuidPropertyData::class,
        InstancedStructPropertyData::class, TimespanPropertyData::class,
        UniqueNetIdReplPropertyData::class,
        FontCharacterPropertyData::class, ColorMaterialInputPropertyData::class,
        ExpressionInputPropertyData::class, FontDataPropertyData::class,
        KeyHandleMapPropertyData::class, MaterialAttributesInputPropertyData::class,
        MaterialInputPropertyData::class, MaterialOverrideNanitePropertyData::class,
        NavAgentSelectorPropertyData::class, ScalarMaterialInputPropertyData::class,
        SkeletalMeshAreaWeightedTriangleSamplerPropertyData::class,
        SkeletalMeshSamplingLODBuiltDataPropertyData::class,
        SkeletalMeshSamplingRegionBuiltDataPropertyData::class,
        SmartNamePropertyData::class, StringCurveKeyPropertyData::class,
        Vector2MaterialInputPropertyData::class, VectorMaterialInputPropertyData::class,
        ViewTargetBlendParamsPropertyData::class, WeightedRandomSamplerPropertyData::class,
        PerPlatformBoolPropertyData::class,
        PerPlatformIntPropertyData::class,
        PerPlatformFloatPropertyData::class,
        PerPlatformFrameRatePropertyData::class,
        PerQualityLevelIntPropertyData::class,
        PerQualityLevelFloatPropertyData::class,
        Box2DPropertyData::class, Box2fPropertyData::class, BoxPropertyData::class,
        IntPointPropertyData::class, IntVector2PropertyData::class, IntVectorPropertyData::class,
        LinearColorPropertyData::class, MatrixPropertyData::class, PlanePropertyData::class,
        QuatPropertyData::class, RotatorPropertyData::class, SplinePropertyData::class,
        TwoVectorsPropertyData::class, Vector2DPropertyData::class, Vector2fPropertyData::class,
        Vector3fPropertyData::class, Vector4fPropertyData::class, Vector4PropertyData::class,
        VectorPropertyData::class, VectorNetQuantize100PropertyData::class,
        VectorNetQuantize10PropertyData::class, VectorNetQuantizeNormalPropertyData::class,
        VectorNetQuantizePropertyData::class,
        FrameNumberPropertyData::class, MovieSceneDoubleChannelPropertyData::class,
        MovieSceneEvalTemplatePtrPropertyData::class,
        MovieSceneEvaluationFieldEntityTreePropertyData::class,
        MovieSceneEvaluationKeyPropertyData::class, MovieSceneEventParametersPropertyData::class,
        MovieSceneFloatChannelPropertyData::class, MovieSceneFloatValuePropertyData::class,
        MovieSceneFrameRangePropertyData::class, MovieSceneGenerationLedgerPropertyData::class,
        MovieSceneSegmentIdentifierPropertyData::class, MovieSceneSegmentPropertyData::class,
        MovieSceneSequenceIDPropertyData::class,
        MovieSceneSequenceInstanceDataPtrPropertyData::class,
        MovieSceneSubSectionFieldDataPropertyData::class, MovieSceneSubSequenceTreePropertyData::class,
        MovieSceneTemplatePropertyData::class, MovieSceneTrackFieldDataPropertyData::class,
        MovieSceneTrackIdentifierPropertyData::class, MovieSceneTrackImplementationPtrPropertyData::class,
        NameCurveKeyPropertyData::class, SectionEvaluationDataTreePropertyData::class,
        NiagaraDataChannelVariablePropertyData::class,
        NiagaraDataInterfaceGPUParamInfoPropertyData::class, NiagaraVariableBasePropertyData::class,
        NiagaraVariablePropertyData::class, NiagaraVariableWithOffsetPropertyData::class,
        FloatRangePropertyData::class, RichCurveKeyPropertyData::class,
        DeprecateSlateVector2DPropertyData::class,
    )

    private val exportClasses = listOf(
        Export::class, NormalExport::class, RawExport::class, DataTableExport::class,
        CurveTableExport::class, ClassExport::class, EnumExport::class, FunctionExport::class,
        StructExport::class, FieldExport::class, PropertyExport::class, MetaDataExport::class,
        LevelExport::class, ActorComponentExport::class, SceneComponentExport::class,
        AssetImportDataExport::class, StringTableExport::class, UserDefinedStructExport::class,
        SerializedInterfaceReference::class,
        UDataTable::class, UCurveTable::class,
    )

    private val fieldClasses = listOf(
        com.github.jpabscale.uasset4j.fieldtypes.UField::class,
        com.github.jpabscale.uasset4j.fieldtypes.UProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UEnumProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UArrayProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.USetProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UObjectProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UWeakObjectProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.USoftObjectProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.ULazyObjectProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UAssetObjectProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UClassProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UAssetClassProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.USoftClassProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UDelegateProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UMulticastDelegateProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UMulticastInlineDelegateProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UMulticastSparseDelegateProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UInterfaceProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UMapProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UBoolProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UByteProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UStructProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UNameProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UStrProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UTextProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UNumericProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UDoubleProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UFloatProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UIntProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.UInt8Property::class,
        com.github.jpabscale.uasset4j.fieldtypes.UInt16Property::class,
        com.github.jpabscale.uasset4j.fieldtypes.UInt64Property::class,
        com.github.jpabscale.uasset4j.fieldtypes.UUInt16Property::class,
        com.github.jpabscale.uasset4j.fieldtypes.UUInt32Property::class,
        com.github.jpabscale.uasset4j.fieldtypes.UUInt64Property::class,
        com.github.jpabscale.uasset4j.fieldtypes.UGenericProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.FField::class,
        com.github.jpabscale.uasset4j.fieldtypes.FProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.FEnumProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.FArrayProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.FSetProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.FObjectProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.FSoftObjectProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.FWeakObjectProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.FClassProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.FSoftClassProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.FDelegateProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.FMulticastDelegateProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.FMulticastInlineDelegateProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.FInterfaceProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.FMapProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.FBoolProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.FByteProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.FStructProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.FNumericProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.FGenericProperty::class,
        com.github.jpabscale.uasset4j.fieldtypes.FOptionalProperty::class,
    )

    private val structValueClasses = listOf(
        com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector::class,
        com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector2D::class,
        com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector2f::class,
        com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector3f::class,
        com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector4::class,
        com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector4f::class,
        com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FIntVector::class,
        com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FIntVector2::class,
        com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FLinearColor::class,
        com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FPlane::class,
        com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FQuat::class,
        com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FRotator::class,
        com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FMatrix::class,
        com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FTwoVectors::class,
        com.github.jpabscale.uasset4j.unrealtypes.objects.engine.FRichCurveKey::class,
        com.github.jpabscale.uasset4j.unrealtypes.objects.engine.FUniqueNetId::class,
        com.github.jpabscale.uasset4j.propertytypes.structs.engine.FNavAgentSelector::class,
        com.github.jpabscale.uasset4j.unrealtypes.objects.engine.FSkeletalMeshSamplingRegionBuiltData::class,
        com.github.jpabscale.uasset4j.propertytypes.structs.engine.FStringCurveKey::class,
        com.github.jpabscale.uasset4j.unrealtypes.objects.engine.FWeightedRandomSampler::class,
        com.github.jpabscale.uasset4j.propertytypes.objects.FSoftObjectPath::class,
        com.github.jpabscale.uasset4j.propertytypes.objects.FTopLevelAssetPath::class,
        com.github.jpabscale.uasset4j.unrealtypes.FPropertyTypeNameNode::class,
        com.github.jpabscale.uasset4j.unrealtypes.FObjectDataResource::class,
        com.github.jpabscale.uasset4j.propertytypes.objects.FFieldPath::class,
        FMovieSceneEventParameters::class,
        com.github.jpabscale.uasset4j.propertytypes.structs.movies.FMovieSceneEvaluationKey::class,
        com.github.jpabscale.uasset4j.propertytypes.structs.movies.FFrameNumber::class,
        com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRange::class,
        com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRangeBound::class,
        com.github.jpabscale.uasset4j.propertytypes.structs.movies.FMovieSceneSegment::class,
        com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.TBox::class,
    )

    private val pocoClasses = listOf(
        UAsset::class, CustomVersion::class, Import::class, FGenerationInfo::class,
        FAssetRegistryRecord::class, FImportTypeHierarchy::class, FGatherableTextData::class,
        FMetaData::class, FWorldTileInfo::class, FObjectThumbnail::class, FEngineVersion::class,
        FDelegate::class,
    )

    private val kismetClasses = listOf(
        KismetExpression::class, KismetExpressionGeneric::class, KismetPropertyPointer::class, FScriptText::class,
        EX_AddMulticastDelegate::class, EX_ArrayConst::class, EX_ArrayGetByRef::class, EX_Assert::class,
        EX_BindDelegate::class, EX_BitFieldConst::class, EX_Breakpoint::class, EX_ByteConst::class,
        EX_CallMath::class, EX_CallMulticastDelegate::class, EX_CastBase::class, EX_ClassContext::class,
        EX_ClassSparseDataVariable::class, EX_ClearMulticastDelegate::class, EX_ComputedJump::class,
        EX_Context::class, EX_Context_FailSilent::class, EX_CrossInterfaceCast::class, EX_DefaultVariable::class,
        EX_DeprecatedOp4A::class, EX_DoubleConst::class, EX_DynamicCast::class, EX_EndArray::class,
        EX_EndArrayConst::class, EX_EndFunctionParms::class, EX_EndMap::class, EX_EndMapConst::class,
        EX_EndOfScript::class, EX_EndParmValue::class, EX_EndSet::class, EX_EndSetConst::class,
        EX_EndStructConst::class, EX_False::class, EX_FieldPathConst::class, EX_FinalFunction::class,
        EX_FloatConst::class, EX_InstanceDelegate::class, EX_InstanceVariable::class, EX_InstrumentationEvent::class,
        EX_Int64Const::class, EX_IntConst::class, EX_IntConstByte::class, EX_InterfaceContext::class,
        EX_InterfaceToObjCast::class, EX_IntOne::class, EX_IntZero::class, EX_Jump::class, EX_JumpIfNot::class,
        EX_Let::class, EX_LetBase::class, EX_LetBool::class, EX_LetDelegate::class, EX_LetMulticastDelegate::class,
        EX_LetObj::class, EX_LetValueOnPersistentFrame::class, EX_LetWeakObjPtr::class, EX_LocalFinalFunction::class,
        EX_LocalOutVariable::class, EX_LocalVariable::class, EX_LocalVirtualFunction::class, EX_MapConst::class,
        EX_MetaCast::class, EX_NameConst::class, EX_NoInterface::class, EX_NoObject::class, EX_Nothing::class,
        EX_NothingInt32::class, EX_ObjectConst::class, EX_ObjToInterfaceCast::class, EX_PopExecutionFlow::class,
        EX_PopExecutionFlowIfNot::class, EX_PrimitiveCast::class, EX_PropertyConst::class, EX_PushExecutionFlow::class,
        EX_RemoveMulticastDelegate::class, EX_Return::class, EX_RotationConst::class, EX_Self::class,
        EX_SetArray::class, EX_SetConst::class, EX_SetMap::class, EX_SetSet::class, EX_Skip::class,
        EX_SkipOffsetConst::class, EX_SoftObjectConst::class, EX_StringConst::class, EX_StructConst::class,
        EX_StructMemberContext::class, EX_SwitchValue::class, EX_TextConst::class, EX_Tracepoint::class,
        EX_TransformConst::class, EX_True::class, EX_UInt64Const::class, EX_UnicodeStringConst::class,
        FKismetSwitchCase::class,
        EX_VariableBase::class, EX_Vector3fConst::class, EX_VectorConst::class, EX_VirtualFunction::class,
        EX_WireTracepoint::class,
    )

    /** C# types whose namespace differs from the Kotlin package layout. */
    private val explicitIds = mapOf(
        FEngineVersion::class.java to "UAssetAPI.FEngineVersion, UAssetAPI",
        UDataTable::class.java to "UAssetAPI.ExportTypes.UDataTable, UAssetAPI",
        UCurveTable::class.java to "UAssetAPI.ExportTypes.UCurveTable, UAssetAPI",
        FFieldPath::class.java to "UAssetAPI.UnrealTypes.FFieldPath, UAssetAPI",
        UniqueNetIdReplPropertyData::class.java to "UAssetAPI.UnrealTypes.UniqueNetIdReplPropertyData, UAssetAPI",
    )

    private val ids = linkedMapOf<Class<*>, String>()

    init {
        val all = propertyClasses + exportClasses + fieldClasses + structValueClasses + pocoClasses + kismetClasses
        for (c in all) {
            val id = explicitIds[c.java] ?: KClassId(c)
            if (id.isNotEmpty()) ids[c.java] = id
        }
    }

    /** Returns the C# type string for [klass], or null if unmapped. */
    fun idFor(klass: Class<*>): String? {
        ids[klass]?.let { return it }
        return KClassId(klass.kotlin).takeIf { it.isNotEmpty() }
    }

    private val idToClass: Map<String, Class<*>> by lazy {
        ids.entries.associate { it.value to it.key }
    }

    /** Resolves a C# type string from JSON back to a JVM class. */
    fun classFor(id: String): Class<*>? {
        idToClass[id]?.let { return it }
        val simple = id.substringBefore(",").substringAfterLast(".")
        val matches = ids.entries.filter { it.key.simpleName == simple }
        return if (matches.size == 1) matches[0].key else null
    }
}

/** Jackson [TypeIdResolver] that emits/accepts the C# assembly-qualified-style `$type` strings. */
class UAssetTypeIdResolver : com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase() {
    override fun idFromValue(value: Any): String =
        UAssetTypeIds.idFor(value.javaClass) ?: value.javaClass.name

    override fun idFromValueAndType(value: Any, suggestedType: Class<*>): String =
        UAssetTypeIds.idFor(suggestedType) ?: UAssetTypeIds.idFor(value.javaClass) ?: suggestedType.name

    override fun typeFromId(ctxt: com.fasterxml.jackson.databind.DatabindContext, id: String): com.fasterxml.jackson.databind.JavaType {
        val resolved = UAssetTypeIds.classFor(id)
            ?: throw IllegalArgumentException("Unknown \$type \"$id\"")
        return ctxt.constructType(resolved)
    }

    override fun getMechanism(): com.fasterxml.jackson.annotation.JsonTypeInfo.Id = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CUSTOM
}
