#!/usr/bin/env python3
"""
Generates uassetapi-jvm's ObjectVersion.kt (ObjectVersion, ObjectVersionUE5, GameSpecificOverride)
from the pinned upstream ObjectVersion.cs. Computes sequential C# enum integer values, preserving
explicit ones (`= 214`, `= 1000`) and the `X = Y_PLUS_ONE - 1` alias.

Usage:
    python3 tools/gen_objectversion.py <path/to/ObjectVersion.cs> <output/ObjectVersion.kt>
"""

import re
import sys

ENUM_HEADER_RE = re.compile(r"^\s*public enum\s+(\w+)\s*$")
ENUM_END_RE = re.compile(r"^\s*}\s*;?\s*$")
DOC_RE = re.compile(r"^\s*///\s?(.*)$")
MEMBER_RE = re.compile(r"^\s*([A-Za-z_]\w*)\s*(?:=\s*(.+?))?,?\s*(?://.*)?$")
PLAIN_COMMENT_RE = re.compile(r"^\s*(//|/\*|\*)")
ALIAS_RE = re.compile(r"^(\w+)\s*-\s*1$")


def gen(source_path: str, out_path: str) -> None:
    with open(source_path, encoding="utf-8") as f:
        lines = f.read().splitlines()

    out = []
    out.append("// GENERATED FILE — DO NOT EDIT BY HAND.")
    out.append("// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr")
    out.append("// Source: UAssetAPI/UAssetAPI/UnrealTypes/ObjectVersion.cs")
    out.append("// Regenerate with: python3 tools/gen_objectversion.py (see header of this file).")
    out.append("package com.github.jpabscale.uasset4j.unrealtypes")
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

        members = []  # (doc, name, value_expr_str) -- value_expr resolved per member
        pending_doc = []
        prev_value = -1
        while i < n:
            line = lines[i]
            if ENUM_END_RE.match(line):
                break
            dm = DOC_RE.match(line)
            if dm:
                pending_doc.append(dm.group(1))
                i += 1
                continue
            if PLAIN_COMMENT_RE.match(line) or not line.strip():
                i += 1
                continue
            mm = MEMBER_RE.match(line)
            if mm:
                name, expr = mm.group(1), mm.group(2)
                if expr is not None:
                    expr = expr.strip()
                if expr is None:
                    prev_value += 1
                    value_expr = str(prev_value)
                else:
                    am = ALIAS_RE.match(expr)
                    if am and am.group(1).endswith("PLUS_ONE"):
                        value_expr = f"{am.group(1)}.value - 1"
                    else:
                        prev_value = int(expr)
                        value_expr = expr
                members.append((" ".join(pending_doc), name, value_expr))
                pending_doc = []
                i += 1
                continue
            i += 1

        out.append(f"enum class {enum_name}(val value: Int) {{")
        for idx, (doc, name, value_expr) in enumerate(members):
            if doc:
                out.append(f"    /** {doc} */")
            trailing = "," if idx < len(members) - 1 else ";"
            out.append(f"    {name}({value_expr}){trailing}")
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
