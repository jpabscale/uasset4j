// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/UnrealTypes/Objects/Engine/EngineEnums.cs
// Subset: only the enums needed by the ported leaf property types.
package com.github.jpabscale.uasset4j.unrealtypes.engineenums

enum class ERichCurveInterpMode {
    RCIM_Linear,
    RCIM_Constant,
    RCIM_Cubic,
    RCIM_None,
    RCIM_MAX,
}

enum class ERichCurveTangentWeightMode {
    RCTWM_WeightedNone,
    RCTWM_WeightedArrive,
    RCTWM_WeightedLeave,
    RCTWM_WeightedBoth,
    RCTWM_MAX,
}

enum class ERichCurveTangentMode {
    RCTM_Auto,
    RCTM_User,
    RCTM_Break,
    RCTM_None,
    RCTM_MAX,
}

enum class ERoundingMode {
    HalfToEven,
    HalfFromZero,
    HalfToZero,
    FromZero,
    ToZero,
    ToNegativeInfinity,
    ToPositiveInfinity,
    ERoundingMode_MAX,
}

enum class EFormatArgumentType {
    Int,
    UInt,
    Float,
    Double,
    Text,
    Gender,
    EFormatArgumentType_MAX,
}
