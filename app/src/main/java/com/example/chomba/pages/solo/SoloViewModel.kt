package com.example.chomba.pages.solo

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chomba.GameUiState
//import com.example.chomba.ui.theme.composable.CardEvaluator
import com.example.chomba.ai.CardEvaluator
import com.example.chomba.data.Card
import com.example.chomba.data.CardSuit
import com.example.chomba.data.CardValue
import com.example.chomba.data.Game
import com.example.chomba.data.Player
import com.example.chomba.data.Score
import com.example.chomba.data.getMissBarrel
import com.example.chomba.data.getTotalScore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

class SoloViewModel(application: Application): AndroidViewModel(application)  {

    val playerList = mutableStateOf<List<Player>>(listOf())
    var uiState = mutableStateOf(SoloUiState())
        private set

    private val auth = FirebaseAuth.getInstance()

    val cardEvaluator = CardEvaluator(getApplication())
    fun newGame() {
//        val newPlayerList = mutableListOf<Player>()
//        newPlayerList.add(Player(name = auth.currentUser?.displayName ?: "", isBot = false))
//        newPlayerList.add(Player(name = "Bot 1", isBot = true))
//        newPlayerList.add(Player(name = "Bot 2", isBot = true))
//
//        playerList.value = newPlayerList

        train()
//        distributeCards()
//        val deck = createDeck()
//        shuffleDeck(deck)
//        val deal = dealCards(newPlayerList, deck, 7)
//        playerList.value = deal.first
//        sortCardsForPlayer()
//        uiState.value = uiState.value.copy(
//            pricup = deal.second,
//            playerHand = playerList.value[0].hand,
//            gameIsStart = false,
//            isTrade = true,
//            declaration = 95,
//            distributorIndex = 0,
//            currentTraderIndex = 1)
//        setDeclarer(playerList.value[1].name)
    }

    private fun distributeCards() {
        val deck = createDeck()
        shuffleDeck(deck)
        val deal = dealCards(playerList.value, deck, 7)
        playerList.value = playerList.value.map { player ->
            val hand = deal.first.first { it.name == player.name }.hand
            var declaration = countBotScore(hand)
            val predict = cardEvaluator.predict(hand)

            if (predict<=0.7){
                declaration = (declaration*(predict+0.2)).roundToInt()
            }

            player.copy(hand = deal.first.first { it.name == player.name }.hand,
                isPass = false,
                declaration = declaration,)
        }
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

    fun setDeclarer(declarer: String, declaration: Int = uiState.value.declaration + 5) {
        uiState.value = uiState.value.copy(
            declarer = declarer,
            declaration = declaration
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
            if (trader.declaration >= uiState.value.declaration) {
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
                    if(uiState.value.pricup.isEmpty() ||
                        card.suit == uiState.value.pricup[0].suit ||
                        (player.hand.none { it.suit == uiState.value.pricup[0].suit} &&
                                uiState.value.pricup.none{it.suit == uiState.value.pricup[0].suit && it.player == player}     ) ||
                        uiState.value.pricup[0] == uiState.value.playedCard){

                        if(uiState.value.playedCard != null){
                            player.hand += uiState.value.playedCard!!
                            uiState.value = uiState.value.copy(
                                pricup = uiState.value.pricup - uiState.value.playedCard!!
                            )

                        }

                        player.hand -= card
                        player.hand = sortCards(player.hand)
                        card.player = player
                        uiState.value = uiState.value.copy(
                            playerHand = player.hand,
                            playedCard = card,
                            pricup = uiState.value.pricup + card
                        )
                    }
                }
                player
            }
        }else{
            playerList.value = playerList.value.map { player ->
                if (player.name == name){
                    player.hand -= card
                    card.player = player
                    uiState.value = uiState.value.copy(
                        pricup = uiState.value.pricup + card
                    )
                }
                player
            }
        }

    }

