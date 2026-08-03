// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/FObjectDataResource.cs
package com.github.jpabscale.uasset4j.unrealtypes

/** UObject binary/bulk data resource type (5.3+). */
enum class EObjectDataResourceVersion(val value: Int) {
    Invalid(0),
    Initial(1),
    AddedCookedIndex(2),
    LatestPlusOne(3);

    companion object {
        const val Latest = 2
    }
}

/** Flags for UObject binary/bulk data resources. */
@JvmInline
value class EObjectDataResourceFlags(val value: Int) {
    companion object {
        const val None = 0
        const val Inline = 1 shl 0
        const val Streaming = 1 shl 1
        const val Optional = 1 shl 2
        const val Duplicate = 1 shl 3
        const val MemoryMapped = 1 shl 4
        const val DerivedDataReference = 1 shl 5
    }
}

/** UObject binary/bulk data resource type. */
class FObjectDataResource {
    var Flags: EObjectDataResourceFlags = EObjectDataResourceFlags(EObjectDataResourceFlags.None)
    var CookedIndex: Int = 0
    var SerialOffset: Long = 0
    var DuplicateSerialOffset: Long = 0
    var SerialSize: Long = 0
    var RawSize: Long = 0
    var OuterIndex: FPackageIndex? = null
    var LegacyBulkDataFlags: Long = 0

    constructor(
        flags: EObjectDataResourceFlags,
        serialOffset: Long,
        duplicateSerialOffset: Long,
        serialSize: Long,
        rawSize: Long,
        outerIndex: FPackageIndex,
        legacyBulkDataFlags: Long,
        cookedIndex: Int = 0,
    ) {
        Flags = flags
        CookedIndex = cookedIndex
        SerialOffset = serialOffset
        DuplicateSerialOffset = duplicateSerialOffset
        SerialSize = serialSize
        RawSize = rawSize
        OuterIndex = outerIndex
        LegacyBulkDataFlags = legacyBulkDataFlags
    }

    constructor()
}
