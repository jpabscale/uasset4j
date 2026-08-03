// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/FTextSourceData.cs, FTextSourceSiteContext.cs, FGatherableTextData.cs
package com.github.jpabscale.uasset4j.unrealtypes

/** Source data for a gatherable text item. */
class FTextSourceData {
    var SourceString: FString? = null
    var SourceStringMetaData: FLocMetadataObject? = null
}

/** Site context for a gatherable text item. */
class FTextSourceSiteContext {
    var KeyName: FString? = null
    var SiteDescription: FString? = null
    var IsEditorOnly: Boolean = false
    var IsOptional: Boolean = false
    var InfoMetaData: FLocMetadataObject? = null
    var KeyMetaData: FLocMetadataObject? = null
}

/** Gatherable text data item. */
class FGatherableTextData {
    var NamespaceName: FString? = null
    var SourceData: FTextSourceData? = null
    var SourceSiteContexts: MutableList<FTextSourceSiteContext> = mutableListOf()
}
