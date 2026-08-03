// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_SetMap.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.util.Ref

class EX_SetMap : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_SetMap

    /** Map property. */
    var MapProperty: KismetExpression? = null

    /** Set entries. */
    var Elements: Array<KismetExpression> = emptyArray()

    override fun Read(reader: AssetBinaryReader) {
        MapProperty = ExpressionSerializer.ReadExpression(reader)
        val numEntries = reader.ReadInt32() // Number of elements
        Elements = reader.ReadExpressionArray(EExprToken.EX_EndMap)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        var offset = 0
        offset += ExpressionSerializer.WriteExpression(MapProperty!!, writer)
        // count is number of pairs so is half the number of elements
        writer.WriteInt32(Elements.size / 2); offset += 4
        for (i in Elements.indices) {
            offset += ExpressionSerializer.WriteExpression(Elements[i], writer)
        }
        offset += ExpressionSerializer.WriteExpression(EX_EndMap(), writer)
        return offset
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        MapProperty!!.Visit(asset, offset, visitor)
        offset.value = offset.value!! + 4 // NumElements
        for (e in Elements) {
            e.Visit(asset, offset, visitor)
        }
        offset.value = offset.value!! + 1 // EX_EndMap
    }
}
