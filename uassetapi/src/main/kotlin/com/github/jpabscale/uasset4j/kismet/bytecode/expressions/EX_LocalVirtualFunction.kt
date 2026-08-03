// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_LocalVirtualFunction.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken

class EX_LocalVirtualFunction : EX_VirtualFunction() {
    override val Token: EExprToken get() = EExprToken.EX_LocalVirtualFunction

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        return super.Write(writer)
    }
}
