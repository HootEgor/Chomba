package com.egorhoot.chomba.pages.onlinegame

import androidx.compose.runtime.MutableState
import androidx.lifecycle.viewModelScope
import com.egorhoot.chomba.ChombaViewModel
import com.egorhoot.chomba.data.Card
import com.egorhoot.chomba.data.CardSuit
import com.egorhoot.chomba.data.CardValue
import com.egorhoot.chomba.data.Player
import com.egorhoot.chomba.pages.PageState
import com.egorhoot.chomba.pages.user.ProfileScreenUiState
import com.egorhoot.chomba.repo.OnLineGameRepository
import com.egorhoot.chomba.repo.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnLineGameViewModel @Inject constructor(
    val userRepo: UserRepository,
    private val onLineGameRepo: OnLineGameRepository,
    val profileUi: MutableState<ProfileScreenUiState>,
    val onLineGameUiState: MutableState<OnLineGameUiState>,
    val pageState: MutableState<PageState>,
) : ChombaViewModel(){

    var thatUserName = ""

    init {
        onLineGameRepo.getThatUserName().let {
            thatUserName = it
        }
        viewModelScope.launch {
            onLineGameRepo.subscribeOnUpdates(onLineGameUiState, profileUi
            ) {
            }
            startGame()
        }
    }

    private fun startProgress(){
        profileUi.value = profileUi.value.copy(inProgress = true,
            alertMsgKey = profileUi.value.saveMsgKey)
    }

    private fun stopProgress(){
        profileUi.value = profileUi.value.copy(inProgress = false)
    }

    fun homePage(){
        pageState.value = pageState.value.copy(currentPage = 0)
    }

    private suspend fun startGame(){
        if(onLineGameRepo.isOwner(onLineGameUiState)){
            if(onLineGameUiState.value.game.game.playerList.isEmpty()){
                createPlayers()
            }
            if(onLineGameUiState.value.game.pricup.isEmpty()){
                distributeCards()
            }
            if(onLineGameUiState.value.game.currentActionPlayer == ""){
                onLineGameUiState.value = onLineGameUiState.value.copy(
                    game = onLineGameUiState.value.game.copy(
                        currentActionPlayer = thatUserName
                    )
                )
            }

            onLineGameRepo.updateGame(onLineGameUiState, profileUi){
            }
        }
    }

    private fun createPlayers(){
        for (user in onLineGameUiState.value.game.userList){
            onLineGameUiState.value.game.game.playerList += Player(
                name = user.name,
                userPicture = user.userPicture)
        }
    }

    fun isPlayerListNotEmpty(): Boolean {
        return onLineGameUiState.value.game.game.playerList.isNotEmpty()
    }

    fun getOtherPlayers(): List<Player> {
        val playerList = onLineGameUiState.value.game.game.playerList
        val currentUserIndex = playerList.indexOfFirst { it.name == thatUserName }
        if (currentUserIndex == -1) return playerList

        return playerList.drop(currentUserIndex + 1) + playerList.take(currentUserIndex)
    }

    private fun createDeck(): List<Card>{
        val deck = mutableListOf<Card>()
        for (cardSuit in CardSuit.entries.filter { it != CardSuit.ACE }){
            for (cardValue in CardValue.entries){
                deck.add(Card(cardValue, cardSuit))
            }
        }
        return deck
    }

    fun distributeCards(){
        clearAllCards()
        val deck = createDeck().shuffled().toMutableList()
        // Distribute 7 cards to players
        for (i in 0 until 7){
            for (player in onLineGameUiState.value.game.game.playerList){
                player.hand += deck[0]
                deck.remove(deck[0])
            }
        }
        // remaining cards are pricup
        for (card in deck){
            onLineGameUiState.value = onLineGameUiState.value.copy(
                game = onLineGameUiState.value.game.copy(
                    pricup = onLineGameUiState.value.game.pricup + card
                )
            )
        }
    }

    private fun clearAllCards(){
        for (player in onLineGameUiState.value.game.game.playerList){
            player.hand = listOf()
        }
        onLineGameUiState.value = onLineGameUiState.value.copy(
            game = onLineGameUiState.value.game.copy(
                pricup = listOf()
            )
        )
    }

    fun binding(){
        val declarer = onLineGameUiState.value.game.game.uiState.declarer
        val currentUser = onLineGameUiState.value.game.game.playerList.first { it.name == thatUserName }
        if(declarer == null){
            currentUser.declaration = 100
        }else{
            currentUser.declaration = declarer.declaration + 5
        }
        onLineGameUiState.value = onLineGameUiState.value.copy(
            game = onLineGameUiState.value.game.copy(
                currentActionPlayer = getOtherPlayers().first().name
            )
        )
        onLineGameUiState.value = onLineGameUiState.value.copy(
            game = onLineGameUiState.value.game.copy(
                game = onLineGameUiState.value.game.game.copy(
                    uiState = onLineGameUiState.value.game.game.uiState.copy(
                        declarer = currentUser
                    )
                )
            )
        )

        viewModelScope.launch {
            onLineGameRepo.updateGame(onLineGameUiState, profileUi){
            }
        }
    }

    fun pass(){
        val currentUser = onLineGameUiState.value.game.game.playerList.first { it.name == thatUserName }
        currentUser.isPass = true
        onLineGameUiState.value = onLineGameUiState.value.copy(
            game = onLineGameUiState.value.game.copy(
                currentActionPlayer = getOtherPlayers().first().name
            )
        )

        viewModelScope.launch {
            onLineGameRepo.updateGame(onLineGameUiState, profileUi){
            }
        }
    }
}