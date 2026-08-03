// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_InterfaceToObjCast.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken

class EX_InterfaceToObjCast : EX_CastBase() {
    override val Token: EExprToken get() = EExprToken.EX_InterfaceToObjCast
}
