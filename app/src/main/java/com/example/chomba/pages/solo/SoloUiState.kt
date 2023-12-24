package com.example.chomba.pages.solo

import com.example.chomba.data.Card
import com.example.chomba.data.Player

data class SoloUiState (
    val pricup: List<Card> = listOf(),
    val playerHand: List<Card> = listOf(),
    val gameIsStart: Boolean = false,
    val declarer: String = "",
    val declaration: Int = 0,
    val isTrade: Boolean = false,
    val playerOnBarrel: Player? = null,
    val winner: Player? = null,
    val round: Int = 1,

    val distributorIndex: Int = 0,
    val currentTraderIndex: Int = 0,
    val currentChomba: Int? = null,

    val playedCard: Card? = null,
)