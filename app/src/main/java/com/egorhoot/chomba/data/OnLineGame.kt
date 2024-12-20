package com.egorhoot.chomba.data

data class OnLineGame (
    val id: String = "",
    val date: Long = 0,
    val room: Room = Room(),
    val userList: List<User> = listOf(),
    val game: Game = Game(),
    val pricup: List<Card> = listOf(),
    val isBindingEnd: Boolean = false,
    val currentActionPlayer: String = "",
)

fun OnLineGame.isFinished(): Boolean {
    return game.isFinished()
}

fun OnLineGame.addUser(userId: String): Boolean {
    if(userList.size < 3){
        val newUser = User(id = userId)
        userList.plus(newUser)
        return true
    }
    return false
}

fun OnLineGame.removeUser(userId: String): Boolean {
    val user = userList.find { it.id == userId }
    if(user != null){
        userList.minus(user)
        return true
    }
    return false
}

fun OnLineGame.isFull(): Boolean {
    return userList.size >= 3
}

