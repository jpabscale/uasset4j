// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_JumpIfNot.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.util.Ref

class EX_JumpIfNot : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_JumpIfNot

    var CodeOffset: Long = 0

    var BooleanExpression: KismetExpression? = null

    override fun Read(reader: AssetBinaryReader) {
        CodeOffset = reader.ReadUInt32()
        BooleanExpression = ExpressionSerializer.ReadExpression(reader)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        var offset = 0
        writer.WriteUInt32(CodeOffset)
        offset += 4
        offset += ExpressionSerializer.WriteExpression(BooleanExpression!!, writer)
        return offset
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = (offset.value ?: 0L) + 4L
        BooleanExpression!!.Visit(asset, offset, visitor)
    }
}
