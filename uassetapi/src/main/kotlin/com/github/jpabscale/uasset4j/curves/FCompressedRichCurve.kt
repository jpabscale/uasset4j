// Reimplemented from CUE4Parse (Apache-2.0) — Copyright (c) FabianFG and contributors
// Source: CUE4Parse/UE4/Objects/Engine/Curves/RichCurve.cs (FCompressedRichCurve + adapters)
//
// The C# source uses `unsafe` pointer math; this port uses the same offsets expressed as
// array indices over the CompressedKeys byte array. No unsafe code.
//@parity:on EXC-002
package com.github.jpabscale.uasset4j.curves

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAPUtils
import com.github.jpabscale.uasset4j.unrealtypes.engineenums.ERichCurveInterpMode
import com.github.jpabscale.uasset4j.unrealtypes.engineenums.ERichCurveTangentMode
import com.github.jpabscale.uasset4j.unrealtypes.engineenums.ERichCurveTangentWeightMode
import com.github.jpabscale.uasset4j.unrealtypes.objects.engine.FRichCurveKey

/** Enumerates curve compression options. */
enum class ERichCurveCompressionFormat {
    /** No keys are present. */
    RCCF_Empty,
    /** All keys use constant interpolation. */
    RCCF_Constant,
    /** All keys use linear interpolation. */
    RCCF_Linear,
    /** All keys use cubic interpolation. */
    RCCF_Cubic,
    /** Keys use mixed interpolation modes. */
    RCCF_Mixed,
    /** Keys use weighted interpolation modes. */
    RCCF_Weighted,
}

/** Enumerates key time compression options. */
enum class ERichCurveKeyTimeCompressionFormat {
    /** Key time is quantized to 16 bits. */
    RCKTCF_uint16,
    /** Key time uses full precision. */
    RCKTCF_float32,
}

/** A compressed rich curve. Mirrors CUE4Parse's `FCompressedRichCurve`. */
class FCompressedRichCurve {
    /** Compression format used by CompressedKeys. */
    var CompressionFormat: ERichCurveCompressionFormat = ERichCurveCompressionFormat.RCCF_Empty

    /** Compression format used to pack the key time. */
    var KeyTimeCompressionFormat: ERichCurveKeyTimeCompressionFormat = ERichCurveKeyTimeCompressionFormat.RCKTCF_uint16

    /** Pre-infinity extrapolation state. */
    var PreInfinityExtrap: ERichCurveExtrapolation = ERichCurveExtrapolation.RCCE_Constant

    /** Post-infinity extrapolation state. */
    var PostInfinityExtrap: ERichCurveExtrapolation = ERichCurveExtrapolation.RCCE_Constant

    /**
     * If the compression format is constant, this is the value returned. Inline here to reduce the
     * likelihood of accessing the compressed keys data for the common case of constant/zero/empty
     * curves. When a curve is linear/cubic/mixed, this holds the number of keys.
     */
    var NumKeysOrConstant: Int = 0

    var CompressedKeys: ByteArray = ByteArray(0)

    /** The decompressed curve. Populated by [ConvertToRaw]. */
    var Curve: FRichCurve = FRichCurve()

    constructor()

    constructor(reader: AssetBinaryReader) {
        CompressionFormat = ERichCurveCompressionFormat.entries[reader.ReadByte()]
        KeyTimeCompressionFormat = ERichCurveKeyTimeCompressionFormat.entries[reader.ReadByte()]
        PreInfinityExtrap = ERichCurveExtrapolation.entries[reader.ReadByte()]
        PostInfinityExtrap = ERichCurveExtrapolation.entries[reader.ReadByte()]
        NumKeysOrConstant = reader.ReadInt32()
        val count = reader.ReadInt32()
        if (count > 0) {
            CompressedKeys = reader.ReadBytes(count)
        }
        Curve = ConvertToRaw(PreInfinityExtrap, PostInfinityExtrap, NumKeysOrConstant, CompressedKeys)
    }

    fun Write(writer: AssetBinaryWriter): Int {
        writer.WriteByte(CompressionFormat.ordinal)
        writer.WriteByte(KeyTimeCompressionFormat.ordinal)
        writer.WriteByte(PreInfinityExtrap.ordinal)
        writer.WriteByte(PostInfinityExtrap.ordinal)
        writer.WriteInt32(NumKeysOrConstant)
        writer.WriteInt32(CompressedKeys.size)
        if (CompressedKeys.isNotEmpty()) writer.WriteBytes(CompressedKeys)
        return 16 + CompressedKeys.size
    }

