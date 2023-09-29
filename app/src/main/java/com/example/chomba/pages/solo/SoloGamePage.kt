package com.example.chomba.pages.solo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.chomba.GameViewModel
import com.example.chomba.R
import com.example.chomba.pages.Dropdown
import com.example.chomba.ui.theme.composable.TopBar

@Composable
fun SoloGamePage(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel,
    soloViewModel: SoloViewModel
){
    val uiState by viewModel.uiState
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
                                viewModel.saveGame()
                                saveAlert.value = true
                            }
                        }
                    },
                    icon = R.drawable.baseline_more_vert_24
                )
            }
        )


    }
}