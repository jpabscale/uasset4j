// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/EngineVersion.cs
package com.github.jpabscale.uasset4j.unrealtypes

/**
 * An enum used to represent all retail versions of the Unreal Engine. Each version entry represents a particular
 * ObjectVersion, a particular ObjectVersionUE5, and the default set of all applicable CustomVersion enum values.
 */
enum class EngineVersion {
    UNKNOWN,
    VER_UE4_OLDEST_LOADABLE_PACKAGE,

    /** 4.0 */
    VER_UE4_0,
    /** 4.1 */
    VER_UE4_1,
    /** 4.2 */
    VER_UE4_2,
    /** 4.3 */
    VER_UE4_3,
    /** 4.4 */
    VER_UE4_4,
    /** 4.5 */
    VER_UE4_5,
    /** 4.6 */
    VER_UE4_6,
    /** 4.7 */
    VER_UE4_7,
    /** 4.8 */
    VER_UE4_8,
    /** 4.9 */
    VER_UE4_9,
    /** 4.10 */
    VER_UE4_10,
    /** 4.11 */
    VER_UE4_11,
    /** 4.12 */
    VER_UE4_12,
    /** 4.13 */
    VER_UE4_13,
    /** 4.14 */
    VER_UE4_14,
    /** 4.15 */
    VER_UE4_15,
    /** 4.16 */
    VER_UE4_16,
    /** 4.17 */
    VER_UE4_17,
    /** 4.18 */
    VER_UE4_18,
    /** 4.19 */
    VER_UE4_19,
    /** 4.20 */
    VER_UE4_20,
    /** 4.21 */
    VER_UE4_21,
    /** 4.22 */
    VER_UE4_22,
    /** 4.23 */
    VER_UE4_23,
    /** 4.24 */
    VER_UE4_24,
    /** 4.25 */
    VER_UE4_25,
    /** 4.26 */
    VER_UE4_26,
    /** 4.27 */
    VER_UE4_27,

    /** 5.0EA */
    VER_UE5_0EA,
    /** 5.0 */
    VER_UE5_0,
    /** 5.1 */
    VER_UE5_1,
    /** 5.2 */
    VER_UE5_2,
    /** 5.3 */
    VER_UE5_3,
    /** 5.4 */
    VER_UE5_4,
    /** 5.5 */
    VER_UE5_5,
    /** 5.6 */
    VER_UE5_6,
    /** 5.7 */
    VER_UE5_7,
    /** 5.8 */
    VER_UE5_8,

    VER_UE4_AUTOMATIC_VERSION_PLUS_ONE,

    /** The newest specified version of the Unreal Engine. (== the entry immediately preceding AUTOMATIC_VERSION_PLUS_ONE) */
    VER_UE4_AUTOMATIC_VERSION;

    companion object {
        /**
         * Mirrors UAssetCLI's engine-arg parsing (Program.cs): accepts the enum-name form
         * (e.g. "VER_UE4_26"), falling back to [UNKNOWN] for anything unrecognized — never throws.
         */
        @JvmStatic
        fun FromString(s: String): EngineVersion =
            entries.firstOrNull { it.name.equals(s, ignoreCase = true) } ?: UNKNOWN
    }
}
