// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/CustomVersion.cs
package com.github.jpabscale.uasset4j

import com.github.jpabscale.uasset4j.unrealtypes.FGuid
import com.github.jpabscale.uasset4j.unrealtypes.FString

/**
 * A custom Version. Controls more specific serialization than the main engine object Version does.
 */
class CustomVersion {
    var Name: FString? = null
    var Key: FGuid
    var FriendlyName: String? = null
    var Version: Int
    var IsSerialized: Boolean = true

    fun SetIsSerialized(val_: Boolean): CustomVersion {
        this.IsSerialized = val_
        return this
    }

    fun clone(): CustomVersion {
        val res = CustomVersion(Key, Version)
        res.Name = Name
        res.FriendlyName = FriendlyName
        res.IsSerialized = IsSerialized
        return res
    }

    constructor(FriendlyName: String, Version: Int) {
        Key = GetCustomVersionGuidFromFriendlyName(FriendlyName)
        this.FriendlyName = FriendlyName
        this.Version = Version
    }

    constructor(Key: FGuid, Version: Int) {
        this.Key = Key
        if (GuidToCustomVersionStringMap.containsKey(Key)) FriendlyName = GuidToCustomVersionStringMap[Key]
        this.Version = Version
    }

    constructor() {
        Key = UnusedCustomVersionKey
        Version = 0
    }

