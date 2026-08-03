// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/KismetSerializer.cs
package com.github.jpabscale.uasset4j.kismet

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.customversions.FReleaseObjectVersion
import com.github.jpabscale.uasset4j.exporttypes.ClassExport
import com.github.jpabscale.uasset4j.exporttypes.StructExport
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
import com.github.jpabscale.uasset4j.fieldtypes.FObjectProperty
import com.github.jpabscale.uasset4j.fieldtypes.FProperty
import com.github.jpabscale.uasset4j.fieldtypes.FSetProperty
import com.github.jpabscale.uasset4j.fieldtypes.FSoftClassProperty
import com.github.jpabscale.uasset4j.fieldtypes.FSoftObjectProperty
import com.github.jpabscale.uasset4j.fieldtypes.FStructProperty
import com.github.jpabscale.uasset4j.kismet.bytecode.EBlueprintTextLiteralType
import com.github.jpabscale.uasset4j.kismet.bytecode.ECastToken
import com.github.jpabscale.uasset4j.kismet.bytecode.EScriptInstrumentationType
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetPropertyPointer
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_AddMulticastDelegate
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ArrayConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ArrayGetByRef
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Assert
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_BindDelegate
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Breakpoint
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ByteConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_CallMath
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_CallMulticastDelegate
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ClassContext
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ClearMulticastDelegate
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ComputedJump
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Context
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Context_FailSilent
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_CrossInterfaceCast
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_DefaultVariable
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_DeprecatedOp4A
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_DoubleConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_DynamicCast
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_EndOfScript
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
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ObjectConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_ObjToInterfaceCast
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_PopExecutionFlow
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_PopExecutionFlowIfNot
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_PrimitiveCast
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_PushExecutionFlow
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_RemoveMulticastDelegate
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Return
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_RotationConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Self
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_SetArray
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_SetConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_SetMap
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_SetSet
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_SkipOffsetConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_SoftObjectConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_StringConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_StructConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_StructMemberContext
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_SwitchValue
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_TextConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_Tracepoint
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_TransformConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_True
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_UInt64Const
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_UnicodeStringConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_VectorConst
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_VirtualFunction
import com.github.jpabscale.uasset4j.kismet.bytecode.expressions.EX_WireTracepoint
import com.github.jpabscale.uasset4j.unrealtypes.EPropertyFlags
import com.github.jpabscale.uasset4j.unrealtypes.FGuid
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5
import com.github.jpabscale.uasset4j.util.Ref

enum class EPinContainerType {
    None,
    Array,
    Set,
    Map,
}

class FSimpleMemberReference {
    var MemberParent: String? = null
    var MemberName: String? = null
    var MemberGuid: FGuid = FGuid(0u, 0u, 0u, 0u)
}

class FEdGraphTerminalType {
    var TerminalCategory: String? = null
    var TerminalSubCategory: String? = null
    var TerminalSubCategoryObject: String? = null
    var bTerminalIsConst: Boolean = false
    var bTerminalIsWeakPointer: Boolean = false
    var bTerminalIsUObjectWrapper: Boolean = false
}

class FEdGraphPinType {
    var PinCategory: String? = null
    var PinSubCategory: String? = null
    var PinSubCategoryObject: String? = null
    var PinSubCategoryMemberReference: FSimpleMemberReference = FSimpleMemberReference()
    var PinValueType: FEdGraphTerminalType = FEdGraphTerminalType()
    var ContainerType: EPinContainerType = EPinContainerType.None
    var bIsReference: Boolean = false
    var bIsConst: Boolean = false
    var bIsWeakPointer: Boolean = false
    var bIsUObjectWrapper: Boolean = false
}

private fun Ref<Int>.Inc() {
    value = value!! + 1
}

private fun Ref<Int>.Add(n: Int) {
    value = value!! + n
}

private fun ObjectNode.Add(key: String, value: String?) {
    if (value == null) putNull(key) else put(key, value)
}

private fun ObjectNode.Add(key: String, value: Int) = put(key, value)

private fun ObjectNode.Add(key: String, value: Long) = put(key, value)

private fun ObjectNode.Add(key: String, value: Float) = put(key, value)

private fun ObjectNode.Add(key: String, value: Double) = put(key, value)

private fun ObjectNode.Add(key: String, value: Boolean) = put(key, value)

private fun ObjectNode.Add(key: String, value: Byte) = put(key, value.toInt())

private fun ObjectNode.Add(key: String, value: JsonNode) {
    set<JsonNode>(key, value)
}

private fun ObjectNode.Add(props: Array<Pair<String, JsonNode>>) {
    for ((k, v) in props) set<JsonNode>(k, v)
}

private fun FGuid.ToDotNetString(): String = String.format(
    "%08x-%04x-%04x-%04x-%012x",
    data1.toInt(),
    data2.toInt() and 0xFFFF,
    data3.toInt() and 0xFFFF,
    (data4 shr 48).toInt() and 0xFFFF,
    (data4 and 0xFFFFFFFFFFFFuL).toLong(),
)

private fun ObjectNode.Add(key: String, value: FGuid) = put(key, value.ToDotNetString())

private fun JProperty(name: String, value: String): Pair<String, JsonNode> = name to TextNode.valueOf(value)

