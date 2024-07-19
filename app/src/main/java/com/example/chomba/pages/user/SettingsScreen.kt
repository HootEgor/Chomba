package com.example.chomba.pages.user

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.chomba.R
import com.example.chomba.data.Language
import com.example.chomba.ui.theme.Shapes

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel
) {
    Surface(
        shape = Shapes.medium,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.speech_rec_idioma),
                style = MaterialTheme.typography.titleMedium
            )
            RadioButtonSample(
                modifier = Modifier.padding(top = 16.dp),
                onSelected = { language ->
                    profileViewModel.SelectSpeechRecLanguage(language)
                },
                selectedOption = profileViewModel.profileUi.value.selectedLanguage
            )
        }
    }

}

@Composable
fun RadioButtonSample(
    modifier: Modifier = Modifier,
    onSelected: (Language) -> Unit = { },
    selectedOption: Language,
) {
    val radioOptions = listOf(
        Language(R.drawable.flag_ua, R.string.tag_ua),
        Language(R.drawable.flag_uk, R.string.tag_uk),
        Language(R.drawable.orc, R.string.tag_ru)
    )
    Column {
        radioOptions.chunked(3).forEach { chunk ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                chunk.forEach { language ->
                    Row(
                        Modifier
                            .selectable(
                                selected = (language == selectedOption),
                                onClick = {onSelected(language) }
                            )
                            .weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = (language == selectedOption),
                            onClick = { onSelected(language) }
                        )
                        Image(
                            painter = painterResource(id = language.icon.toInt()),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                // Fill the remaining space if the chunk has less than 3 items
                repeat(3 - chunk.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}