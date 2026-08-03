// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_ArrayGetByRef.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.util.Ref

class EX_ArrayGetByRef : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_ArrayGetByRef

    var ArrayVariable: KismetExpression? = null

    var ArrayIndex: KismetExpression? = null

    override fun Read(reader: AssetBinaryReader) {
        ArrayVariable = ExpressionSerializer.ReadExpression(reader)
        ArrayIndex = ExpressionSerializer.ReadExpression(reader)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        var offset = 0
        offset += ExpressionSerializer.WriteExpression(ArrayVariable!!, writer)
        offset += ExpressionSerializer.WriteExpression(ArrayIndex!!, writer)
        return offset
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        ArrayVariable!!.Visit(asset, offset, visitor)
        ArrayIndex!!.Visit(asset, offset, visitor)
    }
}
