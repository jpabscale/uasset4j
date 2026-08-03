// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_MapConst.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetPropertyPointer
import com.github.jpabscale.uasset4j.util.Ref

class EX_MapConst : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_MapConst

    /** Pointer to this constant's key property (FProperty*). */
    var KeyProperty: KismetPropertyPointer? = null

    /** Pointer to this constant's value property (FProperty*). */
    var ValueProperty: KismetPropertyPointer? = null

    /** Set constant entries. */
    var Elements: Array<KismetExpression> = emptyArray()

    override fun Read(reader: AssetBinaryReader) {
        KeyProperty = reader.XFER_PROP_POINTER()
        ValueProperty = reader.XFER_PROP_POINTER()
        val numEntries = reader.ReadInt32() // Number of elements
        Elements = reader.ReadExpressionArray(EExprToken.EX_EndMapConst)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        var offset = 0
        offset += writer.XFER_PROP_POINTER(KeyProperty!!)
        offset += writer.XFER_PROP_POINTER(ValueProperty!!)
        writer.WriteInt32(Elements.size / 2); offset += 4
        for (i in Elements.indices) {
            offset += ExpressionSerializer.WriteExpression(Elements[i], writer)
        }
        offset += ExpressionSerializer.WriteExpression(EX_EndMapConst(), writer)
        return offset
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = offset.value!! + 20 // KeyProperty (8) + ValueProperty (8) + NumElements (4)
        for (element in Elements) {
            element.Visit(asset, offset, visitor)
        }
    }
}
