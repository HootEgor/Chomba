package com.egorhoot.chomba.data


data class User(
    val id: String = "",
    val name: String = "",
    val nickname: String = "",
    val ready: Boolean = false,
    val userPicture: String = "",
){
    constructor() : this(
        id = "",
        name = "",
        nickname = "New User",
        ready = false,
        userPicture = ""
    )
}
