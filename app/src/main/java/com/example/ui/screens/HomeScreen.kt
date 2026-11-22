package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.LevelRecordEntity
import com.example.data.repository.ThemeMode
import com.example.domain.generator.WordSearchGenerator
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.util.formatDuration
import com.example.ui.viewmodel.GameUiState
import com.example.ui.viewmodel.LeaderboardStats

private data class LevelTierPreset(
    val level: Int,
    val title: String,
    val subtitle: String
)

private val TIER_PRESETS = listOf(
    LevelTierPreset(1, "Starter", "6×6 · Forward only"),
    LevelTierPreset(3, "Easy", "7×7 · +Diagonals"),
    LevelTierPreset(6, "Moderate", "8×8 · +Reverse"),
    LevelTierPreset(11, "Challenging", "9×9 · Dense"),
    LevelTierPreset(19, "Hard", "10×10 · Decoys"),
    LevelTierPreset(31, "Expert", "11×11 · Deep"),
    LevelTierPreset(51, "Master", "12×12 · 17+ words"),
    LevelTierPreset(100, "Grandmaster", "13×13 · 21+ words"),
    LevelTierPreset(150, "Infinity+", "14×14 · 23+ words")
)

@Composable
fun HomeScreen(
    uiState: GameUiState,
    stats: LeaderboardStats,
    recordsByLevel: Map<Int, LevelRecordEntity>,
    onContinueCurrent: () -> Unit,
    onStartLevel: (Int) -> Unit,
    onCycleTheme: () -> Unit,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        val isWide = maxWidth >= 700.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 920.dp)
                .verticalScroll(scrollState)
                .padding(vertical = 12.dp)
        ) {
            HomeHeader(
                themeMode = uiState.themeMode,
                soundEnabled = uiState.soundEnabled,
                stats = stats,
                onCycleTheme = onCycleTheme,
                onToggleSound = onToggleSound
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CurrentPuzzleHeroCard(
                            uiState = uiState,
                            onContinue = onContinueCurrent
                        )
                        JumpToAnyLevelCard(
                            initialLevel = uiState.currentLevel,
                            onStartLevel = onStartLevel
                        )
                    }
                    DifficultyTiersSection(
                        currentLevel = uiState.currentLevel,
                        recordsByLevel = recordsByLevel,
                        onSelectPresetLevel = onStartLevel,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                CurrentPuzzleHeroCard(
                    uiState = uiState,
                    onContinue = onContinueCurrent
                )
                Spacer(modifier = Modifier.height(14.dp))
                JumpToAnyLevelCard(
                    initialLevel = uiState.currentLevel,
                    onStartLevel = onStartLevel
                )
                Spacer(modifier = Modifier.height(18.dp))
                DifficultyTiersSection(
                    currentLevel = uiState.currentLevel,
                    recordsByLevel = recordsByLevel,
                    onSelectPresetLevel = onStartLevel
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HomeHeader(
    themeMode: ThemeMode,
    soundEnabled: Boolean,
    stats: LeaderboardStats,
    onCycleTheme: () -> Unit,
    onToggleSound: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Rounded.GridOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (stats.totalLevelsCompleted > 0) {
                        "${stats.totalLevelsCompleted} solved · Levels 1 to ∞"
                    } else {
                        stringResource(R.string.home_subtitle)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onToggleSound,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("home_sound_toggle_button")
            ) {
                Icon(
                    imageVector = if (soundEnabled) {
                        Icons.AutoMirrored.Rounded.VolumeUp
                    } else {
                        Icons.AutoMirrored.Rounded.VolumeOff
                    },
                    contentDescription = if (soundEnabled) "Mute sound effects" else "Unmute sound effects",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onCycleTheme,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("home_theme_toggle_button")
            ) {
                Icon(
                    imageVector = when (themeMode) {
                        ThemeMode.SYSTEM -> Icons.Rounded.BrightnessAuto
                        ThemeMode.LIGHT -> Icons.Rounded.LightMode
                        ThemeMode.DARK -> Icons.Rounded.DarkMode
                    },
                    contentDescription = "Switch theme (${themeMode.name.lowercase()})",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CurrentPuzzleHeroCard(
    uiState: GameUiState,
    onContinue: () -> Unit
) {
    val puzzle = uiState.puzzle
    val level = puzzle?.level ?: uiState.currentLevel
    val totalWords = puzzle?.words?.size ?: 0
    val foundCount = uiState.foundWords.size
    val targetProgress = if (totalWords > 0) foundCount.toFloat() / totalWords.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(320),
        label = "hero_progress"
    )
    val hasStarted = foundCount > 0 || uiState.elapsedSeconds > 0L

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_current_puzzle_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = when {
                            uiState.isCompleted -> "COMPLETED"
                            hasStarted -> "IN PROGRESS"
                            else -> "CURRENT BOARD"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDuration(uiState.elapsedSeconds),
                        fontFamily = JetBrainsMonoFamily,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Level $level",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (puzzle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${puzzle.gridSize}×${puzzle.gridSize} · ${puzzle.difficulty.tierLabel} · ${puzzle.themeName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (totalWords > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$foundCount of $totalWords words found",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(targetProgress * 100).toInt()}%",
                        fontFamily = JetBrainsMonoFamily,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("home_continue_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (hasStarted && !uiState.isCompleted) {
                        stringResource(R.string.continue_playing)
                    } else {
                        stringResource(R.string.start_playing, level)
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun JumpToAnyLevelCard(
    initialLevel: Int,
    onStartLevel: (Int) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var levelText by remember(initialLevel) { mutableStateOf(initialLevel.toString()) }
    val parsedLevel = levelText.toIntOrNull()?.coerceIn(1, 999_999) ?: 1
    val previewProfile = remember(parsedLevel) {
        WordSearchGenerator.difficultyForLevel(parsedLevel)
    }

    fun submitCustomLevel() {
        keyboardController?.hide()
        onStartLevel(parsedLevel)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_jump_any_level_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Jump to Any Level",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "All levels from 1 to ∞ are open — skip ahead anytime.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = {
                        val next = (parsedLevel - 1).coerceAtLeast(1)
                        levelText = next.toString()
                    },
                    enabled = parsedLevel > 1,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("home_level_decrement_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = "Decrease level"
                    )
                }

                OutlinedTextField(
                    value = levelText,
                    onValueChange = { raw ->
                        val digitsOnly = raw.filter { it.isDigit() }.take(6)
                        levelText = digitsOnly
                    },
                    singleLine = true,
                    label = { Text("Level #") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = { submitCustomLevel() }
                    ),
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_custom_level_input")
                )

                FilledTonalIconButton(
                    onClick = {
                        val next = (parsedLevel + 1).coerceAtMost(999_999)
                        levelText = next.toString()
                    },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("home_level_increment_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Increase level"
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                val dirsText = when {
                    previewProfile.allowReverse -> "8 directions"
                    previewProfile.allowDiagonals -> "Forward + diagonals"
                    else -> "Horizontal & vertical"
                }
                Text(
                    text = "Level $parsedLevel · ${previewProfile.gridSize}×${previewProfile.gridSize} · ${previewProfile.wordCount} words · ${previewProfile.tierLabel} ($dirsText)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FilledTonalButton(
                onClick = { submitCustomLevel() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("home_go_custom_level_button")
            ) {
                Text(
                    text = "Play Level $parsedLevel",
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DifficultyTiersSection(
    currentLevel: Int,
    recordsByLevel: Map<Int, LevelRecordEntity>,
    onSelectPresetLevel: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Difficulty Milestones",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Each tier increases grid size, word density, reverse diagonals & decoy traps",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TIER_PRESETS.forEach { preset ->
                val isCurrent = currentLevel == preset.level
                val record = recordsByLevel[preset.level]

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isCurrent) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    border = BorderStroke(
                        width = if (isCurrent) 1.5.dp else 1.dp,
                        color = if (isCurrent) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectPresetLevel(preset.level) }
                        .minimumInteractiveComponentSize()
                        .testTag("home_preset_level_${preset.level}")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LVL ${preset.level}",
                                fontFamily = JetBrainsMonoFamily,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = record?.let { formatDuration(it.bestTimeSeconds) } ?: "Open",
                                fontFamily = if (record != null) JetBrainsMonoFamily else null,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = preset.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = preset.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
