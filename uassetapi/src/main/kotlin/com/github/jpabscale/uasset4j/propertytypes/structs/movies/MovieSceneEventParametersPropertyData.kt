// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Movies/MovieSceneEventParametersPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.movies

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.propertytypes.objects.FSoftObjectPath
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class FMovieSceneEventParameters {
    var StructType: FSoftObjectPath? = null
    var StructBytes: ByteArray = ByteArray(0)

    constructor(structType: FSoftObjectPath?, structBytes: ByteArray) {
        StructType = structType
        StructBytes = structBytes
    }

    constructor(reader: AssetBinaryReader) {
        StructType = FSoftObjectPath(reader)
        val length = reader.ReadInt32()
        StructBytes = reader.ReadBytes(length)
    }

    fun Write(writer: AssetBinaryWriter): Int {
        var size = StructType!!.Write(writer)
        writer.WriteInt32(StructBytes.size)
        size += 4
        writer.WriteBytes(StructBytes)
        size += StructBytes.size
        return size
    }
}

class MovieSceneEventParametersPropertyData : PropertyData {
    var Value: FMovieSceneEventParameters?
        get() = GetObject<FMovieSceneEventParameters>()
        set(v) = SetObject(v)

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = FMovieSceneEventParameters(reader)
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        return Value!!.Write(writer)
    }

    override fun CreateClone(): PropertyData = MovieSceneEventParametersPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("MovieSceneEventParameters")
    }
}
