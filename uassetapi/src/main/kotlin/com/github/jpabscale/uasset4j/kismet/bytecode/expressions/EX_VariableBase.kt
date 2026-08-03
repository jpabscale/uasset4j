// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_VariableBase.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetPropertyPointer
import com.github.jpabscale.uasset4j.util.Ref

/** Base class for Kismet Variable expressions */
abstract class EX_VariableBase : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_ClassSparseDataVariable

    /** A pointer to the variable in question. */
    var Variable: KismetPropertyPointer? = null

    override fun Read(reader: AssetBinaryReader) {
        Variable = reader.XFER_PROP_POINTER()
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        return writer.XFER_PROP_POINTER(Variable!!)
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = offset.value!! + 8 // Variable (KismetPropertyPointer)
    }
}
