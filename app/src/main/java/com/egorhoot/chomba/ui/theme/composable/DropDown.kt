package com.egorhoot.chomba.ui.theme.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun Dropdown(
    modifier: Modifier = Modifier,
    buttonsWithIcons: List<Pair<Int, String>>,
    onItemClick: (String) -> Unit,
    icon: Int
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(
        icon = icon,
        modifier = modifier
            .fillMaxHeight()
            .padding(2.dp),
        action = { expanded = true }
    )

    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = Modifier
            .fillMaxHeight()
            .padding(end = 8.dp)
    ) {


        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier

                .background(MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            buttonsWithIcons.forEach { (buttonIcon, buttonText) ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Icon(
                                painter = painterResource(id = buttonIcon),
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp).size(24.dp),
                            )
                            Text(text = buttonText)
                        }
                    },
                    onClick = {
                        onItemClick(buttonText)
                        expanded = false
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                )
            }
        }
    }
}