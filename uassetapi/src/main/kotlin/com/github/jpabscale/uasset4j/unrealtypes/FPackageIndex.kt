// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/FPackageIndex.cs
package com.github.jpabscale.uasset4j.unrealtypes

import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.Import
import com.github.jpabscale.uasset4j.exporttypes.Export

/**
 * Wrapper for an Index into an ImportMap or ExportMap.
 *
 * Values greater than zero indicate an Index into the ExportMap (actual array Index = FPackageIndex - 1).
 * Values less than zero indicate an Index into the ImportMap (actual array Index = -FPackageIndex - 1).
 */
class FPackageIndex : Comparable<FPackageIndex> {
    var Index: Int = 0

    /** Returns true if this is an Index into the import map. */
    fun IsImport(): Boolean = Index < 0

    /** Returns true if this is an Index into the export map. */
    fun IsExport(): Boolean = Index > 0

    /** Returns true if this represents null (neither an import nor an export). */
    fun IsNull(): Boolean = Index == 0

    /** Returns the import that this Index represents in the import map, or null if out of range. */
    fun ToImport(asset: UAsset): Import? {
        if (!IsImport()) throw IllegalStateException("Index = $Index; cannot call ToImport()")

        val newIndex = -Index - 1
        if (newIndex < 0 || newIndex >= asset.Imports.size) return null
        return asset.Imports[newIndex]
    }

    /** Returns the export that this Index represents in the export map. */
    fun ToExport(asset: UAsset): Export? {
        if (!IsExport() || Index > asset.Exports.size) throw IllegalStateException("Index = $Index; cannot call ToExport()")
        return asset.Exports[Index - 1]
    }

    override fun compareTo(other: FPackageIndex): Int = Index.compareTo(other.Index)

    override fun equals(other: Any?): Boolean {
        val comparingPackageIndex = other as? FPackageIndex ?: return false
        return comparingPackageIndex.Index == this.Index
    }

    override fun hashCode(): Int = Index.hashCode()

    override fun toString(): String = Index.toString()

    constructor(Index: Int = 0) {
        this.Index = Index
    }

    constructor(reader: AssetBinaryReader) {
        Index = reader.ReadInt32()
        val a = reader.Asset
        if ((a?.Exports != null && Index > a.Exports.size) ||
            (a?.Imports != null && Index < -a.Imports.size)
        ) {
            throw IllegalStateException("Invalid FPackageIndex value $Index was read")
        }
    }

    fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteInt32(Index)
        return 4
    }

    companion object {
        /** Returns an FPackageIndex based off of the Index provided. */
        fun FromRawIndex(Index: Int): FPackageIndex = FPackageIndex(Index)

        /** Creates an FPackageIndex from an Index in the import map. */
        fun FromImport(importIndex: Int): FPackageIndex {
            if (importIndex < 0) throw IllegalStateException("importIndex must be greater than or equal to zero")
            return FPackageIndex(-importIndex - 1)
        }

        /** Creates an FPackageIndex from an Index in the export map. */
        fun FromExport(exportIndex: Int): FPackageIndex {
            if (exportIndex < 0) throw IllegalStateException("exportIndex must be greater than or equal to zero")
            return FPackageIndex(exportIndex + 1)
        }
    }
}
