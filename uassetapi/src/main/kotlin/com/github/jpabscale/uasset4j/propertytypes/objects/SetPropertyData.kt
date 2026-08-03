// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/SetPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.FormatException
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5
import com.github.jpabscale.uasset4j.unversioned.UsmapArrayData

class SetPropertyData : ArrayPropertyData {
    var ElementsToRemove: List<PropertyData>? = null

    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        ShouldSerializeStructsDifferently = false

        if (includeHeader && !reader.Asset!!.HasUnversionedProperties) {
            if (reader.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
                if (PropertyTypeName == null) throw FormatException("PropertyTypeName is required to read SetProperty with complete type names.")
                ArrayType = PropertyTypeName!!.GetParameter(0).GetName()
            } else {
                ArrayType = reader.ReadFName()
            }
            this.ReadEndPropertyTag(reader)
        }

        val mappings = reader.Asset!!.Mappings
        if (mappings != null && ArrayType == null) {
            val strucDat1 = mappings.TryGetPropertyData(Name, Ancestry, reader.Asset!!) as? UsmapArrayData
            if (strucDat1 != null) {
                ArrayType = FName.DefineDummy(reader.Asset, strucDat1.InnerType!!.Type.toString())
            }
        }

        if (reader.Asset!!.HasUnversionedProperties && ArrayType == null) {
            throw IllegalStateException("Unable to determine array type for array " + (Name?.Value?.Value ?: "") + " in class " + (Ancestry.Parent?.Value?.Value ?: ""))
        }

        val removedItemsDummy = ArrayPropertyData(FName.DefineDummy(reader.Asset, "ElementsToRemove"))
        removedItemsDummy.Ancestry.Initialize(Ancestry, Name)
        removedItemsDummy.ShouldSerializeStructsDifferently = false
        removedItemsDummy.ArrayType = ArrayType
        removedItemsDummy.PropertyTypeName = PropertyTypeName
        removedItemsDummy.Read(reader, false, leng1, leng2)
        ElementsToRemove = removedItemsDummy.Value

        super.Read(reader, false, leng1 - 4, leng2, PropertySerializationContext.Normal)
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        ShouldSerializeStructsDifferently = false

        if ((Value?.size ?: 0) > 0) ArrayType = if (writer.Asset!!.HasUnversionedProperties) FName.DefineDummy(writer.Asset, Value!![0].PropertyType!!) else FName(writer.Asset!!, Value!![0].PropertyType!!)

        if (includeHeader && !writer.Asset!!.HasUnversionedProperties) {
            if (writer.Asset!!.ObjectVersionUE5 < ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
                writer.Write(ArrayType)
            }
            this.WriteEndPropertyTag(writer)
        }

        val removedItemsDummy = ArrayPropertyData(FName.DefineDummy(writer.Asset, "ElementsToRemove"))
        removedItemsDummy.ShouldSerializeStructsDifferently = false
        removedItemsDummy.ArrayType = ArrayType
        removedItemsDummy.Value = ElementsToRemove

        val leng1 = removedItemsDummy.Write(writer, false)
        return leng1 + super.Write(writer, false, PropertySerializationContext.Normal)
    }

    override fun CloneInto(res: PropertyData) {
        super.CloneInto(res)
        val cloningProperty = res as SetPropertyData

        val v = this.Value
        if (this.ElementsToRemove != null) {
            val newData = mutableListOf<PropertyData>()
            for (i in 0 until (v?.size ?: 0)) {
                newData.add(v!![i].clone())
            }
            cloningProperty.ElementsToRemove = newData
        } else {
            cloningProperty.ElementsToRemove = null
        }
    }

    override fun CreateClone(): PropertyData = SetPropertyData()

    constructor(name: FName?) : super(name) {
        Value = emptyList()
        ElementsToRemove = emptyList()
    }

    constructor() : super() {
        Value = emptyList()
        ElementsToRemove = emptyList()
    }

    companion object {
        private val CurrentPropertyType = FString("SetProperty")
    }
}
