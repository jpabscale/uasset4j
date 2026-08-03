// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_InstanceDelegate.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.util.Ref

class EX_InstanceDelegate : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_InstanceDelegate

    var FunctionName: FName? = null

    override fun Read(reader: AssetBinaryReader) {
        FunctionName = reader.XFER_FUNC_NAME()
    }

    override fun Write(writer: AssetBinaryWriter): Int = writer.XFER_FUNC_NAME(FunctionName!!)

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = (offset.value ?: 0L) + 12L
    }
}
