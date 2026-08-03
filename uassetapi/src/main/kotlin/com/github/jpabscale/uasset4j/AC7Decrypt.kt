// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/AC7Decrypt.cs
package com.github.jpabscale.uasset4j

import java.io.File

/** XOR key for decrypting a particular Ace Combat 7 asset. */
class AC7XorKey {
    var NameKey: Int = 0
    var Offset: Int = 0
    var pk1: Int = 0
    var pk2: Int = 0

    private fun SkipCount(count: Int) {
        var num = count % 217
        pk1 += num
        if (pk1 >= 217) {
            pk1 -= 217
        }
        var num2 = count % 1024
        pk2 += num2
        if (pk2 >= 1024) {
            pk2 -= 1024
        }
    }

    private companion object {
        private fun CalcNameKey(fname: String): Int {
            val upper = fname.uppercase()
            var num = 0
            for (i in upper.indices) {
                var num2 = upper[i].code and 0xFF
                num = num xor num2
                num2 = num * 8
                num2 = num2 xor num
                val num3 = num + num
                num2 = num2.inv()
                num2 = (num2 shr 7) and 1
                num = num2 or num3
            }
            return num
        }

        private fun CalcPKeyFromNKey(nkey: Int, dataoffset: Int): Pair<Int, Int> {
            var num = (nkey.toLong() * 7L) and 0xFFFFFFFFL
            val bigInteger = java.math.BigInteger("5440514381186227205")
            num += dataoffset
            val bigInteger2 = bigInteger.multiply(java.math.BigInteger.valueOf(num))
            var num2 = bigInteger2.shiftRight(70).toLong()
            var num3 = num2 shr 63
            num2 += num3
            num3 = num2 * 217
            num -= num3
            val pk1 = (num and 0xFFFFFFFFL).toInt()
            var num4 = (nkey.toLong() * 11L) and 0xFFFFFFFFL
            num4 += dataoffset
            num2 = 0L
            num2 = num2 and 0x3FF
            num4 += num2
            num4 = num4 and 0x3FF
            val num5 = num4 - num2
            val pk2 = (num5 and 0xFFFFFFFFL).toInt()
            return pk1 to pk2
        }
    }

    /** Generates an encryption key for a particular asset on disk. */
    constructor(fname: String) {
        NameKey = CalcNameKey(fname)
        Offset = 4
        val (pk1v, pk2v) = CalcPKeyFromNKey(this.NameKey, this.Offset)
        this.pk1 = pk1v
        this.pk2 = pk2v
    }
}

/** Decryptor for Ace Combat 7 assets. */
class AC7Decrypt {
    /** Decrypts an Ace Combat 7 encrypted asset on disk. */
    fun Decrypt(input: String, output: String) {
        val xorKey = AC7XorKey(File(input).nameWithoutExtension)
        val doneData = DecryptUAssetBytes(File(input).readBytes(), xorKey)
        File(output).writeBytes(doneData)
        try {
            val doneData2 = DecryptUexpBytes(File(ChangeExtension(input, "uexp")).readBytes(), xorKey)
            File(ChangeExtension(output, "uexp")).writeBytes(doneData2)
        } catch (_: Exception) {
        }
    }

    /** Encrypts an Ace Combat 7 encrypted asset on disk. */
    fun Encrypt(input: String, output: String) {
        val xorKey = AC7XorKey(File(output).nameWithoutExtension)
        val doneData = EncryptUAssetBytes(File(input).readBytes(), xorKey)
        File(output).writeBytes(doneData)
        try {
            val doneData2 = EncryptUexpBytes(File(ChangeExtension(input, "uexp")).readBytes(), xorKey)
            File(ChangeExtension(output, "uexp")).writeBytes(doneData2)
        } catch (_: Exception) {
        }
    }

    fun DecryptUAssetBytes(uasset: ByteArray, xorkey: AC7XorKey?): ByteArray {
        if (xorkey == null) throw NullPointerException("Null key provided")
        val array = ByteArray(uasset.size)
        WriteUInt32LE(array, 0, UAsset.UASSET_MAGIC)
        for (i in 4 until array.size) {
            array[i] = GetXorByte(uasset[i], xorkey)
        }
        return array
    }

    fun EncryptUAssetBytes(uasset: ByteArray, xorkey: AC7XorKey?): ByteArray {
        if (xorkey == null) throw NullPointerException("Null key provided")
        val array = ByteArray(uasset.size)
        WriteUInt32LE(array, 0, UAsset.ACE7_MAGIC)
        for (i in 4 until array.size) {
            array[i] = GetXorByte(uasset[i], xorkey)
        }
        return array
    }

    fun DecryptUexpBytes(uexp: ByteArray, xorkey: AC7XorKey?): ByteArray {
        if (xorkey == null) throw NullPointerException("Null key provided")
        val array = ByteArray(uexp.size)
        for (i in array.indices) {
            array[i] = GetXorByte(uexp[i], xorkey)
        }
        WriteUInt32LE(array, array.size - 4, UAsset.UASSET_MAGIC)
        return array
    }

    fun EncryptUexpBytes(uexp: ByteArray, xorkey: AC7XorKey?): ByteArray {
        if (xorkey == null) throw NullPointerException("Null key provided")
        val array = ByteArray(uexp.size)
        for (i in uexp.indices) {
            array[i] = GetXorByte(uexp[i], xorkey)
        }
        return array
    }

    private fun GetXorByte(tagb: Byte, xorkey: AC7XorKey): Byte {
        val keyByte = AC7FullKey[xorkey.pk1 * 1024 + xorkey.pk2].toInt() and 0xFF
        val result = ((tagb.toInt() and 0xFF) xor keyByte xor 0x77) and 0xFF
        xorkey.pk1++
        xorkey.pk2++
        if (xorkey.pk1 >= 217) {
            xorkey.pk1 = 0
        }
        if (xorkey.pk2 >= 1024) {
            xorkey.pk2 = 0
        }
        return result.toByte()
    }

    private companion object {
        val AC7FullKey: ByteArray by lazy {
            val stream = AC7Decrypt::class.java.classLoader.getResourceAsStream("AC7Key.bin")
                ?: throw IllegalStateException("AC7Key.bin resource is missing")
            stream.use { it.readBytes() }
        }

        fun ChangeExtension(path: String, newExtension: String): String {
            val index = path.lastIndexOf('.')
            return if (index == -1) path + "." + newExtension else path.substring(0, index) + "." + newExtension
        }

        fun WriteUInt32LE(array: ByteArray, offset: Int, value: Long) {
            array[offset] = (value and 0xFF).toByte()
            array[offset + 1] = ((value shr 8) and 0xFF).toByte()
            array[offset + 2] = ((value shr 16) and 0xFF).toByte()
            array[offset + 3] = ((value shr 24) and 0xFF).toByte()
        }
    }
}
