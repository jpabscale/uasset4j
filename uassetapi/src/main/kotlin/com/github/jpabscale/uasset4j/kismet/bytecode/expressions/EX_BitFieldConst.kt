// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_BitFieldConst.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetPropertyPointer
import com.github.jpabscale.uasset4j.util.Ref

class EX_BitFieldConst : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_BitFieldConst

    lateinit var Property: KismetPropertyPointer

    var Value: Byte = 0

    override fun Read(reader: AssetBinaryReader) {
        Property = reader.XFER_PROP_POINTER()
        Value = reader.ReadByte().toByte()
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        val offset = writer.XFER_PROP_POINTER(Property)
        writer.WriteByte(Value.toInt())
        return offset + 1
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = (offset.value ?: 0L) + 9L
    }
}
