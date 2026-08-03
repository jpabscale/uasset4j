// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/ExportTypes/StringTableExport.cs
package com.github.jpabscale.uasset4j.exporttypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.unrealtypes.FString

/**
 * A string table. Holds Key->SourceString pairs of text.
 */
class FStringTable : LinkedHashMap<FString, FString> {
    var TableNamespace: FString? = null

    constructor(tableNamespace: FString?) : super() {
        TableNamespace = tableNamespace
    }

    constructor() : super()
}

/**
 * Export data for a string table. See [FStringTable].
 */
class StringTableExport : NormalExport {
    var Table: FStringTable? = null

    constructor(superExport: Export) : super(superExport)

    constructor(data: FStringTable, asset: UAsset?, extras: ByteArray?) : super(asset, extras) {
        Table = data
    }

    constructor() : super()

    override fun Read(reader: AssetBinaryReader, nextStarting: Int) {
        super.Read(reader, nextStarting)

        Table = FStringTable(reader.ReadFString())

        val numEntries = reader.ReadInt32()
        for (i in 0 until numEntries) {
            Table!!.put(reader.ReadFString()!!, reader.ReadFString()!!)
        }
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)

        writer.Write(Table!!.TableNamespace)
        writer.WriteInt32(Table!!.size)
        for (i in 0 until Table!!.size) {
            writer.Write(Table!!.keys.elementAt(i))
            writer.Write(Table!!.values.elementAt(i))
        }
    }
}
