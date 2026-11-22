package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.example.R
import com.example.data.local.LevelRecordEntity
import com.example.data.repository.ThemeMode
import com.example.domain.model.GridCoord
import com.example.ui.components.LevelCompleteDialog
import com.example.ui.components.LevelSelectorSheet
import com.example.ui.components.PausedOverlayCard
import com.example.ui.components.WordBankSection
import com.example.ui.components.WordSearchGrid
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.util.formatDuration
import com.example.ui.viewmodel.GameUiState

@Composable
fun PlayScreen(
    uiState: GameUiState,
    recordsByLevel: Map<Int, LevelRecordEntity>,
    isDarkTheme: Boolean,
    onTogglePause: () -> Unit,
    onSelectionStep: (Int) -> Unit,
    onArmedCellChanged: (GridCoord?) -> Unit,
    onSubmitSelection: (List<GridCoord>) -> Boolean,
    onUseHint: () -> Unit,
    onNewBoard: () -> Unit,
    onNextLevel: () -> Unit,
    onReplayLevel: () -> Unit,
    onSelectLevel: (Int) -> Unit,
    onCycleTheme: () -> Unit,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLevelPicker by remember { mutableStateOf(false) }

    val puzzle = uiState.puzzle
    if (uiState.isLoading || puzzle == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val totalWidth = maxWidth
        val totalHeight = maxHeight
        val isWideLayout = totalWidth >= 640.dp

        if (isWideLayout) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    val side = min(totalWidth * 0.54f, totalHeight - 16.dp)
                    AnimatedContent(
                        targetState = puzzle,
                        contentKey = { it.level to it.seed },
                        transitionSpec = {
                            (fadeIn(tween(240)) + scaleIn(
                                initialScale = 0.96f,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            )) togetherWith fadeOut(tween(140))
                        },
                        label = "wide_board_transition"
                    ) { animatedPuzzle ->
                        Box(modifier = Modifier.size(side)) {
                            WordSearchGrid(
                                puzzle = animatedPuzzle,
                                foundWords = uiState.foundWords,
                                activeHint = uiState.activeHint,
                                armedStartCell = uiState.armedStartCell,
                                invalidAttemptTick = uiState.invalidAttemptTick,
                                isDarkTheme = isDarkTheme,
                                onSelectionStep = onSelectionStep,
                                onArmedCellChanged = onArmedCellChanged,
                                onSubmitSelection = onSubmitSelection,
                                modifier = Modifier.fillMaxSize()
                            )
                            androidx.compose.animation.AnimatedVisibility(
                                visible = uiState.isPaused,
                                enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.95f),
                                exit = fadeOut(tween(140)) + scaleOut(targetScale = 0.95f)
                            ) {
                                PausedOverlayCard(
                                    level = animatedPuzzle.level,
                                    onResume = onTogglePause
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    PlayTopHud(
                        level = puzzle.level,
                        gridSize = puzzle.gridSize,
                        tierLabel = puzzle.difficulty.tierLabel,
                        themeName = puzzle.themeName,
                        elapsedSeconds = uiState.elapsedSeconds,
                        isPaused = uiState.isPaused,
                        themeMode = uiState.themeMode,
                        soundEnabled = uiState.soundEnabled,
                        onOpenLevelPicker = { showLevelPicker = true },
                        onTogglePause = onTogglePause,
                        onCycleTheme = onCycleTheme,
                        onToggleSound = onToggleSound
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        WordBankSection(
                            words = puzzle.words,
                            foundWords = uiState.foundWords,
                            activeHint = uiState.activeHint,
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    PlayActionBar(
                        foundCount = uiState.foundWords.size,
                        totalWords = puzzle.words.size,
                        hintsUsed = uiState.hintsUsed,
                        hasFirstLetterHintActive = uiState.activeHint != null &&
                            !uiState.activeHint.isFullReveal &&
                            uiState.activeHint.word !in uiState.foundWords,
                        isPaused = uiState.isPaused,
                        onUseHint = onUseHint,
                        onNewBoard = onNewBoard
                    )
                }
            }
        } else {
            val boardSide = min(totalWidth, totalHeight * 0.56f)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 560.dp)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PlayTopHud(
                    level = puzzle.level,
                    gridSize = puzzle.gridSize,
                    tierLabel = puzzle.difficulty.tierLabel,
                    themeName = puzzle.themeName,
                    elapsedSeconds = uiState.elapsedSeconds,
                    isPaused = uiState.isPaused,
                    themeMode = uiState.themeMode,
                    soundEnabled = uiState.soundEnabled,
                    onOpenLevelPicker = { showLevelPicker = true },
                    onTogglePause = onTogglePause,
                    onCycleTheme = onCycleTheme,
                    onToggleSound = onToggleSound
                )

                Spacer(modifier = Modifier.height(10.dp))

                AnimatedContent(
                    targetState = puzzle,
                    contentKey = { it.level to it.seed },
                    transitionSpec = {
                        (fadeIn(tween(240)) + scaleIn(
                            initialScale = 0.96f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                        )) togetherWith fadeOut(tween(140))
                    },
                    label = "portrait_board_transition"
                ) { animatedPuzzle ->
                    Box(
                        modifier = Modifier.size(boardSide),
                        contentAlignment = Alignment.Center
                    ) {
                        WordSearchGrid(
                            puzzle = animatedPuzzle,
                            foundWords = uiState.foundWords,
                            activeHint = uiState.activeHint,
                            armedStartCell = uiState.armedStartCell,
                            invalidAttemptTick = uiState.invalidAttemptTick,
                            isDarkTheme = isDarkTheme,
                            onSelectionStep = onSelectionStep,
                            onArmedCellChanged = onArmedCellChanged,
                            onSubmitSelection = onSubmitSelection,
                            modifier = Modifier.fillMaxSize()
                        )
                        androidx.compose.animation.AnimatedVisibility(
                            visible = uiState.isPaused,
                            enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.95f),
                            exit = fadeOut(tween(140)) + scaleOut(targetScale = 0.95f)
                        ) {
                            PausedOverlayCard(
                                level = animatedPuzzle.level,
                                onResume = onTogglePause
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.TopCenter
                ) {
                    WordBankSection(
                        words = puzzle.words,
                        foundWords = uiState.foundWords,
                        activeHint = uiState.activeHint,
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                PlayActionBar(
                    foundCount = uiState.foundWords.size,
                    totalWords = puzzle.words.size,
                    hintsUsed = uiState.hintsUsed,
                    hasFirstLetterHintActive = uiState.activeHint != null &&
                        !uiState.activeHint.isFullReveal &&
                        uiState.activeHint.word !in uiState.foundWords,
                    isPaused = uiState.isPaused,
                    onUseHint = onUseHint,
                    onNewBoard = onNewBoard
                )
            }
        }
    }

    if (uiState.isCompleted) {
        LevelCompleteDialog(
            level = puzzle.level,
            gridSize = puzzle.gridSize,
            elapsedSeconds = uiState.elapsedSeconds,
            hintsUsed = uiState.hintsUsed,
            isNewBestTime = uiState.isNewBestTime,
            previousBestSeconds = uiState.previousBestSeconds,
            onNextLevel = onNextLevel,
            onReplay = onReplayLevel
        )
    }

    if (showLevelPicker) {
        LevelSelectorSheet(
            currentLevel = uiState.currentLevel,
            highestUnlockedLevel = uiState.highestUnlockedLevel,
            recordsByLevel = recordsByLevel,
            onSelectLevel = onSelectLevel,
            onDismiss = { showLevelPicker = false }
        )
    }
}

@Composable
private fun PlayTopHud(
    level: Int,
    gridSize: Int,
    tierLabel: String,
    themeName: String,
    elapsedSeconds: Long,
    isPaused: Boolean,
    themeMode: ThemeMode,
    soundEnabled: Boolean,
    onOpenLevelPicker: () -> Unit,
    onTogglePause: () -> Unit,
    onCycleTheme: () -> Unit,
    onToggleSound: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .weight(1f, fill = false)
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onOpenLevelPicker)
                .minimumInteractiveComponentSize()
                .testTag("level_selector_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.level_label, level),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Select level",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "${gridSize}×${gridSize} · $tierLabel · $themeName",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.testTag("timer_display")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = "Elapsed time",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDuration(elapsedSeconds),
                        fontFamily = JetBrainsMonoFamily,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            IconButton(
                onClick = onTogglePause,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("pause_toggle_button")
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                    contentDescription = if (isPaused) {
                        stringResource(R.string.resume_action)
                    } else {
                        stringResource(R.string.pause_action)
                    },
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(
                onClick = onToggleSound,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("sound_toggle_button")
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
                    .testTag("theme_toggle_button")
            ) {
                Icon(
                    imageVector = when (themeMode) {
                        ThemeMode.SYSTEM -> Icons.Rounded.BrightnessAuto
                        ThemeMode.LIGHT -> Icons.Rounded.LightMode
                        ThemeMode.DARK -> Icons.Rounded.DarkMode
                    },
                    contentDescription = "Switch appearance theme (${themeMode.name.lowercase()})",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PlayActionBar(
    foundCount: Int,
    totalWords: Int,
    hintsUsed: Int,
    hasFirstLetterHintActive: Boolean,
    isPaused: Boolean,
    onUseHint: () -> Unit,
    onNewBoard: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = onUseHint,
                enabled = !isPaused,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("hint_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when {
                        hasFirstLetterHintActive -> "Reveal Word"
                        hintsUsed > 0 -> "${stringResource(R.string.hint_action)} ($hintsUsed)"
                        else -> stringResource(R.string.hint_action)
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }

            OutlinedButton(
                onClick = onNewBoard,
                enabled = !isPaused,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("new_board_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stringResource(R.string.new_board_action),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
            modifier = Modifier.testTag("word_progress_pill")
        ) {
            Text(
                text = "$foundCount/$totalWords",
                fontFamily = JetBrainsMonoFamily,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
            )
        }
    }
}
