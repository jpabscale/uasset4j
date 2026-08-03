// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_CallMath.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken

class EX_CallMath : EX_FinalFunction() {
    override val Token: EExprToken get() = EExprToken.EX_CallMath

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
    }

    override fun Write(writer: AssetBinaryWriter): Int = super.Write(writer)
}
