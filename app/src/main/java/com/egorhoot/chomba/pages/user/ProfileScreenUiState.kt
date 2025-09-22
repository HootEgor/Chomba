package com.egorhoot.chomba.pages.user

import android.net.Uri
// import com.egorhoot.chomba.R // R may no longer be needed here
import com.egorhoot.chomba.data.Game
import com.egorhoot.chomba.data.Language // Should resolve to shared Language
import com.egorhoot.chomba.data.User

data class ProfileScreenUiState(
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val displayName: String = "",
    val nickname: String = "",
    val isAuthenticated: Boolean = false,
    val isAnonymous: Boolean = false,
    val userPicture: Uri? = null, // Stays as Uri for Android-specific UI state

    val titleKey: String = "game_list", // Changed from title: Int, default key

    val gameList: List<Game> = emptyList(),
    val relatedUserList: List<User> = emptyList(),

    val currentGameIndex: String? = null,
    val currentGame: Game? = null,

    val inProgress: Boolean = false,
    val isSuccess: Boolean = false,

    val saveMsgKey: String = "in_progress", // Changed from saveMsg: Int, default key

    val currentScreen: Int = 0,

    val selectedLanguage: Language = Language.fromId("ua"), // Uses KMP Language

    val showAlert: Boolean = false,
    val alertTitleKey: String = "in_progress", // Key for the title
    val alertMsgKey: String = "",           // Key for the message
    val alertMsgArgs: List<Any> = emptyList(), // Arguments for formatted message
    val resolvedAlertTitle: String = "",    // << ADDED: Pre-resolved title for the alert
    val resolvedAlertMessage: String = "",  // << ADDED: Pre-resolved message for the alert
    val alertAction: () -> Unit = {},
    val alertDismiss: () -> Unit = {},

    val scanQrCode: Boolean = false,
    val cameraPermissionDenied: Boolean = false,
    val cameraPermissionGranted: Boolean = false
)
