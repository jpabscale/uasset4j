// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_FinalFunction.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.util.Ref

open class EX_FinalFunction : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_FinalFunction

    var StackNode: FPackageIndex = FPackageIndex(0)

    var Parameters: Array<KismetExpression> = emptyArray()

    override fun Read(reader: AssetBinaryReader) {
        StackNode = reader.XFER_FUNC_POINTER()
        Parameters = reader.ReadExpressionArray(EExprToken.EX_EndFunctionParms)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        var offset = 0
        offset += writer.XFER_FUNC_POINTER(StackNode)
        for (i in Parameters.indices) {
            offset += ExpressionSerializer.WriteExpression(Parameters[i], writer)
        }
        offset += ExpressionSerializer.WriteExpression(EX_EndFunctionParms(), writer)
        return offset
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = (offset.value ?: 0L) + 8L
        for (param in Parameters) {
            param.Visit(asset, offset, visitor)
        }
        offset.value = (offset.value ?: 0L) + 1L
    }
}
