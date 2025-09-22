package com.egorhoot.chomba.data

data class Room(
    val id: String,
    val isPrivate: Boolean, // Renamed from 'private'
) {
    constructor() : this(id = "", isPrivate = true)

}
