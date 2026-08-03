// Copyright (c) 2026 jpabscale — original code (not part of the UAssetAPI port)
// JVM equivalent of .NET ConcurrentDictionary<string, T> with a StringComparer.
package com.github.jpabscale.uasset4j.unversioned

/**
 * Dictionary whose keys are case-insensitive lookups (when [caseInsensitive]) while preserving the
 * original key casing for iteration — mirrors .NET `ConcurrentDictionary<string, T>(StringComparer.InvariantCultureIgnoreCase)`.
 */
class CIMap<V>(val caseInsensitive: Boolean) {
    private val entries = LinkedHashMap<String, V>()
    private val normalized: MutableMap<String, String> = HashMap()

    private fun normalize(key: String): String = if (caseInsensitive) key.lowercase() else key

    fun put(key: String, value: V) {
        normalized[normalize(key)] = key
        entries[key] = value
    }

    fun contains(key: String): Boolean = normalized.containsKey(normalize(key))

    fun get(key: String): V? = normalized[normalize(key)]?.let { entries[it] }

    val size: Int get() = entries.size

    val values: Collection<V> get() = entries.values

    /** Original (non-normalized) keys, in insertion order. */
    val keys: Set<String> get() = entries.keys
}
