// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/FScriptText.cs
package com.github.jpabscale.uasset4j.kismet.bytecode

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex

open class FScriptText {
    var TextLiteralType: EBlueprintTextLiteralType = EBlueprintTextLiteralType.Empty

    var LocalizedSource: KismetExpression? = null

    var LocalizedKey: KismetExpression? = null

    var LocalizedNamespace: KismetExpression? = null

    var InvariantLiteralString: KismetExpression? = null

    var LiteralString: KismetExpression? = null

    var StringTableAsset: FPackageIndex? = null

    var StringTableId: KismetExpression? = null

    var StringTableKey: KismetExpression? = null

    open fun Read(reader: AssetBinaryReader) {
        val typeByte = reader.ReadByte()
        TextLiteralType = EBlueprintTextLiteralType.entries.firstOrNull { it.ordinal == typeByte }
            ?: throw NotImplementedError("Unimplemented blueprint text literal type $typeByte")
        when (TextLiteralType) {
            EBlueprintTextLiteralType.Empty -> {
            }
            EBlueprintTextLiteralType.LocalizedText -> {
                LocalizedSource = ExpressionSerializer.ReadExpression(reader)
                LocalizedKey = ExpressionSerializer.ReadExpression(reader)
                LocalizedNamespace = ExpressionSerializer.ReadExpression(reader)
            }
            EBlueprintTextLiteralType.InvariantText -> {
                InvariantLiteralString = ExpressionSerializer.ReadExpression(reader)
            }
            EBlueprintTextLiteralType.LiteralString -> {
                LiteralString = ExpressionSerializer.ReadExpression(reader)
            }
            EBlueprintTextLiteralType.StringTableEntry -> {
                StringTableAsset = reader.XFER_OBJECT_POINTER()
                StringTableId = ExpressionSerializer.ReadExpression(reader)
                StringTableKey = ExpressionSerializer.ReadExpression(reader)
            }
        }
    }

    open fun Write(writer: AssetBinaryWriter): Int {
        var offset = 0
        writer.WriteByte(TextLiteralType.ordinal)
        offset += 1
        when (TextLiteralType) {
            EBlueprintTextLiteralType.Empty -> {
            }
            EBlueprintTextLiteralType.LocalizedText -> {
                offset += ExpressionSerializer.WriteExpression(LocalizedSource!!, writer)
                offset += ExpressionSerializer.WriteExpression(LocalizedKey!!, writer)
                offset += ExpressionSerializer.WriteExpression(LocalizedNamespace!!, writer)
            }
            EBlueprintTextLiteralType.InvariantText -> {
                offset += ExpressionSerializer.WriteExpression(InvariantLiteralString!!, writer)
            }
            EBlueprintTextLiteralType.LiteralString -> {
                offset += ExpressionSerializer.WriteExpression(LiteralString!!, writer)
            }
            EBlueprintTextLiteralType.StringTableEntry -> {
                offset += writer.XFER_OBJECT_POINTER(StringTableAsset)
                offset += ExpressionSerializer.WriteExpression(StringTableId!!, writer)
                offset += ExpressionSerializer.WriteExpression(StringTableKey!!, writer)
            }
        }
        return offset
    }
}
