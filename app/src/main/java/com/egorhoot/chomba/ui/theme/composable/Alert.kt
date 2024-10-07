package com.egorhoot.chomba.ui.theme.composable

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.egorhoot.chomba.R
import com.egorhoot.chomba.pages.CenterStripesText
import com.egorhoot.chomba.pages.ChombaCard
import com.egorhoot.chomba.pages.RepeatIcon
import com.egorhoot.chomba.pages.ToggleUnderlineText

@Composable
fun SaveGame(
    onDismissRequest: () -> Unit,
    msg: Int
) {
    AlertDialog(
        onDismissRequest = {onDismissRequest()},
        title = { Text(text = stringResource(R.string.saving), style = MaterialTheme.typography.headlineSmall) },
        text = {
            Text(text = stringResource(msg),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center)
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
    msg: Int,
){
    AlertDialog(
        onDismissRequest = {onDismissRequest()},
        title = { Text(text = stringResource(R.string.tips), style = MaterialTheme.typography.headlineSmall) },
        text = {
            val pagerState = rememberPagerState(initialPage = 1,
                pageCount = { 3 })
            val currentPage = remember { mutableStateOf(1) }
            LaunchedEffect(currentPage.value){
                if(currentPage.value == -1) return@LaunchedEffect
                pagerState.animateScrollToPage(currentPage.value)
                currentPage.value = -1
            }
            Column{
                Row {
                    ToggleUnderlineText(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.cards),
                        onClick = { currentPage.value = 0 },
                        isUnderlined = pagerState.currentPage == 0
                    )
                    ToggleUnderlineText(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.chomba),
                        onClick = { currentPage.value = 1 },
                        isUnderlined = pagerState.currentPage == 1
                    )
                    ToggleUnderlineText(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.reshuffle),
                        onClick = { currentPage.value = 2 },
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
                                    painter = painterResource(id = R.drawable.ic_pica),
                                    text = "40")
                                ChombaCard(iconSize = iconSize,
                                    painter = painterResource(id = R.drawable.ic_trebol),
                                    text = "60")
                                ChombaCard(iconSize = iconSize,
                                    painter = painterResource(id = R.drawable.ic_diamante),
                                    text = "80")
                                ChombaCard(iconSize = iconSize,
                                    painter = painterResource(id = R.drawable.ic_corazon),
                                    text = "100")
                                ChombaCard(iconSize = iconSize*2,
                                    painter = painterResource(id = R.drawable.ic_ace),
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
                                    text = stringResource(R.string.hand),
                                    stripeColor = MaterialTheme.colorScheme.primary
                                )
                                Text(text = "<13",
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center)
                                RepeatIcon(painter = painterResource(id = R.drawable.ic_nine),
                                    iconSize = iconSize,
                                    number = 3)
                                RepeatIcon(painter = painterResource(id = R.drawable.ic_jack),
                                    iconSize = iconSize,
                                    number = 4)
                                CenterStripesText(
                                    text = stringResource(R.string.pool),
                                    stripeColor = MaterialTheme.colorScheme.primary
                                )
                                Text(text = "<5",
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center)
                                RepeatIcon(painter = painterResource(id = R.drawable.ic_nine),
                                    iconSize = iconSize,
                                    number = 2)
                                RepeatIcon(painter = painterResource(id = R.drawable.ic_jack),
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
                                    painter = painterResource(id = R.drawable.ic_nine),
                                    text = "0")
                                ChombaCard(iconSize = iconSize,
                                    painter = painterResource(id = R.drawable.ic_jack),
                                    text = "2")
                                ChombaCard(iconSize = iconSize,
                                    painter = painterResource(id = R.drawable.ic_queen),
                                    text = "3")
                                ChombaCard(iconSize = iconSize,
                                    painter = painterResource(id = R.drawable.ic_king),
                                    text = "4")
                                ChombaCard(iconSize = iconSize,
                                    painter = painterResource(id = R.drawable.ic_ten),
                                    text = "10")
                                ChombaCard(iconSize = iconSize,
                                    painter = painterResource(id = R.drawable.ic_ace_one),
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
                    text = stringResource(R.string.ok),
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
fun AlertOk(@StringRes title: Int, @StringRes message: Int, action: () -> Unit) {
    AlertDialog(
        onDismissRequest = action,
        title = { Text(text = stringResource(id = title), style = MaterialTheme.typography.headlineSmall) },
        text = { Text(text = stringResource(id = message), style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(
                onClick = action
            ) {
                Text(text = stringResource(R.string.confirm_button), style = MaterialTheme.typography.bodyMedium)
            }
        }
    )
}