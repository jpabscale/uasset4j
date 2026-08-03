// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/FPropertyTypeName.cs
package com.github.jpabscale.uasset4j.unrealtypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter

class FPropertyTypeNameNode {
    var Name: FName
    var InnerCount: Int

    constructor(Ar: AssetBinaryReader) {
        Name = Ar.ReadFName()
        InnerCount = Ar.ReadInt32()
    }

    constructor(Name: FName, InnerCount: Int) {
        this.Name = Name
        this.InnerCount = InnerCount
    }
}

class FPropertyTypeName {
    var Nodes: MutableList<FPropertyTypeNameNode> = mutableListOf()
    var ShouldSerializeNodes: Boolean = true

    constructor(list: List<FPropertyTypeNameNode>, shouldSerialize: Boolean = false) {
        Nodes = list.toMutableList()
        ShouldSerializeNodes = shouldSerialize
    }

    constructor(reader: AssetBinaryReader) {
        Nodes = mutableListOf()
        var totalNodes = 1
        var i = 0
        while (i < totalNodes) {
            val node = FPropertyTypeNameNode(reader)
            Nodes.add(node)
            totalNodes += node.InnerCount
            i++
        }
    }

    fun Write(writer: AssetBinaryWriter) {
        for (node in Nodes) {
            writer.Write(node.Name)
            writer.WriteInt32(node.InnerCount)
        }
    }

    fun GetName(): FName = if (Nodes.isNotEmpty()) Nodes[0].Name else FName.DefineDummy(null, "None")

    fun GetParameter(paramIndex: Int): FPropertyTypeName {
        if (Nodes.isEmpty() || paramIndex < 0 || paramIndex >= Nodes[0].InnerCount) return FPropertyTypeName(emptyList())

        var param = 1
        var skip = paramIndex
        while (skip > 0) {
            skip += Nodes[param].InnerCount
            skip--
            param++
        }

        return FPropertyTypeName(Nodes.subList(param, Nodes.size).toMutableList())
    }
}