private fun JProperty(name: String, value: ObjectNode): Pair<String, JsonNode> = name to value

object KismetSerializer {
    var asset: UAsset? = null

    private val json = JsonNodeFactory.instance

    private const val PC_Boolean = "Bool"
    private const val PC_Byte = "Byte"
    private const val PC_Class = "Class"
    private const val PC_Int = "Int"
    private const val PC_Int64 = "Int64"
    private const val PC_Float = "Float"
    private const val PC_Double = "Double"
    private const val PC_Name = "Name"
    private const val PC_Delegate = "Delegate"
    private const val PC_MCDelegate = "mcdelegate"
    private const val PC_Object = "Object"
    private const val PC_Interface = "Interface"
    private const val PC_String = "String"
    private const val PC_Text = "Text"
    private const val PC_Struct = "Struct"
    private const val PC_Enum = "Enum"
    private const val PC_SoftObject = "Softobject"
    private const val PC_SoftClass = "Softclass"
    private const val PC_None = "None"

    fun SerializeScript(code: Array<KismetExpression>): ArrayNode {
        val jscript = json.arrayNode()
        val index = Ref<Int>(0)
        for (instruction in code) {
            jscript.add(SerializeExpression(instruction, index, true))
        }
        return jscript
    }

    fun GetName(index: Int): String {
        return when {
            index > 0 -> asset!!.Exports[index - 1].ObjectName?.toString() ?: ""
            index < 0 -> asset!!.Imports[-index - 1].ObjectName?.toString() ?: ""
            else -> ""
        }
    }

    fun GetClassIndex(): Int {
        for (i in 1..asset!!.Exports.size) {
            if (asset!!.Exports[i - 1] is ClassExport) {
                return i
            }
        }
        return 0
    }

    fun GetFullName(index: Int, alt: Boolean = false): String {
        if (index > 0 && index < asset!!.Exports.size) {
            if (asset!!.Exports[index - 1].OuterIndex?.Index != 0) {
                val parent = GetFullName(asset!!.Exports[index - 1].OuterIndex?.Index ?: 0)
                return parent + "." + (asset!!.Exports[index - 1].ObjectName?.toString() ?: "")
            } else {
                return asset!!.Exports[index - 1].ObjectName?.toString() ?: ""
            }
        } else if (index < 0) {
            if (asset!!.Imports[-index - 1].OuterIndex?.Index != 0) {
                val parent = GetFullName(asset!!.Imports[-index - 1].OuterIndex?.Index ?: 0)
                return parent + "." + (asset!!.Imports[-index - 1].ObjectName?.toString() ?: "")
            } else {
                return asset!!.Imports[-index - 1].ObjectName?.toString() ?: ""
            }
        } else {
            return ""
        }
    }

    fun GetParentName(index: Int): String {
        return when {
            index > 0 -> {
                if (asset!!.Exports[index - 1].OuterIndex?.Index != 0) {
                    GetFullName(asset!!.Exports[index - 1].OuterIndex?.Index ?: 0)
                } else {
                    ""
                }
            }
            index < 0 -> {
                if (asset!!.Imports[-index - 1].OuterIndex?.Index != 0) {
                    GetFullName(asset!!.Imports[-index - 1].OuterIndex?.Index ?: 0)
                } else {
                    ""
                }
            }
            else -> ""
        }
    }

    fun FindProperty(index: Int, propname: FName): FProperty? {
        if (index < 0) {
            return null
        }
        val export = asset!!.Exports[index - 1]
        if (export is StructExport) {
            for (prop in export.LoadedProperties ?: emptyArray()) {
                if (prop.Name == propname) {
                    return prop
                }
            }
        }
        return null
    }

    fun GetPropertyCategoryInfo(prop: FProperty): FEdGraphPinType {
        val pin = FEdGraphPinType()
        when (prop) {
            is FInterfaceProperty -> {
                pin.PinCategory = PC_Interface
                pin.PinSubCategoryObject = GetFullName(prop.InterfaceClass.Index)
            }
            is FClassProperty -> {
                pin.PinCategory = PC_Class
                pin.PinSubCategoryObject = GetFullName(prop.MetaClass.Index)
            }
            is FSoftClassProperty -> {
                pin.PinCategory = PC_SoftClass
                pin.PinSubCategoryObject = GetFullName(prop.MetaClass.Index)
            }
            is FSoftObjectProperty -> {
                pin.PinCategory = PC_SoftObject
                pin.PinSubCategoryObject = GetFullName(prop.PropertyClass.Index)
            }
            is FObjectProperty -> {
                pin.PinCategory = PC_Object
                pin.PinSubCategoryObject = GetFullName(prop.PropertyClass.Index)
                if (prop.PropertyFlags.hasFlag(EPropertyFlags.CPF_AutoWeak.value)) {
                    pin.bIsWeakPointer = true
                }
            }
            is FStructProperty -> {
                pin.PinCategory = PC_Struct
                pin.PinSubCategoryObject = GetFullName(prop.Struct.Index)
            }
            is FByteProperty -> {
                pin.PinCategory = PC_Byte
                pin.PinSubCategoryObject = GetFullName(prop.Enum.Index)
            }
            is FEnumProperty -> {
                if (prop.UnderlyingProp !is FByteProperty) {
                } else {
                    pin.PinCategory = PC_Byte
                    pin.PinSubCategoryObject = GetFullName(prop.Enum.Index)
                }
            }
            is FBoolProperty -> {
                pin.PinCategory = PC_Boolean
            }
            is FGenericProperty -> {
                when (prop.SerializedType?.toString()) {
                    "FloatProperty" -> pin.PinCategory = PC_Float
                    "DoubleProperty" -> pin.PinCategory = PC_Double
                    "Int64Property" -> pin.PinCategory = PC_Int64
                    "IntProperty" -> pin.PinCategory = PC_Int
                    "NameProperty" -> pin.PinCategory = PC_Name
                    "StrProperty" -> pin.PinCategory = PC_String
                    "TextProperty" -> pin.PinCategory = PC_Text
                    else -> {
                    }
                }
            }
            else -> {
            }
        }
        return pin
    }

