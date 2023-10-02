package com.example.chomba.pages.solo

import com.example.chomba.data.Card

data class SoloUiState (
    val pricup: List<Card> = listOf(),
    val playerHand: List<Card> = listOf(),
    val gameIsStart: Boolean = false,
    val declarer: String = "",
    val declaration: Int = 0,
    val isTrade: Boolean = false,

    val distributorIndex: Int = 0,
)