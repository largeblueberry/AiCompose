package com.largeblueberry.setting.language

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.largeblueberry.setting.language.ui.LanguageViewModel
import com.largeblueberry.setting.language.ui.Language
import com.largeblueberry.resources.R as ResourceR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingScreen(
    viewModel: LanguageViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit = {}
) {

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = ResourceR.string.language_setting_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate up"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(uiState.availableLanguages) { language ->
                LanguageItem(
                    language = language,
                    isSelected = language.code == uiState.selectedLanguageCode,
                    onLanguageSelected = {
                        viewModel.onLanguageSelected(language.code)
                    }
                )
            }
        }
    }
}

@Composable
fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onLanguageSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLanguageSelected)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = language.name,
            style = MaterialTheme.typography.bodyLarge
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected", // 선택됨
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun LanguageSettingScreenPreview() {
    MaterialTheme {

        val languages = listOf(Language("English", "en"), Language("한국어", "ko"))
        val selectedLanguage = "ko"

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("언어 설정") })
            }
        ) { padding ->
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(languages) { lang ->
                    LanguageItem(
                        language = lang,
                        isSelected = lang.code == selectedLanguage,
                        onLanguageSelected = {}
                    )
                }
            }
        }
    }
}

