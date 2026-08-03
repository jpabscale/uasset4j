// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/KismetPropertyPointer.cs
package com.github.jpabscale.uasset4j.kismet.bytecode

import com.github.jpabscale.uasset4j.propertytypes.objects.FFieldPath
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex

class KismetPropertyPointer {
    var Old: FPackageIndex? = null

    var New: FFieldPath? = null

    fun ShouldSerializeOld(): Boolean = Old != null

    fun ShouldSerializeNew(): Boolean = New != null

    constructor(older: FPackageIndex) {
        Old = older
    }

    constructor(newer: FFieldPath) {
        New = newer
    }

    constructor()
}
