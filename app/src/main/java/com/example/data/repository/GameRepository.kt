package com.example.data.repository

import com.example.data.local.ActiveSessionEntity
import com.example.data.local.LevelRecordEntity
import com.example.data.local.LexiGridDao
import com.example.domain.generator.WordSearchGenerator
import com.example.domain.model.WordSearchPuzzle
import kotlinx.coroutines.flow.Flow

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromString(value: String): ThemeMode =
            entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}

data class CompletionResult(
    val isNewBestTime: Boolean,
    val previousBestSeconds: Long?
)

class GameRepository(private val dao: LexiGridDao) {

    val recordsByFastestTime: Flow<List<LevelRecordEntity>> = dao.observeRecordsByFastestTime()

    val recordsByLevel: Flow<List<LevelRecordEntity>> = dao.observeRecordsByLevel()

    val activeSession: Flow<ActiveSessionEntity?> = dao.observeActiveSession()

    fun generatePuzzle(level: Int, seedOffset: Long = 0L): WordSearchPuzzle {
        return WordSearchGenerator.generate(level = level, seedOffset = seedOffset)
    }

    suspend fun loadOrCreateSession(): ActiveSessionEntity {
        val existing = dao.getActiveSession()
        if (existing != null) return existing
        val initial = ActiveSessionEntity(
            id = 1,
            currentLevel = 1,
            highestUnlockedLevel = 1,
            seedOffset = 0L,
            foundWordsCsv = "",
            elapsedSeconds = 0L,
            hintsUsed = 0,
            themeMode = ThemeMode.SYSTEM.name,
            soundEnabled = true
        )
        dao.upsertActiveSession(initial)
        return initial
    }

    suspend fun saveActiveSession(
        currentLevel: Int,
        highestUnlockedLevel: Int,
        seedOffset: Long,
        foundWords: Set<String>,
        elapsedSeconds: Long,
        hintsUsed: Int,
        themeMode: ThemeMode,
        soundEnabled: Boolean
    ) {
        val entity = ActiveSessionEntity(
            id = 1,
            currentLevel = currentLevel,
            highestUnlockedLevel = highestUnlockedLevel,
            seedOffset = seedOffset,
            foundWordsCsv = foundWords.joinToString(","),
            elapsedSeconds = elapsedSeconds,
            hintsUsed = hintsUsed,
            themeMode = themeMode.name,
            soundEnabled = soundEnabled,
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.upsertActiveSession(entity)
    }

    suspend fun recordLevelCompletion(
        puzzle: WordSearchPuzzle,
        elapsedSeconds: Long,
        hintsUsed: Int
    ): CompletionResult {
        val existing = dao.getRecordForLevel(puzzle.level)
        val now = System.currentTimeMillis()

        val isNewBest: Boolean
        val newBestTime: Long
        val newBestHints: Int

        if (existing == null) {
            isNewBest = true
            newBestTime = elapsedSeconds
            newBestHints = hintsUsed
        } else {
            val beatTime = elapsedSeconds < existing.bestTimeSeconds
            val tiedTimeWithFewerHints =
                elapsedSeconds == existing.bestTimeSeconds && hintsUsed < existing.bestHintsUsed
            isNewBest = beatTime || tiedTimeWithFewerHints
            newBestTime = if (isNewBest) elapsedSeconds else existing.bestTimeSeconds
            newBestHints = if (isNewBest) hintsUsed else existing.bestHintsUsed
        }

        val updated = LevelRecordEntity(
            level = puzzle.level,
            bestTimeSeconds = newBestTime,
            lastTimeSeconds = elapsedSeconds,
            bestHintsUsed = newBestHints,
            timesCompleted = (existing?.timesCompleted ?: 0) + 1,
            gridSize = puzzle.gridSize,
            wordCount = puzzle.words.size,
            themeName = puzzle.themeName,
            completedAtMillis = now
        )
        dao.upsertRecord(updated)

        return CompletionResult(
            isNewBestTime = isNewBest,
            previousBestSeconds = existing?.bestTimeSeconds
        )
    }

    suspend fun clearAllRecords() {
        dao.clearAllRecords()
    }
}
