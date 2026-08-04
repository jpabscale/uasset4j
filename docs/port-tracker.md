# Port tracker — UAssetAPI-33ef77e → uasset4j

Tracks each upstream C# file to its Kotlin port and tests. Status: **not ported** | **stub** (skeleton,
throws for unported paths) | **ported** (statement-parallel, may defer downstream deps).

> The dedicated curve types (`curves/`, `exporttypes/CurveTableExport.kt`) are **not** UAssetAPI
> ports — they are Apache-2.0 derivative work of CUE4Parse (EXC-002). They are out of scope of this
> tracker; see `docs/mapping.md` §Curve support.

## Port tree (dependency levels, ported bottom-up)

Deploy agents at the leaves of a level in parallel; **before moving up a level, resolve any mapping
divergence** the agents introduced into `docs/mapping.md` (canonical mapping — agents must follow it
and report every construct they mapped that wasn't already there). Higher-level code is stubbed as a
compile-time contract first, so every level builds green.

| Lvl | Files | Depends on | Agent strategy |
|---|---|---|---|
| 0 | readers/writers, FName/FString/FGuid/FPackageIndex, versions, Usmap, PropertyData base + tag enums + leaf Objects classes, FPropertyTypeName | — | DONE (committed) |
| 1 | `FFragment`, `FUnversionedHeader`, remaining `Objects` (Delegate/MulticastDelegate + `FDelegate`), `PropertyTypes/Structs` leaf structs (Math/Core/Engine/Ranges/Slate/Movies) | L0 + MainSerializer/Usmap *signatures only* | parallel agents |
| 2 | `Objects` Array/Set/Map/Enum/Byte/Text, `StructPropertyData` | L1 + MainSerializer contract | parallel agents |
| 3 | `MainSerializer` logic (registry, `TypeToClass`, `Read`/`Write`, `GenerateUnversionedHeader`), `Usmap.TryGetProperty*` | L1+L2 | centralized |
| 4 | `ExportTypes`, `UAsset.cs` Read/Write | L3 | parallel agents + centralized core |
| 5 | JSON layer (Newtonsoft-parity: `$type`, order, converters) | L4 | **DONE** (`json/` package: `UAssetJson`, `UAssetTypeIds`/`UAssetTypeIdResolver`, `Converters.kt`, `Mixins.kt`, `UAssetAnnotationIntrospector`, `UAssetBeanSerializerModifier`, `UAssetBeanDeserializerModifier`, `UAssetSerializer`/`UAssetDeserializer`). Acceptance: `JsonOracleTest` — `SerializeJson(true)` reproduces `DT_ArmorLevels.json` byte-for-byte; `fromjson` round-trips to identical JSON and to identical .uasset/.uexp bytes. See `docs/mapping.md` §JSON parity. |
| 6 | `UAssetService`, `Main.kt`, `UAssetCLI.jar`, automod Phase A | L5 | centralized |

**L1+L2 status: DONE** — merged via 4 git worktrees (commits `2814ef1`, `0384d92`, `a076c39`, `efbafeb` → `afb3d03`). Covers: `FFragment`/`FUnversionedHeader`, all remaining `PropertyTypes/Objects` (Array/Set/Map/Enum/Byte/Text/Delegate/MulticastDelegate), `StructPropertyData`, and the Math/Core/Engine/Movies/Niagara/Ranges/Slate struct property classes + their `UnrealTypes/Objects` value types. Mapping divergence resolved in `docs/mapping.md` (BasePropertyData ctor form, sbyte, enums, Color/DateTime/TimeSpan, etc.). Build + existing tests green.

| L3 | `MainSerializer` core logic, `Usmap.TryGetProperty`/`TryGetPropertyData` | **mostly DONE** (`6541864`): MainSerializer core (explicit 119-class registry, TypeToClass, Read, Write, GenerateUnversionedHeader, property tags) + FieldTypes (FProperty/UProperty). Remaining: Usmap.TryGetProperty*, and wiring MainSerializer.FProperty methods to FieldTypes | centralized |

## M1 — Foundation (done)

| C# file | Kotlin file | Status | Tests |
|---|---|---|---|
| `UnrealTypes/FString.cs` | `unrealtypes/FString.kt` | ported | `FStringTest` |
| `UnrealTypes/FGuid.cs` (n/a, System.Guid) | `unrealtypes/FGuid.kt` (new) | ported | `FGuidTest` |
| `UnrealTypes/FName.cs` | `unrealtypes/FName.kt` | ported | `FNameTest` |
| `UnrealTypes/FPackageIndex.cs` | `unrealtypes/FPackageIndex.kt` | ported | `FPackageIndexTest` |
| `UnrealTypes/ObjectVersion.cs` | `unrealtypes/ObjectVersion.kt` | ported (generated) | `ObjectVersionTest` |
| `UnrealTypes/EngineVersion.cs` | `unrealtypes/EngineVersion.kt` | ported | (M0) |
| `UnrealTypes/FObjectThumbnail.cs` | `unrealtypes/FObjectThumbnail.kt` | ported | — |
| `UnrealTypes/FLocMetadataObject.cs` | `unrealtypes/FLocMetadataObject.kt` | ported | — |
| `UnrealTypes/TMap.cs` | → `LinkedHashMap` (mapping.md) | mapped, not ported | — |
| `CustomVersions/CustomVersions.cs` | `customversions/CustomVersions.kt` | ported (generated) | `ObjectVersionTest` |
| `CustomVersion.cs` | `CustomVersion.kt` | ported | `CustomVersionTest` |
| `UAPUtils.cs` | `UAPUtils.kt` | stub (GUID/arith only) | — |
| `MainSerializer.cs` | `MainSerializer.kt` | stub (const only) | — |
| `Import.cs` | `Import.kt` | ported | — |
| `AssetBinaryReader.cs` | `UDataReader.kt` | ported (Kismet stubbed) | `UDataReaderWriterTest` |
| `AssetBinaryWriter.cs` | `UDataWriter.kt` | ported (Kismet stubbed) | `UDataReaderWriterTest` |
| `UAsset.cs` | `UAsset.kt` | stub (INameMap + fields) | — |
| `ExportTypes/Export.cs` | `exporttypes/Export.kt` | stub | — |
| `Unversioned/Usmap.cs` | `unversioned/Usmap.kt` | ported (M2; see deferrals) | `UsmapOracleDifferentialTest` |

## M2 — Unversioned (done: usmap binary + M2b jmap support)

| C# file | Kotlin file | Status | Tests |
|---|---|---|---|
| `Unversioned/Usmap.cs` | `unversioned/Usmap.kt` | ported (incl. `ReadJMAP`/lazy `PopulateIfNeeded`) | `UsmapOracleDifferentialTest` |
| `Unversioned/JmapHelper.cs` | `unversioned/JmapHelper.kt` | ported | — |
| `Unversioned/UsmapBinaryReader.cs` | `UsmapBinaryReader.kt` | ported | `UsmapOracleDifferentialTest` |
| `UnrealTypes/Flags.cs` | `unrealtypes/Flags.kt` | ported (EPropertyFlags only) | — |
| `UnrealTypes/UE4VersionToObjectVersion.cs` | `unrealtypes/UE4VersionToObjectVersion.kt` | ported (moved out of `UAsset.kt`) | — |
| `UnrealTypes/FObjectDataResource.cs` | `unrealtypes/FObjectDataResource.kt` | ported (moved out of `UAsset.kt`) | — |
| `UnrealTypes/FWorldTileInfo.cs` | `unrealtypes/FWorldTileInfo.kt` | ported (moved out of `UAsset.kt`) | — |
| `UnrealTypes/FGatherableTextData.cs` / `FTextSourceData.cs` / `FTextSourceSiteContext.cs` | `unrealtypes/FGatherableTextData.kt` | ported (moved out of `UAsset.kt`) | — |
| `UnrealTypes/FMetaData.cs` | `unrealtypes/FMetaData.kt` | ported (moved out of `UAsset.kt`) | — |
| `UnrealTypes/FUniversalObjectLocatorFragment.cs` | `unrealtypes/FUniversalObjectLocatorFragment.kt` | ported | — |
| `UnrealTypes/Objects/Engine/CoreUObject/CoreUObjectEnums.cs` + `CoreUObjectStructs.cs` | `propertytypes/structs/movies/CoreUObjectStructs.kt` | ported (extended existing port) | — |
| `AC7Decrypt.cs` | `AC7Decrypt.kt` | ported | — |
| `MonitoringStream.cs` | `MonitoringStream.kt` | ported (JVM stream adaption) | — |

**Usmap.cs deferrals** (documented; exempted in `tools/audit_parity.py`):
- `SerializeJSON` + `UsmapSchemaPropertiesJsonConverter` — M4 (Jackson JSON layer).
- `TryGetProperty`/`TryGetPropertyData` — M3 (needs `AncestryInfo`).
- `GetSchemaFromStructExport` + `ConvertFPropertyToUsmapPropertyData`/`ConvertUPropertyToUsmapPropertyData` — M4 (needs `StructExport`/`FProperty`).
- `GetAllPropertiesAnnotated`, `PatchUsmapWithVersion`, `PathToStream` — with their consumers.

## M3 — PropertyData foundation (in progress)

| C# file | Kotlin file | Status | Notes |
|---|---|---|---|
| `PropertyTypes/Objects/PropertyData.cs` | `propertytypes/objects/PropertyData.kt` | ported | base + AncestryInfo + tag enums |
| `UnrealTypes/FPropertyTypeName.cs` | `unrealtypes/FPropertyTypeName.kt` | ported | |
| `PropertyTypes/Objects/IntPropertyData.cs` etc. (17 leaf classes) | `propertytypes/objects/PrimitivePropertyData.kt` + one file per class | ported | Int/Float/Bool/Name/Str/Double/Int8/16/64/UInt16/32/64/Utf8Str/FieldPath/Interface/WeakObject/Unknown/Object/SoftObject |
| `PropertyTypes/Objects/ArrayPropertyData.cs`, `SetPropertyData.cs`, `MapPropertyData.cs`, `EnumPropertyData.cs`, `BytePropertyData.cs`, `TextPropertyData.cs`, `TextHistoryType.cs` | — | not ported | gated on `MainSerializer` |
| `PropertyTypes/Structs/StructPropertyData.cs` (+ structs) | — | not ported | gated on `MainSerializer` |

**M3 blocker**: `StructPropertyData`/Array/Set/Map/Enum/Text depend on `MainSerializer` (property
registry, `TypeToClass`, `Read`/`Write`, `GenerateUnversionedHeader`, `FUnversionedHeader`), which is
the core engine and is the next port (M4 core). The demo gate needs the full pipeline.

Deferred to M4: `UAsset` serialization engine, `MainSerializer`, full `ExportTypes`.

## M6 — Kismet bytecode expressions A–L (in progress)

| C# file | Kotlin file | Status | Tests |
|---|---|---|---|
| `Kismet/Bytecode/KismetExpression.cs` | `kismet/bytecode/KismetExpression.kt` | ported | — |
| `Kismet/Bytecode/EExprToken.cs` | `kismet/bytecode/EExprToken.kt` | ported | — |
| `Kismet/Bytecode/EScriptInstrumentationType.cs` | `kismet/bytecode/EScriptInstrumentationType.kt` | ported | — |
| `Kismet/Bytecode/KismetPropertyPointer.cs` | `kismet/bytecode/KismetPropertyPointer.kt` | ported | — |
| `Kismet/Bytecode/Expressions/EX_AddMulticastDelegate.cs` … `EX_LetWeakObjPtr.cs` (56 files, A–L) | `kismet/bytecode/expressions/*.kt` (one file per class) | ported | — |
| `Kismet/Bytecode/Expressions/EX_VariableBase.cs` (M-Z reference) | `kismet/bytecode/expressions/EX_VariableBase.kt` | stub (Read/Write throw) | — |
| `Kismet/Bytecode/ExpressionSerializer.cs` | `kismet/bytecode/ExpressionSerializer.kt` | stub (unchanged) | — |

M6 resolves the M1 deferral: `XFER_PROP_POINTER`, `ReadExpressionArray`, `KismetPropertyPointer`,
`FFieldPath` (existing) are now implemented in `UDataReader.kt`/`UDataWriter.kt`. See
`docs/mapping.md` §Kismet bytecode (M6) for the generic-base hoisting, enum, and size mappings.

## JSON-parity fixes (2026-08)

Byte-identical `tojson` against the C# oracle on the usmap-backed corpus (TestUE5_4/5_3/5_1,
Bellwright, etc.). Fixes in the `json/` layer and the ported field/property types:

