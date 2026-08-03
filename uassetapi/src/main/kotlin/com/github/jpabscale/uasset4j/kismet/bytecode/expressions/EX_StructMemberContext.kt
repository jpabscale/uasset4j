// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_StructMemberContext.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetPropertyPointer
import com.github.jpabscale.uasset4j.util.Ref

class EX_StructMemberContext : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_StructMemberContext

    /** A pointer to the struct member expression (FProperty*). */
    var StructMemberExpression: KismetPropertyPointer? = null

    /** Struct expression. */
    var StructExpression: KismetExpression? = null

    override fun Read(reader: AssetBinaryReader) {
        StructMemberExpression = reader.XFER_PROP_POINTER()
        StructExpression = ExpressionSerializer.ReadExpression(reader)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        var offset = 0
        offset += writer.XFER_PROP_POINTER(StructMemberExpression!!)
        offset += ExpressionSerializer.WriteExpression(StructExpression!!, writer)
        return offset
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = offset.value!! + 8 // StructMemberExpression (8)
        StructExpression!!.Visit(asset, offset, visitor)
    }
}