    fun FillSimpleMemberReference(index: Int): FSimpleMemberReference {
        val member = FSimpleMemberReference()
        if (index > 0) {
            member.MemberName = asset!!.Exports[index - 1].ObjectName?.toString()
            member.MemberParent = GetName(asset!!.Exports[index - 1].OuterIndex?.Index ?: 0)
            member.MemberGuid = asset!!.Exports[index - 1].PackageGuid
        } else if (index < 0) {
            member.MemberName = asset!!.Imports[-index - 1].ObjectName?.toString()
            member.MemberParent = asset!!.Imports[-index - 1].ClassPackage?.toString()
            member.MemberGuid = FGuid(0u, 0u, 0u, 0u)
        }
        return member
    }

    fun SerializeGraphPinType(pin: FEdGraphPinType): ObjectNode {
        val jpin = json.objectNode()
        jpin.Add("PinCategory", pin.PinCategory)
        jpin.Add("PinSubCategory", pin.PinCategory)
        if (pin.PinSubCategoryObject == "" || pin.PinSubCategoryObject == null) {
        } else {
            jpin.Add("PinSubCategoryObject", pin.PinSubCategoryObject)
        }

        if (pin.PinSubCategoryMemberReference.MemberName != null) {
            val member = pin.PinSubCategoryMemberReference
            if (member.MemberGuid == FGuid(0u, 0u, 0u, 0u)) {
            } else {
                val jmember = json.objectNode()
                if (member.MemberParent != "" || member.MemberParent != null) {
                    jmember.Add("MemberParent", member.MemberParent)
                }
                jmember.Add("MemberName", member.MemberName)
                jmember.Add("MemberGuid", member.MemberGuid)
                jpin.Add("PinSubCategoryMemberReference", jmember)
            }
        }

        if (pin.ContainerType == EPinContainerType.Map) {
            val valuetype = pin.PinValueType
            val jvaluetype = json.objectNode()

            jvaluetype.Add("TerminalCategory", valuetype.TerminalCategory)
            if (valuetype.TerminalSubCategory == null || valuetype.TerminalSubCategory == "") {
                jvaluetype.Add("TerminalSubCategory", "None")
            } else {
                jvaluetype.Add("TerminalSubCategory", valuetype.TerminalSubCategory)
            }
            if (valuetype.TerminalSubCategoryObject != "" && valuetype.TerminalSubCategoryObject != null) {
                jvaluetype.Add("TerminalSubCategoryObject", valuetype.TerminalSubCategoryObject)
            }
            jvaluetype.Add("TerminalIsConst", valuetype.bTerminalIsConst)
            jvaluetype.Add("TerminalIsWeakPointer", valuetype.bTerminalIsWeakPointer)
            jpin.Add("PinValueType", jvaluetype)
        }

        if (pin.ContainerType != EPinContainerType.None) {
            jpin.Add("ContainerType", pin.ContainerType.ordinal)
        }

        if (pin.bIsReference) {
            jpin.Add("IsReference", pin.bIsReference)
        }
        if (pin.bIsConst) {
            jpin.Add("IsConst", pin.bIsConst)
        }
        if (pin.bIsWeakPointer) {
            jpin.Add("IsWeakPointer", pin.bIsWeakPointer)
        }
        return jpin
    }

    fun ConvertPropertyToPinType(property: FProperty): FEdGraphPinType {
        val pin = FEdGraphPinType()
        var prop: FProperty = property

        if (property is FMapProperty) {
            prop = property.KeyProp!!
            pin.ContainerType = EPinContainerType.Map
            pin.bIsWeakPointer = false
            val temppin = GetPropertyCategoryInfo(property.ValueProp!!)
            pin.PinValueType.TerminalCategory = temppin.PinCategory
            pin.PinValueType.TerminalSubCategory = temppin.PinSubCategory
            pin.PinValueType.TerminalSubCategoryObject = temppin.PinSubCategoryObject
            pin.PinValueType.bTerminalIsConst = temppin.bIsConst
            pin.PinValueType.bTerminalIsWeakPointer = temppin.bIsWeakPointer
        } else if (property is FSetProperty) {
            prop = property.ElementProp!!
            pin.ContainerType = EPinContainerType.Set
        } else if (property is FArrayProperty) {
            prop = property.Inner!!
            pin.ContainerType = EPinContainerType.Array
        }
        pin.bIsReference = property.PropertyFlags.hasFlag(EPropertyFlags.CPF_OutParm.value) && property.PropertyFlags.hasFlag(EPropertyFlags.CPF_ReferenceParm.value)
        pin.bIsConst = property.PropertyFlags.hasFlag(EPropertyFlags.CPF_ConstParm.value)

        if (prop is FMulticastDelegateProperty) {
            pin.PinCategory = PC_MCDelegate
            pin.PinSubCategoryMemberReference = FillSimpleMemberReference(prop.SignatureFunction.Index)
        } else if (prop is FDelegateProperty) {
            pin.PinCategory = PC_Delegate
            pin.PinSubCategoryMemberReference = FillSimpleMemberReference(prop.SignatureFunction.Index)
        } else {
            val temppin = GetPropertyCategoryInfo(prop)
            pin.PinCategory = temppin.PinCategory
            pin.PinSubCategory = temppin.PinSubCategory
            pin.PinSubCategoryObject = temppin.PinSubCategoryObject
            pin.bIsWeakPointer = temppin.bIsWeakPointer
        }
        return pin
    }

