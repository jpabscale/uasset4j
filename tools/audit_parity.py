#!/usr/bin/env python3
"""
Parity audit: verifies that every ported Kotlin file exposes the same public member names as its
pinned C# counterpart (the milestone-closure checklist).

For each `ported` row in docs/port-tracker.md, extract public member identifiers from the C# file
and the Kotlin file, then report C# members missing from the Kotlin side. Exits non-zero on any
unexpected mismatch so it can gate a milestone commit.

Mappings applied (see docs/mapping.md):
  - .NET overrides map to their Kotlin forms: Equals->equals, GetHashCode->hashCode,
    ToString->toString, CompareTo->compareTo, Clone->clone.
  - C# `[Flags] enum : ulong` -> Kotlin `@JvmInline value class` + companion consts (members looked
    up in the companion).
  - C# parameter names are not audited (only member/type names).

Usage: python3 tools/audit_parity.py
"""

import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from parity_exceptions import check_source_markers, exempt, load_registry

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TRACKER = os.path.join(REPO, "docs", "port-tracker.md")
CS_BASE = "/home/jpabscale/Repositories/UAssetCLI/UAssetAPI/UAssetAPI"
KT_BASE = os.path.join(REPO, "uassetapi", "src", "main", "kotlin", "com", "github", "jpabscale", "uasset4j")

OVERRIDE_MAP = {
    "Equals": "equals",
    "GetHashCode": "hashCode",
    "ToString": "toString",
    "CompareTo": "compareTo",
    "Clone": "clone",
    # C# interface IStruct<T> (abstract static Read/FromString) -> StructAccessors<T> holder
    "IStruct": "StructAccessors",
}

# C# members deliberately not yet ported (documented in docs/port-tracker.md). Any entry added here
# MUST be accompanied by a tracker/mapping.md note explaining the deferral and target milestone.
# Types declared as nested (top-level file noise) but ported into their own Kotlin files are also
# listed here: the C# file declares them, the Kotlin port files them separately (mapping.md
# project-layout rows), so they are not members "missing" from the sibling Kotlin file.
TYPE_MOVE_EXEMPTIONS = {
    "PropertyTypes/Objects/PropertyData.cs": {
        "PropertySerializationContext",  # -> propertytypes/objects/PropertySerializationContext (own file)
        "EPropertyTagFlags",             # -> unrealtypes/Flags.kt
        "EPropertyTagExtension",         # -> unrealtypes/Flags.kt
        "EOverriddenPropertyOperation",  # -> unrealtypes/Flags.kt
        "AncestryInfo",                  # -> propertytypes/objects/AncestryInfo (own file)
    },
    # C# KismetExpression.cs declares both `class KismetExpression` and `abstract class
    # KismetExpression<T>`. JVM erasure forces the generic sibling into
    # `kismet/bytecode/KismetExpressionGeneric.kt` (mapping.md "Kismet bytecode" row), so its
    # `Value` member is legitimately in the sibling file, not missing from KismetExpression.kt.
    "Kismet/Bytecode/KismetExpression.cs": {
        "Value",  # -> kismet/bytecode/KismetExpressionGeneric.kt
    },
    # Newtonsoft converters are ported into the JSON layer (json/Converters.kt), not onto the
    # value type they serialize (mapping.md "JSON parity" leaf-converters row). FPropertyTypeName
    # has no `[JsonConverter]` in the value-type file; the C# `FPropertyTypeNameConverter` lives
    # in JSON/ and is ported as `FPropertyTypeNameJsonConverter` in json/Converters.kt.
    "UnrealTypes/FPropertyTypeName.cs": {
        "FPropertyTypeNameConverter",  # -> json/Converters.kt (FPropertyTypeNameJsonConverter)
    },
}

