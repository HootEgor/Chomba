package com.egorhoot.chomba.pages.user.leaderboard

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.lifecycle.viewModelScope
import com.egorhoot.chomba.ChombaViewModel
import com.egorhoot.chomba.data.sortedByTotalChombas
import com.egorhoot.chomba.data.sortedByTotalScore
import com.egorhoot.chomba.data.sortedByWinStreak
import com.egorhoot.chomba.data.sortedByWins
import com.egorhoot.chomba.pages.user.ProfileScreenUiState
import com.egorhoot.chomba.repo.UserRepository
import com.egorhoot.chomba.utils.Encryptor
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderBoardViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val encryptor: Encryptor,
    val profileUi: MutableState<ProfileScreenUiState>
): ChombaViewModel(){

    var uiState = mutableStateOf(LeaderBoardUiState())
        private set

    suspend fun getLeaderBoardPlayers() {
        if (userRepo.auth.currentUser != null) {
            viewModelScope.launch {
                val players = userRepo.getLeaderBoardPlayers(profileUi.value.relatedUserList)
                uiState.value = uiState.value.copy(players = players.sortedByWins())
            }
        }
    }

    fun sortPlayersByWins() {
        uiState.value = uiState.value.copy(players = uiState.value.players.sortedByWins())
    }

    fun sortPlayersByTotalScore() {
        uiState.value = uiState.value.copy(players = uiState.value.players.sortedByTotalScore())
    }

    fun sortPlayersByWinStreak() {
        uiState.value = uiState.value.copy(players = uiState.value.players.sortedByWinStreak())
    }

    fun sortPlayersByTotalChombas() {
        uiState.value = uiState.value.copy(players = uiState.value.players.sortedByTotalChombas())
    }

    fun getUserQRCode(userUid: String, size: Int, color: Color, backColor: Color): Bitmap {
        return encryptor.getUserQRCode(userUid, size, color, backColor)
    }
}