// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_Vector3fConst.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.util.Ref

class EX_Vector3fConst : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_Vector3fConst

    var X: Float = 0f
    var Y: Float = 0f
    var Z: Float = 0f

    override fun Read(reader: AssetBinaryReader) {
        X = reader.ReadSingle()
        Y = reader.ReadSingle()
        Z = reader.ReadSingle()
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteSingle(X)
        writer.WriteSingle(Y)
        writer.WriteSingle(Z)
        return Float.SIZE_BYTES * 3
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = offset.value!! + 12 // 3 floats
    }
}
