// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_NameConst.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpressionGeneric
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.util.Ref

class EX_NameConst : KismetExpressionGeneric<FName>() {
    override val Token: EExprToken get() = EExprToken.EX_NameConst

    override fun Read(reader: AssetBinaryReader) {
        Value = reader.XFERNAME()
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        return writer.XFERNAME(Value)
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = offset.value!! + 12 // FName (Index + Number + ?)
    }
}
