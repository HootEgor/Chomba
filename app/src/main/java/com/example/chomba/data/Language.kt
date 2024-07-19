package com.example.chomba.data
import com.example.chomba.R

data class Language(
    val icon: Int,
    val languageTag: Int
) {
    constructor(id: String) : this(
        when (id) {
            "ua" -> R.drawable.flag_ua
            "uk" -> R.drawable.flag_uk
            "ru" -> R.drawable.orc
            else -> R.drawable.flag_ua
        },
        when (id) {
            "ua" -> R.string.tag_ua
            "uk" -> R.string.tag_uk
            "ru" -> R.string.tag_ru
            else -> R.string.tag_ua
        }
    )
}

data class LanguageId(
    val id: String
) {
    constructor(language: Language) : this(language.toId())
}

fun Language.toId(): String {
    return when (this.icon) {
        R.drawable.flag_ua -> "ua"
        R.drawable.flag_uk -> "uk"
        R.drawable.orc -> "ru"
        else -> "ua"
    }
}
