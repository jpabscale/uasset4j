// Reimplemented from CUE4Parse (Apache-2.0) — Copyright (c) FabianFG and contributors
// Source: CUE4Parse/UE4/Objects/Engine/Curves/FCurveMetaData.cs
//
// NOTE (EXC-002): CUE4Parse reads FAnimCurveType as two booleans and skips extra bytes for a
// handful of specific games (TheFirstDescendant, KingdomHearts3, AssaultFireFuture,
// FinalFantasy7Remake). uasset4j does not model per-game archives, so those game-specific
// byte skips are omitted; they are never observed in the assets uasset4j targets.
//@parity:on EXC-002
package com.github.jpabscale.uasset4j.curves

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.customversions.FAnimPhysObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.FName

/** Anim curve type flag pair. Mirrors CUE4Parse's `FAnimCurveType`. */
class FAnimCurveType {
    var bMaterial: Boolean = false
    var bMorphtarget: Boolean = false

    constructor()

    constructor(reader: AssetBinaryReader) {
        bMaterial = reader.ReadBooleanByte()
        bMorphtarget = reader.ReadBooleanByte()
    }

    fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteByte(if (bMaterial) 1 else 0)
        writer.WriteByte(if (bMorphtarget) 1 else 0)
        return 2
    }
}

/** Curve metadata stored in skeletal mesh / skeleton assets. Mirrors CUE4Parse's `FCurveMetaData`. */
class FCurveMetaData {
    var Type: FAnimCurveType = FAnimCurveType()
    var LinkedBones: MutableList<FName> = mutableListOf()
    var MaxLOD: Int = 0

    constructor()

    constructor(reader: AssetBinaryReader) {
        Type = FAnimCurveType(reader)
        val count = reader.ReadInt32()
        LinkedBones = mutableListOf()
        for (i in 0 until count) LinkedBones.add(reader.ReadFName())

        val frmAniVer = reader.Asset?.GetCustomVersion(
            com.github.jpabscale.uasset4j.CustomVersion.GetCustomVersionGuidFromFriendlyName("FAnimPhysObjectVersion"),
        ) ?: -1
        if (frmAniVer >= FAnimPhysObjectVersion.AddLODToCurveMetaData.ordinal) {
            MaxLOD = reader.ReadByte()
        }
    }

    fun Write(writer: AssetBinaryWriter): Int {
        var written = Type.Write(writer)
        writer.WriteInt32(LinkedBones.size)
        for (name in LinkedBones) writer.Write(name)
        written += 4 + LinkedBones.size * 8
        val frmAniVer = writer.Asset?.GetCustomVersion(
            com.github.jpabscale.uasset4j.CustomVersion.GetCustomVersionGuidFromFriendlyName("FAnimPhysObjectVersion"),
        ) ?: -1
        if (frmAniVer >= FAnimPhysObjectVersion.AddLODToCurveMetaData.ordinal) {
            writer.WriteByte(MaxLOD)
            written += 1
        }
        return written
    }
}
//@parity:off EXC-002
