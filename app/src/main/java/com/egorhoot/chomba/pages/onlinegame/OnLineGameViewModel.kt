package com.egorhoot.chomba.pages.onlinegame

import androidx.compose.runtime.MutableState
import com.egorhoot.chomba.ChombaViewModel
import com.egorhoot.chomba.pages.user.ProfileScreenUiState
import com.egorhoot.chomba.repo.OnLineGameRepository
import com.egorhoot.chomba.repo.UserRepository
import com.egorhoot.chomba.utils.IdConverter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnLineGameViewModel @Inject constructor(
    val userRepo: UserRepository,
    val onLineGameRepo: OnLineGameRepository,
    private val idConverter: IdConverter,
    val profileUi: MutableState<ProfileScreenUiState>,
    val onLineGameUiState: MutableState<OnLineGameUiState>
) : ChombaViewModel(){

}