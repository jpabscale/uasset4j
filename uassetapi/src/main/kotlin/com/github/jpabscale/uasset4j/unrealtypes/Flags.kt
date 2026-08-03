// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Flags.cs
// NOTE: M2 — only EPropertyFlags ported (needed by Usmap EATR extension). EObjectFlags landed
// with its consumer (FField). The other [Flags] enums (EClassFlags, EFunctionFlags,
// EStructFlags) land with their consumers.
@file:OptIn(kotlin.ExperimentalUnsignedTypes::class)

package com.github.jpabscale.uasset4j.unrealtypes

@JvmInline
value class EObjectFlags(val value: Long) {
    fun hasFlag(flag: Long): Boolean = (value and flag) == flag

    companion object {
        val RF_NoFlags = EObjectFlags(0x00000000L)
        val RF_Public = EObjectFlags(0x00000001L)
        val RF_Standalone = EObjectFlags(0x00000002L)
        val RF_MarkAsNative = EObjectFlags(0x00000004L)
        val RF_Transactional = EObjectFlags(0x00000008L)
        val RF_ClassDefaultObject = EObjectFlags(0x00000010L)
        val RF_ArchetypeObject = EObjectFlags(0x00000020L)
        val RF_Transient = EObjectFlags(0x00000040L)
        val RF_MarkAsRootSet = EObjectFlags(0x00000080L)
        val RF_TagGarbageTemp = EObjectFlags(0x00000100L)
        val RF_NeedInitialization = EObjectFlags(0x00000200L)
        val RF_NeedLoad = EObjectFlags(0x00000400L)
        val RF_KeepForCooker = EObjectFlags(0x00000800L)
        val RF_NeedPostLoad = EObjectFlags(0x00001000L)
        val RF_NeedPostLoadSubobjects = EObjectFlags(0x00002000L)
        val RF_NewerVersionExists = EObjectFlags(0x00004000L)
        val RF_BeginDestroyed = EObjectFlags(0x00008000L)
        val RF_FinishDestroyed = EObjectFlags(0x00010000L)
        val RF_BeingRegenerated = EObjectFlags(0x00020000L)
        val RF_DefaultSubObject = EObjectFlags(0x00040000L)
        val RF_WasLoaded = EObjectFlags(0x00080000L)
        val RF_TextExportTransient = EObjectFlags(0x00100000L)
        val RF_LoadCompleted = EObjectFlags(0x00200000L)
        val RF_InheritableComponentTemplate = EObjectFlags(0x00400000L)
        val RF_DuplicateTransient = EObjectFlags(0x00800000L)
        val RF_StrongRefOnFrame = EObjectFlags(0x01000000L)
        val RF_NonPIEDuplicateTransient = EObjectFlags(0x02000000L)
        val RF_Dynamic = EObjectFlags(0x04000000L)
        val RF_WillBeLoaded = EObjectFlags(0x08000000L)
        val RF_HasExternalPackage = EObjectFlags(0x10000000L)
    }
}

@JvmInline
value class EClassFlags(val value: Int) {
    fun HasFlag(flag: Int): Boolean = (value and flag) == flag

    companion object {
        const val CLASS_None = 0x00000000
        const val CLASS_Abstract = 0x00000001
        const val CLASS_DefaultConfig = 0x00000002
        const val CLASS_Config = 0x00000004
        const val CLASS_Transient = 0x00000008
        const val CLASS_Parsed = 0x00000010
        const val CLASS_MatchedSerializers = 0x00000020
        const val CLASS_ProjectUserConfig = 0x00000040
        const val CLASS_Native = 0x00000080
        const val CLASS_NoExport = 0x00000100
        const val CLASS_NotPlaceable = 0x00000200
        const val CLASS_PerObjectConfig = 0x00000400
        const val CLASS_ReplicationDataIsSetUp = 0x00000800
        const val CLASS_EditInlineNew = 0x00001000
        const val CLASS_CollapseCategories = 0x00002000
        const val CLASS_Interface = 0x00004000
        const val CLASS_CustomConstructor = 0x00008000
        const val CLASS_Const = 0x00010000
        const val CLASS_LayoutChanging = 0x00020000
        const val CLASS_CompiledFromBlueprint = 0x00040000
        const val CLASS_MinimalAPI = 0x00080000
        const val CLASS_RequiredAPI = 0x00100000
        const val CLASS_DefaultToInstanced = 0x00200000
        const val CLASS_TokenStreamAssembled = 0x00400000
        const val CLASS_HasInstancedReference = 0x00800000
        const val CLASS_Hidden = 0x01000000
        const val CLASS_Deprecated = 0x02000000
        const val CLASS_HideDropDown = 0x04000000
        const val CLASS_GlobalUserConfig = 0x08000000
        const val CLASS_Intrinsic = 0x10000000
        const val CLASS_Constructed = 0x20000000
        const val CLASS_ConfigDoNotCheckDefaults = 0x40000000
        const val CLASS_NewerVersionExists = 0x80000000.toInt()
    }
}

