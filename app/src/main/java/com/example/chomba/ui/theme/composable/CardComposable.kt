package com.example.chomba.ui.theme.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chomba.data.Card
import com.example.chomba.data.CardSuit
import com.example.chomba.data.CardValue

@Composable
fun CardView(
    card: Card?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val suitModifier = Modifier
        .fillMaxWidth()
        .height(24.dp)

    Box(
        modifier = modifier
            .border(2.dp, Color.Black, MaterialTheme.shapes.large)
            .clickable { onClick() },
    ) {
        card?.let { nonNullCard ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),

            ) {
                Box(
                    modifier = suitModifier.weight(1f),
                    contentAlignment = Alignment.TopStart
                ) {
                    Text(
                        text = when (nonNullCard.suit) {
                            CardSuit.CORAZON.ordinal -> "\u2665"
                            CardSuit.DIAMANTE.ordinal -> "\u2666"
                            CardSuit.TREBOL.ordinal -> "\u2663"
                            CardSuit.PICA.ordinal -> "\u2660"
                            else -> ""
                        },
                        fontSize = 24.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (nonNullCard.value) {
                            CardValue.NINE.customValue -> "9"
                            CardValue.JACK.customValue -> "J"
                            CardValue.QUEEN.customValue -> "Q"
                            CardValue.KING.customValue -> "K"
                            CardValue.TEN.customValue -> "10"
                            CardValue.ACE.customValue -> "A"
                            else -> ""
                        },
                        fontSize = 24.sp
                    )
                }
                Box(
                    modifier = suitModifier.weight(1f),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Text(
                        text = when (nonNullCard.suit) {
                            CardSuit.CORAZON.ordinal -> "\u2665"
                            CardSuit.DIAMANTE.ordinal -> "\u2666"
                            CardSuit.TREBOL.ordinal -> "\u2663"
                            CardSuit.PICA.ordinal -> "\u2660"
                            else -> ""
                        },
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}