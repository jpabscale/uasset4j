package com.github.jpabscale.uasset4j

import com.github.jpabscale.uasset4j.curves.*
import com.github.jpabscale.uasset4j.customversions.FFortniteMainBranchObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.engineenums.ERichCurveInterpMode
import com.github.jpabscale.uasset4j.unrealtypes.engineenums.ERichCurveTangentMode
import com.github.jpabscale.uasset4j.unrealtypes.engineenums.ERichCurveTangentWeightMode
import com.github.jpabscale.uasset4j.unrealtypes.objects.engine.FRichCurveKey
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CurveTests {

    private fun Float.toBytes(): ByteArray {
        val bits = toRawBits()
        return byteArrayOf(
            (bits and 0xFF).toByte(),
            ((bits ushr 8) and 0xFF).toByte(),
            ((bits ushr 16) and 0xFF).toByte(),
            ((bits ushr 24) and 0xFF).toByte(),
        )
    }

    @Test
    fun `FRichCurve eval linear interpolation`() {
        val curve = FRichCurve()
        curve.Keys = mutableListOf(
            FRichCurveKey(ERichCurveInterpMode.RCIM_Linear, ERichCurveTangentMode.RCTM_Auto,
                ERichCurveTangentWeightMode.RCTWM_WeightedNone, 0.0f, 0.0f, 0f, 0f, 0f, 0f),
            FRichCurveKey(ERichCurveInterpMode.RCIM_Linear, ERichCurveTangentMode.RCTM_Auto,
                ERichCurveTangentWeightMode.RCTWM_WeightedNone, 1.0f, 1.0f, 0f, 0f, 0f, 0f),
        )
        assertEquals(0.5f, curve.Eval(0.5f), 0.001f)
        assertEquals(0.0f, curve.Eval(0.0f), 0.001f)
        assertEquals(1.0f, curve.Eval(1.0f), 0.001f)
    }

    @Test
    fun `FRichCurve eval cubic interpolation matches CUE4Parse bezier`() {
        val curve = FRichCurve()
        curve.Keys = mutableListOf(
            FRichCurveKey(ERichCurveInterpMode.RCIM_Cubic, ERichCurveTangentMode.RCTM_Auto,
                ERichCurveTangentWeightMode.RCTWM_WeightedNone, 0.0f, 0.4f, 0f, 0f, 0.6f, 0f),
            FRichCurveKey(ERichCurveInterpMode.RCIM_Cubic, ERichCurveTangentMode.RCTM_Auto,
                ERichCurveTangentWeightMode.RCTWM_WeightedNone, 0.7f, 0.4f, 0.6f, 0f, 0.6f, 0f),
        )
        assertEquals(0.4f, curve.Eval(0.0f), 0.001f)
        val mid = curve.Eval(0.35f)
        assertTrue(mid in 0.3f..0.5f, "mid=$mid")
    }

    @Test
    fun `FRichCurve eval extrapolation constant pre and post`() {
        val curve = FRichCurve()
        curve.PreInfinityExtrap = ERichCurveExtrapolation.RCCE_Constant
        curve.PostInfinityExtrap = ERichCurveExtrapolation.RCCE_Constant
        curve.Keys = mutableListOf(
            FRichCurveKey(ERichCurveInterpMode.RCIM_Linear, ERichCurveTangentMode.RCTM_Auto,
                ERichCurveTangentWeightMode.RCTWM_WeightedNone, 0.0f, 5.0f, 0f, 0f, 0f, 0f),
            FRichCurveKey(ERichCurveInterpMode.RCIM_Linear, ERichCurveTangentMode.RCTM_Auto,
                ERichCurveTangentWeightMode.RCTWM_WeightedNone, 1.0f, 10.0f, 0f, 0f, 0f, 0f),
        )
        assertEquals(5.0f, curve.Eval(-1.0f), 0.001f)
        assertEquals(10.0f, curve.Eval(2.0f), 0.001f)
        assertEquals(7.5f, curve.Eval(0.5f), 0.001f)
    }

    @Test
    fun `FSimpleCurve eval linear interpolation`() {
        val curve = FSimpleCurve()
        curve.Keys = mutableListOf(
            FSimpleCurveKey(0.0f, 1.0f),
            FSimpleCurveKey(1.0f, 2.0f),
        )
        assertEquals(1.5f, curve.Eval(0.5f), 0.001f)
    }

    @Test
    fun `FCompressedRichCurve constant decompression`() {
        val compressed = FCompressedRichCurve()
        compressed.CompressionFormat = ERichCurveCompressionFormat.RCCF_Constant
        compressed.KeyTimeCompressionFormat = ERichCurveKeyTimeCompressionFormat.RCKTCF_uint16
        compressed.PreInfinityExtrap = ERichCurveExtrapolation.RCCE_Constant
        compressed.PostInfinityExtrap = ERichCurveExtrapolation.RCCE_Constant
        compressed.NumKeysOrConstant = 3.14f.toRawBits()
        compressed.CompressedKeys = ByteArray(0)

        val decompressed = compressed.ConvertToRaw()
        assertEquals(1, decompressed.Keys.size)
        assertEquals(0.0f, decompressed.Keys[0].Time, 0.001f)
        assertEquals(3.14f, decompressed.Keys[0].Value, 0.001f)
        assertEquals(ERichCurveInterpMode.RCIM_Linear, decompressed.Keys[0].InterpMode)
    }

    @Test
    fun `FCompressedRichCurve empty decompression`() {
        val compressed = FCompressedRichCurve()
        compressed.CompressionFormat = ERichCurveCompressionFormat.RCCF_Empty
        compressed.PreInfinityExtrap = ERichCurveExtrapolation.RCCE_Constant
        compressed.PostInfinityExtrap = ERichCurveExtrapolation.RCCE_Constant
        compressed.NumKeysOrConstant = 99.0f.toRawBits()

        val decompressed = compressed.ConvertToRaw()
        assertEquals(0, decompressed.Keys.size)
        assertEquals(99.0f, decompressed.DefaultValue, 0.001f)
    }

    @Test
    fun `FCompressedRichCurve linear uint16 decompression`() {
        val numKeys = 2
        val compressed = FCompressedRichCurve()
        compressed.CompressionFormat = ERichCurveCompressionFormat.RCCF_Linear
        compressed.KeyTimeCompressionFormat = ERichCurveKeyTimeCompressionFormat.RCKTCF_uint16
        compressed.PreInfinityExtrap = ERichCurveExtrapolation.RCCE_Constant
        compressed.PostInfinityExtrap = ERichCurveExtrapolation.RCCE_Constant
        compressed.NumKeysOrConstant = numKeys

        // For linear: key data is [value_0, value_1, ...] (1 float per key).
        // UniformKeyDataAdapter reads handle+1/+2 for arrive/leave (garbage for linear),
        // so pad with zeros to avoid OOB in test.
        val keyTimes = byteArrayOf(
            0x00, 0x00, // key 0: time = 0.0
            0xFF.toByte(), 0xFF.toByte(), // key 1: time = 1.0
        )
        val keyData = floatArrayOf(
            1.0f, 2.0f, 0f, 0f, 0f, 0f, // values + padding for tangent reads
        ).flatMap { it.toBytes().toList() }.toByteArray()

        compressed.CompressedKeys = keyTimes + 0.0f.toBytes() + 1.0f.toBytes() + keyData

        val decompressed = compressed.ConvertToRaw()
        assertEquals(2, decompressed.Keys.size)
        assertEquals(0.0f, decompressed.Keys[0].Time, 0.001f)
        assertEquals(1.0f, decompressed.Keys[0].Value, 0.001f)
        assertEquals(1.0f, decompressed.Keys[1].Time, 0.001f)
        assertEquals(2.0f, decompressed.Keys[1].Value, 0.001f)
        assertEquals(ERichCurveInterpMode.RCIM_Linear, decompressed.Keys[0].InterpMode)
    }

    @Test
    fun `FCompressedRichCurve linear float32 decompression`() {
        val numKeys = 2
        val compressed = FCompressedRichCurve()
        compressed.CompressionFormat = ERichCurveCompressionFormat.RCCF_Linear
        compressed.KeyTimeCompressionFormat = ERichCurveKeyTimeCompressionFormat.RCKTCF_float32
        compressed.PreInfinityExtrap = ERichCurveExtrapolation.RCCE_Constant
        compressed.PostInfinityExtrap = ERichCurveExtrapolation.RCCE_Constant
        compressed.NumKeysOrConstant = numKeys

        // Same layout as uint16: values + padding
        val keyData = floatArrayOf(
            1.0f, 2.0f, 0f, 0f, 0f, 0f,
        ).flatMap { it.toBytes().toList() }.toByteArray()

        compressed.CompressedKeys = 0.0f.toBytes() + 1.0f.toBytes() + keyData

        val decompressed = compressed.ConvertToRaw()
        assertEquals(2, decompressed.Keys.size)
        assertEquals(0.0f, decompressed.Keys[0].Time, 0.001f)
        assertEquals(1.0f, decompressed.Keys[0].Value, 0.001f)
        assertEquals(1.0f, decompressed.Keys[1].Time, 0.001f)
        assertEquals(2.0f, decompressed.Keys[1].Value, 0.001f)
    }

    @Test
    fun `UCurveTable mode ShrinkCurveTableSize ordinal`() {
        assertEquals(23, FFortniteMainBranchObjectVersion.ShrinkCurveTableSize.ordinal)
    }

    @Test
    fun `ERichCurveExtrapolation round-trip`() {
        assertEquals(6, ERichCurveExtrapolation.entries.size)
        assertEquals(0, ERichCurveExtrapolation.RCCE_Cycle.ordinal)
        assertEquals(5, ERichCurveExtrapolation.RCCE_None.ordinal)
    }

    @Test
    fun `ERichCurveCompressionFormat round-trip`() {
        assertEquals(6, ERichCurveCompressionFormat.entries.size)
        assertEquals(0, ERichCurveCompressionFormat.RCCF_Empty.ordinal)
        assertEquals(5, ERichCurveCompressionFormat.RCCF_Weighted.ordinal)
    }

    @Test
    fun `FKeyHandle default`() {
        val h = FKeyHandle()
        assertEquals(0u, h.Index)
    }

    @Test
    fun `FCurveMetaData defaults`() {
        val m = FCurveMetaData()
        assertEquals(0, m.MaxLOD)
        assertEquals(0, m.LinkedBones.size)
        assertFalse(m.Type.bMaterial)
        assertFalse(m.Type.bMorphtarget)
    }

    @Test
    fun `UCurveLinearColor eval returns RGBA`() {
        val lc = UCurveLinearColor()
        val c = lc.GetUnadjustedLinearColorValue(0.0f)
        assertEquals(0.0f, c.R, 0.001f)
        assertEquals(0.0f, c.G, 0.001f)
        assertEquals(0.0f, c.B, 0.001f)
        assertEquals(1.0f, c.A, 0.001f)
    }

    @Test
    fun `FRichCurve JSON round-trip`() {
        val mapper = com.github.jpabscale.uasset4j.json.UAssetJson.mapper
        val curve = FRichCurve()
        curve.Keys = mutableListOf(
            FRichCurveKey(ERichCurveInterpMode.RCIM_Linear, ERichCurveTangentMode.RCTM_Auto,
                ERichCurveTangentWeightMode.RCTWM_WeightedNone, 0.0f, 1.0f, 0f, 0f, 0f, 0f),
            FRichCurveKey(ERichCurveInterpMode.RCIM_Cubic, ERichCurveTangentMode.RCTM_Break,
                ERichCurveTangentWeightMode.RCTWM_WeightedBoth, 1.0f, 2.0f, 0.5f, 0.3f, 0.7f, 0.4f),
        )
        curve.PreInfinityExtrap = ERichCurveExtrapolation.RCCE_Linear
        curve.PostInfinityExtrap = ERichCurveExtrapolation.RCCE_CycleWithOffset

        val json = mapper.writeValueAsString(curve)
        val restored = mapper.readValue(json, FRichCurve::class.java)
        assertEquals(2, restored.Keys.size)
        assertEquals(0.0f, restored.Keys[0].Time, 0.001f)
        assertEquals(1.0f, restored.Keys[0].Value, 0.001f)
        assertEquals(1.0f, restored.Keys[1].Time, 0.001f)
        assertEquals(2.0f, restored.Keys[1].Value, 0.001f)
        assertEquals(0.5f, restored.Keys[1].ArriveTangent, 0.001f)
        assertEquals(0.7f, restored.Keys[1].LeaveTangent, 0.001f)
        assertEquals(ERichCurveInterpMode.RCIM_Cubic, restored.Keys[1].InterpMode)
        assertEquals(ERichCurveTangentMode.RCTM_Break, restored.Keys[1].TangentMode)
        assertEquals(ERichCurveExtrapolation.RCCE_Linear, restored.PreInfinityExtrap)
        assertEquals(ERichCurveExtrapolation.RCCE_CycleWithOffset, restored.PostInfinityExtrap)
    }

    @Test
    fun `FSimpleCurve JSON round-trip`() {
        val mapper = com.github.jpabscale.uasset4j.json.UAssetJson.mapper
        val curve = FSimpleCurve()
        curve.InterpMode = ERichCurveInterpMode.RCIM_Constant
        curve.Keys = mutableListOf(
            FSimpleCurveKey(0.0f, 10.0f),
            FSimpleCurveKey(5.0f, 20.0f),
        )
        curve.PreInfinityExtrap = ERichCurveExtrapolation.RCCE_Oscillate

        val json = mapper.writeValueAsString(curve)
        val restored = mapper.readValue(json, FSimpleCurve::class.java)
        assertEquals(ERichCurveInterpMode.RCIM_Constant, restored.InterpMode)
        assertEquals(2, restored.Keys.size)
        assertEquals(0.0f, restored.Keys[0].Time, 0.001f)
        assertEquals(10.0f, restored.Keys[0].Value, 0.001f)
        assertEquals(5.0f, restored.Keys[1].Time, 0.001f)
        assertEquals(20.0f, restored.Keys[1].Value, 0.001f)
        assertEquals(ERichCurveExtrapolation.RCCE_Oscillate, restored.PreInfinityExtrap)
    }

    @Test
    fun `UCurveTable JSON round-trip`() {
        val mapper = com.github.jpabscale.uasset4j.json.UAssetJson.mapper
        val table = UCurveTable()
        table.CurveTableMode = ECurveTableMode.RichCurves

        val json = mapper.writeValueAsString(table)
        val restored = mapper.readValue(json, UCurveTable::class.java)
        assertEquals(ECurveTableMode.RichCurves, restored.CurveTableMode)
        assertEquals(0, restored.RowMap.size)
    }
}
