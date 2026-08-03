// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/LevelSequenceObjectReferenceMapPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.unrealtypes.FGuid
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class FLevelSequenceLegacyObjectReference {
    var ObjectId: FGuid
    var ObjectPath: FString?

    constructor(objectId: FGuid, objectPath: FString?) {
        ObjectId = objectId
        ObjectPath = objectPath
    }

    constructor(reader: AssetBinaryReader) {
        ObjectId = reader.ReadGuid()
        ObjectPath = reader.ReadFString()
    }

    fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteGuid(ObjectId)
        var size = 16
        size += writer.Write(ObjectPath)
        return size
    }
}

class LevelSequenceObjectReferenceMapPropertyData : PropertyData {
    var Value: LinkedHashMap<FGuid, FLevelSequenceLegacyObjectReference>?
        get() = GetObject<LinkedHashMap<FGuid, FLevelSequenceLegacyObjectReference>>()
        set(v) = SetObject(v)

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        val num = reader.ReadInt32()
        Value = LinkedHashMap()
        for (i in 0 until num) {
            Value!![reader.ReadGuid()] = FLevelSequenceLegacyObjectReference(reader)
        }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        val offset = writer.position

        if (Value == null) Value = LinkedHashMap()
        writer.WriteInt32(Value!!.size)

        for ((key, value) in Value!!) {
            writer.WriteGuid(key)
            value.Write(writer)
        }

        return writer.position - offset
    }

    override fun CreateClone(): PropertyData = LevelSequenceObjectReferenceMapPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("LevelSequenceObjectReferenceMap")
    }
}
