package com.egorhoot.chomba

import androidx.compose.runtime.MutableState // Still used by selectSpeechRecLanguage
import androidx.lifecycle.ViewModel
import com.egorhoot.chomba.data.Language
import com.egorhoot.chomba.pages.user.ProfileScreenUiState // Still used by selectSpeechRecLanguage

open class ChombaViewModel :ViewModel() {

    // This method operates on ProfileScreenUiState.
    // If other ViewModels need similar functionality for their specific UI states,
    // they should implement it themselves or we might need a more generic approach later.
    fun selectSpeechRecLanguage(profileUi: MutableState<ProfileScreenUiState>, lang: Language){
        profileUi.value = profileUi.value.copy(selectedLanguage = lang)
    }

    // showAlert and dismissAlert are removed.
    // Each ViewModel (ProfileViewModel, GameViewModel) now manages its own alert state
    // and has its own specific methods to trigger and dismiss alerts using its dedicated UI state.
}
