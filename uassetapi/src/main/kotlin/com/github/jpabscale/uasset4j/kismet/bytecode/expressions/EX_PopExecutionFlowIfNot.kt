// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_PopExecutionFlowIfNot.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.util.Ref

class EX_PopExecutionFlowIfNot : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_PopExecutionFlowIfNot

    /** Expression to evaluate to determine whether or not a pop should be performed. */
    var BooleanExpression: KismetExpression? = null

    override fun Read(reader: AssetBinaryReader) {
        BooleanExpression = ExpressionSerializer.ReadExpression(reader)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        return ExpressionSerializer.WriteExpression(BooleanExpression!!, writer)
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        BooleanExpression!!.Visit(asset, offset, visitor)
    }
}
