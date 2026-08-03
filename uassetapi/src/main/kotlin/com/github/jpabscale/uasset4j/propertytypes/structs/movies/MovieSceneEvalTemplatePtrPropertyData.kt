// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Movies/MovieSceneEvalTemplatePtrPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.movies

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.FormatException
import com.github.jpabscale.uasset4j.MainSerializer
import com.github.jpabscale.uasset4j.propertytypes.objects.StrPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

open class MovieSceneTemplatePropertyData : StructPropertyData {
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        val type = StrPropertyData(FName.DefineDummy(reader.Asset, "TypeName"))
        type.Ancestry.Initialize(Ancestry, Name)
        type.Read(reader, includeHeader, leng1)

        if (type.Value != null) {
            StructType = FName.DefineDummy(reader.Asset, type.Value.toString().split(".")[1])
            super.Read(reader, false, 1, 0, PropertySerializationContext.StructFallback)
        }

        Value?.add(0, type)
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        val offset = writer.position

        if (Value != null) {
            val type = Value!!.firstOrNull { it.Name.toString() == "TypeName" } as? StrPropertyData
            if (type == null) throw FormatException("TypeName property not found in $PropertyType")
            writer.Write(type.Value)
            if (type.Value != null) {
                val dat = Value!!.filter { it !== type }.toMutableList()
                MainSerializer.GenerateUnversionedHeader(dat, Name, null, writer.Asset!!)?.Write(writer)

                for (t in dat) {
                    MainSerializer.Write(t, writer, true)
                }
                if (!writer.Asset!!.HasUnversionedProperties) writer.Write(FName(writer.Asset, "None"))
            }
        }

        return writer.position - offset
    }

    override fun CreateClone(): PropertyData = MovieSceneTemplatePropertyData()

    constructor(name: FName?, forcedType: FName?) : super(name, forcedType)
    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("MovieSceneTemplate")
    }
}

class MovieSceneEvalTemplatePtrPropertyData : MovieSceneTemplatePropertyData {
    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = MovieSceneEvalTemplatePtrPropertyData()

    constructor(name: FName?, forcedType: FName?) : super(name, forcedType)
    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("MovieSceneEvalTemplatePtr")
    }
}

class MovieSceneTrackImplementationPtrPropertyData : MovieSceneTemplatePropertyData {
    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = MovieSceneTrackImplementationPtrPropertyData()

    constructor(name: FName?, forcedType: FName?) : super(name, forcedType)
    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("MovieSceneTrackImplementationPtr")
    }
}
