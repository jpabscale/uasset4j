// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Niagara/NiagaraDataInterfaceGPUParamInfoPropertyData.cs
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Engine/Niagara/FNiagaraDataInterfaceGPUParamInfo.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.niagara

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.customversions.FNiagaraCustomVersion
import com.github.jpabscale.uasset4j.propertytypes.objects.BasePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.StructAccessors
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.FString

class FNiagaraVariableCommonReference(reader: AssetBinaryReader) {
    var Name: FName? = reader.ReadFName()
    var Type: FPackageIndex? = FPackageIndex(reader)

    fun Write(writer: AssetBinaryWriter) {
        writer.Write(Name)
        Type!!.Write(writer)
    }
}

class FNiagaraDataInterfaceGeneratedFunction {
    var DefinitionName: FName? = null
    var InstanceName: FString? = null
    var Specifiers: Array<Pair<FName, FName>> = emptyArray()
    var VariadicInputs: Array<FNiagaraVariableCommonReference> = emptyArray()
    var VariadicOutputs: Array<FNiagaraVariableCommonReference> = emptyArray()
    var MiscUsageBitMask: Int = 0

    constructor()

    constructor(reader: AssetBinaryReader) {
        DefinitionName = reader.ReadFName()
        InstanceName = reader.ReadFString()

        Specifiers = reader.ReadArray { reader.ReadFName() to reader.ReadFName() }

        if (reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FNiagaraCustomVersion")) >=
            FNiagaraCustomVersion.AddVariadicParametersToGPUFunctionInfo.value
        ) {
            VariadicInputs = reader.ReadArray { FNiagaraVariableCommonReference(reader) }
            VariadicOutputs = reader.ReadArray { FNiagaraVariableCommonReference(reader) }
        }

        if (reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FNiagaraCustomVersion")) >=
            FNiagaraCustomVersion.SerializeUsageBitMaskToGPUFunctionInfo.value
        ) {
            MiscUsageBitMask = reader.ReadUInt16()
        }
    }

    fun Write(writer: AssetBinaryWriter) {
        writer.Write(DefinitionName)
        writer.Write(InstanceName)

        writer.WriteInt32(Specifiers.size)
        for (spec in Specifiers) {
            writer.Write(spec.first)
            writer.Write(spec.second)
        }

        if (writer.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FNiagaraCustomVersion")) >=
            FNiagaraCustomVersion.AddVariadicParametersToGPUFunctionInfo.value
        ) {
            writer.WriteInt32(VariadicInputs.size)
            for (input in VariadicInputs) {
                input.Write(writer)
            }

            writer.WriteInt32(VariadicOutputs.size)
            for (output in VariadicOutputs) {
                output.Write(writer)
            }
        }

        if (writer.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FNiagaraCustomVersion")) >=
            FNiagaraCustomVersion.SerializeUsageBitMaskToGPUFunctionInfo.value
        ) {
            writer.WriteUInt16(MiscUsageBitMask)
        }
    }
}

class FNiagaraDataInterfaceGPUParamInfo {
    var DataInterfaceHLSLSymbol: FString? = null
    var DIClassName: FString? = null
    var GeneratedFunctions: Array<FNiagaraDataInterfaceGeneratedFunction> = emptyArray()

    constructor()

    constructor(reader: AssetBinaryReader) {
        DataInterfaceHLSLSymbol = reader.ReadFString()
        DIClassName = reader.ReadFString()

        if (reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FNiagaraCustomVersion")) >=
            FNiagaraCustomVersion.AddGeneratedFunctionsToGPUParamInfo.value
        ) {
            GeneratedFunctions = reader.ReadArray { FNiagaraDataInterfaceGeneratedFunction(reader) }
        }
    }

    fun Write(writer: AssetBinaryWriter): Int {
        val offset = writer.position
        writer.Write(DataInterfaceHLSLSymbol)
        writer.Write(DIClassName)
        if (writer.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FNiagaraCustomVersion")) >=
            FNiagaraCustomVersion.AddGeneratedFunctionsToGPUParamInfo.value
        ) {
            writer.WriteInt32(GeneratedFunctions.size)
            for (func in GeneratedFunctions) {
                func.Write(writer)
            }
        }

        return writer.position - offset
    }

    companion object {
        fun Read(reader: AssetBinaryReader): FNiagaraDataInterfaceGPUParamInfo = FNiagaraDataInterfaceGPUParamInfo(reader)

        fun FromString(d: Array<String>, asset: com.github.jpabscale.uasset4j.UAsset): FNiagaraDataInterfaceGPUParamInfo =
            throw NotImplementedError("FNiagaraDataInterfaceGPUParamInfo.FromString")

        val accessors = StructAccessors<FNiagaraDataInterfaceGPUParamInfo>(
            read = { r -> FNiagaraDataInterfaceGPUParamInfo(r) },
            fromString = { d, a -> FromString(d, a) },
            write = { w, v -> v.Write(w) },
            defaultValue = { FNiagaraDataInterfaceGPUParamInfo() },
        )
    }
}

class NiagaraDataInterfaceGPUParamInfoPropertyData() : BasePropertyData<FNiagaraDataInterfaceGPUParamInfo>(FNiagaraDataInterfaceGPUParamInfo.accessors) {
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = NiagaraDataInterfaceGPUParamInfoPropertyData()

    constructor(name: FName?) : this() {
        this.Name = name
    }

    companion object {
        private val CurrentPropertyType = FString("NiagaraDataInterfaceGPUParamInfo")
    }
}
