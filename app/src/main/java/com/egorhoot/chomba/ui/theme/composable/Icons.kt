package com.egorhoot.chomba.ui.theme.composable

import androidx.annotation.DrawableRes
import com.egorhoot.chomba.R

@DrawableRes
fun typeIcon(type: Int): Int {
    return when (type) {
        -1 -> R.drawable.baseline_close_24
        0 -> R.drawable.baseline_horizontal_rule_24
        1 -> R.drawable.baseline_check_24
        2, -2, -4 -> R.drawable.ic_1200952
        3 -> R.drawable.ic_gift
        -3 -> R.drawable.baseline_border_color_24
        else -> R.drawable.baseline_square_24
    }
}

@DrawableRes
fun suitIcon(suit: Int): Int {
    return when (suit) {
        0 -> R.drawable.ic_pica
        1 -> R.drawable.ic_trebol
        2 -> R.drawable.ic_diamante
        3 -> R.drawable.ic_corazon
        4 -> R.drawable.ic_ace
        else -> R.drawable.baseline_square_24
    }
}