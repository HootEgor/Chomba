package com.egorhoot.chomba.util

import android.content.Context
import android.content.res.Resources
import android.util.Log // Optional: for logging errors

/**
 * Android-specific implementation of StringProvider.
 */
actual class StringProvider(private val context: Context) {
    /**
     * Gets the localized string for the given key using Android resources.
     * @param key The unique identifier for the string resource (e.g., "my_string_key").
     * @return The localized string if found, otherwise a fallback string indicating the key was not found or an error occurred.
     */
    actual fun getString(key: String): String {
        return try {
            val resourceId = context.resources.getIdentifier(key, "string", context.packageName)
            if (resourceId != 0) {
                context.getString(resourceId)
            } else {
                Log.w("StringProvider", "String resource key '$key' not found.")
                "[String Key Not Found: $key]"
            }
        } catch (e: Resources.NotFoundException) {
            Log.w("StringProvider", "String resource key '$key' explicitly not found.", e)
            "[String Key Missing: $key]"
        } catch (e: Exception) {
            Log.e("StringProvider", "Error looking up string resource key '$key'.", e)
            "[Error For String Key: $key]"
        }
    }

    /**
     * Gets the localized string for the given key and formats it with the provided arguments.
     * @param key The unique identifier for the string resource.
     * @param args The arguments to be inserted into the formatted string.
     * @return The formatted localized string, or a fallback if the key is not found or an error occurs.
     */
    actual fun getString(key: String, vararg args: Any): String { // Now active
        return try {
            val resourceId = context.resources.getIdentifier(key, "string", context.packageName)
            if (resourceId != 0) {
                context.getString(resourceId, *args)
            } else {
                Log.w("StringProvider", "Formatted string resource key '$key' not found.")
                "[Formatted String Key Not Found: $key]"
            }
        } catch (e: Resources.NotFoundException) {
            Log.w("StringProvider", "Formatted string resource key '$key' explicitly not found.", e)
            "[Formatted String Key Missing: $key]"
        } catch (e: Exception) {
            // This can catch various exceptions, including illegal format conversions if args don't match.
            Log.e("StringProvider", "Error looking up or formatting string resource key '$key'.", e)
            "[Error For Formatted String Key: $key]"
        }
    }
}
