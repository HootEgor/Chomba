package com.example.chomba.pages.solo

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.example.chomba.data.Card
import com.example.chomba.data.CardSuit
import com.example.chomba.data.CardValue
import com.example.chomba.data.Player
import com.google.firebase.auth.FirebaseAuth

class SoloViewModel(application: Application): AndroidViewModel(application)  {

    val playerList = mutableStateOf<List<Player>>(listOf())
    var uiState = mutableStateOf(SoloUiState())
        private set

    private val auth = FirebaseAuth.getInstance()
    fun newGame() {
        val newPlayerList = mutableListOf<Player>()
        newPlayerList.add(Player(name = auth.currentUser?.displayName ?: "", isBot = false))
        newPlayerList.add(Player(name = "Player 2", isBot = true))
        newPlayerList.add(Player(name = "Player 3", isBot = true))

        val deck = createDeck()
        shuffleDeck(deck)
        val deal = dealCards(newPlayerList, deck, 7)
        playerList.value = deal.first
        sortCardsForPlayer()
        uiState.value = uiState.value.copy(
            pricup = deal.second,
            playerHand = playerList.value[0].hand)
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
        if (uiState.value.selectedCard == null) {
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
                selectedCard = card,
                pricup = uiState.value.pricup - card,
            )
        } else {
            playerList.value = playerList.value.map { player ->
                if (!player.isBot) {
                    player.hand -= uiState.value.selectedCard!!
                    player.hand += card
                    player.hand = sortCards(player.hand)
                    uiState.value = uiState.value.copy(
                        playerHand = player.hand
                    )
                }
                player
            }

            uiState.value = uiState.value.copy(
                selectedCard = card,
                pricup = uiState.value.pricup + uiState.value.selectedCard!! - card
            )
        }
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

    // Раздача карт игрокам
    private fun dealCards(players: List<Player>, deck: MutableList<Card>, numberOfCards: Int): Pair<MutableList<Player>, List<Card>>  {
        repeat(numberOfCards) { cardIndex ->
            for (player in players) {
                player.hand += deck.removeAt(0) // Берем верхнюю карту из колоды и добавляем в руку игрока
            }
        }
        uiState.value.selectedCard = null
        return Pair(first = players.toMutableList(),
            second = deck)
    }

    private fun sortCards(cards: List<Card>): List<Card> {
        return cards.sortedWith(compareBy<Card> { it.suit}.thenBy { it.value})
    }

}