    fun ConvertToRaw(): FRichCurve =
        ConvertToRaw(PreInfinityExtrap, PostInfinityExtrap, NumKeysOrConstant, CompressedKeys)

    private fun ConvertToRaw(
        preInfinityExtrap: ERichCurveExtrapolation,
        postInfinityExtrap: ERichCurveExtrapolation,
        constantValueNumKeys: Int,
        compressedKeys: ByteArray,
    ): FRichCurve =
        ConverterMap[CompressionFormat.ordinal][KeyTimeCompressionFormat.ordinal](
            preInfinityExtrap, postInfinityExtrap, constantValueNumKeys, compressedKeys,
        )

    companion object {
        /**
         * Mirrors CUE4Parse's `ConverterMap`:
         * `[ERichCurveCompressionFormat][ERichCurveKeyTimeCompressionFormat] -> converter`.
         * Each converter takes `(preInfinityExtrap, postInfinityExtrap, constantValueNumKeys, compressedKeys)`.
         */
        val ConverterMap: Array<Array<(ERichCurveExtrapolation, ERichCurveExtrapolation, Int, ByteArray) -> FRichCurve>> = arrayOf(
            // RCCF_Empty
            arrayOf(
                { pre, post, constantValue, _ ->
                    FRichCurve().apply {
                        DefaultValue = Float.fromBits(constantValue)
                        PreInfinityExtrap = pre
                        PostInfinityExtrap = post
                        Keys = mutableListOf()
                    }
                },
                { pre, post, constantValue, _ ->
                    FRichCurve().apply {
                        DefaultValue = Float.fromBits(constantValue)
                        PreInfinityExtrap = pre
                        PostInfinityExtrap = post
                        Keys = mutableListOf()
                    }
                },
            ),
            // RCCF_Constant
            arrayOf(
                { pre, post, constantValue, _ ->
                    FRichCurve().apply {
                        DefaultValue = Float.MAX_VALUE
                        PreInfinityExtrap = pre
                        PostInfinityExtrap = post
                        // CUE4Parse: new(0.0f, value) => Linear/Auto/WeightedNone
                        Keys = mutableListOf(
                            FRichCurveKey(
                                ERichCurveInterpMode.RCIM_Linear,
                                ERichCurveTangentMode.RCTM_Auto,
                                ERichCurveTangentWeightMode.RCTWM_WeightedNone,
                                0.0f,
                                Float.fromBits(constantValue),
                                0.0f,
                                0.0f,
                                0.0f,
                                0.0f,
                            ),
                        )
                    }
                },
                { pre, post, constantValue, _ ->
                    FRichCurve().apply {
                        DefaultValue = Float.MAX_VALUE
                        PreInfinityExtrap = pre
                        PostInfinityExtrap = post
                        Keys = mutableListOf(
                            FRichCurveKey(
                                ERichCurveInterpMode.RCIM_Linear,
                                ERichCurveTangentMode.RCTM_Auto,
                                ERichCurveTangentWeightMode.RCTWM_WeightedNone,
                                0.0f,
                                Float.fromBits(constantValue),
                                0.0f,
                                0.0f,
                                0.0f,
                                0.0f,
                            ),
                        )
                    }
                },
            ),
            // RCCF_Linear
            arrayOf(
                // RCKTCF_uint16
                { pre, post, numKeys, compressedKeys ->
                    val keyTimesOffset = 0
                    val keyTimeAdapter = Quantized16BitKeyTimeAdapter(compressedKeys, keyTimesOffset, numKeys)
                    val keyDataAdapter = UniformKeyDataAdapter(ERichCurveCompressionFormat.RCCF_Linear, compressedKeys, keyTimeAdapter.keyDataOffset)
                    convertToRaw(keyTimeAdapter, keyDataAdapter, numKeys, pre, post)
                },
                // RCKTCF_float32
                { pre, post, numKeys, compressedKeys ->
                    val keyTimesOffset = 0
                    val keyTimeAdapter = Float32BitKeyTimeAdapter(compressedKeys, keyTimesOffset, numKeys)
                    val keyDataAdapter = UniformKeyDataAdapter(ERichCurveCompressionFormat.RCCF_Linear, compressedKeys, keyTimeAdapter.keyDataOffset)
                    convertToRaw(keyTimeAdapter, keyDataAdapter, numKeys, pre, post)
                },
            ),
            // RCCF_Cubic
            arrayOf(
                // RCKTCF_uint16
                { pre, post, numKeys, compressedKeys ->
                    val keyTimesOffset = 0
                    val keyTimeAdapter = Quantized16BitKeyTimeAdapter(compressedKeys, keyTimesOffset, numKeys)
                    val keyDataAdapter = UniformKeyDataAdapter(ERichCurveCompressionFormat.RCCF_Cubic, compressedKeys, keyTimeAdapter.keyDataOffset)
                    convertToRaw(keyTimeAdapter, keyDataAdapter, numKeys, pre, post)
                },
                // RCKTCF_float32
                { pre, post, numKeys, compressedKeys ->
                    val keyTimesOffset = 0
                    val keyTimeAdapter = Float32BitKeyTimeAdapter(compressedKeys, keyTimesOffset, numKeys)
                    val keyDataAdapter = UniformKeyDataAdapter(ERichCurveCompressionFormat.RCCF_Cubic, compressedKeys, keyTimeAdapter.keyDataOffset)
                    convertToRaw(keyTimeAdapter, keyDataAdapter, numKeys, pre, post)
                },
            ),
            // RCCF_Mixed
            arrayOf(
                // RCKTCF_uint16
                { pre, post, numKeys, compressedKeys ->
                    val interpModesOffset = 0
                    val keyTimesOffset = align(interpModesOffset + numKeys * 1, 2)
                    val keyTimeAdapter = Quantized16BitKeyTimeAdapter(compressedKeys, keyTimesOffset, numKeys)
                    val keyDataAdapter = MixedKeyDataAdapter(compressedKeys, interpModesOffset, keyTimeAdapter.keyDataOffset)
                    convertToRaw(keyTimeAdapter, keyDataAdapter, numKeys, pre, post)
                },
                // RCKTCF_float32
                { pre, post, numKeys, compressedKeys ->
                    val interpModesOffset = 0
                    val keyTimesOffset = align(interpModesOffset + numKeys * 1, 4)
                    val keyTimeAdapter = Float32BitKeyTimeAdapter(compressedKeys, keyTimesOffset, numKeys)
                    val keyDataAdapter = MixedKeyDataAdapter(compressedKeys, interpModesOffset, keyTimeAdapter.keyDataOffset)
                    convertToRaw(keyTimeAdapter, keyDataAdapter, numKeys, pre, post)
                },
            ),
            // RCCF_Weighted
            arrayOf(
                // RCKTCF_uint16
                { pre, post, numKeys, compressedKeys ->
                    val interpModesOffset = 0
                    val keyTimesOffset = align(interpModesOffset + 2 * numKeys * 1, 2)
                    val keyTimeAdapter = Quantized16BitKeyTimeAdapter(compressedKeys, keyTimesOffset, numKeys)
                    val keyDataAdapter = WeightedKeyDataAdapter(compressedKeys, interpModesOffset, keyTimeAdapter.keyDataOffset)
                    convertToRaw(keyTimeAdapter, keyDataAdapter, numKeys, pre, post)
                },
                // RCKTCF_float32
                { pre, post, numKeys, compressedKeys ->
                    val interpModesOffset = 0
                    val keyTimesOffset = align(interpModesOffset + 2 * numKeys * 1, 4)
                    val keyTimeAdapter = Float32BitKeyTimeAdapter(compressedKeys, keyTimesOffset, numKeys)
                    val keyDataAdapter = WeightedKeyDataAdapter(compressedKeys, interpModesOffset, keyTimeAdapter.keyDataOffset)
                    convertToRaw(keyTimeAdapter, keyDataAdapter, numKeys, pre, post)
                },
            ),
        )

        private fun align(value: Int, alignment: Int): Int = UAPUtils.AlignPadding(value, alignment)

        private fun readFloat(b: ByteArray, off: Int): Float {
            val v = (b[off].toInt() and 0xFF) or
                ((b[off + 1].toInt() and 0xFF) shl 8) or
                ((b[off + 2].toInt() and 0xFF) shl 16) or
                (b[off + 3].toInt() shl 24)
            return Float.fromBits(v)
        }

        private fun convertToRaw(
            keyTimeAdapter: IKeyTimeAdapter,
            keyDataAdapter: IKeyDataAdapter,
            numKeys: Int,
            pre: ERichCurveExtrapolation,
            post: ERichCurveExtrapolation,
        ): FRichCurve {
            val curve = FRichCurve()
            curve.DefaultValue = Float.MAX_VALUE
            curve.PreInfinityExtrap = pre
            curve.PostInfinityExtrap = post
            curve.Keys = ArrayList(numKeys)
            for (keyIndex in 0 until numKeys) {
                val handle = keyDataAdapter.getKeyDataHandle(keyIndex)
                val interpFormat = keyDataAdapter.getKeyInterpMode(keyIndex)
                val key = FRichCurveKey()
                key.InterpMode = when (interpFormat) {
                    ERichCurveCompressionFormat.RCCF_Linear -> ERichCurveInterpMode.RCIM_Linear
                    ERichCurveCompressionFormat.RCCF_Cubic -> ERichCurveInterpMode.RCIM_Cubic
                    ERichCurveCompressionFormat.RCCF_Constant -> ERichCurveInterpMode.RCIM_Constant
                    ERichCurveCompressionFormat.RCCF_Empty -> ERichCurveInterpMode.RCIM_None
                    else -> throw IllegalArgumentException("Can't convert interpMode $interpFormat to ERichCurveInterpMode")
                }
                key.TangentMode = ERichCurveTangentMode.RCTM_Auto
                key.TangentWeightMode = keyDataAdapter.getKeyTangentWeightMode(keyIndex)
                key.Time = keyTimeAdapter.getTime(keyIndex)
                key.Value = keyDataAdapter.getKeyValue(handle)
                key.ArriveTangent = keyDataAdapter.getKeyArriveTangent(handle)
                key.ArriveTangentWeight = keyDataAdapter.getKeyArriveTangentWeight(handle)
                key.LeaveTangent = keyDataAdapter.getKeyLeaveTangent(handle)
                key.LeaveTangentWeight = keyDataAdapter.getKeyLeaveTangentWeight(handle)
                curve.Keys.add(key)
            }
            return curve
        }
    }
}

