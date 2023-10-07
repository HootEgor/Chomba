package com.example.chomba.pages.solo

import android.app.Application
import android.util.Log
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
            gameIsStart = false,
            isTrade = true,
            declaration = 95,
            distributorIndex = 0,
            currentTraderIndex = 1)
        setDeclarer(playerList.value[1].name)
    }

    fun startGame() {
        if(uiState.value.pricup.size != 2) {
            return
        }

        playerList.value = playerList.value.map { player ->
            if(player.name != uiState.value.declarer) {
                player.hand += uiState.value.pricup[0]
                uiState.value = uiState.value.copy(
                    pricup = uiState.value.pricup.drop(1)
                )
                if(!player.isBot){
                    uiState.value = uiState.value.copy(
                        playerHand = player.hand
                    )
                }
            }
            else{
                uiState.value = uiState.value.copy(
                    currentTraderIndex = playerList.value.indexOf(player)
                )
            }
            player
        }
        uiState.value = uiState.value.copy(
            gameIsStart = true,
        )
        botTurn()
    }

    fun setDeclarer(declarer: String) {
        uiState.value = uiState.value.copy(
            declarer = declarer,
            declaration = uiState.value.declaration + 5
        )
        nextTrader()
        botTrade()
    }

    fun pass(p: String) {
        playerList.value = playerList.value.map { player ->
            if (player.name == p) {
                player.isPass = true
            }
            player
        }
        nextTrader()
        botTrade()
    }

    private fun nextTrader() {
        val nextTraderIndex = (uiState.value.currentTraderIndex + 1) % playerList.value.size
        if(playerList.value[nextTraderIndex].isPass) {
            uiState.value = uiState.value.copy(
                currentTraderIndex = nextTraderIndex
            )
            nextTrader()
        }else{
            uiState.value = uiState.value.copy(
                currentTraderIndex = nextTraderIndex
            )
        }
    }

    private fun botTrade(){
        if(playerList.value.filter { it.isPass }.size == 2){
            for(player in playerList.value) {
                if(player.isBot) {
                    if(!player.isPass) {
                        getAllCardsFromPricup(player.name)
                        for (card in player.hand.sortedBy { it.value }.take(2)) {
                            getCardFromPlayer(card, player.name)
                        }
                        startGame()
                        botTurn()
                    }
                }
            }
            uiState.value = uiState.value.copy(
                isTrade = false
            )
            return
        }

        val trader = playerList.value[uiState.value.currentTraderIndex]
        if(trader.isBot && !trader.isPass) {
            val random = Random.nextDouble()
            if (random < 0.5) {
                setDeclarer(trader.name)
            }else{
                pass(trader.name)
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

    private fun getAllCardsFromPricup(bot: String){
        playerList.value = playerList.value.map { player ->
            if (player.name == bot) {
                player.hand += uiState.value.pricup
            }
            player
        }

        uiState.value = uiState.value.copy(
            pricup = listOf(),
        )
    }

    fun getCardFromPlayer(card: Card, name: String = playerList.value[0].name){
        if(uiState.value.pricup.size >= 2) {
            return
        }

        playerList.value = playerList.value.map { player ->
            if (player.name == name) {
                player.hand -= card
                if(!player.isBot){
                    uiState.value = uiState.value.copy(
                        playerHand = player.hand
                    )
                }
            }
            player
        }

        uiState.value = uiState.value.copy(
            pricup = uiState.value.pricup + card,
        )
    }

    fun playCard(card: Card, name: String = ""){
        if(name == ""){
            playerList.value = playerList.value.map { player ->
                if (!player.isBot) {
                    player.hand -= card
                    if(uiState.value.playedCard != null){
                        player.hand += uiState.value.playedCard!!
                        uiState.value = uiState.value.copy(
                            pricup = uiState.value.pricup - uiState.value.playedCard!!
                        )
                    }
                    player.hand = sortCards(player.hand)
                    uiState.value = uiState.value.copy(
                        playerHand = player.hand,
                        playedCard = card,
                        pricup = uiState.value.pricup + card
                    )
                }
                player
            }
        }else{
            playerList.value = playerList.value.map { player ->
                if (player.name == name){
                    player.hand -= card
                    uiState.value = uiState.value.copy(
                        pricup = uiState.value.pricup + card
                    )
                }
                player
            }
        }

    }

    fun confirmTurn(){
        uiState.value = uiState.value.copy(
            playedCard = null
        )
        nextTurner()
        botTurn()
    }

    private fun nextTurner(){
        val nextTurnerIndex = (uiState.value.currentTraderIndex + 1) % playerList.value.size
        uiState.value = uiState.value.copy(
            currentTraderIndex = nextTurnerIndex
        )
    }

    private fun botTurn(){
        if(uiState.value.currentTraderIndex == 0){
            return
        }
        val turner = playerList.value[uiState.value.currentTraderIndex]
        if(turner.isBot) {
            if(uiState.value.pricup.isNotEmpty()){
                val card = turner.hand.filter { it.suit == uiState.value.pricup[0].suit }.minByOrNull { it.value }!!
                playCard(card, turner.name)
            }else{
                val card = turner.hand.maxByOrNull { it.value }!!
                playCard(card, turner.name)
            }

            confirmTurn()
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