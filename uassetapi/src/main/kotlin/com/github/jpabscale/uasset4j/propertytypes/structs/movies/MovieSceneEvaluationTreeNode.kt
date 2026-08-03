// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Movies/MovieSceneEvaluationTreeNode.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.movies

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter

class FEvaluationTreeEntryHandle {
    var EntryIndex: Int = 0

    constructor(_EntryIndex: Int) {
        EntryIndex = _EntryIndex
    }

    constructor(reader: AssetBinaryReader) {
        EntryIndex = reader.ReadInt32()
    }

    constructor()

    fun Write(writer: AssetBinaryWriter) {
        writer.WriteInt32(EntryIndex)
    }
}

class FMovieSceneEvaluationTreeNodeHandle {
    var ChildrenHandle: FEvaluationTreeEntryHandle = FEvaluationTreeEntryHandle()
    var Index: Int = 0

    constructor(_ChildrenHandle: Int, _Index: Int) {
        ChildrenHandle.EntryIndex = _ChildrenHandle
        Index = _Index
    }

    constructor(reader: AssetBinaryReader) {
        ChildrenHandle = FEvaluationTreeEntryHandle(reader.ReadInt32())
        Index = reader.ReadInt32()
    }

    constructor()

    fun Write(writer: AssetBinaryWriter) {
        writer.WriteInt32(ChildrenHandle.EntryIndex)
        writer.WriteInt32(Index)
    }
}

class FMovieSceneEvaluationTreeNode {
    var Range: TRange<FFrameNumber> = TRange(TRangeBound<FFrameNumber>(), TRangeBound<FFrameNumber>())
    var Parent: FMovieSceneEvaluationTreeNodeHandle = FMovieSceneEvaluationTreeNodeHandle()
    var ChildrenID: FEvaluationTreeEntryHandle = FEvaluationTreeEntryHandle()
    var DataID: FEvaluationTreeEntryHandle = FEvaluationTreeEntryHandle()

    constructor(reader: AssetBinaryReader?) {
        if (reader != null) {
            Range = TRange(reader) { FFrameNumber(reader) }
            Parent = FMovieSceneEvaluationTreeNodeHandle(reader.ReadInt32(), reader.ReadInt32())
            ChildrenID = FEvaluationTreeEntryHandle(reader.ReadInt32())
            DataID = FEvaluationTreeEntryHandle(reader.ReadInt32())
        }
    }

    fun Write(writer: AssetBinaryWriter) {
        Range.Write(writer) { it.Write(writer) }
        writer.WriteInt32(Parent.ChildrenHandle.EntryIndex)
        writer.WriteInt32(Parent.Index)
        writer.WriteInt32(ChildrenID.EntryIndex)
        writer.WriteInt32(DataID.EntryIndex)
    }
}
