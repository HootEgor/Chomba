package com.example.chomba.pages.user

import android.net.Uri
import com.example.chomba.GameUiState
import com.example.chomba.data.Game
import com.example.chomba.data.Player

data class ProfileScreenUiState(
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val displayName: String = "",
    val isAuthenticated: Boolean = false,
    val isAnonymous: Boolean = false,
    val userPicture: Uri? = null,

    val gameList: List<Game> = emptyList(),

    val currentGameIndex: String? = null,

    val inProgress: Boolean = false,

)
