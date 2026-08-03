// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_Int64Const.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.util.Ref

class EX_Int64Const : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_Int64Const

    var Value: Long
        get() = GetObject<Long>()
        set(value) = SetObject(value)

    override fun Read(reader: AssetBinaryReader) {
        Value = reader.ReadInt64()
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteInt64(Value)
        return 8
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = (offset.value ?: 0L) + 8L
    }
}
