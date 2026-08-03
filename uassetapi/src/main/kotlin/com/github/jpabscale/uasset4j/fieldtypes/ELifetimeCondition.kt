// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/FieldTypes/ELifetimeCondition.cs
package com.github.jpabscale.uasset4j.fieldtypes

class ELifetimeCondition(val value: Int) {
    val name: String? get() = names[value]

    override fun toString(): String = name ?: value.toString()

    companion object {
        val COND_None = ELifetimeCondition(0)
        val COND_InitialOnly = ELifetimeCondition(1)
        val COND_OwnerOnly = ELifetimeCondition(2)
        val COND_SkipOwner = ELifetimeCondition(3)
        val COND_SimulatedOnly = ELifetimeCondition(4)
        val COND_AutonomousOnly = ELifetimeCondition(5)
        val COND_SimulatedOrPhysics = ELifetimeCondition(6)
        val COND_InitialOrOwner = ELifetimeCondition(7)
        val COND_Custom = ELifetimeCondition(8)
        val COND_ReplayOrOwner = ELifetimeCondition(9)
        val COND_ReplayOnly = ELifetimeCondition(10)
        val COND_SimulatedOnlyNoReplay = ELifetimeCondition(11)
        val COND_SimulatedOrPhysicsNoReplay = ELifetimeCondition(12)
        val COND_SkipReplay = ELifetimeCondition(13)
        val COND_Never = ELifetimeCondition(15)
        val COND_Max = ELifetimeCondition(16)

        private val names: Map<Int, String> = mapOf(
            0 to "COND_None",
            1 to "COND_InitialOnly",
            2 to "COND_OwnerOnly",
            3 to "COND_SkipOwner",
            4 to "COND_SimulatedOnly",
            5 to "COND_AutonomousOnly",
            6 to "COND_SimulatedOrPhysics",
            7 to "COND_InitialOrOwner",
            8 to "COND_Custom",
            9 to "COND_ReplayOrOwner",
            10 to "COND_ReplayOnly",
            11 to "COND_SimulatedOnlyNoReplay",
            12 to "COND_SimulatedOrPhysicsNoReplay",
            13 to "COND_SkipReplay",
            15 to "COND_Never",
            16 to "COND_Max",
        )

        fun fromByte(v: Int): ELifetimeCondition = ELifetimeCondition(v)

        fun fromName(nm: String): ELifetimeCondition = ELifetimeCondition(names.entries.first { it.value == nm }.key)
    }
}
