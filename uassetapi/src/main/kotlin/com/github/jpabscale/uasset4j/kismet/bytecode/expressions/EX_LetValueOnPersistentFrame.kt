// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_LetValueOnPersistentFrame.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetPropertyPointer
import com.github.jpabscale.uasset4j.util.Ref

class EX_LetValueOnPersistentFrame : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_LetValueOnPersistentFrame

    lateinit var DestinationProperty: KismetPropertyPointer

    var AssignmentExpression: KismetExpression? = null

    override fun Read(reader: AssetBinaryReader) {
        DestinationProperty = reader.XFER_PROP_POINTER()
        AssignmentExpression = ExpressionSerializer.ReadExpression(reader)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        var offset = 0
        offset += writer.XFER_PROP_POINTER(DestinationProperty)
        offset += ExpressionSerializer.WriteExpression(AssignmentExpression!!, writer)
        return offset
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = (offset.value ?: 0L) + 8L
        AssignmentExpression!!.Visit(asset, offset, visitor)
    }
}
