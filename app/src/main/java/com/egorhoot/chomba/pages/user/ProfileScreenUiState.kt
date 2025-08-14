package com.egorhoot.chomba.pages.user

import android.net.Uri
import com.egorhoot.chomba.R
import com.egorhoot.chomba.data.Game
import com.egorhoot.chomba.data.Language
import com.egorhoot.chomba.data.User

data class ProfileScreenUiState(
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val displayName: String = "",
    val nickname: String = "",
    val isAuthenticated: Boolean = false,
    val isAnonymous: Boolean = false,
    val userPicture: Uri? = null,

    val gameList: List<Game> = emptyList(),
    val relatedUserList: List<User> = emptyList(),

    val currentGameIndex: String? = null,
    val currentGame: Game? = null,

    val inProgress: Boolean = false,
    val isSuccess: Boolean = false,

    val saveMsg: Int = R.string.in_progress,

    val currentScreen: Int = 0,

    val selectedLanguage: Language = Language(R.drawable.flag_ua, R.string.tag_ua),
    val showAlert: Boolean = false,
    val alertTitle: Int = R.string.in_progress,
    val alertMsg: String = "",
    val alertAction: () -> Unit = {},
    val alertDismiss: () -> Unit = {},

    val scanQrCode: Boolean = false,
    val cameraPermissionDenied: Boolean = false,
    val cameraPermissionGranted: Boolean = false,
    )
