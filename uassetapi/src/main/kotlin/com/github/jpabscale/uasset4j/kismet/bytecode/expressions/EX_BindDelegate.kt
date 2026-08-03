// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_BindDelegate.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.util.Ref

class EX_BindDelegate : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_BindDelegate

    var FunctionName: FName? = null

    var Delegate: KismetExpression? = null

    var ObjectTerm: KismetExpression? = null

    override fun Read(reader: AssetBinaryReader) {
        FunctionName = reader.XFER_FUNC_NAME()
        Delegate = ExpressionSerializer.ReadExpression(reader)
        ObjectTerm = ExpressionSerializer.ReadExpression(reader)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        var offset = 0
        offset += writer.XFER_FUNC_NAME(FunctionName!!)
        offset += ExpressionSerializer.WriteExpression(Delegate!!, writer)
        offset += ExpressionSerializer.WriteExpression(ObjectTerm!!, writer)
        return offset
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = (offset.value ?: 0L) + 12L
        Delegate!!.Visit(asset, offset, visitor)
        ObjectTerm!!.Visit(asset, offset, visitor)
    }
}
