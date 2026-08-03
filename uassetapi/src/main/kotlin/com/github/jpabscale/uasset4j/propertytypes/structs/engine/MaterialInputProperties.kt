// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Engine/MaterialInputProperties.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.engine

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.customversions.FCoreObjectVersion
import com.github.jpabscale.uasset4j.customversions.FFrameworkObjectVersion
import com.github.jpabscale.uasset4j.propertytypes.objects.AncestryInfo
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.propertytypes.structs.core.ColorPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.Vector2DPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.VectorPropertyData
import com.github.jpabscale.uasset4j.unrealtypes.EngineVersion
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.FString

abstract class MaterialInputPropertyData<T> : PropertyData {
    var Expression: FPackageIndex? = null
    var OutputIndex: Int = 0
    var InputName: FName? = null
    var InputNameOld: FString? = null
    var Mask: Int = 0
    var MaskR: Int = 0
    var MaskG: Int = 0
    var MaskB: Int = 0
    var MaskA: Int = 0
    var ExpressionName: FName? = null

    var Value: T?
        get() = GetObject<T>()
        set(v) = SetObject(v)

    constructor()

    constructor(name: FName?) : super(name)

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        if ((reader.Asset!!.GetEngineVersion().ordinal <= EngineVersion.VER_UE5_1.ordinal && !reader.Asset!!.IsFilterEditorOnly) ||
            reader.Asset!!.GetEngineVersion().ordinal >= EngineVersion.VER_UE5_1.ordinal
        ) {
            Expression = reader.XFERPTR()
        }
        OutputIndex = reader.ReadInt32()
        InputName = if (reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFrameworkObjectVersion")) >=
            FFrameworkObjectVersion.PinsStoreFName.ordinal
        ) {
            reader.ReadFName()
        } else {
            null
        }
        InputNameOld = if (reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFrameworkObjectVersion")) >=
            FFrameworkObjectVersion.PinsStoreFName.ordinal
        ) {
            null
        } else {
            reader.ReadFString()
        }
        Mask = reader.ReadInt32()
        MaskR = reader.ReadInt32()
        MaskG = reader.ReadInt32()
        MaskB = reader.ReadInt32()
        MaskA = reader.ReadInt32()
        ExpressionName = if (reader.Asset!!.GetEngineVersion().ordinal <= EngineVersion.VER_UE5_1.ordinal &&
            reader.Asset!!.IsFilterEditorOnly
        ) {
            reader.ReadFName()
        } else {
            null
        }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        return WriteExpressionInput(writer, false)
    }

    protected fun WriteExpressionInput(writer: AssetBinaryWriter, includeHeader: Boolean): Int {
        var totalSize = 0
        if (writer.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FCoreObjectVersion")) >=
            FCoreObjectVersion.MaterialInputNativeSerialize.ordinal
        ) {
            if ((writer.Asset!!.GetEngineVersion().ordinal <= EngineVersion.VER_UE5_1.ordinal && !writer.Asset!!.IsFilterEditorOnly) ||
                writer.Asset!!.GetEngineVersion().ordinal >= EngineVersion.VER_UE5_1.ordinal
            ) {
                writer.XFERPTR(Expression)
                totalSize += 4
            }

            writer.WriteInt32(OutputIndex); totalSize += 4
            if (writer.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFrameworkObjectVersion")) >=
                FFrameworkObjectVersion.PinsStoreFName.ordinal
            ) {
                writer.Write(InputName); totalSize += 8
            } else {
                totalSize += writer.Write(InputNameOld)
            }
            writer.WriteInt32(Mask)
            writer.WriteInt32(MaskR)
            writer.WriteInt32(MaskG)
            writer.WriteInt32(MaskB)
            writer.WriteInt32(MaskA)
            totalSize += 4 * 5
            if (writer.Asset!!.GetEngineVersion().ordinal <= EngineVersion.VER_UE5_1.ordinal &&
                writer.Asset!!.IsFilterEditorOnly
            ) {
                writer.Write(ExpressionName)
                totalSize += 8
            }
        }
        return totalSize
    }

    override fun CloneInto(res: PropertyData) {
        super.CloneInto(res)
        val cloningProperty = res as MaterialInputPropertyData<*>
        cloningProperty.InputName = this.InputName?.clone()
        cloningProperty.InputNameOld = this.InputNameOld?.clone()
        cloningProperty.ExpressionName = this.ExpressionName?.clone()
    }
}

