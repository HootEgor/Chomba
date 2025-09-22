package com.egorhoot.chomba.data

data class Score(
    var value: Int,
    var type: Int,
    var round: Int = 0,
    var takenChombas: List<CardSuit> = listOf(),
){
    constructor() : this(value = 0, type = 10, round = 0, takenChombas = listOf())
}

fun Score.getChombas(): List<CardSuit> {
    return takenChombas.sortedByDescending { it.ordinal }.reversed()
}
