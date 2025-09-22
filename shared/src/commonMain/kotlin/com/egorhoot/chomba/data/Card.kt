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
    CORAZON, DIAMANTE, TREBOL, PICA, ACE
}

fun chombaScore(suit: Int): Int{
    return when(suit){
        0 -> 40
        1 -> 60
        2 -> 80
        3 -> 100
        4 -> 200
        else -> 0
    }
}
