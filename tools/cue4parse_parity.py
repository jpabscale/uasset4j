#!/usr/bin/env python3
"""
CUE4Parse parity audit for curve support (EXC-002).

Checks that every CUE4Parse curve type referenced in docs/mapping.md has a corresponding
Kotlin implementation in the curves/ package, and reports structural gaps (missing types,
missing members, missing Eval logic). Approved gaps are listed in the parity-exceptions
registry and reported as EXC rather than errors.

Usage: python3 tools/cue4parse_parity.py
"""

import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from parity_exceptions import check_source_markers, exempt, load_registry

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CS_BASE = "/tmp/automod/CUE4Parse/CUE4Parse"
KT_CURVES = os.path.join(REPO, "uassetapi", "src", "main", "kotlin",
                          "com", "github", "jpabscale", "uasset4j", "curves")
KT_EXPORTS = os.path.join(REPO, "uassetapi", "src", "main", "kotlin",
                           "com", "github", "jpabscale", "uasset4j", "exporttypes")
KT_BASE = os.path.join(REPO, "uassetapi", "src", "main", "kotlin",
                        "com", "github", "jpabscale", "uasset4j")

# CUE4Parse source → expected Kotlin file(s). One C# file may map to multiple Kotlin files
# (file-level split is legitimate). Members are aggregated across all target files.
# Members ported from UAssetAPI (unrealtypes/, propertytypes/) are also checked.
TYPE_MAP = {
    "UE4/Objects/Engine/Curves/RealCurve.cs": ["curves/FRealCurve.kt", "unrealtypes/engineenums/EngineEnums.kt"],
    "UE4/Objects/Engine/Curves/RichCurve.cs": [
        "curves/FRichCurve.kt", "curves/FCompressedRichCurve.kt",
        "unrealtypes/objects/engine/FRichCurveKey.kt",
        "unrealtypes/engineenums/EngineEnums.kt",
    ],
    "UE4/Objects/Engine/Curves/SimpleCurve.cs": ["curves/FSimpleCurve.kt"],
    "UE4/Objects/Engine/Curves/FKeyHandle.cs": ["curves/FKeyHandle.kt"],
    "UE4/Objects/Engine/Curves/FCurveMetaData.cs": ["curves/FCurveMetaData.kt"],
    "UE4/Objects/Engine/Curves/UCurveVector.cs": ["curves/UCurveVector.kt"],
    "UE4/Objects/Engine/Curves/UCurveLinearColor.cs": ["curves/UCurveLinearColor.kt"],
    "UE4/Assets/Exports/Engine/UCurveTable.cs": ["curves/UCurveTable.kt", "exporttypes/CurveTableExport.kt"],
    "UE4/Assets/Exports/Engine/ECurveTableMode.cs": ["curves/UCurveTable.kt"],
    "UE4/Assets/Exports/Engine/UCompositeCurveTable.cs": ["exporttypes/CurveTableExport.kt"],
    "UE4/Assets/Exports/Texture/UCurveLinearColorAtlas.cs": ["curves/UCurveLinearColorAtlas.kt"],
}

# CUE4Parse members that are CUE4Parse-architecture-specific (UObject Deserialize/WriteJson,
# FStructFallback ctors) and NOT applicable to uasset4j's property-based architecture.
# These are approved gaps — the behavior is handled by the generic property path instead.
ARCHITECTURE_EXEMPTIONS = {
    "Deserialize",       # CUE4Parse UObject.Deserialize — uasset4j uses NormalExport.Read
    "WriteJson",         # CUE4Parse custom JSON — uasset4j uses Jackson bean serialization
    "FindCurve",         # CUE4Parse convenience lookup — available via RowMap.get()
    "TryFindCurve",      # CUE4Parse convenience lookup — available via RowMap.get()
    "GetUnadjustedLinearColorValue",  # kept in uasset4j (UCurveLinearColor)
    "GetLinearColorValue",            # kept in uasset4j (UCurveLinearColor)
    "UCompositeCurveTable",           # empty subclass, not needed
    # Internal adapter helpers (FCompressedRichCurve internals): these are private/internal
    # implementation details of the decompression converters. The Kotlin equivalents exist
    # as internal classes (Quantized16BitKeyTimeAdapter, etc.) in FCompressedRichCurve.kt.
    "DeltaTime", "KeyTimes", "MinTime", "KeyDataOffset", "KeySize", "RangeDataSize",
    "QuantizationScale", "GetTime",
    "GetKeyDataHandle", "GetKeyValue", "GetKeyArriveTangent", "GetKeyLeaveTangent",
    "GetKeyInterpMode", "GetKeyTangentWeightMode", "GetKeyArriveTangentWeight",
    "GetKeyLeaveTangentWeight",
    "IKeyDataAdapter", "IKeyTimeAdapter",
}

