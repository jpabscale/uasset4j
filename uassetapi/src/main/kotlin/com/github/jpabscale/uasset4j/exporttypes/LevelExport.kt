// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/ExportTypes/LevelExport.cs
package com.github.jpabscale.uasset4j.exporttypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.customversions.FReleaseObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.FString

/**
 * URL structure.
 */
class FURL {
    // Protocol, i.e. "unreal" or "http".
    var Protocol: FString? = null
    // Optional hostname, i.e. "204.157.115.40" or "unreal.epicgames.com", blank if local.
    var Host: FString? = null
    // Optional host port.
    var Port: Int = 0
    var Valid: Int = 0
    // Map name, i.e. "SkyCity", default is "Entry".
    var Map: FString? = null
    // Options.
    var Op: MutableList<FString?> = mutableListOf()
    // Portal to enter through, default is "".
    var Portal: FString? = null

    constructor() : this(reader = null)

    constructor(reader: AssetBinaryReader?) {
        if (reader == null) return
        Protocol = reader.ReadFString()
        Host = reader.ReadFString()
        Map = reader.ReadFString()
        Portal = reader.ReadFString()
        val len = reader.ReadInt32()
        Op = mutableListOf()
        for (i in 0 until len) {
            Op.add(reader.ReadFString())
        }
        Port = reader.ReadInt32()
        Valid = reader.ReadInt32()
    }

    fun Write(writer: AssetBinaryWriter): Int {
        val offset = writer.position
        writer.Write(Protocol)
        writer.Write(Host)
        writer.Write(Map)
        writer.Write(Portal)
        writer.WriteInt32(Op.size)
        for (i in Op.indices) {
            writer.Write(Op[i])
        }
        writer.WriteInt32(Port)
        writer.WriteInt32(Valid)
        return writer.position - offset
    }
}

class LevelExport : NormalExport {
    // Owner of TTransArray<AActor> Actors
    var Owner: FPackageIndex? = null
    var Actors: MutableList<FPackageIndex> = mutableListOf()
    var URL: FURL? = null
    var Model: FPackageIndex? = null
    var ModelComponents: MutableList<FPackageIndex> = mutableListOf()
    var LevelScriptActor: FPackageIndex? = null
    var NavListStart: FPackageIndex? = null
    var NavListEnd: FPackageIndex? = null

    constructor(superExport: Export) : super(superExport)

    constructor(asset: UAsset?, extras: ByteArray?) : super(asset, extras)

    constructor() : super()

    override fun Read(reader: AssetBinaryReader, nextStarting: Int) {
        super.Read(reader, nextStarting)

        if (reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FReleaseObjectVersion")) < FReleaseObjectVersion.LevelTransArrayConvertedToTArray.ordinal)
            Owner = FPackageIndex(reader)

        val numIndexEntries = reader.ReadInt32()

        Actors = mutableListOf()
        for (i in 0 until numIndexEntries) {
            Actors.add(FPackageIndex(reader))
        }

        URL = FURL(reader)

        Model = FPackageIndex(reader)
        val numModelEntries = reader.ReadInt32()

        ModelComponents = mutableListOf()
        for (i in 0 until numModelEntries) {
            ModelComponents.add(FPackageIndex(reader))
        }

        LevelScriptActor = FPackageIndex(reader)
        NavListStart = FPackageIndex(reader)
        NavListEnd = FPackageIndex(reader)
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)

        if (writer.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FReleaseObjectVersion")) < FReleaseObjectVersion.LevelTransArrayConvertedToTArray.ordinal)
            Owner!!.Write(writer)

        writer.WriteInt32(Actors.size)
        for (i in Actors.indices) {
            Actors[i].Write(writer)
        }

        URL!!.Write(writer)

        Model!!.Write(writer)
        writer.WriteInt32(ModelComponents.size)
        for (i in ModelComponents.indices) {
            ModelComponents[i].Write(writer)
        }

        LevelScriptActor!!.Write(writer)
        NavListStart!!.Write(writer)
        NavListEnd!!.Write(writer)
    }
}
