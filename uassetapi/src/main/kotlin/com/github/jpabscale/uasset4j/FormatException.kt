// Copyright (c) 2026 jpabscale — original code (not part of the UAssetAPI port)
// Mirrors C# System.FormatException (the port never throws java.util.FormatException).
package com.github.jpabscale.uasset4j

class FormatException(message: String) : RuntimeException(message)