    fun confirmTurn(){
        if(uiState.value.pricup.size == 3){
            var player = Player()
            val suit = if(uiState.value.currentChomba == null){
                uiState.value.pricup[0].suit
            }else{
                uiState.value.currentChomba!!
            }

            var maxCard = uiState.value.pricup.filter { it.suit == suit }.maxByOrNull{ it.value }
            if(maxCard == null){
                maxCard = uiState.value.pricup.filter { it.suit == uiState.value.pricup[0].suit }.maxBy{ it.value }
            }
            player = playerList.value.first { it.name == maxCard.player?.name }
            playerList.value = playerList.value.map { p ->
                if(p.name == player.name){
                    p.scorePerRound += uiState.value.pricup.sumOf { it.value }
                }

                if(p.name == uiState.value.pricup[0].player?.name){
                    p.scorePerRound += chomba(uiState.value.pricup[0], p)
                }
                p
            }

            uiState.value = uiState.value.copy(
                playedCard = null,
                pricup = listOf(),
                currentTraderIndex = playerList.value.indexOf(player),
            )
        }
        else{
            uiState.value = uiState.value.copy(
                playedCard = null
            )
            nextTurner()
        }
        if(!endRound()){
            botTurn()
        }else{
//            nextRound()
        }

    }

    private fun nextRound(){
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            var score = existingPlayer.scorePerRound
            var type = 1

            if (existingPlayer.scorePerRound == 0) {
                type = 0
            }

            if(uiState.value.declarer == existingPlayer.name
                || uiState.value.playerOnBarrel?.name == existingPlayer.name) {

                if(existingPlayer.scorePerRound >= uiState.value.declaration) {
                    type = 1
                } else {
                    type = -1
                }
                score = existingPlayer.declaration
                if(existingPlayer.blind)
                    score *= 2
            }

            if(existingPlayer.getTotalScore() + score >= 880 && type != -1) {
                score = if(existingPlayer.getTotalScore() >= 880 && score >= 120) {
                    120
                } else {
                    880 - existingPlayer.getTotalScore()
                }
            }


            if(uiState.value.playerOnBarrel?.name == existingPlayer.name) {
                if(type == -1 && existingPlayer.getMissBarrel() < 2) {
                    type = -2
                    score = existingPlayer.scorePerRound
                }
                else if(type != 1){
                    type = -4
                    score = 120
                }


//                score = 120
            }else if(existingPlayer.getTotalScore() == 880) {
                type = 2
            }

            val newScore = Score(score, type)

            existingPlayer.copy(scoreList = (existingPlayer.scoreList + newScore),
                scorePerRound = 0, blind = false)

        }

        playerList.value = updatedPlayerList

        playerList.value.map { existingPlayer ->
            if (existingPlayer.getTotalScore() == 1000) {
                setWinner(existingPlayer)
            }
        }

        val playerOnBarrel = getPlayerOnBarrel()
        if (playerOnBarrel != null){
            uiState.value = uiState.value.copy(playerOnBarrel = playerOnBarrel)
            setDeclarer(playerOnBarrel.name, 125)
        }
        else{
            uiState.value = uiState.value.copy(playerOnBarrel = null,
                declarer = "")
        }

        uiState.value = uiState.value.copy(round = uiState.value.round + 1,
            distributorIndex = nextDistributorIndex())

