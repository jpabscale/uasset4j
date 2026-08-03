// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Unversioned/UsmapBinaryReader.cs
package com.github.jpabscale.uasset4j

import com.github.jpabscale.uasset4j.unversioned.Usmap

/** Reads primitive data types from .usmap files. */
class UsmapBinaryReader(
    private val data: ByteArray,
    var File: Usmap? = null,
) {
    var position: Int = 0
        set(value) {
            if (value < 0) throw IndexOutOfBoundsException("negative position $value")
            field = value
        }
    val length: Int get() = data.size

    private fun require(n: Int) {
        if (position + n > data.size) throw java.io.EOFException("Unexpected end of stream at $position")
    }

    fun ReadInt16(): Short {
        require(2)
        val v = (data[position].toInt() and 0xFF) or ((data[position + 1].toInt() and 0xFF) shl 8)
        position += 2
        return v.toShort()
    }

    fun ReadUInt16(): Int {
        require(2)
        val v = (data[position].toInt() and 0xFF) or ((data[position + 1].toInt() and 0xFF) shl 8)
        position += 2
        return v
    }

    fun ReadInt32(): Int {
        require(4)
        val v = (data[position].toInt() and 0xFF) or
            ((data[position + 1].toInt() and 0xFF) shl 8) or
            ((data[position + 2].toInt() and 0xFF) shl 16) or
            (data[position + 3].toInt() shl 24)
        position += 4
        return v
    }

    fun ReadUInt32(): Long = ReadInt32().toLong() and 0xFFFFFFFFL

    fun ReadInt64(): Long {
        require(8)
        var v = 0L
        for (i in 0 until 8) v = v or ((data[position + i].toLong() and 0xFFL) shl (8 * i))
        position += 8
        return v
    }

    fun ReadUInt64(): Long = ReadInt64()

    fun ReadSingle(): Float = Float.fromBits(ReadInt32())

    fun ReadDouble(): Double = Double.fromBits(ReadInt64())

    fun ReadByte(): Int {
        require(1)
        return data[position++].toInt() and 0xFF
    }

    fun ReadBytes(n: Int): ByteArray {
        if (n < 0) throw IndexOutOfBoundsException("n cannot be negative")
        if (n == 0) return ByteArray(0)
        require(n)
        val out = data.copyOfRange(position, position + n)
        position += n
        return out
    }

    fun ReadString(fixedLength: Int = -1): String? {
        val length = if (fixedLength > -1) fixedLength else ReadByte()
        return when (length) {
            0 -> null
        else -> {
            val bytes = ReadBytes(length)
            val chars = CharArray(bytes.size) {
                if ((bytes[it].toInt() and 0xFF) < 0x80) bytes[it].toInt().toChar() else '?'
            }
            String(chars)
        }
        }
    }

    fun ReadName(): String? {
        val v = ReadInt32()
        if (v < 0) return null
        return File?.NameMap?.get(v)
    }
}
