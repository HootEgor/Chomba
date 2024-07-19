package com.example.chomba.data

data class User(
    val email: String,
    val name: String? = null,
    val photoUrl: String? = null,
    val voiceRecLanguage: Language? = null,
)
