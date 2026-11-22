package com.example.ui.util

import com.example.domain.model.GridCoord
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.roundToInt

fun formatDuration(totalSeconds: Long): String {
    val safe = totalSeconds.coerceAtLeast(0L)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60
    val seconds = safe % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

/**
 * Snaps an arbitrary drag vector from [start] to [end] onto the nearest
 * valid 8-directional line (horizontal, vertical, or 45-degree diagonal)
 * clamped inside a [gridSize] x [gridSize] board.
 */
fun computeSnappedLine(
    start: GridCoord,
    end: GridCoord,
    gridSize: Int
): List<GridCoord> {
    val dr = end.row - start.row
    val dc = end.col - start.col
    if (dr == 0 && dc == 0) return listOf(start)

    val angle = atan2(dr.toDouble(), dc.toDouble())
    val octant = ((angle / (PI / 4.0)).roundToInt() + 8) % 8

    val (stepR, stepC) = when (octant) {
        0 -> 0 to 1
        1 -> 1 to 1
        2 -> 1 to 0
        3 -> 1 to -1
        4 -> 0 to -1
        5 -> -1 to -1
        6 -> -1 to 0
        else -> -1 to 1
    }

    val rawSteps = max(abs(dr), abs(dc))
    val result = ArrayList<GridCoord>(rawSteps + 1)
    for (i in 0..rawSteps) {
        val nr = start.row + stepR * i
        val nc = start.col + stepC * i
        if (nr !in 0 until gridSize || nc !in 0 until gridSize) break
        result.add(GridCoord(nr, nc))
    }
    return result
}
