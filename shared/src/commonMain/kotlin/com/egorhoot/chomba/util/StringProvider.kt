package com.egorhoot.chomba.util

/**
 * Provides a platform-agnostic way to retrieve localized strings using a key.
 */
expect class StringProvider {
    /**
     * Gets the localized string for the given key.
     * @param key The unique identifier for the string resource.
     * @return The localized string, or the key itself (or an error indicator) if not found.
     */
    fun getString(key: String): String

    /**
     * Gets the localized string for the given key and formats it with the provided arguments.
     * @param key The unique identifier for the string resource.
     * @param args The arguments to be inserted into the formatted string.
     * @return The formatted localized string, or a fallback if the key is not found or an error occurs.
     */
    fun getString(key: String, vararg args: Any): String // Now active
}
