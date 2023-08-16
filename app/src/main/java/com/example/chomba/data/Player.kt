package com.example.chomba.data

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import java.lang.reflect.GenericDeclaration

data class Player(
    var visible: Boolean = true,
    var name: String = "",
    var color: Color = Color.Magenta,
    var scoreList: List<Score> = listOf(),
    var scorePerRound: Int = 0,
    var declaration: Int = 0,
)

fun Player.getTotalScore(): Int {
    var totalScore = 0
    var zeroNum = 0
    var dissolutionNum = 0
    for (score in scoreList) {
        if(score.type == 0) {
            totalScore -= score.value
            zeroNum++
        }
        else if(score.type == 1 || score.type == 3) {
            totalScore += score.value
        }

        if (score.type == 2 && score.value == 120) {
            totalScore += score.value
        }

        if (score.type == -1 || score.type == -4) {
            totalScore -= score.value
        }

        if (score.type == -3) {
            dissolutionNum++
        }

        if(dissolutionNum == 3) {
            dissolutionNum = 0
            totalScore = 0
        }

        if(zeroNum == 3) {
            zeroNum = 0
            totalScore = 0
        }

        if (totalScore == 555) {
            totalScore = 0
        }
    }

    return totalScore
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

    if (getTotalScore()==880)
        zeroNum = 0

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


