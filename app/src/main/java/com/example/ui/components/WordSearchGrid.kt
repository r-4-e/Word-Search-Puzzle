package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.example.domain.model.GridCoord
import com.example.domain.model.HintState
import com.example.domain.model.WordSearchPuzzle
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.WordHighlightColorsDark
import com.example.ui.theme.WordHighlightColorsLight
import com.example.ui.util.computeSnappedLine
import kotlinx.coroutines.launch
import kotlin.math.floor

@Composable
fun WordSearchGrid(
    puzzle: WordSearchPuzzle,
    foundWords: Set<String>,
    activeHint: HintState?,
    armedStartCell: GridCoord?,
    invalidAttemptTick: Int = 0,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onSelectionStep: (Int) -> Unit = {},
    onArmedCellChanged: (GridCoord?) -> Unit,
    onSubmitSelection: (List<GridCoord>) -> Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val highlightPalette = if (isDarkTheme) WordHighlightColorsDark else WordHighlightColorsLight
    val activeDragColor = MaterialTheme.colorScheme.primary
    val hintColor = MaterialTheme.colorScheme.tertiary
    val gridLineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val defaultTextColor = MaterialTheme.colorScheme.onSurface
    val foundTextColor = MaterialTheme.colorScheme.onSurface

    val infiniteTransition = rememberInfiniteTransition(label = "hint_pulse")
    val hintPulse by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 820, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hint_pulse_alpha"
    )

    // Smooth capsule sweep animation per newly discovered word
    val capsuleProgressMap = remember(puzzle.level, puzzle.seed) {
        mutableStateMapOf<String, Animatable<Float, AnimationVector1D>>().apply {
            foundWords.forEach { word ->
                put(word, Animatable(1f))
            }
        }
    }

    LaunchedEffect(foundWords, puzzle.level, puzzle.seed) {
        foundWords.forEach { word ->
            if (!capsuleProgressMap.containsKey(word)) {
                val anim = Animatable(0f)
                capsuleProgressMap[word] = anim
                launch {
                    anim.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    )
                }
            }
        }
    }

    // Subtle horizontal shake animation when releasing an invalid selection
    val shakeOffsetDp = remember { Animatable(0f) }
    LaunchedEffect(invalidAttemptTick) {
        if (invalidAttemptTick > 0) {
            val keyframes = listOf(-7f, 7f, -5f, 5f, -2.5f, 0f)
            for (target in keyframes) {
                shakeOffsetDp.animateTo(
                    targetValue = target,
                    animationSpec = tween(durationMillis = 36, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    var activeDragCells by remember(puzzle.level, puzzle.seed) {
        mutableStateOf<List<GridCoord>>(emptyList())
    }

    val foundCellSet = remember(puzzle, foundWords) {
        puzzle.words
            .filter { it.word in foundWords }
            .flatMap { it.cells }
            .toSet()
    }

    val activeDragSet = remember(activeDragCells) { activeDragCells.toSet() }
    val hintedCellSet = remember(activeHint) { activeHint?.revealedCells?.toSet().orEmpty() }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val boardSize = min(maxWidth, maxHeight)
        val innerPadding = 10.dp
        val density = LocalDensity.current

        Surface(
            modifier = Modifier
                .size(boardSize)
                .graphicsLayer {
                    translationX = shakeOffsetDp.value * density.density
                }
                .testTag("word_search_board"),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val gridPx = with(LocalDensity.current) { min(maxWidth, maxHeight).toPx() }
                val cellPx = gridPx / puzzle.gridSize.toFloat()
                val letterFontSize = with(LocalDensity.current) {
                    (cellPx * 0.45f).coerceIn(10.5f * this.density, 26f * this.density).toSp()
                }

                fun offsetToCoord(offset: Offset): GridCoord? {
                    if (cellPx <= 0f) return null
                    val col = floor(offset.x / cellPx).toInt()
                    val row = floor(offset.y / cellPx).toInt()
                    return if (row in 0 until puzzle.gridSize && col in 0 until puzzle.gridSize) {
                        GridCoord(row, col)
                    } else {
                        null
                    }
                }

                // Background Canvas: subtle grid lines, sweeping found pills, hint pulse, and live drag capsule
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val n = puzzle.gridSize
                    val actualCell = size.width / n.toFloat()

                    for (i in 1 until n) {
                        val pos = i * actualCell
                        drawLine(
                            color = gridLineColor,
                            start = Offset(pos, 0f),
                            end = Offset(pos, size.height),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = gridLineColor,
                            start = Offset(0f, pos),
                            end = Offset(size.width, pos),
                            strokeWidth = 1f
                        )
                    }

                    fun cellCenter(coord: GridCoord): Offset = Offset(
                        x = (coord.col + 0.5f) * actualCell,
                        y = (coord.row + 0.5f) * actualCell
                    )

                    // Found word capsules with smooth sweep animation
                    for (placed in puzzle.words) {
                        if (placed.word in foundWords) {
                            val progress = capsuleProgressMap[placed.word]?.value ?: 1f
                            val baseColor = highlightPalette[placed.colorIndex % highlightPalette.size]
                            val startOffset = cellCenter(placed.start)
                            val targetEndOffset = cellCenter(placed.end)
                            val animatedEnd = lerp(startOffset, targetEndOffset, progress)
                            drawLine(
                                color = baseColor.copy(alpha = if (isDarkTheme) 0.34f else 0.25f),
                                start = startOffset,
                                end = animatedEnd,
                                strokeWidth = actualCell * 0.72f,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    // Active hint highlight with ripple ring
                    if (activeHint != null && activeHint.word !in foundWords && activeHint.revealedCells.isNotEmpty()) {
                        if (activeHint.isFullReveal && activeHint.revealedCells.size >= 2) {
                            drawLine(
                                color = hintColor.copy(alpha = 0.28f * hintPulse),
                                start = cellCenter(activeHint.revealedCells.first()),
                                end = cellCenter(activeHint.revealedCells.last()),
                                strokeWidth = actualCell * 0.76f,
                                cap = StrokeCap.Round
                            )
                        } else {
                            val center = cellCenter(activeHint.revealedCells.first())
                            drawCircle(
                                color = hintColor.copy(alpha = 0.24f * hintPulse),
                                radius = actualCell * (0.38f + 0.08f * hintPulse),
                                center = center
                            )
                            drawCircle(
                                color = hintColor.copy(alpha = 0.88f * hintPulse),
                                radius = actualCell * 0.40f,
                                center = center,
                                style = Stroke(width = actualCell * 0.07f)
                            )
                        }
                    }

                    // Armed start cell (tap-start, tap-end mode)
                    if (armedStartCell != null && activeDragCells.isEmpty()) {
                        val center = cellCenter(armedStartCell)
                        drawCircle(
                            color = activeDragColor.copy(alpha = 0.22f),
                            radius = actualCell * 0.40f,
                            center = center
                        )
                        drawCircle(
                            color = activeDragColor.copy(alpha = 0.85f),
                            radius = actualCell * 0.40f,
                            center = center,
                            style = Stroke(width = actualCell * 0.06f)
                        )
                    }

                    // Live drag capsule
                    if (activeDragCells.isNotEmpty()) {
                        val first = cellCenter(activeDragCells.first())
                        val last = cellCenter(activeDragCells.last())
                        if (activeDragCells.size == 1) {
                            drawCircle(
                                color = activeDragColor.copy(alpha = 0.28f),
                                radius = actualCell * 0.38f,
                                center = first
                            )
                        } else {
                            drawLine(
                                color = activeDragColor.copy(alpha = 0.32f),
                                start = first,
                                end = last,
                                strokeWidth = actualCell * 0.76f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = activeDragColor.copy(alpha = 0.65f),
                                start = first,
                                end = last,
                                strokeWidth = actualCell * 0.06f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                // Foreground Letter Matrix + Gesture Detector
                Layout(
                    content = {
                        for (r in 0 until puzzle.gridSize) {
                            for (c in 0 until puzzle.gridSize) {
                                val coord = GridCoord(r, c)
                                val ch = puzzle.grid[r][c]
                                val isDragging = coord in activeDragSet || coord == armedStartCell
                                val isHinted = coord in hintedCellSet
                                val isFound = coord in foundCellSet

                                val letterScale by animateFloatAsState(
                                    targetValue = when {
                                        isDragging -> 1.18f
                                        isHinted -> 1.12f
                                        else -> 1.0f
                                    },
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    ),
                                    label = "letter_scale_${r}_${c}"
                                )

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .graphicsLayer {
                                            scaleX = letterScale
                                            scaleY = letterScale
                                        }
                                        .semantics {
                                            contentDescription = "Row ${r + 1} Column ${c + 1} Letter $ch"
                                        }
                                ) {
                                    Text(
                                        text = ch.toString(),
                                        fontFamily = PlusJakartaSansFamily,
                                        fontSize = letterFontSize,
                                        fontWeight = when {
                                            isDragging || isHinted -> FontWeight.Bold
                                            isFound -> FontWeight.SemiBold
                                            else -> FontWeight.Medium
                                        },
                                        color = when {
                                            isDragging -> MaterialTheme.colorScheme.primary
                                            isHinted -> MaterialTheme.colorScheme.tertiary
                                            isFound -> foundTextColor
                                            else -> defaultTextColor
                                        }
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(puzzle.level, puzzle.seed, armedStartCell) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val startCoord = offsetToCoord(down.position) ?: return@awaitEachGesture
                                down.consume()

                                var currentEnd = startCoord
                                var hasMovedToAnotherCell = false
                                activeDragCells = listOf(startCoord)
                                onSelectionStep(1)

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id }
                                        ?: event.changes.firstOrNull()
                                    if (change == null || !change.pressed) {
                                        change?.consume()
                                        break
                                    }
                                    change.consume()

                                    val hovered = offsetToCoord(change.position)
                                    if (hovered != null && hovered != currentEnd) {
                                        if (hovered != startCoord) {
                                            hasMovedToAnotherCell = true
                                        }
                                        val snapped = computeSnappedLine(
                                            start = startCoord,
                                            end = hovered,
                                            gridSize = puzzle.gridSize
                                        )
                                        if (snapped != activeDragCells) {
                                            activeDragCells = snapped
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onSelectionStep(snapped.size)
                                        }
                                        currentEnd = hovered
                                    }
                                }

                                val finalSelection = activeDragCells
                                activeDragCells = emptyList()

                                if (hasMovedToAnotherCell && finalSelection.size >= 2) {
                                    val found = onSubmitSelection(finalSelection)
                                    if (found) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                } else {
                                    val previousArmed = armedStartCell
                                    when {
                                        previousArmed == null -> {
                                            onArmedCellChanged(startCoord)
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                        previousArmed == startCoord -> {
                                            onArmedCellChanged(null)
                                        }
                                        else -> {
                                            val line = computeSnappedLine(
                                                start = previousArmed,
                                                end = startCoord,
                                                gridSize = puzzle.gridSize
                                            )
                                            val found = onSubmitSelection(line)
                                            if (found) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                ) { measurables, constraints ->
                    val side = minOf(constraints.maxWidth, constraints.maxHeight)
                    val cellInt = side / puzzle.gridSize
                    val cellConstraint = constraints.copy(
                        minWidth = cellInt,
                        maxWidth = cellInt,
                        minHeight = cellInt,
                        maxHeight = cellInt
                    )
                    val placeables = measurables.map { it.measure(cellConstraint) }
                    val usedSide = cellInt * puzzle.gridSize
                    val offsetPadding = (side - usedSide) / 2

                    layout(side, side) {
                        placeables.forEachIndexed { index, placeable ->
                            val r = index / puzzle.gridSize
                            val c = index % puzzle.gridSize
                            placeable.placeRelative(
                                x = offsetPadding + c * cellInt,
                                y = offsetPadding + r * cellInt
                            )
                        }
                    }
                }
            }
        }
    }
}
