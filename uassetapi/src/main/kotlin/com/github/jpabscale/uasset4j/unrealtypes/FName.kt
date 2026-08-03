// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/FName.cs
package com.github.jpabscale.uasset4j.unrealtypes

import com.github.jpabscale.uasset4j.INameMap
import com.github.jpabscale.uasset4j.util.Out

enum class EMappedNameType {
    Package,
    Container,
    Global,
}

/**
 * Unreal name — consists of an FString (which is serialized as an Index in the name map) and an
 * instance Number.
 */
class FName {
    /** Instance Number. */
    var Number: Int = 0

    /**
     * The Type of this FName; i.e. whether it points to a package-level name table, container-level
     * name table, or global name table. Always [EMappedNameType.Package] for non-Zen assets.
     */
    var Type: EMappedNameType = EMappedNameType.Package

    /** Does this FName point into the global name table? Always false for non-Zen assets. */
    val IsGlobal: Boolean get() = Type != EMappedNameType.Package

    /** The Asset that this FName is bound to. */
    var Asset: INameMap? = null

    /** Dummy Value. If defined, this FName does not actually point to a Value in any name map. */
    var DummyValue: FString? = null

    /** Is this FName a dummy (its [DummyValue] is set)? */
    val IsDummy: Boolean get() = DummyValue != null

    class DummyFNameSerializationException(
        val Dummy: FString?,
        val NameMap: INameMap?,
    ) : RuntimeException("Attempt to serialize dummy FName \"${Dummy?.Value ?: "<null>"}\"")

    /**
     * Index into the name map of [Asset] that this FName points to.
     * Throws [DummyFNameSerializationException] if this is a dummy FName.
     */
    var Index: Int
        get() {
            if (IsDummy) throw DummyFNameSerializationException(DummyValue, Asset)
            return _index
        }
        set(Value) {
            _index = Value
            DummyValue = null
        }
    private var _index: Int = 0

    /** The string Value of this FName: either the dummy Value, or a reference into the Asset's name map. */
    var Value: FString?
        get() {
            if (DummyValue != null) return DummyValue
            val a = Asset ?: throw IllegalStateException("Attempt to get Value with no Asset defined")
            if (Index < 0) return null
            return a.GetNameReference(Index)
        }
        set(v) {
            DummyValue = null
            val a = Asset ?: throw IllegalStateException("Attempt to set Value with no Asset defined")
            Index = if (v?.Value == null) -1 else a.AddNameReference(v)
        }

    /** Converts this FName instance into a human-readable string (inverse of [FromString]). */
    override fun toString(): String {
        val v = Value
        if (v == null) return FString.NullCase
        return if (Number > 0) "${v}_${Number - 1}" else v.toString()
    }

    /** Creates a new FName with the same string Value and Number but bound to a different Asset. */
    fun Transfer(newAsset: INameMap): FName = FName(newAsset, Value, Number)

    override fun equals(other: Any?): Boolean {
        val name = other as? FName ?: return false
        if (this.Asset !== name.Asset) return this.Value.toString().equals(name.Value.toString())
        return (this.Value == name.Value || this.Value?.Value == name.Value?.Value) && this.Number == name.Number
    }

    override fun hashCode(): Int {
        val v = Value
        return if (v == null) 0 else v.hashCode() xor Number.hashCode()
    }

    fun clone(): FName {
        if (IsDummy) return DefineDummy(Asset, Value?.clone(), Number)
        return FName(Asset, Value?.clone(), Number)
    }

    constructor()

    constructor(asset: INameMap?, `val`: String, number: Int = 0) {
        this.Asset = asset
        Value = FString(`val`)
        this.Number = number
    }

    constructor(asset: INameMap?, `val`: FString?, number: Int = 0) {
        this.Asset = asset
        Value = `val`
        this.Number = number
    }

    constructor(asset: INameMap?, index: Int, number: Int = 0) {
        this.Asset = asset
        this.Index = index
        this.Number = number
    }

    constructor(asset: INameMap?) {
        this.Asset = asset
        Value = FString("")
        Number = 0
    }

    companion object {
        internal val IndexBits = 30
        internal val IndexMask = (1u shl IndexBits) - 1u
        internal val TypeMask = IndexMask.inv()
        internal val TypeShift = IndexBits

        private fun FromStringFragments(`val`: String, outStr: Out<String>, outNum: Out<Int>) {
            var str = `val`
            var num = 0

            val last = `val`[`val`.length - 1]
            if (last >= '0' && last <= '9') {
                var i = `val`.length - 1
                while (i > 1 && (`val`[i] >= '0' && `val`[i] <= '9')) {
                    i--
                }

                if (`val`[i] == '_') {
                    val startSegment = `val`.substring(0, i)
                    val endSegment = `val`.substring(i + 1)
                    if (endSegment.length == 1 || endSegment[0] != '0') {
                        val endSegmentVal = endSegment.toIntOrNull()
                        if (endSegmentVal != null) {
                            str = startSegment
                            num = endSegmentVal + 1
                        }
                    }
                }
            }
            outStr.value = str
            outNum.value = num
        }

        fun IsFromStringValid(asset: INameMap, `val`: String): Boolean {
            if (`val` == FString.NullCase) return true
            if (`val`.isEmpty()) return true

            val valueOut = Out<String>()
            FromStringFragments(`val`, valueOut, Out())
            return asset.ContainsNameReference(FString.FromString(valueOut.value!!)!!)
        }

        /** Converts a human-readable string into an FName instance bound to [Asset]. */
        fun FromString(asset: INameMap, `val`: String): FName? {
            if (`val` == FString.NullCase) return null
            if (`val`.isEmpty()) return FName(asset, `val`, 0)

            val valueOut = Out<String>()
            val numberOut = Out<Int>()
            FromStringFragments(`val`, valueOut, numberOut)
            return FName(asset, valueOut.value!!, numberOut.value!!)
        }

        /** Creates a new dummy FName from an FString. Must never be serialized to disk. */
        fun DefineDummy(asset: INameMap?, `val`: FString?, number: Int = 0): FName {
            if (asset != null && !asset.CanCreateDummies()) {
                return FName(asset, `val`, number)
            }
            val res = FName()
            res.Asset = asset
            res.DummyValue = `val`
            res.Number = number
            return res
        }

        /** Creates a new dummy FName from a string literal. Must never be serialized to disk. */
        fun DefineDummy(asset: INameMap?, `val`: String, number: Int = 0): FName {
            if (asset != null && !asset.CanCreateDummies()) {
                return FName(asset, `val`, number)
            }
            val res = FName()
            res.Asset = asset
            res.DummyValue = FString.FromString(`val`)
            res.Number = number
            return res
        }
    }
}
