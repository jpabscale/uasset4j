// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Unversioned/JmapHelper.cs
package com.github.jpabscale.uasset4j.unversioned

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.jpabscale.uasset4j.FormatException

/**
 * JSON model classes for the subset of a .jmap file that UAssetAPI consumes. Every field the
 * actual .jmap serializes that is not relevant to us is dropped (the C# comments: "only fields
 * relevant to us").
 */
internal open class JmapObjectBase {
    var Type: String? = null

    var Address: String? = null
    var VTable: String? = null
    var ObjectFlags: String? = null
    var Outer: String? = null
    var Class: String? = null
}

internal class JmapObject : JmapObjectBase()

internal class JmapPackage : JmapObjectBase()

internal open class JmapStruct : JmapObjectBase() {
    var SuperStruct: String? = null
    var Properties: List<JmapPropertyBase> = mutableListOf()
}

internal class JmapScriptStruct : JmapStruct()

internal class JmapClass : JmapStruct()

internal class JmapFunction : JmapStruct()

internal class JmapEnum : JmapObjectBase() {
    var CppType: String? = null
    var EnumFlags: String? = null
    var CppForm: String? = null
    var Values: LinkedHashMap<Long, String> = LinkedHashMap()
}

internal open class JmapPropertyBase {
    var Type: String? = null

    var Address: String? = null
    var Name: String? = null
    var Offset: Long = 0
    var ArrayDim: Int = 0
}

internal class JmapProperty : JmapPropertyBase()

internal class JmapStructProperty : JmapPropertyBase() {
    var Struct: String? = null
}

internal class JmapArrayProperty : JmapPropertyBase() {
    var Inner: JmapPropertyBase? = null
}

internal class JmapEnumProperty : JmapPropertyBase() {
    var Container: JmapPropertyBase? = null
    var Enum: String? = null
}

internal class JmapMapProperty : JmapPropertyBase() {
    var Key: JmapPropertyBase? = null
    var Value: JmapPropertyBase? = null
}

internal class JmapSetProperty : JmapPropertyBase() {
    var Key: JmapPropertyBase? = null
}

internal class JmapByteProperty : JmapPropertyBase() {
    var Enum: String? = null
}

internal class JmapObjectProperty : JmapPropertyBase() {
    var PropertyClass: String? = null
}

internal class JmapClassProperty : JmapPropertyBase() {
    var PropertyClass: String? = null
    var MetaClass: String? = null
}

internal class JmapWeakObjectProperty : JmapPropertyBase() {
    var PropertyClass: String? = null
}

internal class JmapSoftObjectProperty : JmapPropertyBase() {
    var PropertyClass: String? = null
}

internal class JmapSoftClassProperty : JmapPropertyBase() {
    var PropertyClass: String? = null
    var MetaClass: String? = null
}

internal class JmapLazyObjectProperty : JmapPropertyBase() {
    var PropertyClass: String? = null
}

internal class JmapInterfaceProperty : JmapPropertyBase() {
    var InterfaceClass: String? = null
}

internal class JmapOptionalProperty : JmapPropertyBase() {
    var Inner: JmapPropertyBase? = null
}

/** Discriminator-based object deserializer (port of JmapObjectConverter). */
internal class JmapObjectConverter {
    fun Read(node: JsonNode): JmapObjectBase? {
        val typeNode = node.get("type")
        if (typeNode == null) throw FormatException("Jmap object is missing the type discriminator")

        val res: JmapObjectBase? = when (typeNode.asText()) {
            "Object" -> JmapObject()
            "Package" -> JmapPackage()
            "ScriptStruct" -> JmapScriptStruct()
            "Class" -> JmapClass()
            "Function" -> JmapFunction()
            "Enum" -> JmapEnum()
            else -> null
        }
        res?.Type = typeNode.asText()
        res?.Address = node.get("address")?.asText()
        res?.VTable = node.get("vtable")?.asText()
        res?.ObjectFlags = node.get("object_flags")?.asText()
        res?.Outer = node.get("outer")?.asText()
        res?.Class = node.get("class")?.asText()
        if (res is JmapStruct) {
            res.SuperStruct = node.get("super_struct")?.asText()
            res.Properties = node.get("properties")?.let { propsNode ->
                propsNode.map { JmapPropertyConverter().Read(it) }
            } ?: mutableListOf()
        }
        if (res is JmapEnum) {
            res.CppType = node.get("cpp_type")?.asText()
            res.EnumFlags = node.get("enum_flags")?.asText()
            res.CppForm = node.get("cpp_form")?.asText()
            node.get("names")?.let { res.Values = JmapEnumNamesConverter().Read(it) }
        }

        return res
    }
}

