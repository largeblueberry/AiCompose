package com.largeblueberry.setting.ui.theme.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.largeblueberry.core_ui.ThemeOption
import com.largeblueberry.resources.R as ResourceR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ThemeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = ResourceR.string.theme_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = ResourceR.string.back_button_description)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id = ResourceR.string.select_theme),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ThemeOptionsList(
                selectedTheme = uiState.selectedTheme,
                onThemeSelected = viewModel::onThemeSelected
            )
        }
    }
}

@Composable
private fun ThemeOptionsList(
    selectedTheme: ThemeOption,
    onThemeSelected: (ThemeOption) -> Unit
) {
    Column(
        modifier = Modifier.selectableGroup()
    ) {
        ThemeOptionItem(
            theme = ThemeOption.SYSTEM,
            title = stringResource(id = ResourceR.string.theme_option_system_title),
            subtitle = stringResource(id = ResourceR.string.theme_option_system_subtitle),
            icon = Icons.Default.Settings,
            isSelected = selectedTheme == ThemeOption.SYSTEM,
            onSelected = onThemeSelected
        )

        Spacer(modifier = Modifier.height(8.dp))

        ThemeOptionItem(
            theme = ThemeOption.LIGHT,
            title = stringResource(id = ResourceR.string.theme_option_light_title),
            subtitle = stringResource(id = ResourceR.string.theme_option_light_subtitle),
            icon = Icons.Default.LightMode,
            isSelected = selectedTheme == ThemeOption.LIGHT,
            onSelected = onThemeSelected
        )

        Spacer(modifier = Modifier.height(8.dp))

        ThemeOptionItem(
            theme = ThemeOption.DARK,
            title = stringResource(id = ResourceR.string.theme_option_dark_title),
            subtitle = stringResource(id = ResourceR.string.theme_option_dark_subtitle),
            icon = Icons.Default.DarkMode,
            isSelected = selectedTheme == ThemeOption.DARK,
            onSelected = onThemeSelected
        )
    }
}


@Composable
private fun ThemeOptionItem(
    theme: ThemeOption,
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onSelected: (ThemeOption) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = { onSelected(theme) },
                role = Role.RadioButton
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = null // 전체 카드가 클릭 가능하므로 null
            )
        }
    }
}
