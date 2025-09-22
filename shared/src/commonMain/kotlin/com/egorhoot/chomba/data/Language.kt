package com.egorhoot.chomba.data

data class Language(
    val id: String,          // e.g., "ua", "uk", "ru" - the unique language code
    val iconName: String,    // e.g., "flag_ua", "flag_uk", "orc". Platform maps this to a drawable.
    val tagNameKey: String   // e.g., "tag_ua", "tag_uk", "tag_ru". Platform uses this as a key for localized string.
) {
    companion object {
        // Factory function to create a Language object from a simple ID.
        // This replaces the Android-specific secondary constructor.
        fun fromId(id: String): Language {
            return when (id.lowercase()) { // Using lowercase for case-insensitivity
                "ua" -> Language(id = "ua", iconName = "flag_ua", tagNameKey = "tag_ua")
                "uk" -> Language(id = "uk", iconName = "flag_uk", tagNameKey = "tag_uk")
                "ru" -> Language(id = "ru", iconName = "orc", tagNameKey = "tag_ru")
                else -> Language(id = "ua", iconName = "flag_ua", tagNameKey = "tag_ua") // Default/fallback
            }
        }
    }
}

// If LanguageId is used as a distinct type for just the ID.
data class LanguageId(
    val id: String
) {
    constructor(language: Language) : this(language.id)
}
