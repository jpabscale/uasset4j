// Copyright (c) 2026 jpabscale — original code (not part of the UAssetAPI port)
package com.github.jpabscale.uasset4j.util

/**
 * Holder used for C# `out` parameters. Per parity contract: the only mechanism for `out` params.
 * C#: `method(out string value)` -> Kotlin: `method(Out<String>().apply { ... })`.
 */
class Out<T>(var value: T? = null)

/**
 * Holder used for C# `ref` parameters. Per parity contract: the only mechanism for `ref` params.
 */
class Ref<T>(var value: T? = null)
