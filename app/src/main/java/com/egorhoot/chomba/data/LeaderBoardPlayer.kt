package com.egorhoot.chomba.data

import android.annotation.SuppressLint

data class LeaderBoardPlayer(
    val name: String,
    val wins: Int,
    val totalScore: Int,
    val winStreak: Int,
    val totalChombas: Int
)

fun List<LeaderBoardPlayer>.sortedByWins(): List<LeaderBoardPlayer> {
    return this.sortedByDescending { it.wins }
}

fun List<LeaderBoardPlayer>.sortedByTotalScore(): List<LeaderBoardPlayer> {
    return this.sortedByDescending { it.totalScore }
}

fun List<LeaderBoardPlayer>.sortedByWinStreak(): List<LeaderBoardPlayer> {
    return this.sortedByDescending { it.winStreak }
}

fun List<LeaderBoardPlayer>.sortedByTotalChombas(): List<LeaderBoardPlayer> {
    return this.sortedByDescending { it.totalChombas }
}

@SuppressLint("DefaultLocale")
fun LeaderBoardPlayer.getScoreText(): String {
    return when {
        totalScore < 0 -> "0"
        totalScore < 1000 -> totalScore.toString()
        totalScore < 1000000 -> String.format("%.1f K", totalScore.toFloat()/1000)
        else -> String.format("%.1f B", totalScore.toFloat()/1000000)
    }
}
