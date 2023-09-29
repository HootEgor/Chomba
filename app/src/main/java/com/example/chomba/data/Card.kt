package com.example.chomba.data

data class Card(
    var value: Int,
    var suit: Int,
){
    constructor() : this(value = 0, suit = 0)
    constructor(value: CardValue, suit: CardSuit) : this(value.ordinal, suit.ordinal)
}

enum class CardValue {
    NINE, JACK, QUEEN, KING, TEN, ACE
}

enum class CardSuit {
    CORAZON, DIAMANTE, TREBOL, PICA
}