DEFERRED_EXEMPTIONS = {
    "Unversioned/Usmap.cs": {
        "SerializeJSON",
        "ReadJMAP",
        "TryGetProperty",
        "TryGetPropertyData",
        "GetSchemaFromStructExport",
        "GetAllPropertiesAnnotated",
        "PatchUsmapWithVersion",
        "PathToStream",
        "ConvertFPropertyToUsmapPropertyData",
        "ConvertUPropertyToUsmapPropertyData",
        # PropertyMapComparer (internal) -> CIMap helper, which carries no public Comparer member
        "Comparer",
    },
}

# tokens that are never member names
NON_IDENTIFIERS = {
    "true", "false", "null", "operator", "return", "if", "else", "switch", "case", "default",
    "new", "this", "base", "out", "ref", "in", "is", "as", "get", "set", "value", "params",
    "class", "struct", "enum", "interface", "namespace", "using", "sizeof", "typeof",
    "checked", "unchecked", "lock", "yield", "async", "await", "var", "val", "fun", "when",
    "do", "for", "foreach", "while", "break", "continue", "goto", "throw", "try", "catch",
    "finally", "delegate", "event", "explicit", "implicit", "static", "void", "bool", "int",
    "uint", "long", "ulong", "short", "ushort", "byte", "sbyte", "float", "double", "decimal",
    "string", "char", "object", "dynamic",
}


def strip_cs_noise(text):
    text = re.sub(r"//.*?$", "", text, flags=re.MULTILINE)
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.DOTALL)
    text = re.sub(r"@\"(?:[^\"]|\"\")*\"", "", text)
    text = re.sub(r"\"(?:[^\"\\]|\\.)*\"", "", text)
    return text


CS_TYPE_RE = re.compile(
    r"\bpublic\s+(?:abstract\s+|sealed\s+|static\s+|partial\s+)*(?:class|struct|interface)\s+(\w+)"
)
CS_ENUM_RE = re.compile(r"\bpublic\s+enum\s+(\w+)")
CS_METHOD_RE = re.compile(
    r"\bpublic\s+(?:(?:static|virtual|override|abstract|new|sealed|async|unsafe|extern)\s+)*\s*"
    r"(?:\S+)\s+(\w+)\s*\("
)
CS_PROPERTY_RE = re.compile(
    r"\bpublic\s+(?:(?:static|virtual|override|abstract|new|sealed)\s+)*\s*(?:\S+)\s+(\w+)\s*\{"
)
CS_FIELD_RE = re.compile(r"\bpublic\s+(?:(?:static|readonly|const|volatile|new)\s+)*(?:\S+)\s+(\w+)\s*[;=]")
CS_CONST_RE = re.compile(r"\bpublic\s+(?:static\s+)?(?:readonly|const)\s+(?:\S+)\s+(\w+)\s*=")


