// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/Expressions/EX_RotationConst.cs
package com.github.jpabscale.uasset4j.kismet.bytecode.expressions

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5
import com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FRotator
import com.github.jpabscale.uasset4j.util.Ref

class EX_RotationConst : KismetExpression() {
    override val Token: EExprToken get() = EExprToken.EX_RotationConst

    var Value: FRotator = FRotator()

    override fun Read(reader: AssetBinaryReader) {
        Value = if (reader.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.LARGE_WORLD_COORDINATES) {
            FRotator(reader.ReadDouble(), reader.ReadDouble(), reader.ReadDouble())
        } else {
            FRotator(reader.ReadSingle(), reader.ReadSingle(), reader.ReadSingle())
        }
    }

    override fun Write(writer: AssetBinaryWriter): Int {
        if (writer.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.LARGE_WORLD_COORDINATES) {
            writer.WriteDouble(Value.Pitch)
            writer.WriteDouble(Value.Yaw)
            writer.WriteDouble(Value.Roll)
            return Double.SIZE_BYTES * 3
        } else {
            writer.WriteSingle(Value.PitchFloat)
            writer.WriteSingle(Value.YawFloat)
            writer.WriteSingle(Value.RollFloat)
            return Float.SIZE_BYTES * 3
        }
    }

    override fun Visit(asset: UAsset, offset: Ref<Long>, visitor: (KismetExpression, Long) -> Unit) {
        super.Visit(asset, offset, visitor)
        offset.value = offset.value!! + (if (asset.ObjectVersionUE5 >= ObjectVersionUE5.LARGE_WORLD_COORDINATES) 24L else 12L)
    }
}