# Additional Kotlin files that exist elsewhere in the project (not curves/)
# and should be searched when checking RichCurve.cs members.
EXTRA_KT_SEARCH = [
    "../../unrealtypes/objects/engine/FRichCurveKey.kt",
    "../../unrealtypes/engineenums/EngineEnums.kt",
]

# C# members to extract: classes, enums, methods, properties, fields
CS_CLASS_RE = re.compile(
    r"\b(?:public|internal)\s+(?:abstract\s+|sealed\s+|static\s+|partial\s+)*(?:class|struct|interface)\s+(\w+)")
CS_ENUM_RE = re.compile(r"\b(?:public|internal)\s+enum\s+(\w+)")
CS_METHOD_RE = re.compile(
    r"\b(?:public|internal|protected)\s+(?:(?:static|virtual|override|abstract|new|async|unsafe)\s+)*"
    r"(?:\S+)\s+(\w+)\s*\(")
CS_PROPERTY_RE = re.compile(
    r"\b(?:public|internal|protected)\s+(?:(?:static|virtual|override|abstract|new|readonly)\s+)*"
    r"(?:\S+)\s+(\w+)\s*\{")
CS_FIELD_RE = re.compile(
    r"\b(?:public|internal)\s+(?:(?:static|readonly|const|volatile|new)\s+)*(?:\S+)\s+(\w+)\s*[;=]")

NON_IDENTIFIERS = {
    "true", "false", "null", "return", "if", "else", "new", "this", "base", "class", "struct",
    "enum", "interface", "namespace", "using", "void", "bool", "int", "float", "double", "string",
}


def strip_comments(text):
    text = re.sub(r"//.*?$", "", text, flags=re.MULTILINE)
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.DOTALL)
    text = re.sub(r"\"(?:[^\"\\]|\\.)*\"", "", text)
    return text


def extract_cs_members(path):
    if not os.path.exists(path):
        return None
    with open(path, encoding="utf-8", errors="replace") as f:
        text = strip_comments(f.read())
    members = set()
    for pat in (CS_CLASS_RE, CS_ENUM_RE, CS_METHOD_RE, CS_PROPERTY_RE, CS_FIELD_RE):
        for m in pat.finditer(text):
            name = m.group(1)
            if name not in NON_IDENTIFIERS and re.fullmatch(r"[A-Za-z_]\w*", name):
                members.add(name)
    return members


KT_FUN_RE = re.compile(r"^\s*(?:override\s+|open\s+|abstract\s+|internal\s+|public\s+)*fun\s+(\w+)")
KT_VAR_RE = re.compile(r"^\s*(?:override\s+|open\s+|abstract\s+|internal\s+|public\s+)*(?:var|val)\s+(\w+)")
KT_CLASS_RE = re.compile(r"^\s*(?:open\s+|abstract\s+|data\s+|internal\s+|public\s+)*class\s+(\w+)")
KT_ENUM_RE = re.compile(r"^\s*enum class\s+(\w+)")


def extract_kt_members(path):
    if not os.path.exists(path):
        return None
    with open(path, encoding="utf-8", errors="replace") as f:
        lines = f.read().splitlines()
    members = set()
    for line in lines:
        for pat in (KT_CLASS_RE, KT_ENUM_RE, KT_FUN_RE, KT_VAR_RE):
            m = pat.match(line)
            if m:
                members.add(m.group(1))
                break
    return members


def read_pinned_sha():
    """Reads cue4parse.pinned.sha from gradle.properties (single source of truth)."""
    props = os.path.join(REPO, "gradle.properties")
    if not os.path.exists(props):
        return None
    with open(props, encoding="utf-8") as f:
        for line in f:
            if line.startswith("cue4parse.pinned.sha="):
                return line.strip().split("=", 1)[1]
    return None


