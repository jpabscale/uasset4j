// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Movies/MovieSceneEvaluationTree.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.movies

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter

class FEntry {
    var StartIndex: Int = 0
    var Size: Int = 0
    var Capacity: Int = 0

    constructor(startIndex: Int, size: Int, capacity: Int) {
        StartIndex = startIndex
        Size = size
        Capacity = capacity
    }

    constructor(reader: AssetBinaryReader) {
        StartIndex = reader.ReadInt32()
        Size = reader.ReadInt32()
        Capacity = reader.ReadInt32()
    }

    constructor()

    fun Write(writer: AssetBinaryWriter) {
        writer.WriteInt32(StartIndex)
        writer.WriteInt32(Size)
        writer.WriteInt32(Capacity)
    }
}

class TEvaluationTreeEntryContainer<T> {
    var Entries: Array<FEntry> = emptyArray()
    @Suppress("UNCHECKED_CAST")
    var Items: Array<T> = emptyArray<Any>() as Array<T>

    constructor(entries: Array<FEntry>, items: Array<T>) {
        Entries = entries
        Items = items
    }

    constructor(reader: AssetBinaryReader?, valueReader: () -> T) {
        if (reader != null) {
            val entriesamount = reader.ReadInt32()
            Entries = Array(entriesamount) { FEntry(reader) }

            val itemsamount = reader.ReadInt32()
            @Suppress("UNCHECKED_CAST")
            val newItems = arrayOfNulls<Any>(itemsamount) as Array<T>
            for (i in 0 until itemsamount) newItems[i] = valueReader()
            Items = newItems
        }
    }

    fun Write(writer: AssetBinaryWriter, valueWriter: (T) -> Unit) {
        writer.WriteInt32(Entries.size)
        for (i in Entries.indices) {
            Entries[i].Write(writer)
        }

        writer.WriteInt32(Items.size)
        for (i in Items.indices) {
            valueWriter(Items[i])
        }
    }
}

open class FMovieSceneEvaluationTree(reader: AssetBinaryReader?) {
    var RootNode: FMovieSceneEvaluationTreeNode = FMovieSceneEvaluationTreeNode(reader)
    var ChildNodes: TEvaluationTreeEntryContainer<FMovieSceneEvaluationTreeNode> =
        TEvaluationTreeEntryContainer(reader) { FMovieSceneEvaluationTreeNode(reader) }

    fun Write(writer: AssetBinaryWriter) {
        RootNode.Write(writer)
        ChildNodes.Write(writer) { it.Write(writer) }
    }
}

class TMovieSceneEvaluationTree<T>(
    reader: AssetBinaryReader?,
    valueReader: () -> T,
) : FMovieSceneEvaluationTree(reader) {
    var Data: TEvaluationTreeEntryContainer<T> = TEvaluationTreeEntryContainer(null) { valueReader() }

    init {
        if (reader != null) Data = TEvaluationTreeEntryContainer(reader, valueReader)
    }

    fun Write(writer: AssetBinaryWriter, valueWriter: (T) -> Unit) {
        super.Write(writer)
        Data.Write(writer, valueWriter)
    }
}
