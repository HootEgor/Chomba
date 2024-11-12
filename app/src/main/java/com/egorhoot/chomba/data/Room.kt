package com.egorhoot.chomba.data

data class Room(
    val id: String,
    val private: Boolean,
) {
    constructor() : this(id = "", private = true)
}
