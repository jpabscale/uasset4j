// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/ExportTypes/StructExport.cs
package com.github.jpabscale.uasset4j.exporttypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomSerializationFlags
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.MainSerializer
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.customversions.FCoreObjectVersion
import com.github.jpabscale.uasset4j.customversions.FFrameworkObjectVersion
import com.github.jpabscale.uasset4j.fieldtypes.FProperty
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex

/**
 * Base export for all UObject types that contain fields.
 */
open class StructExport : FieldExport {
    /** Struct this inherits from, may be null. */
    var SuperStruct: FPackageIndex? = null

    /** List of child fields. */
    var Children: Array<FPackageIndex>? = null

    /** Properties serialized with this struct definition. */
    var LoadedProperties: Array<FProperty>? = null

    /** The bytecode instructions contained within this struct. */
    var ScriptBytecode: Array<KismetExpression>? = null

    /** Bytecode size in total in deserialized memory. Filled out in lieu of [ScriptBytecode] if an error occurs during bytecode parsing. */
    var ScriptBytecodeSize: Int = 0

    /** Raw binary bytecode data. Filled out in lieu of [ScriptBytecode] if an error occurs during bytecode parsing. */
    var ScriptBytecodeRaw: ByteArray? = null

    constructor(superExport: Export) : super(superExport)

    constructor(asset: UAsset?, extras: ByteArray?) : super(asset, extras)

    constructor() : super()

    override fun Read(reader: AssetBinaryReader, nextStarting: Int) {
        super.Read(reader, nextStarting)

        SuperStruct = FPackageIndex(reader.ReadInt32())

        if (Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFrameworkObjectVersion")) < FFrameworkObjectVersion.RemoveUField_Next.ordinal) {
            val firstChild = FPackageIndex(reader.ReadInt32())
            Children = if (firstChild.IsNull()) emptyArray() else arrayOf(firstChild)
        } else {
            val numIndexEntries = reader.ReadInt32()
            Children = Array(numIndexEntries) { FPackageIndex(reader.ReadInt32()) }
        }

        if (Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FCoreObjectVersion")) >= FCoreObjectVersion.FProperties.ordinal) {
            val numProps = reader.ReadInt32()
            LoadedProperties = Array(numProps) { MainSerializer.ReadFProperty(reader) }
        } else {
            LoadedProperties = emptyArray()
        }

        ScriptBytecodeSize = reader.ReadInt32() // # of bytes in total in deserialized memory
        val scriptStorageSize = reader.ReadInt32() // # of bytes in total
        val startedReading = reader.position.toLong()

        var willParseRaw = true
        try {
            if (Asset!!.CustomSerializationFlags != CustomSerializationFlags.SkipParsingBytecode) {
                val tempCode = mutableListOf<KismetExpression>()
                while ((reader.position - startedReading) < scriptStorageSize) {
                    tempCode.add(ExpressionSerializer.ReadExpression(reader))
                }
                ScriptBytecode = tempCode.toTypedArray()
                willParseRaw = false
            }
        } catch (ex: Throwable) {
        }

        if (willParseRaw) {
            reader.position = startedReading.toInt()
            ScriptBytecode = null
            ScriptBytecodeRaw = reader.ReadBytes(scriptStorageSize)
        }
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)

        writer.WriteInt32(SuperStruct?.Index ?: 0)

        if (Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFrameworkObjectVersion")) < FFrameworkObjectVersion.RemoveUField_Next.ordinal) {
            if (Children!!.isEmpty()) {
                writer.WriteInt32(0)
            } else {
                writer.WriteInt32(Children!![0].Index)
            }
        } else {
            writer.WriteInt32(Children!!.size)
            for (i in Children!!.indices) {
                writer.WriteInt32(Children!![i].Index)
            }
        }

        if (Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FCoreObjectVersion")) >= FCoreObjectVersion.FProperties.ordinal) {
            writer.WriteInt32(LoadedProperties!!.size)
            for (i in LoadedProperties!!.indices) {
                MainSerializer.WriteFProperty(LoadedProperties!![i], writer)
            }
        }

        if (ScriptBytecode == null) {
            writer.WriteInt32(ScriptBytecodeSize)
            writer.WriteInt32(ScriptBytecodeRaw!!.size)
            writer.WriteBytes(ScriptBytecodeRaw!!)
        } else {
            val lengthOffset1 = writer.position
            writer.WriteInt32(0) // total iCode offset; to be filled out after serialization
            val lengthOffset2 = writer.position
            writer.WriteInt32(0) // size on disk; to be filled out after serialization

            var totalICodeOffset = 0
            val startMetric = writer.position
            for (i in ScriptBytecode!!.indices) {
                totalICodeOffset += ExpressionSerializer.WriteExpression(ScriptBytecode!![i], writer)
            }
            val endMetric = writer.position

            val totalLength = endMetric - startMetric
            val here = writer.position
            writer.position = lengthOffset1
            writer.WriteInt32(totalICodeOffset)
            writer.position = lengthOffset2
            writer.WriteInt32(totalLength)
            writer.position = here
        }
    }
}