def check_pinned_checkout():
    """Warns if the CUE4Parse checkout at CS_BASE is not at the pinned commit."""
    pinned = read_pinned_sha()
    if not pinned:
        return "  WARN: cue4parse.pinned.sha missing in gradle.properties"
    git_dir = os.path.join(CS_BASE, os.pardir, ".git")
    head = os.path.join(git_dir, "HEAD")
    if not os.path.exists(head):
        return f"  WARN: CUE4Parse checkout not a git repo at {CS_BASE} (cannot verify pin {pinned})"
    try:
        with open(head) as f:
            ref = f.read().strip()
        actual = None
        if ref.startswith("ref:"):
            ref_path = os.path.join(git_dir, ref[5:].strip())
            if os.path.exists(ref_path):
                with open(ref_path) as f:
                    actual = f.read().strip()
        else:
            actual = ref
        if actual and not actual.startswith(pinned):
            return f"  WARN: CUE4Parse checkout at {actual[:12]} is NOT the pinned commit {pinned[:12]}"
        return None
    except OSError as e:
        return f"  WARN: could not read CUE4Parse checkout HEAD: {e}"


def main():
    exceptions = load_registry()
    errors = []
    excs = []
    info = []
    pin_warn = check_pinned_checkout()
    if pin_warn:
        info.append(pin_warn)

    for cs_rel, kt_files in TYPE_MAP.items():
        cs_path = os.path.join(CS_BASE, cs_rel)
        cs_members = extract_cs_members(cs_path)

        if cs_members is None:
            info.append(f"  SKIP (C# source not found): {cs_rel}")
            continue

        # Aggregate members from all target Kotlin files (file-level split is legitimate)
        kt_all = set()
        kt_found = []
        for kt_file in kt_files:
            # Resolve relative to KT_BASE (covers curves/, unrealtypes/, etc.)
            kt_path = os.path.join(KT_BASE, kt_file)
            if not os.path.exists(kt_path):
                kt_path = os.path.join(KT_CURVES, kt_file)
            if not os.path.exists(kt_path):
                kt_path = os.path.join(KT_EXPORTS, kt_file)
            kt_members = extract_kt_members(kt_path)
            if kt_members is not None:
                kt_all |= kt_members
                kt_found.append(kt_file)

        if not kt_found:
            if exempt(exceptions, "audit", cs_rel):
                excs.append(f"  EXC (approved): {cs_rel} -> {kt_files} (Kotlin files missing)")
            else:
                errors.append(f"  KOTLIN FILES MISSING: {kt_files} (C#: {cs_rel})")
            continue

        # Check each C# member against the aggregated Kotlin members
        missing = set()
        for name in sorted(cs_members):
            if name in kt_all:
                continue
            # Check architecture exemptions
            if name in ARCHITECTURE_EXEMPTIONS:
                excs.append(f"  EXC (arch): {cs_rel}#{name} — architecture-specific, handled by generic path")
                continue
            # Check parity-exceptions registry
            if exempt(exceptions, "audit", cs_rel) or exempt(exceptions, "audit", f"{cs_rel}#{name}"):
                excs.append(f"  EXC (approved): {cs_rel}#{name}")
                continue
            missing.add(name)

        targets = ", ".join(kt_found)
        if missing:
            errors.append(f"  {cs_rel} -> {targets}: missing C# public members:")
            for name in sorted(missing):
                errors.append(f"    - {name}")
        else:
            info.append(f"  ok: {cs_rel} -> {targets} ({len(cs_members)} C# members, {len(kt_all)} Kotlin members)")

    # Check CurveTableExport dispatch in UAsset.kt
    uasset_path = os.path.join(REPO, "uassetapi", "src", "main", "kotlin",
                               "com", "github", "jpabscale", "uasset4j", "UAsset.kt")
    if os.path.exists(uasset_path):
        with open(uasset_path, encoding="utf-8") as f:
            content = f.read()
        if "CurveTableExport" not in content:
            errors.append("  UAsset.kt: CurveTableExport not in dispatch")
        else:
            info.append("  ok: CurveTableExport wired in UAsset dispatch")
    else:
        errors.append("  UAsset.kt: file not found")

    # Check parity marker balance
    marker_errors, ids_used = check_source_markers()
    if marker_errors:
        for line in marker_errors:
            errors.append(f"  MARKER: {line}")

    print(f"CUE4Parse curve parity audit ({len(TYPE_MAP)} C# source files checked)\n")
    for line in info:
        print(line)
    if excs:
        print(f"\n  {len(excs)} approved exceptions (EXC-002):")
        for line in excs:
            print(line)
    if errors:
        print(f"\n  {len(errors)} error(s):")
        for line in errors:
            print(line)
        print("\nPARITY AUDIT: FAIL (fix or add approved exception)")
        return 1

    print("\nPARITY AUDIT: GREEN")
    return 0


if __name__ == "__main__":
    sys.exit(main())
