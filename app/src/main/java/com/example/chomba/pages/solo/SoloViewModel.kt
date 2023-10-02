package com.example.chomba.pages.solo

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.chomba.data.Card
import com.example.chomba.data.CardSuit
import com.example.chomba.data.CardValue
import com.example.chomba.data.Player
import com.google.firebase.auth.FirebaseAuth
import kotlin.random.Random

class SoloViewModel(application: Application): AndroidViewModel(application)  {

    val playerList = mutableStateOf<List<Player>>(listOf())
    var uiState = mutableStateOf(SoloUiState())
        private set

    private val auth = FirebaseAuth.getInstance()
    fun newGame() {
        val newPlayerList = mutableListOf<Player>()
        newPlayerList.add(Player(name = auth.currentUser?.displayName ?: "", isBot = false))
        newPlayerList.add(Player(name = "Bot 1", isBot = true))
        newPlayerList.add(Player(name = "Bot 2", isBot = true))

        val deck = createDeck()
        shuffleDeck(deck)
        val deal = dealCards(newPlayerList, deck, 7)
        playerList.value = deal.first
        sortCardsForPlayer()
        uiState.value = uiState.value.copy(
            pricup = deal.second,
            playerHand = playerList.value[0].hand,
            isTrade = true,
            declaration = 100)
    }

    fun startGame() {
        if(uiState.value.pricup.size != 2) {
            return
        }
        for(player in playerList.value) {
            if(player.isBot) {
                player.hand += uiState.value.pricup[0]
                uiState.value = uiState.value.copy(
                    pricup = uiState.value.pricup.drop(1)
                )
            }
        }
        uiState.value = uiState.value.copy(
            gameIsStart = true,
        )
    }

    fun setDeclarer(declarer: String) {
        uiState.value = uiState.value.copy(
            declarer = declarer,
            declaration = uiState.value.declaration + 5
        )
    }

    fun pass(p: String) {
        playerList.value = playerList.value.map { player ->
            if (player.name == p) {
                player.isPass = true
            }
            player
        }
    }

    fun botTrade(){
        for(player in playerList.value) {
            if(player.isBot && !player.isPass) {
                val random = Random.nextDouble()
                if (random < 0.5) {
                    setDeclarer(player.name)
                }else{
                    pass(player.name)
                }
            }
        }

        nextTradeRound()
    }

    fun nextTradeRound(){
        if(playerList.value[0].isPass){
            for(player in playerList.value) {
                if(player.isBot) {
                    if(!player.isPass) {
                        botTrade()
                    }
                }
            }
            uiState.value = uiState.value.copy(
                isTrade = false
            )
        }

        uiState.value = uiState.value.copy(
            isTrade = false
        )
        for(player in playerList.value) {
            if(player.isBot) {
                if(!player.isPass) {
                    uiState.value = uiState.value.copy(
                        isTrade = true
                    )
                }
            }
        }


    }

    private fun sortCardsForPlayer() {
        playerList.value = playerList.value.map { player ->
            if (!player.isBot) {
                player.hand = sortCards(player.hand)
            }
            player
        }
    }

    fun getCardFromPricup(card: Card){
        playerList.value = playerList.value.map { player ->
            if (!player.isBot) {
                player.hand += card
                player.hand = sortCards(player.hand)
                uiState.value = uiState.value.copy(
                    playerHand = player.hand
                )
            }
            player
        }

        uiState.value = uiState.value.copy(
            pricup = uiState.value.pricup - card,
        )
    }

    fun getCardFromPlayer(card: Card){
        if(uiState.value.pricup.size >= 2) {
            return
        }
        playerList.value = playerList.value.map { player ->
            if (!player.isBot) {
                player.hand -= card
                uiState.value = uiState.value.copy(
                    playerHand = player.hand
                )
            }
            player
        }

        uiState.value = uiState.value.copy(
            pricup = uiState.value.pricup + card,
        )
    }

    private fun createDeck(): MutableList<Card> {
        val deck = mutableListOf<Card>()
        for (value in CardValue.values()) {
            for (suit in CardSuit.values()) {
                deck.add(Card(value, suit))
            }
        }
        return deck
    }

    private fun shuffleDeck(deck: MutableList<Card>) {
        deck.shuffle()
    }

    private fun dealCards(players: List<Player>, deck: MutableList<Card>, numberOfCards: Int): Pair<MutableList<Player>, List<Card>>  {
        repeat(numberOfCards) { cardIndex ->
            for (player in players) {
                player.hand += deck.removeAt(0)
            }
        }

        return Pair(first = players.toMutableList(),
            second = deck)
    }

    private fun sortCards(cards: List<Card>): List<Card> {
        return cards.sortedWith(compareBy<Card> { it.suit}.thenBy { it.value})
    }

}