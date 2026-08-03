// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Niagara/NiagaraVariablePropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.niagara

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAPUtils
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData
import com.github.jpabscale.uasset4j.unrealtypes.EngineVersion
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class NiagaraDataChannelVariablePropertyData : NiagaraVariableBasePropertyData {
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = NiagaraDataChannelVariablePropertyData()

    constructor(name: FName?, forcedType: FName?) : super(name, forcedType)
    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("NiagaraDataChannelVariable")
    }
}

open class NiagaraVariableBasePropertyData : StructPropertyData {
    var VariableName: FName? = null
    var TypeDef: StructPropertyData? = null

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (reader.Asset!!.GetEngineVersion() <= EngineVersion.VER_UE4_25) {
            StructType = FName.DefineDummy(reader.Asset, PropertyType)
            super.Read(reader, includeHeader, 1, 0, PropertySerializationContext.StructFallback)
            return
        }

        VariableName = reader.ReadFName()
        TypeDef = StructPropertyData(FName.DefineDummy(reader.Asset, "TypeDef"), FName.DefineDummy(reader.Asset, "NiagaraTypeDefinition"))
        TypeDef!!.Read(reader, false, 1, 0, serializationContext)
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (writer.Asset!!.GetEngineVersion() <= EngineVersion.VER_UE4_25) {
            StructType = FName.DefineDummy(writer.Asset, PropertyType)
            return super.Write(writer, includeHeader, PropertySerializationContext.StructFallback)
        }

        val offset = writer.position
        writer.Write(VariableName)
        if (TypeDef == null) TypeDef = StructPropertyData(FName.DefineDummy(writer.Asset, "TypeDef"), FName.DefineDummy(writer.Asset, "NiagaraTypeDefinition"))
        TypeDef!!.Write(writer, false)
        return writer.position - offset
    }

    override fun FromString(d: Array<String>, asset: UAsset) {
        VariableName = FName.FromString(asset, d[0])
    }

    constructor(name: FName?, forcedType: FName?) : super(name, forcedType)
    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("NiagaraVariableBase")
    }
}

class NiagaraVariablePropertyData : NiagaraVariableBasePropertyData {
    var VarData: ByteArray? = null

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        super.Read(reader, includeHeader, leng1, leng2, serializationContext)

        if (reader.Asset!!.GetEngineVersion() >= EngineVersion.VER_UE4_26) {
            val varDataSize = reader.ReadInt32()
            VarData = reader.ReadBytes(varDataSize)
        }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        var sz = super.Write(writer, includeHeader, serializationContext)
        if (writer.Asset!!.GetEngineVersion() <= EngineVersion.VER_UE4_25) return sz

        if (VarData == null) VarData = ByteArray(0)
        writer.WriteInt32(VarData!!.size); sz += 4
        writer.WriteBytes(VarData!!); sz += VarData!!.size
        return sz
    }

    override fun FromString(d: Array<String>, asset: UAsset) {
        super.FromString(d, asset)
        VarData = UAPUtils.ConvertStringToByteArray(d[2])
    }

    override fun CreateClone(): PropertyData = NiagaraVariablePropertyData()

    constructor(name: FName?, forcedType: FName?) : super(name, forcedType)
    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("NiagaraVariable")
    }
}

class NiagaraVariableWithOffsetPropertyData : NiagaraVariableBasePropertyData {
    var VariableOffset: Int = 0

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        super.Read(reader, includeHeader, leng1, leng2, serializationContext)

        if (reader.Asset!!.GetEngineVersion() >= EngineVersion.VER_UE4_26) {
            VariableOffset = reader.ReadInt32()
        }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        var sz = super.Write(writer, includeHeader, PropertySerializationContext.Normal)
        if (writer.Asset!!.GetEngineVersion() <= EngineVersion.VER_UE4_25) return sz

        writer.WriteInt32(VariableOffset)
        sz += 4
        return sz
    }

    override fun FromString(d: Array<String>, asset: UAsset) {
        super.FromString(d, asset)
        VariableOffset = 0
        d[2].toIntOrNull()?.let { VariableOffset = it }
    }

    override fun CreateClone(): PropertyData = NiagaraVariableWithOffsetPropertyData()

    constructor(name: FName?, forcedType: FName?) : super(name, forcedType)
    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("NiagaraVariableWithOffset")
    }
}
