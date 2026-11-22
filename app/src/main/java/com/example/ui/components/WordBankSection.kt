package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.HintState
import com.example.domain.model.PlacedWord
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.WordHighlightColorsDark
import com.example.ui.theme.WordHighlightColorsLight

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordBankSection(
    words: List<PlacedWord>,
    foundWords: Set<String>,
    activeHint: HintState?,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier
) {
    val palette = if (isDarkTheme) WordHighlightColorsDark else WordHighlightColorsLight

    FlowRow(
        modifier = modifier.testTag("word_bank_flow"),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        words.forEach { placed ->
            val isFound = placed.word in foundWords
            val isHinted = !isFound && activeHint?.word == placed.word
            val accentColor = palette[placed.colorIndex % palette.size]

            var wasInitiallyFound by remember(placed.word, words) { mutableStateOf(isFound) }
            val chipScale = remember(placed.word, words) { Animatable(1f) }

            LaunchedEffect(isFound) {
                if (isFound && !wasInitiallyFound) {
                    chipScale.animateTo(
                        targetValue = 1.09f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessHigh
                        )
                    )
                    chipScale.animateTo(
                        targetValue = 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                }
                wasInitiallyFound = isFound
            }

            val containerColor by animateColorAsState(
                targetValue = when {
                    isFound -> accentColor.copy(alpha = if (isDarkTheme) 0.16f else 0.12f)
                    isHinted -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.surface
                },
                animationSpec = tween(220),
                label = "chip_bg"
            )

            val borderColor by animateColorAsState(
                targetValue = when {
                    isFound -> accentColor.copy(alpha = 0.35f)
                    isHinted -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.outline
                },
                animationSpec = tween(220),
                label = "chip_border"
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = containerColor,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = chipScale.value
                        scaleY = chipScale.value
                    }
                    .testTag("word_chip_${placed.word}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedVisibility(
                        visible = isFound,
                        enter = fadeIn(tween(180)) + expandHorizontally(),
                        exit = fadeOut(tween(120)) + shrinkHorizontally()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Found ${placed.word}",
                                tint = accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }

                    AnimatedVisibility(
                        visible = isHinted,
                        enter = fadeIn(tween(180)) + expandHorizontally(),
                        exit = fadeOut(tween(120)) + shrinkHorizontally()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Lightbulb,
                                contentDescription = "Hinted word ${placed.word}",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }

                    Text(
                        text = placed.word,
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 13.sp,
                        fontWeight = if (isHinted) FontWeight.Bold else FontWeight.SemiBold,
                        letterSpacing = 0.8.sp,
                        textDecoration = if (isFound) TextDecoration.LineThrough else TextDecoration.None,
                        color = when {
                            isFound -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                            isHinted -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}
