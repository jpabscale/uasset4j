// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/Kismet/Bytecode/EScriptInstrumentationType.cs
package com.github.jpabscale.uasset4j.kismet.bytecode

enum class EScriptInstrumentationType {
    Class,
    ClassScope,
    Instance,
    Event,
    InlineEvent,
    ResumeEvent,
    PureNodeEntry,
    NodeDebugSite,
    NodeEntry,
    NodeExit,
    PushState,
    RestoreState,
    ResetState,
    SuspendState,
    PopState,
    TunnelEndOfThread,
    Stop,
}
