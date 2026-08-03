// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Core/Math/FTransform.cs
package com.github.jpabscale.uasset4j.unrealtypes.objects.core.math

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter

class FTransform {
    var Rotation: FQuat
    var Translation: FVector
    var Scale3D: FVector

    constructor(rotation: FQuat, translation: FVector, scale3D: FVector) {
        Rotation = rotation
        Translation = translation
        Scale3D = scale3D
    }

    constructor(reader: AssetBinaryReader) {
        Rotation = FQuat(reader)
        Translation = FVector(reader)
        Scale3D = FVector(reader)
    }

    constructor() {
        Rotation = FQuat()
        Translation = FVector()
        Scale3D = FVector()
    }

    fun Write(writer: AssetBinaryWriter): Int {
        var size = 0
        size += Rotation.Write(writer)
        size += Translation.Write(writer)
        size += Scale3D.Write(writer)
        return size
    }
}