    companion object {
        /** A GUID that represents an unused custom Version. */
        val UnusedCustomVersionKey: FGuid = FGuid.fromUnsignedInts(0u, 0u, 0u, 0xF99D40C1u)

        /**
         * Static map of custom Version GUIDs to the object or enum that they represent in the
         * Unreal Engine. Not necessarily exhaustive.
         */
        val GuidToCustomVersionStringMap: Map<FGuid, String> = linkedMapOf(
            UnusedCustomVersionKey to "UnusedCustomVersionKey",
            FGuid.fromUnsignedInts(0xB0D832E4u, 0x1F894F0Du, 0xACCF7EB7u, 0x36FD4AA2u) to "FBlueprintsObjectVersion",
            FGuid.fromUnsignedInts(0xE1C64328u, 0xA22C4D53u, 0xA36C8E86u, 0x6417BD8Cu) to "FBuildObjectVersion",
            FGuid.fromUnsignedInts(0x375EC13Cu, 0x06E448FBu, 0xB50084F0u, 0x262A717Eu) to "FCoreObjectVersion",
            FGuid.fromUnsignedInts(0xE4B068EDu, 0xF49442E9u, 0xA231DA0Bu, 0x2E46BB41u) to "FEditorObjectVersion",
            FGuid.fromUnsignedInts(0xCFFC743Fu, 0x43B04480u, 0x939114DFu, 0x171D2073u) to "FFrameworkObjectVersion",
            FGuid.fromUnsignedInts(0xB02B49B5u, 0xBB2044E9u, 0xA30432B7u, 0x52E40360u) to "FMobileObjectVersion",
            FGuid.fromUnsignedInts(0xA4E4105Cu, 0x59A149B5u, 0xA7C540C4u, 0x547EDFEEu) to "FNetworkingObjectVersion",
            FGuid.fromUnsignedInts(0x39C831C9u, 0x5AE647DCu, 0x9A449C17u, 0x3E1C8E7Cu) to "FOnlineObjectVersion",
            FGuid.fromUnsignedInts(0x78F01B33u, 0xEBEA4F98u, 0xB9B484EAu, 0xCCB95AA2u) to "FPhysicsObjectVersion",
            FGuid.fromUnsignedInts(0x6631380Fu, 0x2D4D43E0u, 0x8009CF27u, 0x6956A95Au) to "FPlatformObjectVersion",
            FGuid.fromUnsignedInts(0x12F88B9Fu, 0x88754AFCu, 0xA67CD90Cu, 0x383ABD29u) to "FRenderingObjectVersion",
            FGuid.fromUnsignedInts(0x7B5AE74Cu, 0xD2704C10u, 0xA9585798u, 0x0B212A5Au) to "FSequencerObjectVersion",
            FGuid.fromUnsignedInts(0xD7296918u, 0x1DD64BDDu, 0x9DE264A8u, 0x3CC13884u) to "FVRObjectVersion",
            FGuid.fromUnsignedInts(0xC2A15278u, 0xBFE74AFEu, 0x6C1790FFu, 0x531DF755u) to "FLoadTimesObjectVersion",
            FGuid.fromUnsignedInts(0x6EACA3D4u, 0x40EC4CC1u, 0xB7868BEDu, 0x09428FC5u) to "FGeometryObjectVersion",
            FGuid.fromUnsignedInts(0x29E575DDu, 0xE0A34627u, 0x9D10D276u, 0x232CDCEAu) to "FAnimPhysObjectVersion",
            FGuid.fromUnsignedInts(0xAF43A65Du, 0x7FD34947u, 0x98733E8Eu, 0xD9C1BB05u) to "FAnimObjectVersion",
            FGuid.fromUnsignedInts(0x6B266CECu, 0x1EC74B8Fu, 0xA30BE4D9u, 0x0942FC07u) to "FReflectionCaptureObjectVersion",
            FGuid.fromUnsignedInts(0x0DF73D61u, 0xA23F47EAu, 0xB72789E9u, 0x0C41499Au) to "FAutomationObjectVersion",
            FGuid.fromUnsignedInts(0x601D1886u, 0xAC644F84u, 0xAA16D3DEu, 0x0DEAC7D6u) to "FFortniteMainBranchObjectVersion",
            FGuid.fromUnsignedInts(0x9DFFBCD6u, 0x494F0158u, 0xE2211282u, 0x3C92A888u) to "FEnterpriseObjectVersion",
            FGuid.fromUnsignedInts(0xF2AED0ACu, 0x9AFE416Fu, 0x8664AA7Fu, 0xFA26D6FCu) to "FNiagaraObjectVersion",
            FGuid.fromUnsignedInts(0x174F1F0Bu, 0xB4C645A5u, 0xB13F2EE8u, 0xD0FB917Du) to "FDestructionObjectVersion",
            FGuid.fromUnsignedInts(0x35F94A83u, 0xE258406Cu, 0xA31809F5u, 0x9610247Cu) to "FExternalPhysicsCustomObjectVersion",
            FGuid.fromUnsignedInts(0xB68FC16Eu, 0x8B1B42E2u, 0xB453215Cu, 0x058844FEu) to "FExternalPhysicsMaterialCustomObjectVersion",
            FGuid.fromUnsignedInts(0xB2E18506u, 0x4273CFC2u, 0xA54EF4BBu, 0x758BBA07u) to "FCineCameraObjectVersion",
            FGuid.fromUnsignedInts(0x64F58936u, 0xFD1B42BAu, 0xBA967289u, 0xD5D0FA4Eu) to "FVirtualProductionObjectVersion",
            FGuid.fromUnsignedInts(0x6F0ED827u, 0xA6094895u, 0x9C91998Du, 0x90180EA4u) to "FMediaFrameworkObjectVersion",
            FGuid.fromUnsignedInts(0xAFE08691u, 0x3A0D4952u, 0xB673673Bu, 0x7CF22D1Eu) to "FPoseDriverCustomVersion",
            FGuid.fromUnsignedInts(0xCB8AB0CDu, 0xE78C4BDEu, 0xA8621393u, 0x14E9EF62u) to "FTempCustomVersion",
            FGuid.fromUnsignedInts(0x2EB5FDBDu, 0x01AC4D10u, 0x8136F38Fu, 0x3393A5DAu) to "FAnimationCustomVersion",
            FGuid.fromUnsignedInts(0x717F9EE7u, 0xE9B0493Au, 0x88B39132u, 0x1B388107u) to "FAssetRegistryVersion",
            FGuid.fromUnsignedInts(0xFB680AF2u, 0x59EF4BA3u, 0xBAA819B5u, 0x73C8443Du) to "FClothingAssetCustomVersion",
            FGuid.fromUnsignedInts(0x9C54D522u, 0xA8264FBEu, 0x94210746u, 0x61B482D0u) to "FReleaseObjectVersion",
            FGuid.fromUnsignedInts(0x4A56EB40u, 0x10F511DCu, 0x92D3347Eu, 0xB2C96AE7u) to "FParticleSystemCustomVersion",
            FGuid.fromUnsignedInts(0xD78A4A00u, 0xE8584697u, 0xBAA819B5u, 0x487D46B4u) to "FSkeletalMeshCustomVersion",
            FGuid.fromUnsignedInts(0x5579F886u, 0x933A4C1Fu, 0x83BA087Bu, 0x6361B92Fu) to "FRecomputeTangentCustomVersion",
            FGuid.fromUnsignedInts(0x612FBE52u, 0xDA53400Bu, 0x910D4F91u, 0x9FB1857Cu) to "FOverlappingVerticesCustomVersion",
            FGuid.fromUnsignedInts(0x430C4D19u, 0x71544970u, 0x87699B69u, 0xDF90B0E5u) to "FFoliageCustomVersion",
            FGuid.fromUnsignedInts(0xAAFE32BDu, 0x53954C14u, 0xB66A5E25u, 0x1032D1DDu) to "FProceduralFoliageCustomVersion",
            FGuid.fromUnsignedInts(0xAB965196u, 0x45D808FCu, 0xB7D7228Du, 0x78AD569Eu) to "FLiveLinkCustomVersion",
            FGuid.fromUnsignedInts(0xE7086368u, 0x6B234C58u, 0x84391B70u, 0x16265E91u) to "FFortniteReleaseBranchCustomObjectVersion",
            FGuid.fromUnsignedInts(0xD89B5E42u, 0x24BD4D46u, 0x8412ACA8u, 0xDF641779u) to "FUE5ReleaseStreamObjectVersion",
            FGuid.fromUnsignedInts(0xFCF57AFAu, 0x50764283u, 0xB9A9E658u, 0xFFA02D32u) to "FNiagaraCustomVersion",
            FGuid.fromUnsignedInts(0x697DD581u, 0xE64F41ABu, 0xAA4A51ECu, 0xBEB7B628u) to "FUE5MainStreamObjectVersion",
            FGuid.fromUnsignedInts(0x59DA5D52u, 0x12324948u, 0xB8785978u, 0x70B8E98Bu) to "FUE5SpecialProjectStreamObjectVersion",
            FGuid.fromUnsignedInts(0xE21E1CAAu, 0xAF47425Eu, 0x89BF6AD4u, 0x4C44A8BBu) to "FInstancedStructCustomVersion",
        )

        fun GetCustomVersionFriendlyNameFromGuid(guid: FGuid): String? {
            return GuidToCustomVersionStringMap[guid]
        }

        fun GetCustomVersionGuidFromFriendlyName(FriendlyName: String): FGuid {
            for ((Key, value) in GuidToCustomVersionStringMap) {
                if (value == FriendlyName) return Key
            }
            return UnusedCustomVersionKey
        }
    }
}