/** Discriminator-based property deserializer (port of JmapPropertyConverter). */
internal class JmapPropertyConverter {
    fun Read(node: JsonNode): JmapPropertyBase {
        val typeNode = node.get("type")
        if (typeNode == null) throw FormatException("Jmap property is missing the type discriminator")
        val typeDiscriminator = typeNode.asText()

        val res: JmapPropertyBase = when (typeDiscriminator) {
            "StructProperty" -> JmapStructProperty()
            "ArrayProperty" -> JmapArrayProperty()
            "EnumProperty" -> JmapEnumProperty()
            "MapProperty" -> JmapMapProperty()
            "SetProperty" -> JmapSetProperty()
            "ByteProperty" -> JmapByteProperty()
            "ObjectProperty" -> JmapObjectProperty()
            "ClassProperty" -> JmapClassProperty()
            "WeakObjectProperty" -> JmapWeakObjectProperty()
            "SoftObjectProperty" -> JmapSoftObjectProperty()
            "SoftClassProperty" -> JmapSoftClassProperty()
            "LazyObjectProperty" -> JmapLazyObjectProperty()
            "InterfaceProperty" -> JmapInterfaceProperty()
            "OptionalProperty" -> JmapOptionalProperty()
            else -> JmapProperty()
        }
        res.Type = typeDiscriminator
        res.Address = node.get("address")?.asText()
        res.Name = node.get("name")?.asText()
        res.Offset = node.get("offset")?.asLong() ?: 0
        res.ArrayDim = node.get("array_dim")?.asInt() ?: 0

        when (res) {
            is JmapStructProperty -> res.Struct = node.get("struct")?.asText()
            is JmapArrayProperty -> node.get("inner")?.takeIf { it.isObject }?.let { res.Inner = JmapPropertyConverter().Read(it) }
            is JmapEnumProperty -> {
                node.get("container")?.takeIf { it.isObject }?.let { res.Container = JmapPropertyConverter().Read(it) }
                res.Enum = node.get("enum")?.asText()
            }
            is JmapMapProperty -> {
                node.get("key_prop")?.takeIf { it.isObject }?.let { res.Key = JmapPropertyConverter().Read(it) }
                node.get("value_prop")?.takeIf { it.isObject }?.let { res.Value = JmapPropertyConverter().Read(it) }
            }
            is JmapSetProperty -> node.get("key_prop")?.takeIf { it.isObject }?.let { res.Key = JmapPropertyConverter().Read(it) }
            is JmapByteProperty -> res.Enum = node.get("enum")?.asText()
            is JmapObjectProperty -> res.PropertyClass = node.get("property_class")?.asText()
            is JmapClassProperty -> {
                res.PropertyClass = node.get("property_class")?.asText()
                res.MetaClass = node.get("meta_class")?.asText()
            }
            is JmapWeakObjectProperty -> res.PropertyClass = node.get("property_class")?.asText()
            is JmapSoftObjectProperty -> res.PropertyClass = node.get("property_class")?.asText()
            is JmapSoftClassProperty -> {
                res.PropertyClass = node.get("property_class")?.asText()
                res.MetaClass = node.get("meta_class")?.asText()
            }
            is JmapLazyObjectProperty -> res.PropertyClass = node.get("property_class")?.asText()
            is JmapInterfaceProperty -> res.InterfaceClass = node.get("interface_class")?.asText()
            is JmapOptionalProperty -> node.get("inner")?.takeIf { it.isObject }?.let { res.Inner = JmapPropertyConverter().Read(it) }
        }

        return res
    }
}

