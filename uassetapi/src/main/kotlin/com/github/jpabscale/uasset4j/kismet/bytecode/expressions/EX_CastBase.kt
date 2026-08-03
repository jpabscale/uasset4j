// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_CastBase.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.util.Ref

abstract class EX_CastBase : KismetExpression() {
    var ClassPtr: FPackageIndex = FPackageIndex(0)

    var Target: KismetExpression? = null

    override fun Read(reader: AssetBinaryReader) {
        ClassPtr = reader.XFERPTR()
        Target = ExpressionSerializer.ReadExpression(reader)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        var offset = 0
        offset += writer.XFERPTR(ClassPtr)
        offset += ExpressionSerializer.WriteExpression(Target!!, writer)
        return offset
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = (offset.value ?: 0L) + 8L
        Target!!.Visit(asset, offset, visitor)
    }
}
