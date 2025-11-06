package com.egorhoot.chomba.theme.composable

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chomba.shared.generated.resources.Res
import chomba.shared.generated.resources.cards
import chomba.shared.generated.resources.chomba
import chomba.shared.generated.resources.confirm_button
import chomba.shared.generated.resources.hand
import chomba.shared.generated.resources.ic_ace
import chomba.shared.generated.resources.ic_ace_one
import chomba.shared.generated.resources.ic_corazon
import chomba.shared.generated.resources.ic_diamante
import chomba.shared.generated.resources.ic_jack
import chomba.shared.generated.resources.ic_king
import chomba.shared.generated.resources.ic_nine
import chomba.shared.generated.resources.ic_pica
import chomba.shared.generated.resources.ic_queen
import chomba.shared.generated.resources.ic_ten
import chomba.shared.generated.resources.ic_trebol
import chomba.shared.generated.resources.ok
import chomba.shared.generated.resources.pool
import chomba.shared.generated.resources.reshuffle
import chomba.shared.generated.resources.saving
import chomba.shared.generated.resources.tips
import com.egorhoot.chomba.pages.CenterStripesText
import com.egorhoot.chomba.pages.ChombaCard
import com.egorhoot.chomba.pages.RepeatIcon
import com.egorhoot.chomba.pages.ToggleUnderlineText
import com.egorhoot.chomba.util.StringProvider
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SaveGame(
    onDismissRequest: () -> Unit,
    msg: String,
    delayTimer: Float = -1.0f
) {
    val progress = remember { Animatable(delayTimer) }

    LaunchedEffect(delayTimer) {
        if (delayTimer >= 0) {
            progress.snapTo(1f)
            progress.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = (delayTimer * 1000).toInt(),
                    easing = LinearEasing
                )
            )
            onDismissRequest()
        }
    }

    AlertDialog(
        onDismissRequest = {onDismissRequest()},
        title = { Text(text = stringResource(Res.string.saving), style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ){
                Text(text = msg,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.size(16.dp))

                if (progress.value >= 0) {
                    LinearProgressIndicator(
                        progress = { progress.value.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        drawStopIndicator = {}
                    )
                }else{
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
        },
        dismissButton = {
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tips(
    onDismissRequest: () -> Unit,
){
    AlertDialog(
        onDismissRequest = {onDismissRequest()},
        title = { Text(text = stringResource(Res.string.tips), style = MaterialTheme.typography.headlineSmall) },
        text = {
            val pagerState = rememberPagerState(initialPage = 1,
                pageCount = { 3 })
            val currentPage = remember { mutableIntStateOf(1) }
            LaunchedEffect(currentPage.intValue){
                if(currentPage.intValue == -1) return@LaunchedEffect
                pagerState.animateScrollToPage(currentPage.intValue)
                currentPage.intValue = -1
            }
            Column{
                Row {
                    ToggleUnderlineText(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Res.string.cards),
                        onClick = { currentPage.intValue = 0 },
                        isUnderlined = pagerState.currentPage == 0
                    )
                    ToggleUnderlineText(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Res.string.chomba),
                        onClick = { currentPage.intValue = 1 },
                        isUnderlined = pagerState.currentPage == 1
                    )
                    ToggleUnderlineText(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Res.string.reshuffle),
                        onClick = { currentPage.intValue = 2 },
                        isUnderlined = pagerState.currentPage == 2
                    )
                }
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .size(400.dp)
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        if(page == 1){
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceEvenly,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val iconSize = 32.dp
                                ChombaCard(iconSize = iconSize,
                                    painter = painterResource(Res.drawable.ic_pica),
                                    text = "40")
                                ChombaCard(iconSize = iconSize,
                                    painter = painterResource(Res.drawable.ic_trebol),
                                    text = "60")
                                ChombaCard(iconSize = iconSize,
                                    painter = painterResource(Res.drawable.ic_diamante),
                                    text = "80")
                                ChombaCard(iconSize = iconSize,
                                    painter = painterResource(Res.drawable.ic_corazon),
                                    text = "100")
                                ChombaCard(iconSize = iconSize*2,
                                    painter = painterResource(Res.drawable.ic_ace),
                                    text = "200")
                            }

                        }
                        else if(page == 2){
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.SpaceEvenly,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val iconSize = 40.dp
                                CenterStripesText(
                                    text = stringResource(Res.string.hand),
                                    stripeColor = MaterialTheme.colorScheme.primary
                                )
                                Text(text = "<13",
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center)
                                RepeatIcon(painter = painterResource(Res.drawable.ic_nine),
                                    iconSize = iconSize,
                                    number = 3)
                                RepeatIcon(painter = painterResource(Res.drawable.ic_jack),
                                    iconSize = iconSize,
                                    number = 4)
                                CenterStripesText(
                                    text = stringResource(Res.string.pool),
                                    stripeColor = MaterialTheme.colorScheme.primary
                                )
                                Text(text = "<5",
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center)
                                RepeatIcon(painter = painterResource(Res.drawable.ic_nine),
                                    iconSize = iconSize,
                                    number = 2)
                                RepeatIcon(painter = painterResource(Res.drawable.ic_jack),
                                    iconSize = iconSize,
                                    number = 3)
                            }
                        }
                        else{
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.SpaceEvenly,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val iconSize = 50.dp
                                ChombaCard(iconSize = iconSize,
                                    painter = painterResource(Res.drawable.ic_nine),
                                    text = "0")
                                ChombaCard(iconSize = iconSize,
                                    painter = painterResource(Res.drawable.ic_jack),
                                    text = "2")
                                ChombaCard(iconSize = iconSize,
                                    painter = painterResource(Res.drawable.ic_queen),
                                    text = "3")
                                ChombaCard(iconSize = iconSize,
                                    painter = painterResource(Res.drawable.ic_king),
                                    text = "4")
                                ChombaCard(iconSize = iconSize,
                                    painter = painterResource(Res.drawable.ic_ten),
                                    text = "10")
                                ChombaCard(iconSize = iconSize,
                                    painter = painterResource(Res.drawable.ic_ace_one),
                                    text = "11")
                            }
                        }
                    }

                }
            }

        },
        confirmButton = {
            TextButton(
                onClick = { onDismissRequest()}
            ) {
                Text(
                    text = stringResource(Res.string.ok),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
        }
    )
}

@Composable
fun AlertOk(
    titleKey: StringResource,
    messageKey: StringResource,
    action: () -> Unit) {

    val titleKey = remember(titleKey) { titleKey }
    val messageKey = remember(messageKey) { messageKey }

    AlertDialog(
        onDismissRequest = action,
        title = { Text(text = stringResource(titleKey), style = MaterialTheme.typography.headlineSmall) },
        text = { Text(text = stringResource(messageKey), style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(
                onClick = action
            ) {
                Text(text = stringResource(Res.string.confirm_button), style = MaterialTheme.typography.bodyMedium)
            }
        }
    )
}
