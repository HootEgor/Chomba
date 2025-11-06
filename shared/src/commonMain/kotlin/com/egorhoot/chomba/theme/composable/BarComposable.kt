package com.egorhoot.chomba.theme.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chomba.shared.generated.resources.Res
import chomba.shared.generated.resources.baseline_arrow_back_ios_24
import chomba.shared.generated.resources.win_streak
import org.jetbrains.compose.resources.DrawableResource

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    title: String,
    firstButtonIcon: DrawableResource = Res.drawable.baseline_arrow_back_ios_24,
    onFirstActionClick: () -> Unit = {},
    secondButtonIcon: DrawableResource = Res.drawable.win_streak,
    onSecondActionClick: () -> Unit = {},
    titleClickAction: () -> Unit = {},
    firstIconEnabled: Boolean = true,
    secondIconEnabled: Boolean = false,
    isMenuExpanded: Boolean = false,
    menu: @Composable () -> Unit = {}
){
    Box(
        modifier = modifier
            .height(56.dp),
    ) {
        Surface(shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,) {
            Row(modifier = modifier.height(56.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    icon = firstButtonIcon,
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(2.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    action = onFirstActionClick,
                    isEnabled = firstIconEnabled,
                    noIcon = !firstIconEnabled,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    modifier = Modifier
                        .clickable {
                            titleClickAction()
                        }
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isMenuExpanded) {
                    menu()
                }
                else{
                    IconButton(
                        icon = secondButtonIcon,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                        isEnabled = secondIconEnabled,
                        action = onSecondActionClick,
                        noIcon = !secondIconEnabled
                    )
                }

            }
        }
    }
}