// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/FLocMetadataObject.cs
package com.github.jpabscale.uasset4j.unrealtypes

class FLocMetadataObject {
    class FLocMetadataValue {
        // TODO:
    }

    var Values: MutableList<FLocMetadataValue> = mutableListOf()
}

enum class ELocMetadataType {
    None,
    Boolean,
    String,
    Array,
    Object,
}
