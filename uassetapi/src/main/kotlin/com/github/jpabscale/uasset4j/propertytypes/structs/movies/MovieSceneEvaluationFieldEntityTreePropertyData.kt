// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Movies/MovieSceneEvaluationFieldEntityTreePropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.movies

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.FString

class MovieSceneSubSectionFieldDataPropertyData : PropertyData {
    var Value: FMovieSceneSubSectionFieldData?
        get() = GetObject<FMovieSceneSubSectionFieldData>()
        set(v) = SetObject(v)

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = FMovieSceneSubSectionFieldData(reader)
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        return Value!!.Write(writer)
    }

    override fun CreateClone(): PropertyData = MovieSceneSubSectionFieldDataPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("MovieSceneSubSectionFieldData")
    }
}

class MovieSceneEvaluationFieldEntityTreePropertyData : PropertyData {
    var Value: FMovieSceneEvaluationFieldEntityTree?
        get() = GetObject<FMovieSceneEvaluationFieldEntityTree>()
        set(v) = SetObject(v)

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = FMovieSceneEvaluationFieldEntityTree(reader)
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        return Value!!.Write(writer)
    }

    override fun CreateClone(): PropertyData = MovieSceneEvaluationFieldEntityTreePropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("MovieSceneEvaluationFieldEntityTree")
    }
}

class MovieSceneSubSequenceTreePropertyData : PropertyData {
    var Value: FMovieSceneSubSequenceTree?
        get() = GetObject<FMovieSceneSubSequenceTree>()
        set(v) = SetObject(v)

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = FMovieSceneSubSequenceTree(reader)
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        return Value!!.Write(writer)
    }

    override fun CreateClone(): PropertyData = MovieSceneSubSequenceTreePropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("MovieSceneSubSequenceTree")
    }
}

class MovieSceneSequenceInstanceDataPtrPropertyData : PropertyData {
    var Value: FPackageIndex?
        get() = GetObject<FPackageIndex>()
        set(v) = SetObject(v)

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = FPackageIndex(reader)
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        writer.WriteInt32(Value?.Index ?: 0)
        return 4
    }

    override fun CreateClone(): PropertyData = MovieSceneSequenceInstanceDataPtrPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("MovieSceneSequenceInstanceDataPtr")
    }
}

class SectionEvaluationDataTreePropertyData : PropertyData {
    var Value: FSectionEvaluationDataTree?
        get() = GetObject<FSectionEvaluationDataTree>()
        set(v) = SetObject(v)

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = FSectionEvaluationDataTree(reader)
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        return Value!!.Write(writer)
    }

    override fun CreateClone(): PropertyData = SectionEvaluationDataTreePropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("SectionEvaluationDataTree")
    }
}

class MovieSceneTrackFieldDataPropertyData : PropertyData {
    var Value: FMovieSceneTrackFieldData?
        get() = GetObject<FMovieSceneTrackFieldData>()
        set(v) = SetObject(v)

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = FMovieSceneTrackFieldData(reader)
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        return Value!!.Write(writer)
    }

    override fun CreateClone(): PropertyData = MovieSceneTrackFieldDataPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("MovieSceneTrackFieldData")
    }
}