internal interface IKeyTimeAdapter {
    val keyDataOffset: Int
    fun getTime(keyIndex: Int): Float
}

internal interface IKeyDataAdapter {
    fun getKeyDataHandle(keyIndexToQuery: Int): Int
    fun getKeyValue(handle: Int): Float
    fun getKeyArriveTangent(handle: Int): Float
    fun getKeyLeaveTangent(handle: Int): Float
    fun getKeyInterpMode(keyIndex: Int): ERichCurveCompressionFormat
    fun getKeyTangentWeightMode(keyIndex: Int): ERichCurveTangentWeightMode
    fun getKeyArriveTangentWeight(handle: Int): Float
    fun getKeyLeaveTangentWeight(handle: Int): Float
}

internal class Quantized16BitKeyTimeAdapter(basePtr: ByteArray, keyTimesOffset: Int, numKeys: Int) : IKeyTimeAdapter {
    private val keyTimes = IntArray(numKeys)
    private val minTime: Float
    private val deltaTime: Float
    override val keyDataOffset: Int

    init {
        val rangeDataOffset = UAPUtils.AlignPadding(keyTimesOffset + numKeys * 2, 4)
        keyDataOffset = rangeDataOffset + 8
        for (i in 0 until numKeys) {
            val off = keyTimesOffset + i * 2
            keyTimes[i] = (basePtr[off].toInt() and 0xFF) or ((basePtr[off + 1].toInt() and 0xFF) shl 8)
        }
        minTime = readFloat(basePtr, rangeDataOffset)
        deltaTime = readFloat(basePtr, rangeDataOffset + 4)
    }

