// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Import.cs
package com.github.jpabscale.uasset4j

import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5

/**
 * UObject resource type for objects that are referenced by this package, but contained within
 * another package.
 */
class Import {
    /** The name of the UObject represented by this resource. */
    var ObjectName: FName? = null

    /** Location of the resource for this resource's Outer (import/other export). 0 = top-level UPackage. */
    var OuterIndex: FPackageIndex? = null
    var ClassPackage: FName? = null
    var ClassName: FName? = null

    /** Package name this import belongs to. Can be none; follow the outer chain until a set PackageName is found. */
    var PackageName: FName? = null
    var bImportOptional: Boolean = false

    constructor(ClassPackage: String, ClassName: String, OuterIndex: FPackageIndex, ObjectName: String, importOptional: Boolean, asset: UAsset) {
        this.ObjectName = FName(asset, ObjectName)
        this.OuterIndex = OuterIndex
        this.ClassPackage = FName(asset, ClassPackage)
        this.ClassName = FName(asset, ClassName)
        bImportOptional = importOptional
    }

    constructor(ClassPackage: FName?, ClassName: FName?, OuterIndex: FPackageIndex, ObjectName: FName?, importOptional: Boolean) {
        this.ObjectName = ObjectName
        this.OuterIndex = OuterIndex
        this.ClassPackage = ClassPackage
        this.ClassName = ClassName
        bImportOptional = importOptional
    }

    constructor(reader: AssetBinaryReader) {
        ClassPackage = reader.ReadFName()
        ClassName = reader.ReadFName()
        OuterIndex = FPackageIndex(reader.ReadInt32())
        ObjectName = reader.ReadFName()

        val a = reader.Asset
        if (a?.ObjectVersion != null &&
            a.ObjectVersion >= ObjectVersion.VER_UE4_NON_OUTER_PACKAGE_IMPORT &&
            !a.IsFilterEditorOnly
        ) {
            PackageName = reader.ReadFName()
        }

        if (a?.ObjectVersionUE5 != null && a.ObjectVersionUE5 >= ObjectVersionUE5.OPTIONAL_RESOURCES) {
            bImportOptional = reader.ReadInt32() == 1
        }
    }

    constructor()
}
