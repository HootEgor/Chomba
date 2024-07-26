package com.egorhoot.chomba.data

data class Score(
    var value: Int,
    var type: Int
){
    constructor() : this(value = 0, type = 10)
}