        distributeCards()
    }

    fun isPlayerMakeTurn(): Boolean{
        return !uiState.value.pricup.none { it.player?.name == playerList.value[0].name }
    }

    private fun endRound(): Boolean{
        if(playerList.value.all { it.hand.isEmpty() }){
            return true
        }
        return false
    }

    private fun nextTurner(){
        val nextTurnerIndex = (uiState.value.currentTraderIndex + 1) % playerList.value.size
        uiState.value = uiState.value.copy(
            currentTraderIndex = nextTurnerIndex
        )
    }

    private fun botTurn(){
        if(uiState.value.currentTraderIndex == 0 && !playerList.value[0].isBot){
            return
        }
        val turner = playerList.value[uiState.value.currentTraderIndex]
        if(turner.isBot) {
            if(uiState.value.pricup.isNotEmpty()){
                var card = turner.hand.filter { it.suit == uiState.value.pricup[0].suit }.minByOrNull { it.value }
                if(card == null){
                    card = turner.hand.minBy { it.value }
                }
                playCard(card, turner.name)
            }else{
                val card = turner.hand.maxByOrNull { it.value }!!
                playCard(card, turner.name)
            }

            confirmTurn()
        }
    }

    private fun chomba(card: Card, player: Player): Int {
        if(card.value == CardValue.KING.customValue || card.value == CardValue.QUEEN.customValue
            && player.scorePerRound > 0){
            if(player.hand.any { it.value == CardValue.KING.customValue ||
                it.value == CardValue.QUEEN.customValue && it.suit == card.suit}){
                uiState.value = uiState.value.copy(
                    currentChomba = card.suit
                )
                return when(card.suit){
                    CardSuit.CORAZON.ordinal -> 100
                    CardSuit.DIAMANTE.ordinal -> 80
                    CardSuit.TREBOL.ordinal -> 60
                    CardSuit.PICA.ordinal -> 40
                    else -> 0
                }
            }
        }
        return 0
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

    fun getCurrentGame(): Game {
        val gameUiState = GameUiState()

        return Game(
            id = "",
            date = 0,
            playerList = playerList.value,
            uiState = gameUiState
        )
    }

    fun setWinner(player: Player?) {
        uiState.value = uiState.value.copy(winner = player)
    }

    private fun getPlayerOnBarrel(): Player? {

        val scoreListSize = playerList.value[0].scoreList.size
        var playersOnBarrel : List<Player> = listOf()
        for (player in playerList.value){
            if (player.getTotalScore() == 880){
                playersOnBarrel = playersOnBarrel + player
            }
        }

        if (playersOnBarrel.size == 1){
            return playersOnBarrel[0]
        }
        else if (playersOnBarrel.size > 1){
            var countList: List<Int> = listOf()
            for (player in playersOnBarrel){
                var count = 0
                for(i in playersOnBarrel.size-1 downTo 1){
                    if (player.scoreList[scoreListSize - i].type == 2 ||
                        player.scoreList[scoreListSize - i].type == -2){
                        count++
                    }
                }
                countList = countList + count
            }

            val minIndex = countList.indexOf(countList.minOrNull())
            var playerOnBarrel = playersOnBarrel[0]
            //count equal values in countList
            if(countList.count(countList[minIndex]::equals) == playersOnBarrel.size){
                for(player in playersOnBarrel){
                    makePenalty(player)
                }
                return null
            }else{
                for(player in playersOnBarrel){
                    if(playersOnBarrel.indexOf(player) == minIndex){
                        playerOnBarrel = player
                    }else{
                        makePenalty(player)
                    }
                }
            }

            return playerOnBarrel
        }

        return null
    }

    fun makePenalty(player: Player){
        val updatedPlayerList = playerList.value.map { existingPlayer ->
            var score = 0

            if(existingPlayer.name == player.name) {
                score = -120
            }

            val newScore = Score(score, 1)

            if (score != 0){
                existingPlayer.copy(scoreList = (existingPlayer.scoreList + newScore),
                    scorePerRound = 0)
            }
            else{
                existingPlayer
            }

        }

        playerList.value = updatedPlayerList
    }

    private fun nextDistributorIndex(): Int {
        val index = uiState.value.distributorIndex
        return if (index == playerList.value.size - 1) 0 else index + 1
    }

    private fun train() {
        val cardEvaluator = CardEvaluator(getApplication())

        val totalIterations = 1000
            //for (i in 0..10) {
        val hands = mutableListOf<List<Card>>()
        val actualPointsList = mutableListOf<Double>()
        val ap = mutableListOf<Double>()

        for (iteration in 0..totalIterations) {
            val newPlayerList = mutableListOf<Player>()
            newPlayerList.add(Player(name = "Bot 1", isBot = true))
            newPlayerList.add(Player(name = "Bot 2", isBot = true))
            newPlayerList.add(Player(name = "Bot 3", isBot = true))

            playerList.value = newPlayerList

            val deck = createDeck()
            shuffleDeck(deck)
            val deal = dealCards(playerList.value, deck, 7)
            playerList.value = playerList.value.map { player ->
                player.copy(
                    hand = deal.first.first { it.name == player.name }.hand,
                    isPass = false,
                    declaration = cardEvaluator.predict(player.hand).roundToInt(),
                    scorePerRound = 0
                )
            }
            uiState.value = uiState.value.copy(
                pricup = deal.second,
            )
            val currentHand: MutableList<List<Card>> = mutableListOf()
            for (player in playerList.value) {
                currentHand.add(player.hand)
            }

            val predictedPoints: MutableList<Double> = mutableListOf()
            for (player in playerList.value) {
                predictedPoints.add(player.declaration.toDouble())
            }
            botPlay()
            val actualPoints: MutableList<Double> = mutableListOf()
            for (player in playerList.value) {
                actualPoints.add(player.scorePerRound.toDouble())
            }

            hands.add(currentHand[0])
            ap.add(actualPoints[0])
            if ((actualPoints[0] - countBotScore(hands[0]).toDouble()) >= 0)
                actualPoints[0] = 1.0
            else
                actualPoints[0] = 0.0
            actualPointsList.add(actualPoints[0])

//            cardEvaluator.train(currentHand[0], predictedPoints[0], actualPoints[0])

//            Log.d("AI_test", "Predicted: ${cardEvaluator.predict(currentHand[0])}, Actual: ${deNormalizeValues(actualPoints)}")
//            if (iteration % 1000 == 0 || iteration >= totalIterations - 10 || iteration <= 10)
//                Log.d(
//                    "AI_test",
//                    "Iteration $iteration: predicted ${cardEvaluator.predict(currentHand[0])}, actual ${actualPoints[0]}"
//                )
        }
            cardEvaluator.train(hands, actualPointsList)
        cardEvaluator.endTraining()


        var x = 0
        for (iteration in 0..totalIterations) {

            var predict = cardEvaluator.predict(hands[iteration])
//                if (iteration >= totalIterations - 100) {
//                    Log.d(
//                        "AI_test",
//                        "Precision: ${predict} -actual: ${actualPointsList[iteration]}"
//                    )
//                }
            if (predict <= 0.4 && 0.0 == actualPointsList[iteration]) {
                x++
            }
            if (predict >= 0.7 && 1.0 == actualPointsList[iteration]) {
                x++
            }
        }
        Log.d("AI_test", "Matches: ${x} / ${totalIterations}")


        //  }
    }

    private fun botPlay(){
        val turner = playerList.value[0]
        getAllCardsFromPricup(turner.name)
        for (card in turner.hand.sortedBy { it.value }.take(2)) {
            getCardFromPlayer(card, turner.name)
        }
        playerList.value = playerList.value.map { player ->
            if(player.name != turner.name) {
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
                    currentTraderIndex = playerList.value.indexOf(player),
                    declaration = player.declaration,
                    declarer = player.name
                )
            }
            player
        }

        botTurn()

    }

    fun countBotScore(hand: List<Card>): Int{
        var score = 0
        hand.forEach { card ->
            score += card.value
        }

        for(card in hand){
            if(card.value == CardValue.KING.customValue || card.value == CardValue.QUEEN.customValue){
                if(hand.any { it.value == CardValue.KING.customValue ||
                    it.value == CardValue.QUEEN.customValue && it.suit == card.suit && it != card}){
                    score += when(card.suit){
                        CardSuit.CORAZON.ordinal -> 50
                        CardSuit.DIAMANTE.ordinal -> 40
                        CardSuit.TREBOL.ordinal -> 30
                        CardSuit.PICA.ordinal -> 20
                        else -> 0
                    }
                }
            }
        }

        return score
    }



}