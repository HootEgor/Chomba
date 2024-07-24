package com.example.chomba.ui.theme.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.chomba.R
import com.example.chomba.ui.theme.Shapes

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    title: String,
    firstButtonIcon: Int = R.drawable.baseline_arrow_back_ios_24,
    onFirstActionClick: () -> Unit = {},
    secondButtonIcon: Int = R.drawable.baseline_save_24,
    onSecondActionClick: () -> Unit = {},
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
                    action = onFirstActionClick
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
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