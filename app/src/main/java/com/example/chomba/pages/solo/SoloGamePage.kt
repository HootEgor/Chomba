package com.example.chomba.pages.solo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.chomba.GameViewModel
import com.example.chomba.R
import com.example.chomba.data.Card
import com.example.chomba.data.CardSuit
import com.example.chomba.data.CardValue
import com.example.chomba.pages.Dropdown
import com.example.chomba.pages.PlayerCard
import com.example.chomba.ui.theme.composable.BasicIconButton
import com.example.chomba.ui.theme.composable.CardView
import com.example.chomba.ui.theme.composable.SaveGame
import com.example.chomba.ui.theme.composable.Tips
import com.example.chomba.ui.theme.composable.TopBar
import com.example.chomba.ui.theme.ext.basicButton
import kotlin.random.Random

@Composable
fun SoloGamePage(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel,
    soloViewModel: SoloViewModel
){
    val uiState by viewModel.uiState
    val soloUiState by soloViewModel.uiState
    val playerList by viewModel.playerList
    val nextRound = remember { mutableStateOf(false) }
    val setDeclarer = remember { mutableStateOf(false) }
    val showTip = remember { mutableStateOf(false) }
    val saveButton = remember { mutableStateOf(false) }
    val saveAlert = remember { mutableStateOf(false) }
    val isMenuExpanded = remember { mutableStateOf(false) }
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        TopBar(
            title = stringResource(R.string.round) + " " + viewModel.getCurrentRound().toString(),
            onFirstActionClick = { viewModel.setCurrentPage(0) },
            secondButtonIcon = R.drawable.baseline_menu_24,
            onSecondActionClick = {isMenuExpanded.value = true},
            secondIconEnabled = true,
            isMenuExpanded = true,
            menu = {
                val buttonsWithIcons = listOf(
                    R.drawable.baseline_lightbulb_24 to stringResource(R.string.tips),
                    R.drawable.baseline_save_24 to stringResource(R.string.save_game),
                )

                Dropdown(
                    buttonsWithIcons = buttonsWithIcons,
                    onItemClick = { selectedItem ->
                        when (selectedItem) {
                            buttonsWithIcons[0].second -> showTip.value = true
                            buttonsWithIcons[1].second -> {
//                                viewModel.saveGame()
                                saveAlert.value = true
                            }
                        }
                    },
                    icon = R.drawable.baseline_more_vert_24
                )
            }
        )

        Surface(
            modifier = modifier
                .fillMaxSize(0.9f),
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {

                        }

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(0.5f)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Spacer(modifier = Modifier.size(32.dp))
                            soloUiState.pricup.let{
                                for(card in it){
                                    CardView(card = card,
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .aspectRatio(0.7f)
                                            .padding(4.dp),
                                        onClick = {soloViewModel.getCardFromPricup(card)})
                                }
                            }
                            Spacer(modifier = Modifier.size(32.dp))
                        }
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.6f),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    items(soloUiState.playerHand) { card ->
                        CardView(card = card,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.7f)
                                .padding(4.dp))
                    }
                }
            }
        }

        BasicIconButton(text = R.string.confirm,
            icon = R.drawable.baseline_check_24,
            modifier = Modifier
                .basicButton()
                .weight(1f),
            action = {})


    }

    if(saveAlert.value){
        SaveGame(onDismissRequest = {saveAlert.value = false},
            msg = uiState.saveMsg)
    }

    if(showTip.value){
        Tips(onDismissRequest = {showTip.value = false},
            msg = R.string.tips)
    }
}