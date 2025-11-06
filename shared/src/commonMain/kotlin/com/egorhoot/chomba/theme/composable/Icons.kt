package com.egorhoot.chomba.theme.composable

import chomba.shared.generated.resources.Res
import chomba.shared.generated.resources.baseline_border_color_24
import chomba.shared.generated.resources.baseline_check_24
import chomba.shared.generated.resources.baseline_close_24
import chomba.shared.generated.resources.baseline_horizontal_rule_24
import chomba.shared.generated.resources.baseline_square_24
import chomba.shared.generated.resources.ic_1200952
import chomba.shared.generated.resources.ic_ace
import chomba.shared.generated.resources.ic_corazon
import chomba.shared.generated.resources.ic_diamante
import chomba.shared.generated.resources.ic_gift
import chomba.shared.generated.resources.ic_pica
import chomba.shared.generated.resources.ic_trebol
import org.jetbrains.compose.resources.DrawableResource

fun typeIcon(type: Int): DrawableResource {
    return when (type) {
        -1 -> Res.drawable.baseline_close_24
        0 -> Res.drawable.baseline_horizontal_rule_24
        1 -> Res.drawable.baseline_check_24
        2, -2, -4 -> Res.drawable.ic_1200952
        3 -> Res.drawable.ic_gift
        -3 -> Res.drawable.baseline_border_color_24
        else -> Res.drawable.baseline_square_24
    }
}

fun suitIcon(suit: Int): DrawableResource {
    return when (suit) {
        0 -> Res.drawable.ic_pica
        1 -> Res.drawable.ic_trebol
        2 -> Res.drawable.ic_diamante
        3 -> Res.drawable.ic_corazon
        4 -> Res.drawable.ic_ace
        else -> Res.drawable.baseline_square_24
    }
}