// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_EndSetConst.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression

class EX_EndSetConst : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_EndSetConst

    override fun Read(reader: AssetBinaryReader): Unit {
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        return 0
    }
}
