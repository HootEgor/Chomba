package com.egorhoot.chomba.pages.onlinegame

import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.egorhoot.chomba.R
import com.egorhoot.chomba.data.User
import com.egorhoot.chomba.ui.theme.Shapes
import com.egorhoot.chomba.ui.theme.composable.IconButton

@Composable
fun OnLineGame(
    modifier: Modifier = Modifier,
    viewModel: OnLineGameViewModel = hiltViewModel()
) {
    val uiState = viewModel.onLineGameUiState.value
    Surface(
        modifier = modifier.fillMaxSize()
    ) {

        Column {
            Text(text = uiState.game.roomCode)
            for (user in uiState.game.userList) {
                UserPreview(user = user)
            }
        }
    }
}

@Composable
fun UserPreview(
    modifier: Modifier = Modifier,
    user: User
) {
    Surface(
        shape = Shapes.medium,
        //shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = modifier.height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (user.userPicture != "") {
                AsyncImage(
                    model = user.userPicture.let { Uri.parse(it) },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = user.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                modifier = modifier.weight(1f)
            )

        }
    }
}