def cs_members(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        text = strip_cs_noise(f.read())
    members = set()
    for pat in (CS_TYPE_RE, CS_ENUM_RE, CS_METHOD_RE, CS_PROPERTY_RE, CS_FIELD_RE, CS_CONST_RE):
        for m in pat.finditer(text):
            name = m.group(1)
            # skip false positives: constructor params/defaults like "FPackageIndex(int index ="
            if m.group(0)[: m.start(1)].count("(") > 0:
                continue
            if name not in NON_IDENTIFIERS and re.fullmatch(r"[A-Za-z_]\w*", name):
                members.add(name)
    return members


KT_METHOD_RE = re.compile(
    r"^\s*(?:(?:inline|internal|private|public|override|open|abstract|suspend|tailrec|external|protected)\s+)*fun\s+(\w+)"
)
KT_PROPERTY_RE = re.compile(r"^\s*(?:(?:private|internal|public|protected|override|open|abstract|lateinit|const)\s+)*(?:var|val)\s+(\w+)")
KT_CTOR_PROPERTY_RE = re.compile(r"\((?:var|val)\s+(\w+)")
KT_FIELD_RE = re.compile(r"^\s*@JvmField\s*$")
KT_TYPE_RE = re.compile(
    r"^\s*(?:(?:open|abstract|sealed|data|internal|public|enum)\s+)*(?:class|object)\s+(\w+)"
)
KT_ENUM_RE = re.compile(r"^\s*enum class\s+(\w+)")


def kt_members(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        lines = f.read().splitlines()
    members = set()
    in_companion = False
    for line in lines:
        s = line.strip()
        if s.startswith("companion object"):
            in_companion = True
            continue
        if in_companion and (s == "}" or s.startswith("class ") or s.startswith("enum class")):
            in_companion = False
        for pat in (KT_TYPE_RE, KT_ENUM_RE, KT_METHOD_RE, KT_PROPERTY_RE):
            m = pat.match(line)
            if m:
                members.add(m.group(1))
                break
        m = KT_CTOR_PROPERTY_RE.search(line)
        if m:
            members.add(m.group(1))
        if in_companion:
            m = re.match(r"^\s*(?:val|var|const val|fun)\s+(\w+)", line)
            if m:
                members.add(m.group(1))
    return members


def audit_pair(cs_rel, kt_rel, errors, info, exceptions):
    cs_path = os.path.join(CS_BASE, cs_rel)
    kt_path = os.path.join(KT_BASE, kt_rel)
    if not os.path.exists(cs_path):
        info.append(f"  C# source missing: {cs_path}")
        return
    if not os.path.exists(kt_path):
        errors.append(f"  KOTLIN FILE MISSING: {kt_rel} (C#: {cs_rel})")
        return

    cs = cs_members(cs_path)
    kt = kt_members(kt_path)
    exemptions = DEFERRED_EXEMPTIONS.get(cs_rel, set()) | TYPE_MOVE_EXEMPTIONS.get(cs_rel, set())
    approved = set()
    for name in sorted(cs):
        # An approved exception either covers the whole file or a single C# member.
        if exempt(exceptions, "audit", cs_rel):
            approved.add(name)
        if exempt(exceptions, "audit", f"{cs_rel}#{name}"):
            approved.add(name)
    missing = set()
    for name in sorted(cs):
        if name in kt:
            continue
        mapped = OVERRIDE_MAP.get(name)
        if mapped and mapped in kt:
            continue
        if name in exemptions or name in approved:
            continue
        missing.add(name)

    if missing:
        errors.append(f"  {cs_rel} -> {kt_rel}: missing/renamed C# public members:")
        for name in sorted(missing):
            errors.append(f"    - {name}")
    else:
        info.append(f"  ok: {cs_rel} -> {kt_rel} ({len(cs)} C# members, {len(kt)} Kotlin members)")


def main():
    errors = []
    info = []
    exceptions = load_registry()
    with open(TRACKER, encoding="utf-8") as f:
        tracker = f.read()

    pairs = []
    for m in re.finditer(r"\| `([^`]+\.cs)` \| `([^`]+\.kt)` \| (\w+) \|", tracker):
        cs_rel, kt_rel, status = m.group(1), m.group(2), m.group(3)
        if status == "ported":
            pairs.append((cs_rel, kt_rel))

    if not pairs:
        print("no ported files found in port-tracker")
        return 2

    for cs_rel, kt_rel in pairs:
        audit_pair(cs_rel, kt_rel, errors, info, exceptions)

    print(f"audited {len(pairs)} ported file pairs\n")
    for line in info:
        print(line)
    if errors:
        print("\nPARITY MISMATCHES:")
        for line in errors:
            print(line)
        print(f"\n{len(errors)} error group(s). Fix the Kotlin names (or document an exemption in mapping.md).")
        return 1

    # Validate //@parity:on/off markers against the approved-exception registry.
    marker_errors, ids_used = check_source_markers()
    if marker_errors:
        print("\nPARITY MARKER MISMATCHES:")
        for line in marker_errors:
            print(line)
        print("\nMarkers must be balanced, non-nested, and reference approved exception ids.")
        return 1
    if ids_used:
        print(f"parity markers ok: {', '.join(sorted(ids_used))}")

    print("\nPARITY AUDIT: GREEN")
    return 0


if __name__ == "__main__":
    sys.exit(main())
