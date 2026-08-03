// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/KismetExpression.cs
package com.github.jpabscale.uasset4j.kismet.bytecode

open class KismetExpressionGeneric<T> : KismetExpression() {
    var Value: T
        get() = GetObject<T>()
        set(value) = SetObject(value)
}
