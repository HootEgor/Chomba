package com.example.chomba.data

data class Card(
    val value: Int,
    val suit: Int,
)

enum class CardValue {
    NINE, JACK, QUEEN, KING, TEN, ACE
}

enum class CardSuit {
    HEARTS, DIAMONDS, CLUBS, SPADES
}