- `json/Converters.kt`: added `EClassFlags`/`EPropertyFlags`/`EFunctionFlags`/
  `EObjectDataResourceFlags` named-flags serializers (StringEnumConverter parity); added
  `NewtonsoftDouble` (Newtonsoft `WriteValue(double)` formatting); unsigned `byte` converter;
  `ECastTokenValue` converter; `FPropertyTypeName` nodes now typed (emit `$type`).
- `json/Mixins.kt`: `$type` mixins for `UField`/`FField` (FieldTypes), the struct value types
  (math/engine `F*` + `FSoftObjectPath`/`FTopLevelAssetPath`/`FPropertyTypeNameNode`/
  `FObjectDataResource`), `ClassExport.FuncMap` as a TMap, `FBoolProperty` unsigned bytes.
- `json/TypeIds.kt`: added `fieldtypes.` namespace + field/struct classes to the id map.
- `json/UAssetBeanSerializerModifier.kt`: derived-first member ordering for all export subclasses,
  FField/UProperty hierarchies, and the struct value beans (`RawStructPropertyData` own members too).
- `exporttypes/StructExport.kt`: bytecode parse fallback catches `Throwable` (C# `catch (Exception)`
  also swallows `NotImplementedException`, which the port represents as `NotImplementedError`).
- `propertytypes/structs/engine/MaterialInputProperties.kt`: `DefaultValue` 0/0f for the
  `int`/`float`-typed `MaterialInputPropertyData` subclasses (C# `GetObject<T>()` returns `default(T)`).
- `fieldtypes/UField.kt`: `Next` is nullable (`FPackageIndex? = null`, C# reference-type default).
- `propertytypes/objects/SoftObjectPropertyData.kt`: now `BasePropertyData<FSoftObjectPath>` via
  `FSoftObjectPath.accessors` (C# `SoftObjectPropertyData : BasePropertyData<FSoftObjectPath>`), so
  `Read`/`Write`/`FromString` come from the base like C# instead of inline overrides.
- `propertytypes/objects/BytePropertyData.kt`: `InitializeZero` sets `Value = 0` (C# `byte` default).
- `kismet/bytecode/expressions/EX_End*.kt`: the seven `EX_End*` marker expressions now no-op
  (C# empty `Read`/`Write`); `EX_PrimitiveCast` keeps undefined cast bytes via `ECastTokenValue`.

## Re-generation

- `tools/gen_customversions.py` ← `CustomVersions/CustomVersions.cs` (14 enums)
- `tools/gen_objectversion.py` ← `UnrealTypes/ObjectVersion.cs` (3 enums)

## Tooling

- `tools/sweep.py` is the canonical corpus differential (Python + `ThreadPoolExecutor`, absolute
  paths, no temp-dir copying). Full 369-asset corpus runs in ~20s; `SWEEP_JOBS`-style parallelism is
  automatic. Old `tools/sweep.sh` (bash/xargs) retired — process-substitution deadlocks and
  per-asset `cp` made it fragile. Usage:
  `python3 tools/sweep.py <jar> <csharp-dir> <corpus> [--jobs N] [--only SUBSTR] [--limit N] [--match-list FILE]`
- `tools/differential.sh` takes absolute paths (fixed the same relative-jar `cd` bug as sweep.sh).
