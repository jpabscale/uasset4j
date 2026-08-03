// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_SetArray.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion
import com.github.jpabscale.uasset4j.util.Ref

class EX_SetArray : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_SetArray

    /** Array property to assign to */
    var AssigningProperty: KismetExpression? = null

    /** Pointer to the array inner property (FProperty*). Only used in engine versions prior to ObjectVersion.VER_UE4_CHANGE_SETARRAY_BYTECODE. */
    var ArrayInnerProp: FPackageIndex? = null

    /** Array items. */
    var Elements: Array<KismetExpression> = emptyArray()

    override fun Read(reader: AssetBinaryReader) {
        if (reader.Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_CHANGE_SETARRAY_BYTECODE) {
            AssigningProperty = ExpressionSerializer.ReadExpression(reader)
        } else {
            ArrayInnerProp = reader.XFERPTR()
        }

        Elements = reader.ReadExpressionArray(EExprToken.EX_EndArray)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        var offset = 0
        if (writer.Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_CHANGE_SETARRAY_BYTECODE) {
            offset += ExpressionSerializer.WriteExpression(AssigningProperty!!, writer)
        } else {
            offset += writer.XFERPTR(ArrayInnerProp)
        }

        for (i in Elements.indices) {
            offset += ExpressionSerializer.WriteExpression(Elements[i], writer)
        }
        offset += ExpressionSerializer.WriteExpression(EX_EndArray(), writer)
        return offset
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        AssigningProperty!!.Visit(asset, offset, visitor)
        for (element in Elements) {
            element.Visit(asset, offset, visitor)
        }
        offset.value = offset.value!! + 1 // EX_EndArray
    }
}