    override fun getTime(keyIndex: Int): Float {
        val keyNormalizedTime = keyTimes[keyIndex] * (1.0f / 65535.0f)
        return (keyNormalizedTime * deltaTime) + minTime
    }
}

internal class Float32BitKeyTimeAdapter(basePtr: ByteArray, keyTimesOffset: Int, numKeys: Int) : IKeyTimeAdapter {
    private val keyTimes = FloatArray(numKeys)
    override val keyDataOffset: Int

    init {
        for (i in 0 until numKeys) {
            keyTimes[i] = readFloat(basePtr, keyTimesOffset + i * 4)
        }
        keyDataOffset = UAPUtils.AlignPadding(keyTimesOffset + numKeys * 4, 4)
    }

    override fun getTime(keyIndex: Int): Float = keyTimes[keyIndex]
}

internal class UniformKeyDataAdapter(
    private val format: ERichCurveCompressionFormat,
    basePtr: ByteArray,
    private val keyDataOffset: Int,
) : IKeyDataAdapter {
    private val keyData = basePtr

    override fun getKeyDataHandle(keyIndexToQuery: Int): Int =
        if (format == ERichCurveCompressionFormat.RCCF_Cubic) keyIndexToQuery * 3 else keyIndexToQuery

    override fun getKeyValue(handle: Int): Float = readFloat(keyData, keyDataOffset + handle * 4)
    override fun getKeyArriveTangent(handle: Int): Float = readFloat(keyData, keyDataOffset + handle * 4 + 4)
    override fun getKeyLeaveTangent(handle: Int): Float = readFloat(keyData, keyDataOffset + handle * 4 + 8)
    override fun getKeyInterpMode(keyIndex: Int): ERichCurveCompressionFormat = format
    override fun getKeyTangentWeightMode(keyIndex: Int): ERichCurveTangentWeightMode = ERichCurveTangentWeightMode.RCTWM_WeightedNone
    override fun getKeyArriveTangentWeight(handle: Int): Float = 0.0f
    override fun getKeyLeaveTangentWeight(handle: Int): Float = 0.0f
}

