// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/TextHistoryType.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

enum class TextHistoryType(val value: Int) {
    None(-1),
    Base(0),
    NamedFormat(1),
    OrderedFormat(2),
    ArgumentFormat(3),
    AsNumber(4),
    AsPercent(5),
    AsCurrency(6),
    AsDate(7),
    AsTime(8),
    AsDateTime(9),
    Transform(10),
    StringTableEntry(11),
    TextGenerator(12),
    RawText(13),
}
