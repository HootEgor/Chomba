package com.example.chomba.pages.solo

import com.example.chomba.data.Card

data class SoloUiState (
    var pricup: List<Card> = listOf(),
    var selectedCard: Card? = null,
    var playerHand: List<Card> = listOf()
)