internal class MixedKeyDataAdapter(
    basePtr: ByteArray,
    private val interpModesOffset: Int,
    private val keyDataOffset: Int,
) : IKeyDataAdapter {
    private val interpModes = basePtr
    private val keyData = basePtr

    override fun getKeyDataHandle(keyIndexToQuery: Int): Int = keyIndexToQuery * 3
    override fun getKeyValue(handle: Int): Float = readFloat(keyData, keyDataOffset + handle * 4)
    override fun getKeyArriveTangent(handle: Int): Float = readFloat(keyData, keyDataOffset + handle * 4 + 4)
    override fun getKeyLeaveTangent(handle: Int): Float = readFloat(keyData, keyDataOffset + handle * 4 + 8)
    override fun getKeyInterpMode(keyIndex: Int): ERichCurveCompressionFormat =
        ERichCurveCompressionFormat.entries[interpModes[interpModesOffset + keyIndex].toInt() and 0xFF]
    override fun getKeyTangentWeightMode(keyIndex: Int): ERichCurveTangentWeightMode = ERichCurveTangentWeightMode.RCTWM_WeightedNone
    override fun getKeyArriveTangentWeight(handle: Int): Float = 0.0f
    override fun getKeyLeaveTangentWeight(handle: Int): Float = 0.0f
}

internal class WeightedKeyDataAdapter(
    basePtr: ByteArray,
    private val interpModesOffset: Int,
    private val keyDataOffset: Int,
) : IKeyDataAdapter {
    private val interpModes = basePtr
    private val keyData = basePtr

    override fun getKeyDataHandle(keyIndexToQuery: Int): Int = keyIndexToQuery * 5
    override fun getKeyValue(handle: Int): Float = readFloat(keyData, keyDataOffset + handle * 4)
    override fun getKeyArriveTangent(handle: Int): Float = readFloat(keyData, keyDataOffset + handle * 4 + 4)
    override fun getKeyLeaveTangent(handle: Int): Float = readFloat(keyData, keyDataOffset + handle * 4 + 8)
    override fun getKeyArriveTangentWeight(handle: Int): Float = readFloat(keyData, keyDataOffset + handle * 4 + 12)
    override fun getKeyLeaveTangentWeight(handle: Int): Float = readFloat(keyData, keyDataOffset + handle * 4 + 16)
    override fun getKeyInterpMode(keyIndex: Int): ERichCurveCompressionFormat =
        ERichCurveCompressionFormat.entries[interpModes[interpModesOffset + keyIndex].toInt() and 0xFF]
    override fun getKeyTangentWeightMode(keyIndex: Int): ERichCurveTangentWeightMode =
        ERichCurveTangentWeightMode.entries[interpModes[interpModesOffset + keyIndex + 1].toInt() and 0xFF]
}

private fun readFloat(b: ByteArray, off: Int): Float {
    val v = (b[off].toInt() and 0xFF) or
        ((b[off + 1].toInt() and 0xFF) shl 8) or
        ((b[off + 2].toInt() and 0xFF) shl 16) or
        (b[off + 3].toInt() shl 24)
    return Float.fromBits(v)
}
//@parity:off EXC-002
