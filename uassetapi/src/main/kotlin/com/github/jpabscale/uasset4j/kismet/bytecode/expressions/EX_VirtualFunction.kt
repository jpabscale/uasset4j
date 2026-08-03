// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_VirtualFunction.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.util.Ref

open class EX_VirtualFunction : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_VirtualFunction

    /** Virtual function name. */
    var VirtualFunctionName: FName = FName()

    /** List of parameters for this function. */
    var Parameters: Array<KismetExpression> = emptyArray()

    override fun Read(reader: AssetBinaryReader) {
        VirtualFunctionName = reader.XFER_FUNC_NAME()

        Parameters = reader.ReadExpressionArray(EExprToken.EX_EndFunctionParms)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        var offset = 0
        offset += writer.XFER_FUNC_NAME(VirtualFunctionName)

        for (i in Parameters.indices) {
            offset += ExpressionSerializer.WriteExpression(Parameters[i], writer)
        }
        offset += ExpressionSerializer.WriteExpression(EX_EndFunctionParms(), writer)
        return offset
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = offset.value!! + 12 // VirtualFunctionName (12)
        for (param in Parameters) {
            param.Visit(asset, offset, visitor)
        }
        offset.value = offset.value!! + 1 // EX_EndFunctionParms
    }
}
