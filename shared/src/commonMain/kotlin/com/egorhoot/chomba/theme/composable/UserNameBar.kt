package com.egorhoot.chomba.theme.composable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import chomba.shared.generated.resources.Res
import chomba.shared.generated.resources.baseline_logout_24
import chomba.shared.generated.resources.baseline_person_24
import coil3.compose.AsyncImage
import com.egorhoot.chomba.theme.Shapes
import org.jetbrains.compose.resources.painterResource

@Composable
fun UserNameBar(
    name: String,
    picture: String?,
    signOutAction: () -> Unit,
    modifier: Modifier = Modifier
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
            if (picture != null) {
                AsyncImage(
                    model = picture,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                )
            } else {
                Icon(
                    painter = painterResource(Res.drawable.baseline_person_24),
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                modifier = modifier.weight(1f)
            )
            IconButton(
                icon = Res.drawable.baseline_logout_24,
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(2.dp),
                action = signOutAction,
            )

        }
    }
}