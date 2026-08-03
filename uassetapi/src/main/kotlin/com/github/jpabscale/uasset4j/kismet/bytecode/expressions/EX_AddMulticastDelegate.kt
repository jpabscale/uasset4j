// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_AddMulticastDelegate.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.util.Ref

class EX_AddMulticastDelegate : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_AddMulticastDelegate

    var Delegate: KismetExpression? = null

    var DelegateToAdd: KismetExpression? = null

    override fun Read(reader: AssetBinaryReader) {
        Delegate = ExpressionSerializer.ReadExpression(reader)
        DelegateToAdd = ExpressionSerializer.ReadExpression(reader)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        var offset = 0
        offset += ExpressionSerializer.WriteExpression(Delegate!!, writer)
        offset += ExpressionSerializer.WriteExpression(DelegateToAdd!!, writer)
        return offset
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        Delegate!!.Visit(asset, offset, visitor)
        DelegateToAdd!!.Visit(asset, offset, visitor)
    }
}
