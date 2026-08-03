// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/FFieldPath.cs
package com.github.jpabscale.uasset4j.unrealtypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.FormatException
import com.github.jpabscale.uasset4j.UAsset

class FFieldPath {
    /** Path to the FField object from the innermost FField to the outermost UObject (UPackage) */
    var Path: Array<FName>

    /** The cached owner of this field. */
    var ResolvedOwner: FPackageIndex

    constructor(path: Array<FName>, resolvedOwner: FPackageIndex, numExports: Int = -1) {
        Path = path
        ResolvedOwner = resolvedOwner

        if (numExports > 0 && ResolvedOwner.Index > numExports) {
            throw FormatException("Received nonsensical FFieldPath ResolvedOwner: " + ResolvedOwner.Index)
        }
    }

    constructor() {
        Path = emptyArray()
        ResolvedOwner = FPackageIndex.FromRawIndex(0)
    }

    constructor(reader: AssetBinaryReader) {
        Path = reader.ReadArray { reader.ReadFName() }
        ResolvedOwner = FPackageIndex(reader.ReadInt32())
    }

    fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteInt32(Path.size)
        for (name in Path) {
            writer.Write(name)
        }
        writer.WriteInt32(ResolvedOwner.Index)
        return 4 * (2 + Path.size * 2)
    }

    companion object {
        fun Read(reader: AssetBinaryReader): FFieldPath = FFieldPath(reader)

        fun FromString(d: Array<String>, asset: UAsset): FFieldPath =
            throw NotImplementedError("FFieldPath.FromString")
    }
}
