// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_Breakpoint.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression

class EX_Breakpoint : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_Breakpoint

    override fun Read(reader: AssetBinaryReader) {
    }

    override fun Write(writer: AssetBinaryWriter): Int = 0
}
