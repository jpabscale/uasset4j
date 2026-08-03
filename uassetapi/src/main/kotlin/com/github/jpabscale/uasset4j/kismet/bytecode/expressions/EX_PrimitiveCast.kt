// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_PrimitiveCast.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.ECastToken
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.util.Ref

class ECastTokenValue(val value: Int) {
    val name: String? get() = ECastToken.entries.firstOrNull { it.value == value }?.name

    override fun toString(): String = name ?: value.toString()
}

class EX_PrimitiveCast : KismetExpression() {
    /** The type to cast to. */
    var ConversionType: ECastTokenValue? = null

    /** The target of this expression. */
    var Target: KismetExpression? = null

    override val Token: EExprToken get() = EExprToken.EX_PrimitiveCast

    override fun Read(reader: AssetBinaryReader) {
        ConversionType = ECastTokenValue(reader.ReadByte())
        Target = ExpressionSerializer.ReadExpression(reader)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        var offset = 0
        writer.WriteByte(ConversionType!!.value); offset += 1
        offset += ExpressionSerializer.WriteExpression(Target!!, writer)
        return offset
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = offset.value!! + 1 // ConversionType (1)
        if (ConversionType!!.value == ECastToken.ObjectToInterface.value) {
            offset.value = offset.value!! + 8 // InterfaceClass
        }
        Target!!.Visit(asset, offset, visitor)
    }
}
