// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/JSON/UAssetContractResolver.cs (member ordering / OptIn filtering)
package com.github.jpabscale.uasset4j.json

import com.github.jpabscale.uasset4j.exporttypes.Export
import com.github.jpabscale.uasset4j.fieldtypes.FField
import com.github.jpabscale.uasset4j.fieldtypes.FProperty
import com.github.jpabscale.uasset4j.fieldtypes.UField
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpressionGeneric
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetPropertyPointer
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.LevelSequenceObjectReferenceMapPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.MaterialInputPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.engine.TPerPlatformPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.movies.TRangeBound
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import com.fasterxml.jackson.core.JsonGenerator
import java.lang.reflect.Method

/**
 * Reproduces Newtonsoft's member handling for the ported classes:
 *
 *  - `[JsonObject(MemberSerialization.OptIn)]` PropertyData subclasses only serialize their
 *    `[JsonProperty]` members. The modifier filters each bean to the exact member list below.
 *  - `[JsonObject(MemberSerialization.OptOut)]` Export subclasses serialize every public member;
 *    the modifier appends each export's own members after the Export base members (C# orders
 *    derived-class fields first, then base-class fields, then properties).
 *  - Newtonsoft orders fields before properties; since the Kotlin port has no field/property
 *    distinction, every class that appears in JSON has an explicit member order list.
 *  - `ShouldSerializeXxx()` methods are honored by wrapping the property writer (the C#
 *    ShouldSerialize contract).
 */
class UAssetBeanSerializerModifier : BeanSerializerModifier() {
    override fun changeProperties(
        config: SerializationConfig,
        beanDesc: BeanDescription,
        beanProperties: List<BeanPropertyWriter>,
    ): List<BeanPropertyWriter> {
        val clazz = beanDesc.beanClass
        val props = beanProperties.toMutableList()

        val order: List<String>? = when {
            PropertyData::class.java.isAssignableFrom(clazz) -> propertyDataOrder(clazz)
            Export::class.java.isAssignableFrom(clazz) -> exportOrder(clazz, props)
            FField::class.java.isAssignableFrom(clazz) -> fieldOrder(clazz)
            UField::class.java.isAssignableFrom(clazz) -> uFieldOrder(clazz)
            KismetExpression::class.java.isAssignableFrom(clazz) -> kismetExpressionOrder(clazz)
            KismetPropertyPointer::class.java.isAssignableFrom(clazz) -> listOf("Old", "New")
            else -> pocoOrder(clazz) ?: structOrders[clazz.simpleName]
        }

        if (order != null) {
            val orderIndex = order.withIndex().associate { it.value to it.index }
            props.retainAll { it.name in orderIndex }
            props.sortBy { orderIndex[it.name] ?: Int.MAX_VALUE }
        }

        for (p in props) {
            if (p.name == "Value") {
                when {
                    clazz == LevelSequenceObjectReferenceMapPropertyData::class.java ->
                        p.assignSerializer(LevelSequenceObjectReferenceMapJsonSerializer())
                    needsDynamicValueSerializer(clazz) ->
                        p.assignSerializer(DynamicScalarValueSerializer())
                }
            }
        }

        return props.map { wrapShouldSerialize(clazz, it) }
    }

