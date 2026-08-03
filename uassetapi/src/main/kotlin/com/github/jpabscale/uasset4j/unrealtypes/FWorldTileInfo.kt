// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/FWorldTileInfo.cs
package com.github.jpabscale.uasset4j.unrealtypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.customversions.FFortniteMainBranchObjectVersion
import com.github.jpabscale.uasset4j.propertytypes.objects.AncestryInfo
import com.github.jpabscale.uasset4j.propertytypes.structs.math.BoxPropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.math.IntPointPropertyData
import com.github.jpabscale.uasset4j.util.Out

/** World layer information for tile tagging. */
class FWorldTileLayer {
    var Name: FString? = null
    var Reserved0: Int = 0
    var Reserved1: IntPointPropertyData? = null
    var StreamingDistance: Int = 0
    var DistanceStreamingEnabled: Boolean = false

    fun Read(reader: AssetBinaryReader, asset: UAsset) {
        Name = reader.ReadFString()
        Reserved0 = reader.ReadInt32()
        Reserved1 = IntPointPropertyData(FName.DefineDummy(asset, "Reserved1"))
        val pcen2 = Out<FName?>()
        Reserved1!!.Ancestry.Initialize(null, reader.Asset!!.GetParentClassExportName(pcen2), pcen2.value)
        Reserved1!!.Read(reader, false, 0, 0)

        if (asset.ObjectVersion >= ObjectVersion.VER_UE4_WORLD_LEVEL_INFO_UPDATED) {
            StreamingDistance = reader.ReadInt32()
        }

        if (asset.ObjectVersion >= ObjectVersion.VER_UE4_WORLD_LAYER_ENABLE_DISTANCE_STREAMING) {
            DistanceStreamingEnabled = reader.ReadInt32() == 1
        }
    }

    fun ResolveAncestries(asset: UAsset, ancestrySoFar: AncestryInfo) {
        val ancestryNew = ancestrySoFar.clone()
        ancestryNew.SetAsParent(FName.DefineDummy(asset, "WorldTileLayer"))

        Reserved1!!.ResolveAncestries(asset, ancestryNew)
    }

    fun Write(writer: AssetBinaryWriter, asset: UAsset) {
        writer.Write(this.Name)
        writer.WriteInt32(Reserved0)
        Reserved1!!.Write(writer, false)

        if (asset.ObjectVersion >= ObjectVersion.VER_UE4_WORLD_LEVEL_INFO_UPDATED) {
            writer.WriteInt32(StreamingDistance)
        }

        if (asset.ObjectVersion >= ObjectVersion.VER_UE4_WORLD_LAYER_ENABLE_DISTANCE_STREAMING) {
            writer.WriteBooleanInt(DistanceStreamingEnabled)
        }
    }

    constructor(name: FString?, reserved0: Int, reserved1: IntPointPropertyData?, streamingDistance: Int, distanceStreamingEnabled: Boolean) {
        Name = name
        Reserved0 = reserved0
        Reserved1 = reserved1
        StreamingDistance = streamingDistance
        DistanceStreamingEnabled = distanceStreamingEnabled
    }

    constructor()
}

/** Describes a LOD entry in a world tile. */
class FWorldTileLODInfo {
    var RelativeStreamingDistance: Int = 0
    var Reserved0: Float = 0F
    var Reserved1: Float = 0F
    var Reserved2: Int = 0
    var Reserved3: Int = 0

    constructor(relativeStreamingDistance: Int, reserved0: Float, reserved1: Float, reserved2: Int, reserved3: Int) {
        RelativeStreamingDistance = relativeStreamingDistance
        Reserved0 = reserved0
        Reserved1 = reserved1
        Reserved2 = reserved2
        Reserved3 = reserved3
    }

    fun Read(reader: AssetBinaryReader, asset: UAsset) {
        RelativeStreamingDistance = reader.ReadInt32()
        Reserved0 = reader.ReadSingle()
        Reserved1 = reader.ReadSingle()
        Reserved2 = reader.ReadInt32()
        Reserved3 = reader.ReadInt32()
    }

    fun Write(writer: AssetBinaryWriter, asset: UAsset) {
        writer.WriteInt32(RelativeStreamingDistance)
        writer.WriteSingle(Reserved0)
        writer.WriteSingle(Reserved1)
        writer.WriteInt32(Reserved2)
        writer.WriteInt32(Reserved3)
    }

    constructor()
}

