package com.example.chomba.data

data class Score(
    var value: Int,
    var type: Int
){
    constructor() : this(value = 0, type = 10)
}
