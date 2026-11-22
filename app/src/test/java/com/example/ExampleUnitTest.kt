package com.example

import com.example.domain.generator.WordSearchGenerator
import com.example.domain.model.GridCoord
import com.example.ui.util.computeSnappedLine
import com.example.ui.util.formatDuration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WordSearchGeneratorTest {

    @Test
    fun difficultyIncreasesFromLevel1ToInfinity() {
        val lvl1 = WordSearchGenerator.difficultyForLevel(1)
        val lvl5 = WordSearchGenerator.difficultyForLevel(5)
        val lvl15 = WordSearchGenerator.difficultyForLevel(15)
        val lvl50 = WordSearchGenerator.difficultyForLevel(50)
        val lvl100 = WordSearchGenerator.difficultyForLevel(100)
        val lvl250 = WordSearchGenerator.difficultyForLevel(250)

        assertEquals(6, lvl1.gridSize)
        assertTrue(!lvl1.allowDiagonals && !lvl1.allowReverse)

        assertTrue(lvl5.allowDiagonals)
        assertTrue(lvl15.allowReverse)
        assertTrue(lvl50.gridSize > lvl15.gridSize)
        assertTrue(lvl100.gridSize > lvl50.gridSize)
        assertTrue(lvl250.gridSize >= 14)
        assertTrue(lvl250.wordCount > lvl100.wordCount)
        assertTrue(lvl250.decoyWeight > lvl50.decoyWeight)
        assertEquals("Grandmaster", lvl250.tierLabel)
    }

    @Test
    fun generatedPuzzlesHaveValidPlacedWordsAndNoEmptyCells() {
        val testLevels = listOf(1, 2, 4, 8, 15, 25, 45, 75, 150, 999)
        for (level in testLevels) {
            val puzzle = WordSearchGenerator.generate(level = level, seedOffset = 0L)
            assertEquals(puzzle.gridSize, puzzle.grid.size)
            assertTrue("Level $level should have placed words", puzzle.words.isNotEmpty())

            for (row in puzzle.grid) {
                assertEquals(puzzle.gridSize, row.size)
                for (ch in row) {
                    assertTrue("Expected A-Z character in grid, found '$ch'", ch in 'A'..'Z')
                }
            }

            for (placed in puzzle.words) {
                assertEquals(placed.word.length, placed.cells.size)
                val reconstructed = placed.cells.map { coord ->
                    puzzle.grid[coord.row][coord.col]
                }.joinToString("")
                assertEquals(
                    "Reconstructed word on level $level mismatch",
                    placed.word,
                    reconstructed
                )
            }
        }
    }

    @Test
    fun snappedLineSelectsCleanEightWayLines() {
        val horizontal = computeSnappedLine(GridCoord(2, 1), GridCoord(2, 4), 6)
        assertEquals(
            listOf(GridCoord(2, 1), GridCoord(2, 2), GridCoord(2, 3), GridCoord(2, 4)),
            horizontal
        )

        val diagonal = computeSnappedLine(GridCoord(0, 0), GridCoord(3, 3), 6)
        assertEquals(
            listOf(GridCoord(0, 0), GridCoord(1, 1), GridCoord(2, 2), GridCoord(3, 3)),
            diagonal
        )
    }

    @Test
    fun formatDurationFormatsMinutesAndHoursCleanly() {
        assertEquals("00:00", formatDuration(0))
        assertEquals("00:42", formatDuration(42))
        assertEquals("03:05", formatDuration(185))
        assertEquals("1:01:09", formatDuration(3669))
    }
}