    fun SerializePropertyPointer(pointer: KismetPropertyPointer?, names: Array<String>): Array<Pair<String, JsonNode>> {
        val jproparray = arrayOfNulls<Pair<String, JsonNode>>(names.size)

        if (asset!!.GetCustomVersion<FReleaseObjectVersion>() >= FReleaseObjectVersion.FFieldPathOwnerSerialization.ordinal) {
            val newer = pointer?.New
            if (newer != null && newer.ResolvedOwner.Index != 0) {
                val property = FindProperty(newer.ResolvedOwner.Index, newer.Path[0])
                jproparray[0] = if (property != null) {
                    val PropertyType = ConvertPropertyToPinType(property)
                    JProperty(names[0], SerializeGraphPinType(PropertyType))
                } else {
                    JProperty(names[0], "##NOT SERIALIZED##")
                }
                if (names.size > 1) {
                    jproparray[1] = JProperty(names[1], newer.Path[0].toString())
                }
                return jproparray.map { it!! }.toTypedArray()
            }
        }
        val older = pointer?.Old
        if (older != null && older.Index != 0) {
            if (names.size > 1) {
                val split = GetFullName(older.Index).split('.')
                jproparray[0] = JProperty(names[0], split[0])
                var path = ""
                for (i in 1 until split.size) {
                    path += split[i] + "."
                }
                if (path.endsWith(".")) {
                    path = path.substring(0, path.length - 1)
                }
                jproparray[1] = JProperty(names[1], path)
            } else {
                jproparray[0] = JProperty(names[0], GetFullName(older.Index))
            }
        } else {
            jproparray[0] = JProperty(names[0], "#Pointer Error#")
            if (names.size > 1) {
                jproparray[1] = JProperty(names[1], "^^^^^")
            }
        }
        return jproparray.map { it!! }.toTypedArray()
    }

