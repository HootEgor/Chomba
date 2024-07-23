package com.example.chomba.pages.user

import android.net.Uri
import com.example.chomba.R
import com.example.chomba.data.Game
import com.example.chomba.data.Language

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

    val saveMsg: Int = R.string.in_progress,

    val isSettings: Boolean = false,

    val selectedLanguage: Language = Language(R.drawable.flag_ua, R.string.tag_ua),

    val showAlert: Boolean = false,
    val alertTitle: Int = R.string.in_progress,
    val alertMsg: String = "",
    val alertAction: () -> Unit = {},
    val alertDismiss: () -> Unit = {},

    )
