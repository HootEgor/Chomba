package com.egorhoot.chomba.data

data class Score(
    var value: Int,
    var type: Int,
    var round: Int = 0
){
    constructor() : this(value = 0, type = 10, round = 0)
}