    fun SerializeExpression(expression: KismetExpression?, index: Ref<Int>, addindex: Boolean = false): ObjectNode {
        val savedindex = index.value
        val jexp = json.objectNode()
        index.Inc()
        when (expression) {
            is EX_PrimitiveCast -> {
                jexp.Add("Inst", expression.Inst)
                index.Inc()
                when (expression.ConversionType?.value) {
                    ECastToken.InterfaceToBool.value -> {
                        jexp.Add("CastType", "InterfaceToBool")
                    }
                    ECastToken.ObjectToBool.value -> {
                        jexp.Add("CastType", "ObjectToBool")
                    }
                    ECastToken.ObjectToInterface.value -> {
                        jexp.Add("CastType", "ObjectToInterface")
                        index.Add(8)
                        jexp.Add("InterfaceClass", "##NOT SERIALIZED##")
                    }
                    else -> {
                    }
                }
                jexp.Add("Expression", SerializeExpression(expression.Target, index))
            }
            is EX_SetSet -> {
                jexp.Add("Inst", expression.Inst)
                jexp.Add("LeftSideExpression", SerializeExpression(expression.SetProperty, index))
                val jparams = json.arrayNode()
                index.Add(4)
                for (param in expression.Elements) {
                    jparams.add(SerializeExpression(param, index))
                }
                index.Inc()
                jexp.Add("Values", jparams)
            }
            is EX_SetConst -> {
                index.Add(8)
                jexp.Add("Inst", expression.Inst)
                jexp.Add(SerializePropertyPointer(expression.InnerProperty, arrayOf("InnerProperty")))
                index.Add(4)
                val jparams = json.arrayNode()
                for (param in expression.Elements) {
                    jparams.add(SerializeExpression(param, index))
                }
                index.Inc()
                jexp.Add("Values", jparams)
            }
            is EX_SetMap -> {
                jexp.Add("Inst", expression.Inst)
                jexp.Add("LeftSideExpression", SerializeExpression(expression.MapProperty, index))
                index.Add(4)
                val jparams = json.arrayNode()
                for (j in 1..expression.Elements.size / 2) {
                    val jobject = json.objectNode()
                    jobject.Add("Key", SerializeExpression(expression.Elements[2 * (j - 1)], index))
                    jobject.Add("Value", SerializeExpression(expression.Elements[2 * (j - 1) + 1], index))
                    jparams.add(jobject)
                }
                index.Inc()
                jexp.Add("Values", jparams)
            }
            is EX_MapConst -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(8)
                jexp.Add(SerializePropertyPointer(expression.KeyProperty, arrayOf("KeyProperty")))
                jexp.Add(SerializePropertyPointer(expression.ValueProperty, arrayOf("ValueProperty")))
                index.Add(4)
                val jparams = json.arrayNode()
                for (j in 1..expression.Elements.size / 2) {
                    val jobject = json.objectNode()
                    jobject.Add("Key", SerializeExpression(expression.Elements[2 * (j - 1)], index))
                    jobject.Add("Value", SerializeExpression(expression.Elements[2 * (j - 1) + 1], index))
                    jparams.add(jobject)
                }
                index.Inc()
                jexp.Add("Values", jparams)
            }
            is EX_ObjToInterfaceCast -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(8)
                jexp.Add("InterfaceClass", GetFullName(expression.ClassPtr.Index))
                jexp.Add("Expression", SerializeExpression(expression.Target, index))
            }
            is EX_CrossInterfaceCast -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(8)
                jexp.Add("InterfaceClass", GetFullName(expression.ClassPtr.Index))
                jexp.Add("Expression", SerializeExpression(expression.Target, index))
            }
            is EX_InterfaceToObjCast -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(8)
                jexp.Add("ObjectClass", GetFullName(expression.ClassPtr.Index))
                jexp.Add("Expression", SerializeExpression(expression.Target, index))
            }
            is EX_Let -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(8)
                jexp.Add("Variable", SerializeExpression(expression.Variable, index))
                jexp.Add("Expression", SerializeExpression(expression.Expression, index))
            }
            is EX_LetObj -> {
                jexp.Add("Inst", expression.Inst)
                jexp.Add("Variable", SerializeExpression(expression.VariableExpression, index))
                jexp.Add("Expression", SerializeExpression(expression.AssignmentExpression, index))
            }
            is EX_LetWeakObjPtr -> {
                jexp.Add("Inst", expression.Inst)
                jexp.Add("Variable", SerializeExpression(expression.VariableExpression, index))
                jexp.Add("Expression", SerializeExpression(expression.AssignmentExpression, index))
            }
            is EX_LetBool -> {
                jexp.Add("Inst", expression.Inst)
                jexp.Add("Variable", SerializeExpression(expression.VariableExpression, index))
                jexp.Add("Expression", SerializeExpression(expression.AssignmentExpression, index))
            }
            is EX_LetValueOnPersistentFrame -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(8)
                jexp.Add(SerializePropertyPointer(expression.DestinationProperty, arrayOf("Property Outer", "Property Name")))
                jexp.Add("Expression", SerializeExpression(expression.AssignmentExpression, index))
            }
            is EX_StructMemberContext -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(8)
                jexp.Add(SerializePropertyPointer(expression.StructMemberExpression, arrayOf("Property Outer", "Property Name")))
                jexp.Add("StructExpression", SerializeExpression(expression.StructExpression, index))
            }
            is EX_LetDelegate -> {
                jexp.Add("Inst", expression.Inst)
                jexp.Add("Variable", SerializeExpression(expression.VariableExpression, index))
                jexp.Add("Expression", SerializeExpression(expression.AssignmentExpression, index))
            }
            is EX_LocalVirtualFunction -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(12)
                jexp.Add("FunctionName", expression.VirtualFunctionName.toString())
                val jparams = json.arrayNode()
                for (param in expression.Parameters) {
                    jparams.add(SerializeExpression(param, index))
                }
                index.Inc()
                jexp.Add("Parameters", jparams)
            }
            is EX_LocalFinalFunction -> {
                jexp.Add("Inst", expression.Inst)
                jexp.Add("Function", GetName(expression.StackNode.Index))
                index.Add(8)
                val jparams = json.arrayNode()
                for (param in expression.Parameters) {
                    jparams.add(SerializeExpression(param, index))
                }
                index.Inc()
                jexp.Add("Parameters", jparams)
            }
            is EX_LetMulticastDelegate -> {
                jexp.Add("Inst", expression.Inst)
                jexp.Add("Variable", SerializeExpression(expression.VariableExpression, index))
                jexp.Add("Expression", SerializeExpression(expression.AssignmentExpression, index))
            }
            is EX_ComputedJump -> {
                jexp.Add("Inst", expression.Inst)
                jexp.Add("OffsetExpression", SerializeExpression(expression.CodeOffsetExpression, index))
            }
            is EX_Jump -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(4)
                jexp.Add("Offset", expression.CodeOffset)
            }
            is EX_LocalVariable -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(8)
                jexp.Add(SerializePropertyPointer(expression.Variable, arrayOf("Variable Outer", "Variable Name")))
            }
            is EX_DefaultVariable -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(8)
                jexp.Add(SerializePropertyPointer(expression.Variable, arrayOf("Variable Outer", "Variable Name")))
            }
            is EX_InstanceVariable -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(8)
                jexp.Add(SerializePropertyPointer(expression.Variable, arrayOf("Variable Outer", "Variable Name")))
            }
            is EX_LocalOutVariable -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(8)
                jexp.Add(SerializePropertyPointer(expression.Variable, arrayOf("Variable Outer", "Variable Name")))
            }
            is EX_InterfaceContext -> {
                jexp.Add("Inst", expression.Inst)
                jexp.Add("Expression", SerializeExpression(expression.InterfaceValue, index))
            }
            is EX_DeprecatedOp4A, is EX_Nothing, is EX_EndOfScript, is EX_IntZero, is EX_IntOne, is EX_True, is EX_False, is EX_NoObject, is EX_NoInterface, is EX_Self -> {
                jexp.Add("Inst", expression.Inst)
            }
            is EX_Return -> {
                jexp.Add("Inst", expression.Inst)
                jexp.Add("Expression", SerializeExpression(expression.ReturnExpression, index))
            }
            is EX_CallMath -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(8)
                jexp.Add("Function", GetName(expression.StackNode.Index))
                jexp.Add("ContextClass", GetParentName(expression.StackNode.Index))
                val jparams = json.arrayNode()
                for (param in expression.Parameters) {
                    jparams.add(SerializeExpression(param, index))
                }
                index.Inc()
                jexp.Add("Parameters", jparams)
            }
            is EX_CallMulticastDelegate -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(8)
                val jsign = json.objectNode()
                val bIsSelfContext = GetClassIndex() == expression.StackNode.Index
                jsign.Add("IsSelfContext", bIsSelfContext)
                jsign.Add("MemberParent", GetFullName(expression.StackNode.Index))
                jsign.Add("MemberName", GetName(expression.StackNode.Index))
                jexp.Add("DelegateSignatureFunction", jsign)
                jexp.Add("Delegate", SerializeExpression(expression.Delegate, index))

                val jparams = json.arrayNode()
                for (param in expression.Parameters) {
                    jparams.add(SerializeExpression(param, index))
                }
                index.Inc()
                jexp.Add("Parameters", jparams)
            }
            is EX_FinalFunction -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(8)
                jexp.Add("Function", GetName(expression.StackNode.Index))
                val jparams = json.arrayNode()
                for (param in expression.Parameters) {
                    jparams.add(SerializeExpression(param, index))
                }
                index.Inc()
                jexp.Add("Parameters", jparams)
            }
            is EX_VirtualFunction -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(12)
                jexp.Add("Function", expression.VirtualFunctionName.toString())
                val jparams = json.arrayNode()
                for (param in expression.Parameters) {
                    jparams.add(SerializeExpression(param, index))
                }
                index.Inc()
                jexp.Add("Parameters", jparams)
            }
            is EX_Context -> {
                jexp.Add("Inst", expression.Inst)
                jexp.Add("Context", SerializeExpression(expression.ObjectExpression, index))
                index.Add(4)
                jexp.Add("SkipOffsetForNull", expression.Offset)
                index.Add(8)
                jexp.Add(SerializePropertyPointer(expression.RValuePointer, arrayOf("RValuePropertyOuter", "RValuePropertyName")))
                jexp.Add("Expression", SerializeExpression(expression.ContextExpression, index))
            }
            is EX_IntConst -> {
                index.Add(4)
                jexp.Add("Inst", expression.Inst)
                jexp.Add("Value", expression.Value)
            }
            is EX_SkipOffsetConst -> {
                index.Add(4)
                jexp.Add("Inst", expression.Inst)
                jexp.Add("Value", expression.Value)
            }
            is EX_FloatConst -> {
                index.Add(4)
                jexp.Add("Inst", expression.Inst)
                jexp.Add("Value", expression.Value)
            }
            is EX_DoubleConst -> {
                index.Add(8)
                jexp.Add("Inst", expression.Inst)
                jexp.Add("Value", expression.Value)
            }
            is EX_StringConst -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(expression.Value.length + 1)
                jexp.Add("Value", expression.Value)
            }
            is EX_UnicodeStringConst -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(2 * (expression.Value.length + 1))
                jexp.Add("Value", expression.Value)
            }
            is EX_TextConst -> {
                jexp.Add("Inst", expression.Inst)
                index.Inc()
                when (expression.Value.TextLiteralType) {
                    EBlueprintTextLiteralType.Empty -> {
                        jexp.Add("TextLiteralType", "Empty")
                    }
                    EBlueprintTextLiteralType.LocalizedText -> {
                        jexp.Add("TextLiteralType", "LocalizedText")
                        jexp.Add("SourceString", ReadString(expression.Value.LocalizedSource, index))
                        jexp.Add("LocalizationKey", ReadString(expression.Value.LocalizedKey, index))
                        jexp.Add("LocalizationNamespace", ReadString(expression.Value.LocalizedNamespace, index))
                    }
                    EBlueprintTextLiteralType.InvariantText -> {
                        jexp.Add("TextLiteralType", "InvariantText")
                        jexp.Add("SourceString", ReadString(expression.Value.InvariantLiteralString, index))
                    }
                    EBlueprintTextLiteralType.LiteralString -> {
                        jexp.Add("TextLiteralType", "LiteralString")
                        jexp.Add("SourceString", ReadString(expression.Value.LiteralString, index))
                    }
                    EBlueprintTextLiteralType.StringTableEntry -> {
                        jexp.Add("TextLiteralType", "StringTableEntry")
                        index.Add(8)
                        jexp.Add("TableId", ReadString(expression.Value.StringTableId, index))
                        jexp.Add("TableKey", ReadString(expression.Value.StringTableKey, index))
                    }
                }
            }
            is EX_ObjectConst -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(8)
                jexp.Add("Object", GetFullName(expression.Value.Index))
            }
            is EX_SoftObjectConst -> {
                jexp.Add("Inst", expression.Inst)
                jexp.Add("Value", SerializeExpression(expression.Value, index))
            }
            is EX_NameConst -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(12)
                jexp.Add("Value", expression.Value.toString())
            }
            is EX_RotationConst -> {
                jexp.Add("Inst", expression.Inst)
                index.Add((if (asset!!.ObjectVersionUE5 >= ObjectVersionUE5.LARGE_WORLD_COORDINATES) 8 else 4) * 3)
                jexp.Add("Pitch", expression.Value.Pitch)
                jexp.Add("Yaw", expression.Value.Yaw)
                jexp.Add("Roll", expression.Value.Roll)
            }
            is EX_VectorConst -> {
                jexp.Add("Inst", expression.Inst)
                index.Add((if (asset!!.ObjectVersionUE5 >= ObjectVersionUE5.LARGE_WORLD_COORDINATES) 8 else 4) * 3)
                jexp.Add("X", expression.Value.X)
                jexp.Add("Y", expression.Value.Y)
                jexp.Add("Z", expression.Value.Z)
            }
            is EX_TransformConst -> {
                jexp.Add("Inst", expression.Inst)
                index.Add((if (asset!!.ObjectVersionUE5 >= ObjectVersionUE5.LARGE_WORLD_COORDINATES) 8 else 4) * 10)
                val jrot = json.objectNode()
                val jtrans = json.objectNode()
                val jscale = json.objectNode()

                jrot.Add("X", expression.Value.Rotation.X)
                jrot.Add("Y", expression.Value.Rotation.Y)
                jrot.Add("Z", expression.Value.Rotation.Z)
                jrot.Add("W", expression.Value.Rotation.W)

                jtrans.Add("X", expression.Value.Translation.X)
                jtrans.Add("Y", expression.Value.Translation.Y)
                jtrans.Add("Z", expression.Value.Translation.Z)

                jscale.Add("X", expression.Value.Scale3D.X)
                jscale.Add("Y", expression.Value.Scale3D.Y)
                jscale.Add("Z", expression.Value.Scale3D.Z)

                jexp.Add("Rotation", jrot)
                jexp.Add("Translation", jtrans)
                jexp.Add("Scale", jscale)
            }
            is EX_StructConst -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(8)
                jexp.Add("Struct", GetFullName(expression.Struct.Index))

                index.Add(4)
                val jstruct = json.objectNode()
                var tempindex = 0
                for (param in expression.Value) {
                    val jstructpart = json.arrayNode()
                    jstructpart.add(SerializeExpression(param, index))
                    jstruct.Add("Missing property name" + tempindex, jstructpart)
                    tempindex++
                }
                index.Inc()
                jexp.Add("Properties", jstruct)
            }
            is EX_SetArray -> {
                jexp.Add("Inst", expression.Inst)
                jexp.Add("LeftSideExpression", SerializeExpression(expression.AssigningProperty, index))
                val jparams = json.arrayNode()
                for (param in expression.Elements) {
                    jparams.add(SerializeExpression(param, index))
                }
                index.Inc()
                jexp.Add("Values", jparams)
            }
            is EX_ArrayConst -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(8)
                jexp.Add(SerializePropertyPointer(expression.InnerProperty, arrayOf("Variable Outer")))
                index.Add(4)
                val jparams = json.arrayNode()
                for (param in expression.Elements) {
                    jparams.add(SerializeExpression(param, index))
                }
                index.Inc()
                jexp.Add("Values", jparams)
            }
            is EX_ByteConst -> {
                jexp.Add("Inst", expression.Inst)
                index.Inc()
                jexp.Add("Value", expression.Value)
            }
            is EX_IntConstByte -> {
                jexp.Add("Inst", expression.Inst)
                index.Inc()
                jexp.Add("Value", expression.Value)
            }
            is EX_Int64Const -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(8)
                jexp.Add("Value", expression.Value)
            }
            is EX_UInt64Const -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(8)
                jexp.Add("Value", expression.Value)
            }
            is EX_FieldPathConst -> {
                jexp.Add("Inst", expression.Inst)
                jexp.Add("Expression", SerializeExpression(expression.Value, index))
            }
            is EX_MetaCast -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(8)
                jexp.Add("Class", GetFullName(expression.ClassPtr.Index))
                jexp.Add("Expression", SerializeExpression(expression.Target, index))
            }
            is EX_DynamicCast -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(8)
                jexp.Add("Class", GetFullName(expression.ClassPtr.Index))
                jexp.Add("Expression", SerializeExpression(expression.Target, index))
            }
            is EX_JumpIfNot -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(4)
                jexp.Add("Offset", expression.CodeOffset)
                jexp.Add("Condition", SerializeExpression(expression.BooleanExpression, index))
            }
            is EX_Assert -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(3)
                jexp.Add("LineNumber", expression.LineNumber)
                jexp.Add("Debug", expression.DebugMode)
                jexp.Add("Expression", SerializeExpression(expression.AssertExpression, index))
            }
            is EX_InstanceDelegate -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(12)
                jexp.Add("FunctionName", expression.FunctionName.toString())
            }
            is EX_AddMulticastDelegate -> {
                jexp.Add("Inst", expression.Inst)
                jexp.Add("MulticastDelegate", SerializeExpression(expression.Delegate, index))
                jexp.Add("Delegate", SerializeExpression(expression.DelegateToAdd, index))
            }
            is EX_RemoveMulticastDelegate -> {
                jexp.Add("Inst", expression.Inst)
                jexp.Add("MulticastDelegate", SerializeExpression(expression.Delegate, index))
                jexp.Add("Delegate", SerializeExpression(expression.DelegateToAdd, index))
            }
            is EX_ClearMulticastDelegate -> {
                jexp.Add("Inst", expression.Inst)
                jexp.Add("MulticastDelegate", SerializeExpression(expression.DelegateToClear, index))
            }
            is EX_BindDelegate -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(12)
                jexp.Add("FunctionName", expression.FunctionName.toString())
                jexp.Add("Delegate", SerializeExpression(expression.Delegate, index))
                jexp.Add("Object", SerializeExpression(expression.ObjectTerm, index))
            }
            is EX_PushExecutionFlow -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(4)
                jexp.Add("Offset", expression.PushingAddress)
            }
            is EX_PopExecutionFlow -> {
                jexp.Add("Inst", expression.Inst)
            }
            is EX_PopExecutionFlowIfNot -> {
                jexp.Add("Inst", expression.Inst)
                jexp.Add("Condition", SerializeExpression(expression.BooleanExpression, index))
            }
            is EX_Breakpoint -> {
                jexp.Add("Inst", expression.Inst)
            }
            is EX_WireTracepoint -> {
                jexp.Add("Inst", expression.Inst)
            }
            is EX_InstrumentationEvent -> {
                jexp.Add("Inst", expression.Inst)
                index.Inc()
                when (expression.EventType) {
                    EScriptInstrumentationType.Class -> jexp.Add("EventType", "Class")
                    EScriptInstrumentationType.ClassScope -> jexp.Add("EventType", "ClassScope")
                    EScriptInstrumentationType.Instance -> jexp.Add("EventType", "Instance")
                    EScriptInstrumentationType.Event -> jexp.Add("EventType", "Event")
                    EScriptInstrumentationType.InlineEvent -> {
                        index.Add(12)
                        jexp.Add("EventType", "InlineEvent")
                        jexp.Add("EventName", expression.EventName.toString())
                    }
                    EScriptInstrumentationType.ResumeEvent -> jexp.Add("EventType", "ResumeEvent")
                    EScriptInstrumentationType.PureNodeEntry -> jexp.Add("EventType", "PureNodeEntry")
                    EScriptInstrumentationType.NodeDebugSite -> jexp.Add("EventType", "NodeDebugSite")
                    EScriptInstrumentationType.NodeEntry -> jexp.Add("EventType", "NodeEntry")
                    EScriptInstrumentationType.NodeExit -> jexp.Add("EventType", "NodeExit")
                    EScriptInstrumentationType.PushState -> jexp.Add("EventType", "PushState")
                    EScriptInstrumentationType.RestoreState -> jexp.Add("EventType", "RestoreState")
                    EScriptInstrumentationType.ResetState -> jexp.Add("EventType", "ResetState")
                    EScriptInstrumentationType.SuspendState -> jexp.Add("EventType", "SuspendState")
                    EScriptInstrumentationType.PopState -> jexp.Add("EventType", "PopState")
                    EScriptInstrumentationType.TunnelEndOfThread -> jexp.Add("EventType", "TunnelEndOfThread")
                    EScriptInstrumentationType.Stop -> jexp.Add("EventType", "Stop")
                }
            }
            is EX_Tracepoint -> {
                jexp.Add("Inst", expression.Inst)
            }
            is EX_SwitchValue -> {
                jexp.Add("Inst", expression.Inst)
                index.Add(6)

                jexp.Add("Expression", SerializeExpression(expression.IndexTerm, index))
                jexp.Add("OffsetToSwitchEnd", expression.EndGotoOffset)
                val jcases = json.arrayNode()

                for (j in expression.Cases.indices) {
                    val jcase = json.objectNode()
                    jcase.Add("CaseValue", SerializeExpression(expression.Cases[j].CaseIndexValueTerm, index))
                    index.Add(4)
                    jcase.Add("OffsetToNextCase", expression.Cases[j].NextOffset)
                    jcase.Add("CaseResult", SerializeExpression(expression.Cases[j].CaseTerm, index))
                    jcases.add(jcase)
                }

                jexp.Add("Cases", jcases)
                jexp.Add("DefaultResult", SerializeExpression(expression.DefaultTerm, index))
            }
            is EX_ArrayGetByRef -> {
                jexp.Add("Inst", expression.Inst)
                jexp.Add("ArrayExpression", SerializeExpression(expression.ArrayVariable, index))
                jexp.Add("IndexExpression", SerializeExpression(expression.ArrayIndex, index))
            }
            else -> {
            }
        }
        if (addindex) {
            jexp.Add("StatementIndex", savedindex!!)
        }
        return jexp
    }

    fun ReadString(expr: KismetExpression?, index: Ref<Int>): String {
        var result = ""
        index.Inc()
        when (expr) {
            is EX_StringConst -> {
                result = expr.Value
                index.Add(result.length + 1)
            }
            is EX_UnicodeStringConst -> {
                result = expr.Value
                index.Add(2 * (result.length + 1))
            }
            else -> {
            }
        }
        return result
    }
}
