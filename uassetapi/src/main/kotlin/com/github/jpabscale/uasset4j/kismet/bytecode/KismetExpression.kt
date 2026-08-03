// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/KismetExpression.cs
package com.github.jpabscale.uasset4j.kismet.bytecode

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.util.Ref

open class KismetExpression {
    open val Token: EExprToken get() = EExprToken.EX_Nothing

    val Inst: String get() = Token.name.substring(3)

    var Tag: Any? = null

    var RawValue: Any? = null

    fun SetObject(value: Any?) {
        RawValue = value
    }

    fun <T> GetObject(): T {
        @Suppress("UNCHECKED_CAST")
        return RawValue as T
    }

    open fun Read(reader: AssetBinaryReader) {
    }

    open fun Write(writer: AssetBinaryWriter): Int = 0

    open fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        visitor(this, offset.value!!)
        offset.value = offset.value!! + 1L
    }

    fun GetSize(asset: UAsset): Long {
        val offset = Ref<Long>(0L)
        Visit(asset, offset) { _, _ -> }
        return offset.value!!
    }
}
