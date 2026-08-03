// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_Skip.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.util.Ref

class EX_Skip : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_Skip

    /** The offset to skip to. */
    var CodeOffset: Long = 0

    /** An expression to possibly skip. */
    var SkipExpression: KismetExpression? = null

    override fun Read(reader: AssetBinaryReader) {
        CodeOffset = reader.ReadUInt32()
        SkipExpression = ExpressionSerializer.ReadExpression(reader)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        var offset = 0
        writer.WriteUInt32(CodeOffset); offset += 4
        offset += ExpressionSerializer.WriteExpression(SkipExpression!!, writer)
        return offset
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = offset.value!! + 4 // CodeOffset
        SkipExpression!!.Visit(asset, offset, visitor)
    }
}
