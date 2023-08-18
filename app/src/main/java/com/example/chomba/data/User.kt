package com.example.chomba.data

import android.net.Uri

data class User(
    val email: String,
    val name: String? = null,
    val photoUrl: String? = null
)
