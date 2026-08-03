// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_SwitchValue.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.util.Ref

/** Represents a case in a Kismet bytecode switch statement. */
class FKismetSwitchCase {
    /** The index value term of this case. */
    var CaseIndexValueTerm: KismetExpression? = null

    /** Code offset to the next case. */
    var NextOffset: Long = 0

    /** The main case term. */
    var CaseTerm: KismetExpression? = null

    constructor()

    constructor(caseIndexValueTerm: KismetExpression, nextOffset: Long, caseTerm: KismetExpression) {
        CaseIndexValueTerm = caseIndexValueTerm
        NextOffset = nextOffset
        CaseTerm = caseTerm
    }
}

class EX_SwitchValue : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_SwitchValue

    /** Code offset to jump to when finished. */
    var EndGotoOffset: Long = 0

    /** The index term of this switch statement. */
    var IndexTerm: KismetExpression? = null

    /** The default term of this switch statement. */
    var DefaultTerm: KismetExpression? = null

    /** All the cases in this switch statement. */
    var Cases: Array<FKismetSwitchCase> = emptyArray()

    override fun Read(reader: AssetBinaryReader) {
        val numCases = reader.ReadUInt16() // number of cases, without default one
        EndGotoOffset = reader.ReadUInt32()
        IndexTerm = ExpressionSerializer.ReadExpression(reader)

        Cases = Array(numCases) {
            val termA = ExpressionSerializer.ReadExpression(reader)
            val termB = reader.ReadUInt32()
            val termC = ExpressionSerializer.ReadExpression(reader)
            FKismetSwitchCase(termA, termB, termC)
        }

        DefaultTerm = ExpressionSerializer.ReadExpression(reader)
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        var offset = 0
        writer.WriteUInt16(Cases.size); offset += 2
        writer.WriteUInt32(EndGotoOffset); offset += 4
        offset += ExpressionSerializer.WriteExpression(IndexTerm!!, writer)
        for (i in Cases.indices) {
            offset += ExpressionSerializer.WriteExpression(Cases[i].CaseIndexValueTerm!!, writer)
            writer.WriteUInt32(Cases[i].NextOffset); offset += 4
            offset += ExpressionSerializer.WriteExpression(Cases[i].CaseTerm!!, writer)
        }
        offset += ExpressionSerializer.WriteExpression(DefaultTerm!!, writer)
        return offset
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = offset.value!! + 6 // NumCases (2) + EndGotoOffset (4)
        IndexTerm!!.Visit(asset, offset, visitor)
        for (c in Cases) {
            c.CaseIndexValueTerm!!.Visit(asset, offset, visitor)
            offset.value = offset.value!! + 4 // NextOffset
            c.CaseTerm!!.Visit(asset, offset, visitor)
        }
        DefaultTerm!!.Visit(asset, offset, visitor)
    }
}
