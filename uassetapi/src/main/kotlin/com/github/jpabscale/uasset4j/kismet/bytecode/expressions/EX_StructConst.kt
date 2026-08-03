// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_StructConst.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpressionGeneric
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.util.Ref

class EX_StructConst : KismetExpressionGeneric<Array<KismetExpression>>() {
    override val Token: EExprToken get() = EExprToken.EX_StructConst

    /** Pointer to the UScriptStruct in question. */
    lateinit var Struct: FPackageIndex

    /** The size of the struct that this constant represents in memory in bytes. */
    var StructSize: Int = 0

    override fun Read(reader: AssetBinaryReader) {
        Struct = reader.XFERPTR()
        StructSize = reader.ReadInt32()
        Value = reader.ReadExpressionArray(EExprToken.EX_EndStructConst)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        var offset = 0
        offset += writer.XFERPTR(Struct)
        writer.WriteInt32(StructSize); offset += 4

        for (i in Value.indices) {
            offset += ExpressionSerializer.WriteExpression(Value[i], writer)
        }

        offset += ExpressionSerializer.WriteExpression(EX_EndStructConst(), writer)
        return offset
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = offset.value!! + 12 // Struct (8) + StructSize (4)
        for (v in Value) {
            v.Visit(asset, offset, visitor)
        }
        offset.value = offset.value!! + 1 // EX_EndStructConst
    }
}
