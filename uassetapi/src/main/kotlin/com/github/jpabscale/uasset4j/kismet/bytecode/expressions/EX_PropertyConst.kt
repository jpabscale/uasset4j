// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_PropertyConst.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetPropertyPointer
import com.github.jpabscale.uasset4j.util.Ref

class EX_PropertyConst : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_PropertyConst

    /** A pointer to the property in question. */
    var Property: KismetPropertyPointer? = null

    override fun Read(reader: AssetBinaryReader) {
        Property = reader.XFER_PROP_POINTER()
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        return writer.XFER_PROP_POINTER(Property!!)
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = offset.value!! + 8 // Property (KismetPropertyPointer)
    }
}