@JvmInline
value class EFunctionFlags(val value: Int) {
    fun HasFlag(flag: Int): Boolean = (value and flag) == flag

    companion object {
        const val FUNC_None = 0x00000000
        const val FUNC_Final = 0x00000001
        const val FUNC_RequiredAPI = 0x00000002
        const val FUNC_BlueprintAuthorityOnly = 0x00000004
        const val FUNC_BlueprintCosmetic = 0x00000008
        const val FUNC_Net = 0x00000040
        const val FUNC_NetReliable = 0x00000080
        const val FUNC_NetRequest = 0x00000100
        const val FUNC_Exec = 0x00000200
        const val FUNC_Native = 0x00000400
        const val FUNC_Event = 0x00000800
        const val FUNC_NetResponse = 0x00001000
        const val FUNC_Static = 0x00002000
        const val FUNC_NetMulticast = 0x00004000
        const val FUNC_UbergraphFunction = 0x00008000
        const val FUNC_MulticastDelegate = 0x00010000
        const val FUNC_Public = 0x00020000
        const val FUNC_Private = 0x00040000
        const val FUNC_Protected = 0x00080000
        const val FUNC_Delegate = 0x00100000
        const val FUNC_NetServer = 0x00200000
        const val FUNC_HasOutParms = 0x00400000
        const val FUNC_HasDefaults = 0x00800000
        const val FUNC_NetClient = 0x01000000
        const val FUNC_DLLImport = 0x02000000
        const val FUNC_BlueprintCallable = 0x04000000
        const val FUNC_BlueprintEvent = 0x08000000
        const val FUNC_BlueprintPure = 0x10000000
        const val FUNC_EditorOnly = 0x20000000
        const val FUNC_Const = 0x40000000
        const val FUNC_NetValidate = 0x80000000.toInt()
        const val FUNC_AllFlags = -1
    }
}

@JvmInline
value class EPropertyFlags(val value: Long) {
    fun hasFlag(flag: Long): Boolean = (value and flag) == flag

    companion object {
        val CPF_None = EPropertyFlags(0L)
        val CPF_Edit = EPropertyFlags(0x0000000000000001L)
        val CPF_ConstParm = EPropertyFlags(0x0000000000000002L)
        val CPF_BlueprintVisible = EPropertyFlags(0x0000000000000004L)
        val CPF_ExportObject = EPropertyFlags(0x0000000000000008L)
        val CPF_BlueprintReadOnly = EPropertyFlags(0x0000000000000010L)
        val CPF_Net = EPropertyFlags(0x0000000000000020L)
        val CPF_EditFixedSize = EPropertyFlags(0x0000000000000040L)
        val CPF_Parm = EPropertyFlags(0x0000000000000080L)
        val CPF_OutParm = EPropertyFlags(0x0000000000000100L)
        val CPF_ZeroConstructor = EPropertyFlags(0x0000000000000200L)
        val CPF_ReturnParm = EPropertyFlags(0x0000000000000400L)
        val CPF_DisableEditOnTemplate = EPropertyFlags(0x0000000000000800L)
        val CPF_Transient = EPropertyFlags(0x0000000000002000L)
        val CPF_Config = EPropertyFlags(0x0000000000004000L)
        val CPF_DisableEditOnInstance = EPropertyFlags(0x0000000000010000L)
        val CPF_EditConst = EPropertyFlags(0x0000000000020000L)
        val CPF_GlobalConfig = EPropertyFlags(0x0000000000040000L)
        val CPF_InstancedReference = EPropertyFlags(0x0000000000080000L)
        val CPF_DuplicateTransient = EPropertyFlags(0x0000000000200000L)
        val CPF_SaveGame = EPropertyFlags(0x0000000001000000L)
        val CPF_NoClear = EPropertyFlags(0x0000000002000000L)
        val CPF_ReferenceParm = EPropertyFlags(0x0000000008000000L)
        val CPF_BlueprintAssignable = EPropertyFlags(0x0000000010000000L)
        val CPF_Deprecated = EPropertyFlags(0x0000000020000000L)
        val CPF_IsPlainOldData = EPropertyFlags(0x0000000040000000L)
        val CPF_RepSkip = EPropertyFlags(0x0000000080000000L)
        val CPF_RepNotify = EPropertyFlags(0x0000000100000000L)
        val CPF_Interp = EPropertyFlags(0x0000000200000000L)
        val CPF_NonTransactional = EPropertyFlags(0x0000000400000000L)
        val CPF_EditorOnly = EPropertyFlags(0x0000000800000000L)
        val CPF_NoDestructor = EPropertyFlags(0x0000001000000000L)
        val CPF_AutoWeak = EPropertyFlags(0x0000004000000000L)
        val CPF_ContainsInstancedReference = EPropertyFlags(0x0000008000000000L)
        val CPF_AssetRegistrySearchable = EPropertyFlags(0x0000010000000000L)
        val CPF_SimpleDisplay = EPropertyFlags(0x0000020000000000L)
        val CPF_AdvancedDisplay = EPropertyFlags(0x0000040000000000L)
        val CPF_Protected = EPropertyFlags(0x0000080000000000L)
        val CPF_BlueprintCallable = EPropertyFlags(0x0000100000000000L)
        val CPF_BlueprintAuthorityOnly = EPropertyFlags(0x0000200000000000L)
        val CPF_TextExportTransient = EPropertyFlags(0x0000400000000000L)
        val CPF_NonPIEDuplicateTransient = EPropertyFlags(0x0000800000000000L)
        val CPF_ExposeOnSpawn = EPropertyFlags(0x0001000000000000L)
        val CPF_PersistentInstance = EPropertyFlags(0x0002000000000000L)
        val CPF_UObjectWrapper = EPropertyFlags(0x0004000000000000L)
        val CPF_HasGetValueTypeHash = EPropertyFlags(0x0008000000000000L)
        val CPF_NativeAccessSpecifierPublic = EPropertyFlags(0x0010000000000000L)
        val CPF_NativeAccessSpecifierProtected = EPropertyFlags(0x0020000000000000L)
        val CPF_NativeAccessSpecifierPrivate = EPropertyFlags(0x0040000000000000L)
        val CPF_SkipSerialization = EPropertyFlags(0x0080000000000000L)
    }
}
