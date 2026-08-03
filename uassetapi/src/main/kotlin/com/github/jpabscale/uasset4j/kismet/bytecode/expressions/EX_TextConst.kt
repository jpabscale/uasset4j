// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_TextConst.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EBlueprintTextLiteralType
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.FScriptText
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpressionGeneric
import com.github.jpabscale.uasset4j.util.Ref

class EX_TextConst : KismetExpressionGeneric<FScriptText>() {
    override val Token: EExprToken get() = EExprToken.EX_TextConst

    override fun Read(reader: AssetBinaryReader) {
        Value = FScriptText()
        Value.Read(reader)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        return Value.Write(writer)
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = offset.value!! + 1 // TextLiteralType
        when (Value.TextLiteralType) {
            EBlueprintTextLiteralType.Empty -> {}
            EBlueprintTextLiteralType.LocalizedText -> {
                Value.LocalizedSource!!.Visit(asset, offset, visitor)
                Value.LocalizedKey!!.Visit(asset, offset, visitor)
                Value.LocalizedNamespace!!.Visit(asset, offset, visitor)
            }
            EBlueprintTextLiteralType.InvariantText -> Value.InvariantLiteralString!!.Visit(asset, offset, visitor)
            EBlueprintTextLiteralType.LiteralString -> Value.LiteralString!!.Visit(asset, offset, visitor)
            EBlueprintTextLiteralType.StringTableEntry -> {
                offset.value = offset.value!! + 8 // StringTableAsset
                Value.StringTableId!!.Visit(asset, offset, visitor)
                Value.StringTableKey!!.Visit(asset, offset, visitor)
            }
        }
    }
}
