// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/MonitoringStream.cs
// NOTE: The C# original is compiled only in DEBUG builds and wraps a bidirectional Stream.
// The JVM port wraps a java.io.InputStream and tracks the number of bytes read so the
// debug stop-offset logic can compare against a virtual stream position.
package com.github.jpabscale.uasset4j

/** Pass-through stream for debugging. Only present in debug builds. */
class MonitoringStream(
    private val InnerStream: java.io.InputStream,
    var Asset: UAsset? = null,
) {
    /** Number of bytes that have been consumed from [InnerStream] so far. */
    var Position: Long = 0
        private set(value) {
            field = value
        }

    fun Read(buffer: ByteArray, offset: Int, count: Int): Int {
        if (Asset?.IsParsingToPullSchemas == true) return InnerStream.read(buffer, offset, count)

        var ourStopOffset = StopOffset
        if (StopOffset >= 0 && IsUexpOffset) {
            if (Asset == null || Asset!!.Exports.size == 0) {
                ourStopOffset = -1
            } else {
                ourStopOffset += Asset!!.Exports[0].SerialOffset
            }
        }
        if (ourStopOffset >= 0 && Position <= ourStopOffset && (Position + count) > ourStopOffset) {
            val correctedPosition = if (IsUexpOffset) Position - Asset!!.Exports[0].SerialOffset else Position
            // Stop byte reached at read time: reading $count bytes starting at $correctedPosition
        }

        val bytesRead = InnerStream.read(buffer, offset, count)
        if (bytesRead > 0) Position += bytesRead
        return bytesRead
    }

    fun ReadBytes(n: Int): ByteArray {
        val buffer = ByteArray(n)
        var total = 0
        while (total < n) {
            val read = Read(buffer, total, n - total)
            if (read < 0) break
            total += read
        }
        return if (total == n) buffer else buffer.copyOfRange(0, total)
    }

    fun Skip(n: Long): Long {
        val skipped = InnerStream.skip(n)
        Position += skipped
        return skipped
    }

    companion object {
        /** Whether or not to enable monitoring. */
        var Enabled: Boolean = false

        /** Offset of a byte to place a breakpoint at for debugging purposes. Set to -1 to disable. */
        var StopOffset: Long = -1

        /** If true, [StopOffset] is interpreted as an offset relative to the start of the .uexp file. */
        var IsUexpOffset: Boolean = true
    }
}
