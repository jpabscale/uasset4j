// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_Assert.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.util.Ref

class EX_Assert : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_Assert

    var LineNumber: Int = 0

    var DebugMode: Boolean = false

    var AssertExpression: KismetExpression? = null

    override fun Read(reader: AssetBinaryReader) {
        LineNumber = reader.ReadUInt16()
        DebugMode = reader.ReadBooleanByte()
        AssertExpression = ExpressionSerializer.ReadExpression(reader)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        var offset = 0
        writer.WriteUInt16(LineNumber)
        offset += 2
        writer.WriteBooleanByte(DebugMode)
        offset += 1
        offset += ExpressionSerializer.WriteExpression(AssertExpression!!, writer)
        return offset
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = (offset.value ?: 0L) + 3L
        AssertExpression!!.Visit(asset, offset, visitor)
    }
}
