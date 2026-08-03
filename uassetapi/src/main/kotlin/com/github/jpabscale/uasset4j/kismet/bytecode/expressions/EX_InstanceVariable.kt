// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_InstanceVariable.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken

class EX_InstanceVariable : EX_VariableBase() {
    override val Token: EExprToken get() = EExprToken.EX_InstanceVariable
}
