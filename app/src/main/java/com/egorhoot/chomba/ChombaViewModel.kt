package com.egorhoot.chomba

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.egorhoot.chomba.data.Language
import com.egorhoot.chomba.pages.user.ProfileScreenUiState
import javax.inject.Inject
import javax.inject.Singleton

open class ChombaViewModel :ViewModel() {

    fun selectSpeechRecLanguage(profileUi: MutableState<ProfileScreenUiState>, lang: Language){
        profileUi.value = profileUi.value.copy(selectedLanguage = lang)
    }

    fun showAlert(profileUi: MutableState<ProfileScreenUiState>,title: Int, msg: String, action: () -> Unit, dismiss: () -> Unit){
        profileUi.value = profileUi.value.copy(showAlert = true,
            alertTitle = title,
            alertMsg = msg,
            alertAction = action,
            alertDismiss = dismiss)
    }

    fun dismissAlert(profileUi: MutableState<ProfileScreenUiState>,){
        profileUi.value = profileUi.value.copy(showAlert = false)
    }

}