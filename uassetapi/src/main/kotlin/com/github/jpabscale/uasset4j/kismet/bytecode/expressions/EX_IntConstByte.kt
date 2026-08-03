// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_IntConstByte.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression

class EX_IntConstByte : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_IntConstByte

    var Value: Byte
        get() = GetObject<Byte>()
        set(value) = SetObject(value)

    override fun Read(reader: AssetBinaryReader) {
        Value = reader.ReadByte().toByte()
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteByte(Value.toInt())
        return 1
    }
}
