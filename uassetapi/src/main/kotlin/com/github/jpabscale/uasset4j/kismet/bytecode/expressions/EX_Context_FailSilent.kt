// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_Context_FailSilent.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken

class EX_Context_FailSilent : EX_Context() {
    override val Token: EExprToken get() = EExprToken.EX_Context_FailSilent

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
    }

    override fun Write(writer: AssetBinaryWriter): Int = super.Write(writer)
}
