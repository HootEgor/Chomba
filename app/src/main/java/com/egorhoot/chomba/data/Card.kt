package com.egorhoot.chomba.data

data class Card(
    var value: Int,
    var suit: Int,
    var player: Player? = null
){
    constructor() : this(value = 0, suit = 0, player = null)
    constructor(value: CardValue, suit: CardSuit) : this(value.customValue, suit.ordinal)
}

enum class CardValue(val customValue: Int) {
    NINE(0),
    JACK(2),
    QUEEN(3),
    KING(4),
    TEN(10),
    ACE(11)
}


enum class CardSuit {
    CORAZON, DIAMANTE, TREBOL, PICA
}
