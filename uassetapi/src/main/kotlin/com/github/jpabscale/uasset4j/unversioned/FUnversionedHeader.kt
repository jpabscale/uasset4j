// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Unversioned/FUnversionedHeader.cs
package com.github.jpabscale.uasset4j.unversioned

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAPUtils
import java.util.BitSet

class FUnversionedHeader {
    var Fragments: MutableList<FFragment> = mutableListOf()
    var CurrentFragment: Int = 0
    var UnversionedPropertyIndex: Int = 0
    var ZeroMaskIndex: Int = 0
    var ZeroMaskNum: Long = 0
    var ZeroMask: BitSet = BitSet()
    var ZeroMaskLength: Int = 0
    var bHasNonZeroValues: Boolean = false

    val HasValues: Boolean get() = bHasNonZeroValues || (ZeroMaskLength > 0)
    val HasNonZeroValues: Boolean get() = bHasNonZeroValues

    fun Read(reader: AssetBinaryReader) {
        if (!reader.Asset!!.HasUnversionedProperties) return
        Fragments = mutableListOf()

        var UnmaskedNum: Long = 0
        var firstNum = 0
        var Fragment: FFragment
        do {
            Fragment = FFragment.Unpack(reader.ReadUInt16())
            Fragment.FirstNum = firstNum + Fragment.SkipNum
            firstNum = firstNum + Fragment.SkipNum + Fragment.ValueNum
            Fragments.add(Fragment)

            if (Fragment.bHasAnyZeroes) {
                ZeroMaskNum += Fragment.ValueNum.toLong()
            } else {
                UnmaskedNum += Fragment.ValueNum.toLong()
            }
        } while (!Fragment.bIsLast)

        if (ZeroMaskNum > 0) {
            LoadZeroMaskData(reader, ZeroMaskNum)
            bHasNonZeroValues = UnmaskedNum > 0 || !CheckIfZeroMaskIsAllOnes()
        } else {
            ZeroMask = BitSet()
            ZeroMaskLength = 0
            bHasNonZeroValues = UnmaskedNum > 0
        }

        CurrentFragment = 0
        UnversionedPropertyIndex = Fragments[CurrentFragment].FirstNum
    }

    fun LoadZeroMaskData(reader: AssetBinaryReader, NumBits: Long) {
        if (NumBits <= 8) {
            ZeroMask = BitSet.valueOf(reader.ReadBytes(1))
            ZeroMaskLength = 8
        } else if (NumBits <= 16) {
            ZeroMask = BitSet.valueOf(reader.ReadBytes(2))
            ZeroMaskLength = 16
        } else {
            val numWords = UAPUtils.DivideAndRoundUp(NumBits.toInt(), 32)
            ZeroMask = BitSet()
            for (i in 0 until numWords) {
                val word = reader.ReadInt32()
                for (b in 0 until 32) {
                    if ((word and (1 shl b)) != 0) ZeroMask.set(i * 32 + b)
                }
            }
            ZeroMaskLength = numWords * 32
        }
    }

    fun SaveZeroMaskData(): ByteArray {
        val NumBits = ZeroMaskLength

        val res: ByteArray = if (NumBits <= 8) {
            ByteArray(1)
        } else if (NumBits <= 16) {
            ByteArray(2)
        } else {
            ByteArray(UAPUtils.DivideAndRoundUp(NumBits, 32) * 4)
        }

        val from = ZeroMask.toByteArray()
        from.copyInto(res, 0, 0, minOf(res.size, from.size))
        return res
    }

    fun CheckIfZeroMaskIsAllOnes(): Boolean {
        for (i in 0 until ZeroMaskLength) {
            if (!ZeroMask.get(i)) return false
        }
        return true
    }

    fun Write(writer: AssetBinaryWriter) {
        if (!writer.Asset!!.HasUnversionedProperties) return
        for (Fragment in Fragments) {
            writer.WriteUInt16(Fragment.Pack())
        }

        if (ZeroMaskLength > 0) {
            writer.WriteBytes(SaveZeroMaskData())
        }
    }

    constructor()

    constructor(reader: AssetBinaryReader) {
        Read(reader)
    }
}