/** Deserializer for the enum `names` field (port of JmapEnumNamesConverter). */
internal class JmapEnumNamesConverter {
    fun Read(namesNode: JsonNode): LinkedHashMap<Long, String> {
        val output = LinkedHashMap<Long, String>()
        for (entry in namesNode) {
            if (entry.isArray) {
                val key = entry.get(0)?.asText()
                val value = entry.get(1)?.asLong()
                if (key != null && value != null) output[value] = key
            }
        }
        return output
    }
}

/** Helper class for operations on .jmap files. https://github.com/trumank/jmap/blob/master/jmap/src/lib.rs */
internal object JmapHelper {
    private val mapper = ObjectMapper()

    /** Converts a jmap type string to its corresponding usmap UsmapPropertyType enum value. */
    private fun UsmapPropertyTypeStringToEnum(type: String?): UsmapPropertyType {
        return when (type) {
            "StructProperty" -> UsmapPropertyType.StructProperty
            "StrProperty" -> UsmapPropertyType.StrProperty
            "NameProperty" -> UsmapPropertyType.NameProperty
            "TextProperty" -> UsmapPropertyType.TextProperty
            "MulticastInlineDelegateProperty" -> UsmapPropertyType.MulticastDelegateProperty
            "MulticastSparseDelegateProperty" -> UsmapPropertyType.MulticastDelegateProperty
            "MulticastDelegateProperty" -> UsmapPropertyType.MulticastDelegateProperty
            "DelegateProperty" -> UsmapPropertyType.DelegateProperty
            "BoolProperty" -> UsmapPropertyType.BoolProperty
            "ArrayProperty" -> UsmapPropertyType.ArrayProperty
            "EnumProperty" -> UsmapPropertyType.EnumProperty
            "MapProperty" -> UsmapPropertyType.MapProperty
            "SetProperty" -> UsmapPropertyType.SetProperty
            "FloatProperty" -> UsmapPropertyType.FloatProperty
            "DoubleProperty" -> UsmapPropertyType.DoubleProperty
            "ByteProperty" -> UsmapPropertyType.ByteProperty
            "UInt16Property" -> UsmapPropertyType.UInt16Property
            "UInt32Property" -> UsmapPropertyType.UInt32Property
            "UInt64Property" -> UsmapPropertyType.UInt64Property
            "Int8Property" -> UsmapPropertyType.Int8Property
            "Int16Property" -> UsmapPropertyType.Int16Property
            "IntProperty" -> UsmapPropertyType.IntProperty
            "Int64Property" -> UsmapPropertyType.Int64Property
            "ObjectProperty" -> UsmapPropertyType.ObjectProperty
            "ClassProperty" -> UsmapPropertyType.ObjectProperty
            "WeakObjectProperty" -> UsmapPropertyType.WeakObjectProperty
            "SoftObjectProperty" -> UsmapPropertyType.SoftObjectProperty
            "SoftClassProperty" -> UsmapPropertyType.SoftObjectProperty
            "LazyObjectProperty" -> UsmapPropertyType.LazyObjectProperty
            "InterfaceProperty" -> UsmapPropertyType.InterfaceProperty
            "FieldPathProperty" -> UsmapPropertyType.FieldPathProperty
            "OptionalProperty" -> UsmapPropertyType.OptionalProperty
            "FUtf8StrProperty" -> UsmapPropertyType.Utf8StrProperty
            "AnsiStrProperty" -> UsmapPropertyType.AnsiStrProperty
            else -> UsmapPropertyType.Unknown
        }
    }

    private fun SubstringAfterLast(value: String, substring: String): String {
        val index = value.lastIndexOf(substring)
        return if (index == -1) value else value.substring(index + substring.length)
    }

