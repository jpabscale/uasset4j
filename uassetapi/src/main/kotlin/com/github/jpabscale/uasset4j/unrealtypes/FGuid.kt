// Copyright (c) 2026 jpabscale — original code (not part of the UAssetAPI port)
// Replaces C# System.Guid with .NET-compatible byte layout (Guid.ToByteArray()).
@file:OptIn(kotlin.ExperimentalUnsignedTypes::class)

package com.github.jpabscale.uasset4j.unrealtypes

/**
 * C# `System.Guid` equivalent with the exact .NET byte layout so on-disk parity is preserved:
 * `Guid.ToByteArray()` emits [data1] little-endian (bytes 0-3), [data2] LE16 (bytes 4-5),
 * [data3] LE16 (bytes 6-7) and [data4] big-endian (bytes 8-15).
 *
 * [data2]/[data3] carry the two 16-bit fields in their low 16 bits; [data4] is the 8-byte field.
 */
class FGuid(
    val data1: UInt,
    val data2: UInt,
    val data3: UInt,
    val data4: ULong,
) {
    fun toByteArray(): ByteArray {
        val out = ByteArray(16)
        writeU32LE(out, 0, data1)
        writeU16LE(out, 4, data2 and 0xFFFFu)
        writeU16LE(out, 6, data3 and 0xFFFFu)
        writeU64BE(out, 8, data4)
        return out
    }

    /** Four LE uint32s of [toByteArray], matching UAPUtils.ToUnsignedInts. */
    fun toUnsignedInts(): UIntArray {
        val b = toByteArray()
        return UIntArray(4) { readU32LE(b, it * 4) }
    }

    /** Standard UUID form (for JSON interop later). */
    fun toUuid(): java.util.UUID {
        val mostSig = (data1.toLong() shl 32) or (data2.toLong() shl 16) or data3.toLong()
        return java.util.UUID(mostSig, data4.toLong())
    }

    /**
     * UAssetGUI-style brace form matching the C# oracle: `UAPUtils.ConvertToString`, which
     * reverses `Guid.ToByteArray()` and re-orders the hex digits
     * (e.g. `{375EC13C-06E4-48FB-B500-84F0262A717E}`).
     */
    fun toPrettyString(): String {
        val reversed = toByteArray().reversedArray()
        val hex = reversed.joinToString("") { "%02x".format(it) }
        return ("{" + hex.substring(24, 32) + "-" + hex.substring(16, 20) + "-" + hex.substring(20, 24) +
            "-" + hex.substring(8, 12) + "-" + hex.substring(12, 16) + hex.substring(0, 8) + "}").uppercase()
    }

    /** .NET Guid.ToString() "D" format (lowercase, hyphenated, data4 split 4+8 hex). */
    override fun toString(): String =
        String.format(
            "%08x-%04x-%04x-%04x-%012x",
            data1.toInt(),
            data2.toInt(),
            data3.toInt(),
            (data4 shr 48).toInt(),
            (data4 and 0xFFFFFFFFFFFFuL).toLong(),
        )

    override fun equals(other: Any?): Boolean =
        other is FGuid && data1 == other.data1 && data2 == other.data2 && data3 == other.data3 && data4 == other.data4

    override fun hashCode(): Int {
        var h = data1.hashCode()
        h = h * 31 + data2.hashCode()
        h = h * 31 + data3.hashCode()
        h = h * 31 + data4.hashCode()
        return h
    }

    companion object {
        /** Matches UAPUtils.GUID(v1,v2,v3,v4): 4 uints -> .NET Guid. */
        fun fromUnsignedInts(v1: UInt, v2: UInt, v3: UInt, v4: UInt): FGuid {
            // C# UAPUtils.GUID builds bytes = [v1 LE][v2 LE][v3 LE][v4 LE], then new Guid(bytes):
            // data1 = v1, data2 = low16(v2) LE, data3 = high16(v2) LE,
            // data4 = BE64 of ([v3 LE bytes][v4 LE bytes]).
            val data2 = v2 and 0xFFFFu
            val data3 = (v2 shr 16) and 0xFFFFu
            val data4 = (java.lang.Integer.reverseBytes(v3.toInt()).toUInt().toULong() shl 32) or
                java.lang.Integer.reverseBytes(v4.toInt()).toUInt().toULong()
            return FGuid(v1, data2, data3, data4)
        }

        fun fromBytes(bytes: ByteArray, offset: Int = 0): FGuid {
            return FGuid(
                readU32LE(bytes, offset),
                readU16LE(bytes, offset + 4).toUInt(),
                readU16LE(bytes, offset + 6).toUInt(),
                readU64BE(bytes, offset + 8),
            )
        }

        fun fromUuid(uuid: java.util.UUID): FGuid {
            return FGuid(
                (uuid.mostSignificantBits ushr 32).toUInt(),
                ((uuid.mostSignificantBits ushr 16) and 0xFFFFL).toUInt(),
                (uuid.mostSignificantBits and 0xFFFFL).toUInt(),
                uuid.leastSignificantBits.toULong(),
            )
        }

        private fun writeU32LE(b: ByteArray, o: Int, v: UInt) {
            b[o] = (v and 0xFFu).toByte()
            b[o + 1] = ((v shr 8) and 0xFFu).toByte()
            b[o + 2] = ((v shr 16) and 0xFFu).toByte()
            b[o + 3] = ((v shr 24) and 0xFFu).toByte()
        }

        private fun writeU16LE(b: ByteArray, o: Int, v: UInt) {
            b[o] = (v and 0xFFu).toByte()
            b[o + 1] = ((v shr 8) and 0xFFu).toByte()
        }

        private fun writeU64BE(b: ByteArray, o: Int, v: ULong) {
            for (i in 0 until 8) b[o + i] = ((v shr (56 - i * 8)) and 0xFFu).toByte()
        }

        private fun readU32LE(b: ByteArray, o: Int): UInt {
            return (b[o].toUInt() and 0xFFu) or
                ((b[o + 1].toUInt() and 0xFFu) shl 8) or
                ((b[o + 2].toUInt() and 0xFFu) shl 16) or
                ((b[o + 3].toUInt() and 0xFFu) shl 24)
        }

        private fun readU16LE(b: ByteArray, o: Int): Int {
            return (b[o].toInt() and 0xFF) or ((b[o + 1].toInt() and 0xFF) shl 8)
        }

        private fun readU64BE(b: ByteArray, o: Int): ULong {
            var v = 0uL
            for (i in 0 until 8) v = (v shl 8) or (b[o + i].toULong() and 0xFFu)
            return v
        }
    }
}