/** Tile information used by WorldComposition. */
class FWorldTileInfo {
    var Position: IntArray? = null
    var AbsolutePosition: IntArray? = null
    var Bounds: BoxPropertyData? = null
    var Layer: FWorldTileLayer? = null
    var bHideInTileView: Boolean = false
    var ParentTilePackageName: FString? = null
    var LODList: Array<FWorldTileLODInfo>? = null
    var ZOrder: Int = 0

    fun Read(reader: AssetBinaryReader, asset: UAsset) {
        Position = IntArray(3)
        AbsolutePosition = IntArray(3)

        if (asset.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFortniteMainBranchObjectVersion")) < FFortniteMainBranchObjectVersion.WorldCompositionTile3DOffset.ordinal) {
            Position!![0] = reader.ReadInt32()
            Position!![1] = reader.ReadInt32()
            Position!![2] = 0
        } else {
            Position!![0] = reader.ReadInt32()
            Position!![1] = reader.ReadInt32()
            Position!![2] = reader.ReadInt32()
        }
        Bounds = BoxPropertyData(FName.DefineDummy(asset, "Bounds"))
        val pcen2 = Out<FName?>()
        Bounds!!.Ancestry.Initialize(null, reader.Asset!!.GetParentClassExportName(pcen2), pcen2.value)
        Bounds!!.Read(reader, false, 0, 0)
        Layer = FWorldTileLayer()
        Layer!!.Read(reader, asset)

        if (asset.ObjectVersion >= ObjectVersion.VER_UE4_WORLD_LEVEL_INFO_UPDATED) {
            bHideInTileView = reader.ReadInt32() == 1
            ParentTilePackageName = reader.ReadFString()
        }

        if (asset.ObjectVersion >= ObjectVersion.VER_UE4_WORLD_LEVEL_INFO_LOD_LIST) {
            val numEntries = reader.ReadInt32()
            LODList = Array(numEntries) {
                val lod = FWorldTileLODInfo()
                lod.Read(reader, asset)
                lod
            }
        }

        if (asset.ObjectVersion >= ObjectVersion.VER_UE4_WORLD_LEVEL_INFO_ZORDER) {
            ZOrder = reader.ReadInt32()
        }
    }

    fun ResolveAncestries(asset: UAsset, ancestrySoFar: AncestryInfo) {
        val ancestryNew = ancestrySoFar.clone()
        ancestryNew.SetAsParent(FName.DefineDummy(asset, "WorldTileInfo"))

        Bounds!!.ResolveAncestries(asset, ancestryNew)
        Layer!!.ResolveAncestries(asset, ancestryNew)
    }

    fun Write(writer: AssetBinaryWriter, asset: UAsset) {
        if (asset.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFortniteMainBranchObjectVersion")) < FFortniteMainBranchObjectVersion.WorldCompositionTile3DOffset.ordinal) {
            writer.WriteInt32(Position!![0])
            writer.WriteInt32(Position!![1])
        } else {
            writer.WriteInt32(Position!![0])
            writer.WriteInt32(Position!![1])
            writer.WriteInt32(Position!![2])
        }
        Bounds!!.Write(writer, false)
        Layer!!.Write(writer, asset)

        if (asset.ObjectVersion >= ObjectVersion.VER_UE4_WORLD_LEVEL_INFO_UPDATED) {
            writer.WriteBooleanInt(bHideInTileView)
            writer.Write(ParentTilePackageName)
        }

        if (asset.ObjectVersion >= ObjectVersion.VER_UE4_WORLD_LEVEL_INFO_LOD_LIST) {
            writer.WriteInt32(LODList!!.size)
            for (i in LODList!!.indices) {
                LODList!![i].Write(writer, asset)
            }
        }

        if (asset.ObjectVersion >= ObjectVersion.VER_UE4_WORLD_LEVEL_INFO_ZORDER) {
            writer.WriteInt32(ZOrder)
        }
    }

    constructor(
        position: IntArray,
        absolutePosition: IntArray,
        bounds: BoxPropertyData?,
        layer: FWorldTileLayer?,
        bHideInTileView: Boolean,
        parentTilePackageName: FString?,
        lODList: Array<FWorldTileLODInfo>?,
        zOrder: Int,
    ) {
        Position = position
        AbsolutePosition = absolutePosition
        Bounds = bounds
        Layer = layer
        this.bHideInTileView = bHideInTileView
        ParentTilePackageName = parentTilePackageName
        LODList = lODList
        ZOrder = zOrder
    }

    constructor()
}