    private fun SetupUsmapProperty(jmapProp: JmapPropertyBase): UsmapPropertyData {
        val typEnum = UsmapPropertyTypeStringToEnum(jmapProp.Type)
        return when (typEnum) {
            UsmapPropertyType.EnumProperty -> {
                val enumProp = jmapProp as JmapEnumProperty
                UsmapEnumData().apply {
                    InnerType = SetupUsmapProperty(enumProp.Container!!)
                    Name = SubstringAfterLast(enumProp.Enum!!, ".")
                }
            }
            UsmapPropertyType.StructProperty -> UsmapStructData(SubstringAfterLast((jmapProp as JmapStructProperty).Struct!!, "."))
            UsmapPropertyType.SetProperty -> UsmapArrayData(typEnum).apply {
                InnerType = SetupUsmapProperty((jmapProp as JmapSetProperty).Key!!)
            }
            UsmapPropertyType.ArrayProperty -> UsmapArrayData(typEnum).apply {
                InnerType = SetupUsmapProperty((jmapProp as JmapArrayProperty).Inner!!)
            }
            UsmapPropertyType.OptionalProperty -> UsmapArrayData(typEnum).apply {
                InnerType = SetupUsmapProperty((jmapProp as JmapOptionalProperty).Inner!!)
            }
            UsmapPropertyType.MapProperty -> {
                val mapProp = jmapProp as JmapMapProperty
                UsmapMapData().apply {
                    InnerType = SetupUsmapProperty(mapProp.Key!!)
                    ValueType = SetupUsmapProperty(mapProp.Value!!)
                }
            }
            else -> UsmapPropertyData(typEnum)
        }
    }

    fun GetObjectBase(objectJSON: String): JmapObjectBase? {
        return JmapObjectConverter().Read(mapper.readTree(objectJSON))
    }

    fun GetObjectBase(objectJSON: ByteArray): JmapObjectBase? {
        return JmapObjectConverter().Read(mapper.readTree(objectJSON))
    }

    fun ReadSchema(objectBase: JmapObjectBase?, templateSchema: UsmapSchema) {
        if (objectBase is JmapStruct) {
            val str = objectBase
            val superStruct = str.SuperStruct
            val ind = superStruct?.lastIndexOf('.') ?: -1
            if (superStruct != null && ind > 0) {
                templateSchema.SuperType = superStruct.substring(ind + 1)
                templateSchema.SuperTypeModulePath = superStruct.substring(0, ind)
            } else {
                templateSchema.SuperType = superStruct
                templateSchema.SuperTypeModulePath = null
            }

            templateSchema.propertiesInternal = LinkedHashMap()
            var propIdx = 0
            for (jmapProp in str.Properties) {
                val usmapProp = UsmapProperty(jmapProp.Name, propIdx, 0, jmapProp.ArrayDim, null)
                usmapProp.PropertyData = SetupUsmapProperty(jmapProp)

                templateSchema.propertiesInternal[propIdx] = usmapProp
                for (i in 1 until jmapProp.ArrayDim) {
                    val cln = usmapProp.clone()
                    cln.SchemaIndex += i
                    cln.ArrayIndex = i
                    templateSchema.propertiesInternal[propIdx + i] = cln
                }
                propIdx += jmapProp.ArrayDim
            }
            templateSchema.ConstructPropertiesMap(false)
            templateSchema.PropCount = templateSchema.propertiesInternal.size
        }

        templateSchema.StructKind = when {
            objectBase is JmapClass -> UsmapStructKind.UClass
            objectBase is JmapScriptStruct -> UsmapStructKind.UScriptStruct
            else -> UsmapStructKind.None
        }
    }

    fun ReadSchema(objectJSON: ByteArray, templateSchema: UsmapSchema) {
        ReadSchema(GetObjectBase(objectJSON), templateSchema)
    }

    fun ReadSchema(objectJSON: String, templateSchema: UsmapSchema) {
        ReadSchema(GetObjectBase(objectJSON), templateSchema)
    }

    fun ReadEnum(objectBase: JmapObjectBase?, templateEnum: UsmapEnum) {
        if (objectBase is JmapEnum) {
            templateEnum.EnumFlags = 0
            templateEnum._Values = LinkedHashMap<Long, String?>().apply { putAll(objectBase.Values) }
        }
    }

    fun ReadEnum(objectJSON: ByteArray, templateEnum: UsmapEnum) {
        ReadEnum(GetObjectBase(objectJSON), templateEnum)
    }

    fun ReadEnum(objectJSON: String, templateEnum: UsmapEnum) {
        ReadEnum(GetObjectBase(objectJSON), templateEnum)
    }
}
