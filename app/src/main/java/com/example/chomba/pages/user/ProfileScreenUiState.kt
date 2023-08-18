package com.example.chomba.pages.user

import android.net.Uri

data class ProfileScreenUiState(
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val displayName: String = "",
    val isAuthenticated: Boolean = false,
    val isAnonymous: Boolean = false,
    val userPicture: Uri? = null,

)
