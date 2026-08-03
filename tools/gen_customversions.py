#!/usr/bin/env python3
"""
Generates uassetapi-jvm's CustomVersions.kt from the pinned upstream CustomVersions.cs.

Usage:
    python3 tools/gen_customversions.py \
        <path/to/CustomVersions.cs> \
        <output/CustomVersions.kt>

Re-run this script when re-porting from a newer upstream tip (see the README pinned-commit policy).
Outputs the 14 custom-version enums as Kotlin enum classes where `[Introduced(EngineVersion.X)]`
becomes a constructor arg and the C# integer value is recoverable via `val value` (ordinal-based,
with the `LatestVersion = VersionPlusOne - 1` special case handled).
"""

import re
import sys

INTRO_RE = re.compile(r"^\s*\[Introduced\(EngineVersion\.(\w+)\)\]\s*$")
ENUM_HEADER_RE = re.compile(r"^\s*public enum\s+(\w+)\s*(:\s*\w+)?\s*$")
ENUM_END_RE = re.compile(r"^\s*}\s*;?\s*$")
MEMBER_RE = re.compile(r"^\s*([A-Za-z_]\w*)\s*")
DOC_RE = re.compile(r"^\s*///\s?(.*)$")
PLAIN_COMMENT_RE = re.compile(r"^\s*(//|/\*|\*)")


def gen(source_path: str, out_path: str) -> None:
    with open(source_path, encoding="utf-8") as f:
        lines = f.read().splitlines()

    out = []
    out.append("// GENERATED FILE — DO NOT EDIT BY HAND.")
    out.append("// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr")
    out.append("// Source: UAssetAPI/UAssetAPI/CustomVersions/CustomVersions.cs")
    out.append("// Regenerate with: python3 tools/gen_customversions.py (see header of this file).")
    out.append("package com.github.jpabscale.uasset4j.customversions")
    out.append("")
    out.append("import com.github.jpabscale.uasset4j.unrealtypes.EngineVersion")
    out.append("")

    i = 0
    n = len(lines)
    enum_count = 0
    while i < n:
        m = ENUM_HEADER_RE.match(lines[i])
        if not m:
            i += 1
            continue
        enum_name = m.group(1)
        i += 1

        members = []  # (doc, introduced, name)
        pending_doc = []
        pending_intro = None
        while i < n:
            line = lines[i]
            em = ENUM_END_RE.match(line)
            if em:
                break
            dm = DOC_RE.match(line)
            if dm and pending_intro is None:
                pending_doc.append(dm.group(1))
                i += 1
                continue
            im = INTRO_RE.match(line)
            if im:
                pending_intro = im.group(1)
                i += 1
                continue
            if PLAIN_COMMENT_RE.match(line) or not line.strip():
                i += 1
                continue
            mm = MEMBER_RE.match(line)
            if mm:
                if pending_intro is None:
                    raise SystemExit(f"member without Introduced attr at line {i + 1}: {line}")
                members.append((" ".join(pending_doc), pending_intro, mm.group(1)))
                pending_doc = []
                pending_intro = None
                i += 1
                continue
            i += 1

        has_plus_one = any(mem[2] == "VersionPlusOne" for mem in members)
        has_latest = any(mem[2] == "LatestVersion" for mem in members)
        out.append(f"enum class {enum_name}(val introduced: EngineVersion) {{")
        for idx, (doc, intro, name) in enumerate(members):
            if doc:
                out.append(f"    /** {doc} */")
            trailing = "," if idx < len(members) - 1 else ";"
            out.append(f"    {name}(EngineVersion.{intro}){trailing}")
        out.append("")
        if has_plus_one and has_latest:
            out.append("    val value: Int get() = ordinal - (if (this == LatestVersion) 2 else 0)")
        else:
            out.append("    val value: Int get() = ordinal")
        out.append("}")
        out.append("")
        enum_count += 1
        i += 1

    if enum_count == 0:
        raise SystemExit("no enums found; is the source path correct?")

    with open(out_path, "w", encoding="utf-8") as f:
        f.write("\n".join(out))
    print(f"generated {enum_count} enums -> {out_path}")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        raise SystemExit(__doc__)
    gen(sys.argv[1], sys.argv[2])
