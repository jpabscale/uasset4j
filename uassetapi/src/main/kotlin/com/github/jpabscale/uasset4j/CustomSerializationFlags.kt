// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UAsset.cs
package com.github.jpabscale.uasset4j

/** Flags that can be used to override certain optional behavior in how UAssetAPI serializes assets. */
@JvmInline
value class CustomSerializationFlags(val value: Int) {
    fun HasFlag(flag: CustomSerializationFlags): Boolean = (value and flag.value) == flag.value

    companion object {
        val None = CustomSerializationFlags(0)
        val NoDummies = CustomSerializationFlags(1)
        val SkipParsingBytecode = CustomSerializationFlags(2)
        val SkipPreloadDependencyLoading = CustomSerializationFlags(4)
        val SkipParsingExports = CustomSerializationFlags(8)
        val SkipLoadingExports = CustomSerializationFlags(16)
    }
}
