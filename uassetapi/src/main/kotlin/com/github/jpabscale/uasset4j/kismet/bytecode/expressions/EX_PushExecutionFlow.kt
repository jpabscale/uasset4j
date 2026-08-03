// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_PushExecutionFlow.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.util.Ref

class EX_PushExecutionFlow : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_PushExecutionFlow

    /** The address to push onto the execution flow stack. */
    var PushingAddress: Long = 0

    override fun Read(reader: AssetBinaryReader) {
        PushingAddress = reader.ReadUInt32()
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteUInt32(PushingAddress)
        return 4
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = offset.value!! + 4 // PushingAddress
    }
}