    companion object {
        /** C# PropertyData `[JsonProperty]` base members, in declaration order. */
        private val propertyDataBase = listOf(
            "Name", "ArrayIndex", "PropertyGuid", "IsZero", "PropertyTagFlags",
            "PropertyTypeName", "PropertyTagExtensions", "OverrideOperation", "bExperimentalOverridableLogic",
        )

        /** C# Export public members, in declaration order (Asset/[JsonIgnore] internals excluded). */
        private val exportBase = listOf(
            "ObjectName", "OuterIndex", "ClassIndex", "SuperIndex", "TemplateIndex", "ObjectFlags",
            "SerialSize", "SerialOffset", "ScriptSerializationStartOffset", "ScriptSerializationEndOffset",
            "bForcedExport", "bNotForClient", "bNotForServer", "PackageGuid", "IsInheritedInstance",
            "PackageFlags", "bNotAlwaysLoadedForEditorGame", "bIsAsset", "GeneratePublicHash",
            "SerializationBeforeSerializationDependencies", "CreateBeforeSerializationDependencies",
            "SerializationBeforeCreateDependencies", "CreateBeforeCreateDependencies", "Extras",
        )

        private val normalExportOwn = listOf(
            "Data", "ObjectGuid", "SerializationControl", "Operation", "HasLeadingFourNullBytes",
        )

        private val exportExcluded = setOf(
            "Asset", "alreadySerialized", "FirstExportDependencyOffset",
            "SerializationBeforeSerializationDependenciesSize", "CreateBeforeSerializationDependenciesSize",
            "SerializationBeforeCreateDependenciesSize", "CreateBeforeCreateDependenciesSize",
        )

        /** Own public members per Export subclass, ordered derived-first (C# field order). */
        private val exportOwnMembers = mapOf(
            "ClassExport" to listOf(
                "FuncMap", "ClassFlags", "ClassWithin", "ClassConfigName", "Interfaces",
                "ClassGeneratedBy", "bDeprecatedForceScriptOrder", "bCooked", "ClassDefaultObject",
            ),
            "FunctionExport" to listOf("FunctionFlags"),
            "UserDefinedStructExport" to listOf("StructFlags", "StructData", "SerializationControl2", "Operation2"),
            "EnumExport" to listOf("Enum"),
            "StructExport" to listOf(
                "SuperStruct", "Children", "LoadedProperties", "ScriptBytecode",
                "ScriptBytecodeSize", "ScriptBytecodeRaw",
            ),
            "FieldExport" to listOf("Field"),
            "PropertyExport" to listOf("Property"),
            "NormalExport" to normalExportOwn,
            "RawExport" to listOf("Data"),
            "DataTableExport" to listOf("Table"),
            "CurveTableExport" to listOf("Table"),
            "StringTableExport" to listOf("Table"),
            "MetaDataExport" to listOf("ObjectMetaData", "RootMetaData"),
            "LevelExport" to listOf(
                "Owner", "Actors", "URL", "Model", "ModelComponents",
                "LevelScriptActor", "NavListStart", "NavListEnd",
            ),
            "ActorComponentExport" to listOf("UCSModifiedProperties"),
            "SceneComponentExport" to listOf(
                "bComputeBoundsOnceForGame", "bComputedBoundsOnceForGame", "bIsCooked", "Bounds",
            ),
            "AssetImportDataExport" to listOf("Json"),
        )

        /** FProperty base members in C# declaration order. */
        private val fPropertyBase = listOf(
            "ArrayDim", "ElementSize", "PropertyFlags", "RepIndex", "RepNotifyFunc",
            "BlueprintReplicationCondition", "RawValue",
        )

        /** UProperty base members in C# declaration order. */
        private val uPropertyBase = listOf(
            "ArrayDim", "ElementSize", "PropertyFlags", "RepNotifyFunc",
            "BlueprintReplicationCondition", "RawValue",
        )

        /** FField base members in C# declaration order. */
        private val fFieldBase = listOf("SerializedType", "Name", "Flags", "MetaDataMap")

        /** UField base members in C# declaration order. */
        private val uFieldBase = listOf("Next")

        /** Own members per FProperty subclass. */
        private val fPropertyOwn = mapOf(
            "FEnumProperty" to listOf("Enum", "UnderlyingProp"),
            "FArrayProperty" to listOf("Inner"),
            "FSetProperty" to listOf("ElementProp"),
            "FObjectProperty" to listOf("PropertyClass"),
            "FClassProperty" to listOf("MetaClass"),
            "FSoftClassProperty" to listOf("MetaClass"),
            "FDelegateProperty" to listOf("SignatureFunction"),
            "FInterfaceProperty" to listOf("InterfaceClass"),
            "FMapProperty" to listOf("KeyProp", "ValueProp"),
            "FBoolProperty" to listOf("FieldSize", "ByteOffset", "ByteMask", "FieldMask", "NativeBool", "Value"),
            "FByteProperty" to listOf("Enum"),
            "FStructProperty" to listOf("Struct"),
            "FOptionalProperty" to listOf("ValueProperty"),
        )

        /** Own members per UProperty subclass. */
        private val uPropertyOwn = mapOf(
            "UEnumProperty" to listOf("Enum", "UnderlyingProp"),
            "UArrayProperty" to listOf("Inner"),
            "USetProperty" to listOf("ElementProp"),
            "UObjectProperty" to listOf("PropertyClass"),
            "UClassProperty" to listOf("MetaClass"),
            "UAssetClassProperty" to listOf("MetaClass"),
            "USoftClassProperty" to listOf("MetaClass"),
            "UDelegateProperty" to listOf("SignatureFunction"),
            "UInterfaceProperty" to listOf("InterfaceClass"),
            "UMapProperty" to listOf("KeyProp", "ValueProp"),
            "UBoolProperty" to listOf("NativeBool"),
            "UByteProperty" to listOf("Enum"),
            "UStructProperty" to listOf("Struct"),
        )

        private fun hierarchyOrder(clazz: Class<*>, root: Class<*>, own: Map<String, List<String>>, base: List<String>): List<String> {
            val res = mutableListOf<String>()
            var cur: Class<*>? = clazz
            while (cur != null && root.isAssignableFrom(cur)) {
                own[cur.simpleName]?.let { res.addAll(it) }
                if (cur == root) break
                cur = cur.superclass
            }
            res.addAll(base)
            return res
        }

        fun exportOrder(clazz: Class<*>, props: List<BeanPropertyWriter>): List<String> {
            val res = hierarchyOrder(clazz, Export::class.java, exportOwnMembers, exportBase).toMutableList()
            // OptOut: any other public member is appended in declaration order.
            for (p in props) {
                if (p.name in res || p.name in exportExcluded || p.name.contains("$")) continue
                res.add(p.name)
            }
            return res
        }

        fun fieldOrder(clazz: Class<*>): List<String> {
            val own = mutableListOf<String>()
            var cur: Class<*>? = clazz
            while (cur != null && FProperty::class.java.isAssignableFrom(cur)) {
                fPropertyOwn[cur.simpleName]?.let { own.addAll(it) }
                cur = cur.superclass
            }
            return own + fPropertyBase + fFieldBase
        }

        fun uFieldOrder(clazz: Class<*>): List<String> {
            val own = mutableListOf<String>()
            var cur: Class<*>? = clazz
            while (cur != null && UField::class.java.isAssignableFrom(cur)) {
                uPropertyOwn[cur.simpleName]?.let { own.addAll(it) }
                cur = cur.superclass
            }
            return own + uPropertyBase + uFieldBase
        }

        /** Own `[JsonProperty]` members for PropertyData subclasses, derived-first (C# field order). */
        private val propertyOwn = mapOf(
            "StructPropertyData" to listOf(
                "StructType", "SerializeNone", "StructGUID", "SerializationControl", "Operation",
            ),
            "InstancedStructPropertyData" to listOf("Struct", "Version"),
            "RawStructPropertyData" to listOf("StructType", "SerializeNone", "StructGUID"),
            "SoftObjectPathPropertyData" to listOf("Path"),
            "SoftClassPathPropertyData" to listOf("Path"),
            "SoftAssetPathPropertyData" to listOf("Path"),
            "StringAssetReferencePropertyData" to listOf("Path"),
            "StringClassReferencePropertyData" to listOf("Path"),
            "BytePropertyData" to listOf("ByteType", "EnumType", "Value", "EnumValue"),
            "EnumPropertyData" to listOf("EnumType", "InnerType"),
            "MapPropertyData" to listOf("Value", "KeyType", "ValueType", "KeysToRemove"),
            "ArrayPropertyData" to listOf("ArrayType", "DummyStruct"),
            "SetPropertyData" to listOf("ArrayType", "DummyStruct"),
            "FieldPathPropertyData" to listOf("Path", "ResolvedOwner", "Value"),
            "TextPropertyData" to listOf(
                "Flags", "HistoryType", "TableId", "Namespace", "CultureInvariantString", "SourceFmt",
                "Arguments", "ArgumentsData", "TransformType", "SourceValue", "FormatOptions", "TargetCulture",
            ),
            "UnknownPropertyData" to listOf("SerializingPropertyType"),
        )

        private fun needsDynamicValueSerializer(clazz: Class<*>): Boolean =
            MaterialInputPropertyData::class.java.isAssignableFrom(clazz) ||
                TPerPlatformPropertyData::class.java.isAssignableFrom(clazz) ||
                TRangeBound::class.java.isAssignableFrom(clazz)

        fun propertyDataOrder(clazz: Class<*>): List<String> {
            val own = propertyOwn[clazz.simpleName]
            if (own != null) {
                return if (own.contains("Value")) {
                    own + propertyDataBase
                } else {
                    own + propertyDataBase + "Value"
                }
            }
            if (StructPropertyData::class.java.isAssignableFrom(clazz)) {
                return propertyOwn.getValue("StructPropertyData") + propertyDataBase + "Value"
            }
            return propertyDataBase + "Value"
        }

        /**
         * Own `[JsonProperty]` members for the Kismet bytecode hierarchy, in C# declaration order.
         * The generic const `Value` property is appended after the derived fields, mirroring how
         * Newtonsoft orders the OptIn members (fields before the base class property).
         */
        private val kismetOwn = mapOf(
            "EX_AddMulticastDelegate" to listOf("Delegate", "DelegateToAdd"),
            "EX_ArrayConst" to listOf("InnerProperty", "Elements", "Value"),
            "EX_ArrayGetByRef" to listOf("ArrayVariable", "ArrayIndex"),
            "EX_Assert" to listOf("LineNumber", "DebugMode", "AssertExpression"),
            "EX_BindDelegate" to listOf("FunctionName", "Delegate", "ObjectTerm"),
            "EX_BitFieldConst" to listOf("Property", "Value"),
            "EX_ByteConst" to listOf("Value"),
            "EX_CallMulticastDelegate" to listOf("Delegate"),
            "EX_CastBase" to listOf("ClassPtr", "Target"),
            "EX_ClearMulticastDelegate" to listOf("DelegateToClear"),
            "EX_ComputedJump" to listOf("CodeOffsetExpression"),
            "EX_Context" to listOf(
                "ObjectExpression", "Offset", "PropertyType", "RValuePointer", "ContextExpression",
            ),
            "EX_DoubleConst" to listOf("Value"),
            "EX_FinalFunction" to listOf("StackNode", "Parameters"),
            "EX_FloatConst" to listOf("Value"),
            "EX_InstanceDelegate" to listOf("FunctionName"),
            "EX_InstrumentationEvent" to listOf("EventType", "EventName"),
            "EX_Int64Const" to listOf("Value"),
            "EX_IntConst" to listOf("Value"),
            "EX_IntConstByte" to listOf("Value"),
            "EX_InterfaceContext" to listOf("InterfaceValue"),
            "EX_Jump" to listOf("CodeOffset"),
            "EX_JumpIfNot" to listOf("CodeOffset", "BooleanExpression"),
            "EX_Let" to listOf("Value", "Variable", "Expression"),
            "EX_LetBase" to listOf("VariableExpression", "AssignmentExpression"),
            "EX_LetValueOnPersistentFrame" to listOf("DestinationProperty", "AssignmentExpression"),
            "EX_MapConst" to listOf("KeyProperty", "ValueProperty", "Elements"),
            "EX_PopExecutionFlowIfNot" to listOf("BooleanExpression"),
            "EX_PrimitiveCast" to listOf("ConversionType", "Target"),
            "EX_PropertyConst" to listOf("Property"),
            "EX_PushExecutionFlow" to listOf("PushingAddress"),
            "EX_RemoveMulticastDelegate" to listOf("Delegate", "DelegateToAdd"),
            "EX_Return" to listOf("ReturnExpression"),
            "EX_RotationConst" to listOf("Value"),
            "EX_SetArray" to listOf("AssigningProperty", "ArrayInnerProp", "Elements"),
            "EX_SetConst" to listOf("InnerProperty", "Elements"),
            "EX_SetMap" to listOf("MapProperty", "Elements"),
            "EX_SetSet" to listOf("SetProperty", "Elements"),
            "EX_Skip" to listOf("CodeOffset", "SkipExpression"),
            "EX_StructConst" to listOf("Struct", "StructSize"),
            "EX_StructMemberContext" to listOf("StructMemberExpression", "StructExpression"),
            "EX_SwitchValue" to listOf(
                "CaseIndexValueTerm", "NextOffset", "CaseTerm", "EndGotoOffset",
                "IndexTerm", "DefaultTerm", "Cases",
            ),
            "EX_VariableBase" to listOf("Variable"),
            "EX_Vector3fConst" to listOf("X", "Y", "Z"),
            "EX_VectorConst" to listOf("Value"),
            "EX_VirtualFunction" to listOf("VirtualFunctionName", "Parameters"),
        )

        fun kismetExpressionOrder(clazz: Class<*>): List<String> {
            val res = mutableListOf<String>()
            var cur: Class<*>? = clazz
            while (cur != null && KismetExpression::class.java.isAssignableFrom(cur)) {
                kismetOwn[cur.simpleName]?.let { res.addAll(it) }
                if (cur == KismetExpression::class.java) break
                cur = cur.superclass
            }
            if (KismetExpressionGeneric::class.java.isAssignableFrom(clazz)) res.add("Value")
            return res
        }

        /** Declaration order of the standalone POCOs (they have no opt-in/opt-out filtering). */
        private val pocoOrders = mapOf(
            "CustomVersion" to listOf("Name", "Key", "FriendlyName", "Version", "IsSerialized"),
            "Import" to listOf("ObjectName", "OuterIndex", "ClassPackage", "ClassName", "PackageName", "bImportOptional"),
            "FGenerationInfo" to listOf("ExportCount", "NameCount"),
            "FEngineVersion" to listOf("Major", "Minor", "Patch", "Changelist", "Branch"),
            "UDataTable" to listOf("Data"),
        )

        /** Struct value classes serialized as beans: member order in C# declaration order. */
        private val structOrders = mapOf(
            "FVector" to listOf("X", "Y", "Z"),
            "FVector2D" to listOf("X", "Y"),
            "FVector4" to listOf("X", "Y", "Z", "W"),
            "FQuat" to listOf("X", "Y", "Z", "W"),
            "FPlane" to listOf("X", "Y", "Z", "W"),
            "FRotator" to listOf("Pitch", "Yaw", "Roll"),
            "FLinearColor" to listOf("R", "G", "B", "A"),
            "FIntVector" to listOf("X", "Y", "Z"),
            "FIntVector2" to listOf("X", "Y"),
            "FMatrix" to listOf("XPlane", "YPlane", "ZPlane", "WPlane"),
            "FTwoVectors" to listOf("V1", "V2"),
            "FVector2f" to listOf("X", "Y"),
            "FVector3f" to listOf("X", "Y", "Z"),
            "FVector4f" to listOf("X", "Y", "Z", "W"),
            "FRichCurveKey" to listOf(
                "InterpMode", "TangentMode", "TangentWeightMode", "Time", "Value",
                "ArriveTangent", "ArriveTangentWeight", "LeaveTangent", "LeaveTangentWeight",
            ),
            "FNavAgentSelector" to listOf("PackedBits"),
            "FSkeletalMeshSamplingRegionBuiltData" to listOf("TriangleIndices", "Vertices", "BoneIndices"),
            "FStringCurveKey" to listOf("Time", "Value"),
            "FWeightedRandomSampler" to listOf("Prob", "Alias", "TotalWeight"),
            "FSoftObjectPath" to listOf("AssetPath", "SubPathString"),
            "FTopLevelAssetPath" to listOf("PackageName", "AssetName"),
            "FPropertyTypeNameNode" to listOf("Name", "InnerCount"),
            "FObjectDataResource" to listOf(
                "Flags", "CookedIndex", "SerialOffset", "DuplicateSerialOffset",
                "SerialSize", "RawSize", "OuterIndex", "LegacyBulkDataFlags",
            ),
            "FScriptText" to listOf(
                "TextLiteralType", "LocalizedSource", "LocalizedKey", "LocalizedNamespace",
                "InvariantLiteralString", "LiteralString", "StringTableAsset", "StringTableId",
                "StringTableKey",
            ),
            "FUniqueNetId" to listOf("Type", "Contents"),
            "FMovieSceneEvaluationKey" to listOf("SequenceID", "TrackIdentifier", "SectionIndex"),
            "SerializedInterfaceReference" to listOf("Class", "PointerOffset", "bImplementedByK2"),
            "FKismetSwitchCase" to listOf("CaseIndexValueTerm", "NextOffset", "CaseTerm"),
            "FDelegate" to listOf("Object", "Delegate"),
        )

        fun pocoOrder(clazz: Class<*>): List<String>? = pocoOrders[clazz.simpleName]

        private val shouldSerializeCache =
            java.util.concurrent.ConcurrentHashMap<Pair<Class<*>, String>, Method>()

        private fun shouldSerializeMethod(clazz: Class<*>, propName: String): Method? {
            val key = clazz to propName
            shouldSerializeCache[key]?.let { return it }
            val method = try {
                clazz.getMethod("ShouldSerialize$propName")
            } catch (_: NoSuchMethodException) {
                null
            }
            if (method != null) shouldSerializeCache[key] = method
            return method
        }

        private fun wrapShouldSerialize(clazz: Class<*>, prop: BeanPropertyWriter): BeanPropertyWriter {
            val method = shouldSerializeMethod(clazz, prop.name) ?: return prop
            val accessor = method
            return object : BeanPropertyWriter(prop) {
                override fun serializeAsField(bean: Any, gen: JsonGenerator, prov: SerializerProvider) {
                    if (accessor.invoke(bean) == true) {
                        super.serializeAsField(bean, gen, prov)
                    }
                }
            }
        }
    }
}
