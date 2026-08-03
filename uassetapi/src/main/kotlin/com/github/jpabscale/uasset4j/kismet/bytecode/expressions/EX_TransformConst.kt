// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_TransformConst.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpressionGeneric
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5
import com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FTransform
import com.github.jpabscale.uasset4j.util.Ref

class EX_TransformConst : KismetExpressionGeneric<FTransform>() {
    override val Token: EExprToken get() = EExprToken.EX_TransformConst

    init {
        Value = FTransform()
    }

    override fun Read(reader: AssetBinaryReader) {
        Value = FTransform(reader)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        return Value.Write(writer)
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = offset.value!! + (if (asset.ObjectVersionUE5 >= ObjectVersionUE5.LARGE_WORLD_COORDINATES) 80L else 40L)
    }
}
