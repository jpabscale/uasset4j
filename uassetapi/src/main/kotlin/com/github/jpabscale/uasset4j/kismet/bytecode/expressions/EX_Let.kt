// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_Let.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetPropertyPointer
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion
import com.github.jpabscale.uasset4j.util.Ref

class EX_Let : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_Let

    lateinit var Value: KismetPropertyPointer

    var Variable: KismetExpression? = null

    var Expression: KismetExpression? = null

    override fun Read(reader: AssetBinaryReader) {
        if ((reader.Asset?.ObjectVersion ?: ObjectVersion.UNKNOWN) > ObjectVersion.VER_UE4_SERIALIZE_BLUEPRINT_EVENTGRAPH_FASTCALLS_IN_UFUNCTION) {
            Value = reader.XFER_PROP_POINTER()
        }
        Variable = ExpressionSerializer.ReadExpression(reader)
        Expression = ExpressionSerializer.ReadExpression(reader)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        var offset = 0
        if ((writer.Asset?.ObjectVersion ?: ObjectVersion.UNKNOWN) > ObjectVersion.VER_UE4_SERIALIZE_BLUEPRINT_EVENTGRAPH_FASTCALLS_IN_UFUNCTION) {
            offset += writer.XFER_PROP_POINTER(Value)
        }
        offset += ExpressionSerializer.WriteExpression(Variable!!, writer)
        offset += ExpressionSerializer.WriteExpression(Expression!!, writer)
        return offset
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = (offset.value ?: 0L) + 8L
        Variable!!.Visit(asset, offset, visitor)
        Expression!!.Visit(asset, offset, visitor)
    }
}
