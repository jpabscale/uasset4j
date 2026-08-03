// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/ExportTypes/ActorComponentExport.cs
package com.github.jpabscale.uasset4j.exporttypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.customversions.FFortniteReleaseBranchCustomObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.FGuid
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex

class FSimpleMemberReference {
    var MemberParent: FPackageIndex? = null
    var MemberName: FName? = null
    var MemberGuid: FGuid? = null
}

open class ActorComponentExport : NormalExport {
    var UCSModifiedProperties: MutableList<FSimpleMemberReference>? = null

    constructor(superExport: Export) : super(superExport)

    constructor(asset: UAsset?, extras: ByteArray?) : super(asset, extras)

    constructor() : super()

    override fun Read(reader: AssetBinaryReader, nextStarting: Int) {
        super.Read(reader, nextStarting)
        if (reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFortniteReleaseBranchCustomObjectVersion")) >= FFortniteReleaseBranchCustomObjectVersion.ActorComponentUCSModifiedPropertiesSparseStorage.ordinal) {
            UCSModifiedProperties = mutableListOf()
            val count = reader.ReadInt32()
            for (i in 0 until count) {
                val MemberParent = FPackageIndex(reader)
                val MemberName = reader.ReadFName()
                val MemberGuid = reader.ReadGuid()
                UCSModifiedProperties!!.add(
                    FSimpleMemberReference().apply {
                        this.MemberParent = MemberParent
                        this.MemberName = MemberName
                        this.MemberGuid = MemberGuid
                    },
                )
            }
        }
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        if (writer.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFortniteReleaseBranchCustomObjectVersion")) >= FFortniteReleaseBranchCustomObjectVersion.ActorComponentUCSModifiedPropertiesSparseStorage.ordinal) {
            writer.WriteInt32(UCSModifiedProperties!!.size)
            for (i in UCSModifiedProperties!!.indices) {
                writer.WriteInt32(UCSModifiedProperties!![i].MemberParent!!.Index)
                writer.Write(UCSModifiedProperties!![i].MemberName)
                writer.WriteGuid(UCSModifiedProperties!![i].MemberGuid!!)
            }
        }
    }
}
