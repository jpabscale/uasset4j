// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_LetBase.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.util.Ref

abstract class EX_LetBase : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_LetDelegate

    var VariableExpression: KismetExpression? = null

    var AssignmentExpression: KismetExpression? = null

    override fun Read(reader: AssetBinaryReader) {
        VariableExpression = ExpressionSerializer.ReadExpression(reader)
        AssignmentExpression = ExpressionSerializer.ReadExpression(reader)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        var offset = 0
        offset += ExpressionSerializer.WriteExpression(VariableExpression!!, writer)
        offset += ExpressionSerializer.WriteExpression(AssignmentExpression!!, writer)
        return offset
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        VariableExpression!!.Visit(asset, offset, visitor)
        AssignmentExpression!!.Visit(asset, offset, visitor)
    }
}