class ExpressionInputPropertyData : MaterialInputPropertyData<Int> {
    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType
    override val DefaultValue: Any get() = 0

    override fun CreateClone(): PropertyData = ExpressionInputPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("ExpressionInput")
    }
}

class MaterialAttributesInputPropertyData : MaterialInputPropertyData<Int> {
    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = MaterialAttributesInputPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("MaterialAttributesInput")
    }
}

class ColorMaterialInputPropertyData : MaterialInputPropertyData<ColorPropertyData> {
    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        super.Read(reader, includeHeader, leng1, leng2, serializationContext)

        reader.ReadInt32()
        Value = ColorPropertyData(Name)
        Value!!.Ancestry.Initialize(Ancestry, Name)
        Value!!.Read(reader, false, 0)
    }

    override fun ResolveAncestries(asset: UAsset, ancestrySoFar: AncestryInfo) {
        val ancestryNew = ancestrySoFar.clone()
        ancestryNew.SetAsParent(Name)

        Value!!.ResolveAncestries(asset, ancestryNew)
        super.ResolveAncestries(asset, ancestrySoFar)
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        val expLength = super.Write(writer, includeHeader, serializationContext)
        writer.WriteInt32(0)
        return expLength + Value!!.Write(writer, false) + 4
    }

    override fun CreateClone(): PropertyData = ColorMaterialInputPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("ColorMaterialInput")
    }
}

class ScalarMaterialInputPropertyData : MaterialInputPropertyData<Float> {
    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType
    override val DefaultValue: Any get() = 0f

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        super.Read(reader, includeHeader, leng1, leng2, serializationContext)

        reader.ReadInt32()
        Value = reader.ReadSingle()
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        val expLength = WriteExpressionInput(writer, false)
        writer.WriteInt32(0)
        writer.WriteSingle(Value ?: 0f)
        return expLength + 4 + 4
    }

    override fun CreateClone(): PropertyData = ScalarMaterialInputPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("ScalarMaterialInput")
    }
}

class VectorMaterialInputPropertyData : MaterialInputPropertyData<VectorPropertyData> {
    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        super.Read(reader, includeHeader, leng1, leng2, serializationContext)

        reader.ReadInt32()
        Value = VectorPropertyData(Name)
        Value!!.Ancestry.Initialize(Ancestry, Name)
        Value!!.Read(reader, false, 0)
    }

    override fun ResolveAncestries(asset: UAsset, ancestrySoFar: AncestryInfo) {
        val ancestryNew = ancestrySoFar.clone()
        ancestryNew.SetAsParent(Name)

        Value!!.ResolveAncestries(asset, ancestryNew)
        super.ResolveAncestries(asset, ancestrySoFar)
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        val expLength = WriteExpressionInput(writer, false)
        writer.WriteInt32(0)
        return expLength + Value!!.Write(writer, false) + 4
    }

    override fun CreateClone(): PropertyData = VectorMaterialInputPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("VectorMaterialInput")
    }
}

class Vector2MaterialInputPropertyData : MaterialInputPropertyData<Vector2DPropertyData> {
    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        super.Read(reader, includeHeader, leng1, leng2, serializationContext)

        reader.ReadInt32()
        Value = Vector2DPropertyData(Name)
        Value!!.Ancestry.Initialize(Ancestry, Name)
        Value!!.Read(reader, false, 0)
    }

    override fun ResolveAncestries(asset: UAsset, ancestrySoFar: AncestryInfo) {
        val ancestryNew = ancestrySoFar.clone()
        ancestryNew.SetAsParent(Name)

        Value!!.ResolveAncestries(asset, ancestryNew)
        super.ResolveAncestries(asset, ancestrySoFar)
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        val expLength = WriteExpressionInput(writer, false)
        writer.WriteInt32(0)
        return expLength + Value!!.Write(writer, false) + 4
    }

    override fun CreateClone(): PropertyData = Vector2MaterialInputPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("Vector2MaterialInput")
    }
}
