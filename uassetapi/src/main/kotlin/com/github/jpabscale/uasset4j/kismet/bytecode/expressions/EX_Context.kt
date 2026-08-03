// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_Context.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetPropertyPointer
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion
import com.github.jpabscale.uasset4j.util.Ref

open class EX_Context : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_Context

    var ObjectExpression: KismetExpression? = null

    var Offset: Long = 0

    var PropertyType: Byte = 0

    lateinit var RValuePointer: KismetPropertyPointer

    var ContextExpression: KismetExpression? = null

    override fun Read(reader: AssetBinaryReader) {
        ObjectExpression = ExpressionSerializer.ReadExpression(reader)
        Offset = reader.ReadUInt32()
        RValuePointer = reader.XFER_PROP_POINTER()
        if ((reader.Asset?.ObjectVersion ?: ObjectVersion.UNKNOWN) <= ObjectVersion.VER_UE4_SERIALIZE_BLUEPRINT_EVENTGRAPH_FASTCALLS_IN_UFUNCTION) {
            PropertyType = reader.ReadByte().toByte()
        }
        ContextExpression = ExpressionSerializer.ReadExpression(reader)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        var offset = 0
        offset += ExpressionSerializer.WriteExpression(ObjectExpression!!, writer)
        writer.WriteUInt32(Offset)
        offset += 4
        offset += writer.XFER_PROP_POINTER(RValuePointer)
        if ((writer.Asset?.ObjectVersion ?: ObjectVersion.UNKNOWN) <= ObjectVersion.VER_UE4_SERIALIZE_BLUEPRINT_EVENTGRAPH_FASTCALLS_IN_UFUNCTION) {
            writer.WriteByte(PropertyType.toInt())
            offset += 1
        }
        offset += ExpressionSerializer.WriteExpression(ContextExpression!!, writer)
        return offset
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        ObjectExpression!!.Visit(asset, offset, visitor)
        offset.value = (offset.value ?: 0L) + 12L
        ContextExpression!!.Visit(asset, offset, visitor)
    }
}
