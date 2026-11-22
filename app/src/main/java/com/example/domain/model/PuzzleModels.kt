package com.example.domain.model

data class GridCoord(
    val row: Int,
    val col: Int
)

enum class Direction(val dRow: Int, val dCol: Int, val isDiagonal: Boolean, val isReverse: Boolean) {
    RIGHT(0, 1, isDiagonal = false, isReverse = false),
    DOWN(1, 0, isDiagonal = false, isReverse = false),
    DOWN_RIGHT(1, 1, isDiagonal = true, isReverse = false),
    UP_RIGHT(-1, 1, isDiagonal = true, isReverse = false),
    LEFT(0, -1, isDiagonal = false, isReverse = true),
    UP(-1, 0, isDiagonal = false, isReverse = true),
    UP_LEFT(-1, -1, isDiagonal = true, isReverse = true),
    DOWN_LEFT(1, -1, isDiagonal = true, isReverse = true)
}

data class PlacedWord(
    val word: String,
    val start: GridCoord,
    val end: GridCoord,
    val cells: List<GridCoord>,
    val colorIndex: Int
)

data class DifficultyProfile(
    val level: Int,
    val gridSize: Int,
    val wordCount: Int,
    val minWordLength: Int,
    val maxWordLength: Int,
    val allowDiagonals: Boolean,
    val allowReverse: Boolean,
    val reverseWeight: Float,
    val decoyWeight: Float,
    val tierLabel: String
)

data class WordSearchPuzzle(
    val level: Int,
    val seed: Long,
    val themeName: String,
    val gridSize: Int,
    val grid: List<List<Char>>,
    val words: List<PlacedWord>,
    val difficulty: DifficultyProfile
)

data class HintState(
    val word: String,
    val revealedCells: List<GridCoord>,
    val isFullReveal: Boolean
)
