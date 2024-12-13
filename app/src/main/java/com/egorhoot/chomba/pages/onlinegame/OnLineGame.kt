package com.egorhoot.chomba.pages.onlinegame

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.egorhoot.chomba.R
import com.egorhoot.chomba.data.User
import com.egorhoot.chomba.ui.theme.Shapes
import com.egorhoot.chomba.ui.theme.composable.BasicTextButton
import com.egorhoot.chomba.ui.theme.composable.IconButton
import com.egorhoot.chomba.ui.theme.composable.TopBar

@Composable
fun OnLineGame(
    modifier: Modifier = Modifier,
    viewModel: OnLineGameViewModel = hiltViewModel(),
) {
    val uiState = viewModel.onLineGameUiState.value
    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopBar(
                title = uiState.topBarText,
                onFirstActionClick = { viewModel.homePage()},
                secondButtonIcon = R.drawable.baseline_content_copy_24,
                onSecondActionClick = {},
                secondIconEnabled = true
            )
            Column(
                modifier = modifier.fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


            }
        }

    }
}

