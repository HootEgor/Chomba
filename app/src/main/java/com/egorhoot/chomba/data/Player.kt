package com.egorhoot.chomba.data

import android.net.Uri
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class Player(
    var visible: Boolean = true,
    var userId: String = "",
    var name: String = "",
    var color: String = generateRandomColor().toString(),
    var scoreList: List<Score> = listOf(),
    var scorePerRound: Int = 0,
    var declaration: Int = 0,
    var blind: Boolean = false,
    var hand: List<Card> = listOf(),
    var isBot: Boolean = false,
    var isPass: Boolean = false,
    var takenChombas: List<CardSuit> = listOf(),
    var userPicture: String = "",
){
    constructor() : this(visible = true,
        name = generateRandomName(),
        color = generateRandomColor().toString(),
        scoreList = listOf(),
        scorePerRound = 0,
        declaration = 0,
        blind = false,
        hand = listOf(),
        isBot = false,
        isPass = false,
        takenChombas = listOf(),
        userPicture = "")
}

fun generateRandomName(): String {
    val animals = listOf(
        "Tiger", "Panda", "Koala", "Eagle", "Owl", "Lion", "Kangaroo", "Penguin", "Dolphin",
        "Fox", "Rabbit", "Bear", "Wolf", "Hawk", "Elephant", "Giraffe", "Monkey", "Zebra",
        "Whale", "Turtle"
    )

    val randomAnimal = animals.random()

    return randomAnimal
}

fun generateRandomColor(): ULong {
    val red = Random.nextInt(256)
    val green = Random.nextInt(256)
    val blue = Random.nextInt(256)
    return Color(red, green, blue).value
}

fun Player.getMaxRound(): Int {
    val useIndex = scoreList.all { it.round == 0 }
    return if (useIndex) {
        scoreList.size
    } else {
        scoreList.maxOf { it.round }
    }
}

fun Player.getTotalScore(roundLimit: Int = -1): Int {
    var totalScore = 0
    var zeroNum = 0
    var dissolutionNum = 0

    val useIndex = scoreList.all { it.round == 0 }

    val scoresToProcess = if (useIndex) {
        if (roundLimit > -1 && roundLimit < scoreList.size) {
            scoreList.subList(0, roundLimit)
        } else {
            scoreList
        }
    } else {
        if (roundLimit > -1) {
            scoreList.filter { it.round <= roundLimit }
        } else {
            scoreList
        }
    }

    for (score in scoresToProcess) {
        when (score.type) {
            0 -> {
                totalScore -= score.value
                zeroNum++
            }
            1, 3 -> totalScore += score.value
            2 -> if (score.value == 120) totalScore += score.value
            -1, -4 -> totalScore -= score.value
            -3 -> dissolutionNum++
        }

        if (dissolutionNum == 3) {
            dissolutionNum = 0
            totalScore = 0
        }

        if (zeroNum == 3) {
            zeroNum = 0
            totalScore = 0
        }

        if (totalScore == 555) {
            totalScore = 0
        }
    }

    return totalScore
}

fun Player.getScoreSum(): Int {
    var sum = 0
    for (i in 0 .. getMaxRound()) {
        sum += getTotalScore(i)
    }
    return sum
}


fun Player.getZeroNum(): Int {
    var zeroNum = 0
    for (score in scoreList) {
        if (score.type == 0) {
            zeroNum++
        }

        if(zeroNum == 4) {
            zeroNum = 1
        }
    }

    if (zeroNum == 3) {
        zeroNum = 0
    }

//    if (getTotalScore()==880)
//        zeroNum = 0

    return zeroNum
}

fun Player.getMissBarrel(): Int {
    var missBarrel = 0
    for (score in scoreList) {
        if (score.type == -2) {
            missBarrel++
        }else if (score.type != 2){
            missBarrel = 0
        }

        if(missBarrel == 4) {
            missBarrel = 1
        }
    }

    return missBarrel
}

fun Player.getBarrel(): Int {
    var barrel = 0
    for (score in scoreList) {
        if (score.type == -2 || score.type == -4) {
            barrel++
        }
    }

    return barrel
}

fun Player.getDissolution(): Int{
    var dissolutionNum = 0
    for (score in scoreList) {
        if (score.type == -3) {
            dissolutionNum++
        }

        if(dissolutionNum == 4) {
            dissolutionNum = 1
        }
    }

    if (dissolutionNum == 3) {
        dissolutionNum = 0
    }

    return dissolutionNum
}

fun Player.getTotalChombas(): Int {
    var totalChombas = 0
    for (score in scoreList) {
        totalChombas += score.takenChombas.size
    }

    return totalChombas
}

fun Player.getChombaNum(suit: CardSuit): Int {
    return scoreList.filter { it.takenChombas.contains(suit) }.size
}

fun Player.getTotalGain(): Int {
    return scoreList.filter { (it.type == 1 || it.type == 3) && it.value != -120 }.sumOf { it.value }
}

fun Player.getTotalLoss(): Int {
    return scoreList.filter { it.type == -1 || it.type == -4 }.sumOf { -it.value }
}

fun Player.getChombaScore(): Int {
    return takenChombas.sumOf { chombaScore(it.ordinal) }
}


