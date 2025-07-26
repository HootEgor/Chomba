package com.egorhoot.chomba.ui.theme.composable

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BasicIconButton(@StringRes text: Int, @DrawableRes icon: Int, modifier: Modifier, action: () -> Unit) {
    Button(
        onClick = action,
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        colors =
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(text),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun BasicTextButton(
    @StringRes text: Int,
    modifier: Modifier,
    action: () -> Unit,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ),
    elevation: Int = 4,
    isEnabled: Boolean = true
) {
    Button(
        onClick = action,
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = elevation.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        colors = colors,
        enabled = isEnabled
    ) {
        Text(
            text = stringResource(text),
            fontSize = 16.sp
        )
    }
}

@Composable
fun IconButton(
    @DrawableRes icon: Int,
    modifier: Modifier,
    action: () -> Unit,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ),
    noIcon: Boolean = false,
    isEnabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.medium
) {
    Button(
        onClick = action,
        modifier = modifier,
        shape = shape,
        colors = colors,
        enabled = isEnabled
    ) {
        if (noIcon) {
            Spacer(modifier = Modifier.width(24.dp))
        } else {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun FullIconButton(
    @DrawableRes icon: Int,
    modifier: Modifier,
    action: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    isEnabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .clickable(onClick = action, enabled = isEnabled)
            .fillMaxHeight()
            .background(color = containerColor)
            .border(
                width = 1.dp,
                color = containerColor,
                shape = MaterialTheme.shapes.medium
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .padding(1.dp),
            tint = contentColor
        )
    }
}