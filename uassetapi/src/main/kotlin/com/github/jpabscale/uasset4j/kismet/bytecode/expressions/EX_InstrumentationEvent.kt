// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_InstrumentationEvent.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.EScriptInstrumentationType
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.util.Ref

class EX_InstrumentationEvent : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_InstrumentationEvent

    var EventType: EScriptInstrumentationType = EScriptInstrumentationType.Class

    var EventName: FName? = null

    override fun Read(reader: AssetBinaryReader) {
        EventType = EScriptInstrumentationType.entries[reader.ReadByte()]

        if (EventType == EScriptInstrumentationType.InlineEvent) {
            EventName = reader.XFER_FUNC_NAME()
        }
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteByte(EventType.ordinal)
        return if (EventType == EScriptInstrumentationType.InlineEvent) {
            writer.XFER_FUNC_NAME(EventName!!)
            1 + 2 * 4
        } else {
            1
        }
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = (offset.value ?: 0L) + 1L
        if (EventType == EScriptInstrumentationType.InlineEvent) {
            offset.value = (offset.value ?: 0L) + 12L
        }
    }
}
