package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LevelRecordEntity
import com.example.data.local.LexiGridDatabase
import com.example.data.repository.GameRepository
import com.example.data.repository.ThemeMode
import com.example.domain.model.GridCoord
import com.example.domain.model.HintState
import com.example.domain.model.WordSearchPuzzle
import com.example.ui.audio.SoundEffectsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class GameUiState(
    val isLoading: Boolean = true,
    val puzzle: WordSearchPuzzle? = null,
    val foundWords: Set<String> = emptySet(),
    val elapsedSeconds: Long = 0L,
    val isPaused: Boolean = false,
    val isCompleted: Boolean = false,
    val isNewBestTime: Boolean = false,
    val previousBestSeconds: Long? = null,
    val hintsUsed: Int = 0,
    val activeHint: HintState? = null,
    val currentLevel: Int = 1,
    val highestUnlockedLevel: Int = 1,
    val seedOffset: Long = 0L,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val soundEnabled: Boolean = true,
    val armedStartCell: GridCoord? = null,
    val foundWordTick: Int = 0,
    val invalidAttemptTick: Int = 0
)

data class LeaderboardStats(
    val highestLevelReached: Int = 1,
    val totalLevelsCompleted: Int = 0,
    val fastestTimeSeconds: Long? = null,
    val pureSolvesCount: Int = 0,
    val averageTimeSeconds: Long? = null
)

data class RecordsUiState(
    val fastestRecords: List<LevelRecordEntity> = emptyList(),
    val levelRecords: List<LevelRecordEntity> = emptyList(),
    val stats: LeaderboardStats = LeaderboardStats()
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository by lazy {
        val db = LexiGridDatabase.getInstance(application)
        GameRepository(db.lexiGridDao())
    }

    private val soundEffects = SoundEffectsManager()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val recordsUiState: StateFlow<RecordsUiState> = combine(
        repository.recordsByFastestTime,
        repository.recordsByLevel,
        repository.activeSession
    ) { fastest, byLevel, session ->
        val pureCount = fastest.count { it.bestHintsUsed == 0 }
        val avg = if (fastest.isNotEmpty()) {
            fastest.sumOf { it.bestTimeSeconds } / fastest.size
        } else {
            null
        }
        RecordsUiState(
            fastestRecords = fastest,
            levelRecords = byLevel,
            stats = LeaderboardStats(
                highestLevelReached = session?.highestUnlockedLevel ?: 1,
                totalLevelsCompleted = fastest.size,
                fastestTimeSeconds = fastest.firstOrNull()?.bestTimeSeconds,
                pureSolvesCount = pureCount,
                averageTimeSeconds = avg
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecordsUiState()
    )

    private var timerJob: Job? = null
    private var isAppForeground = true
    private var isPlayScreenActive = true

    init {
        loadInitialSession()
    }

    private fun loadInitialSession() {
        viewModelScope.launch {
            val session = repository.loadOrCreateSession()
            val puzzle = repository.generatePuzzle(
                level = session.currentLevel,
                seedOffset = session.seedOffset
            )
            val validWordSet = puzzle.words.map { it.word }.toSet()
            val restoredFound = session.foundWordsCsv
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() && it in validWordSet }
                .toSet()
            val alreadyCompleted = restoredFound.size == puzzle.words.size && puzzle.words.isNotEmpty()

            soundEffects.isSoundEnabled = session.soundEnabled

            _uiState.update {
                it.copy(
                    isLoading = false,
                    puzzle = puzzle,
                    foundWords = restoredFound,
                    elapsedSeconds = session.elapsedSeconds,
                    isPaused = false,
                    isCompleted = alreadyCompleted,
                    hintsUsed = session.hintsUsed,
                    currentLevel = session.currentLevel,
                    highestUnlockedLevel = maxOf(session.highestUnlockedLevel, session.currentLevel),
                    seedOffset = session.seedOffset,
                    themeMode = ThemeMode.fromString(session.themeMode),
                    soundEnabled = session.soundEnabled
                )
            }
            restartTimerLoop()
        }
    }

    private fun restartTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000L)
                val state = _uiState.value
                val shouldTick = !state.isLoading &&
                    state.puzzle != null &&
                    !state.isPaused &&
                    !state.isCompleted &&
                    isAppForeground &&
                    isPlayScreenActive
                if (shouldTick) {
                    val nextElapsed = state.elapsedSeconds + 1L
                    _uiState.update { it.copy(elapsedSeconds = nextElapsed) }
                    if (nextElapsed % 5L == 0L) {
                        persistCurrentSession()
                    }
                }
            }
        }
    }

    fun setAppForeground(inForeground: Boolean) {
        isAppForeground = inForeground
        if (!inForeground) {
            persistCurrentSessionAsync()
        }
    }

    fun setPlayScreenActive(active: Boolean) {
        isPlayScreenActive = active
        if (!active) {
            persistCurrentSessionAsync()
        }
    }

    fun togglePause() {
        val state = _uiState.value
        if (state.isCompleted || state.puzzle == null) return
        val nextPaused = !state.isPaused
        _uiState.update { it.copy(isPaused = nextPaused, armedStartCell = null) }
        if (nextPaused) {
            persistCurrentSessionAsync()
        }
    }

    fun onGridSelectionStep(stepCount: Int) {
        soundEffects.playGridSelectionStep((stepCount - 1).coerceAtLeast(0))
    }

    fun setArmedStartCell(coord: GridCoord?) {
        val state = _uiState.value
        if (state.isPaused || state.isCompleted) return
        if (coord != null) {
            soundEffects.playGridSelectionStep(0)
        }
        _uiState.update { it.copy(armedStartCell = coord) }
    }

    fun submitSelection(cells: List<GridCoord>): Boolean {
        val state = _uiState.value
        val puzzle = state.puzzle ?: return false
        if (state.isPaused || state.isCompleted || cells.size < 2) return false

        val match = puzzle.words.firstOrNull { placed ->
            placed.word !in state.foundWords &&
                (cells == placed.cells || cells == placed.cells.asReversed())
        }

        if (match == null) {
            soundEffects.playInvalidRelease()
            _uiState.update {
                it.copy(
                    armedStartCell = null,
                    invalidAttemptTick = it.invalidAttemptTick + 1
                )
            }
            return false
        }

        val updatedFound = state.foundWords + match.word
        val completedNow = updatedFound.size == puzzle.words.size
        if (completedNow) {
            soundEffects.playLevelComplete()
        } else {
            soundEffects.playWordFound()
        }
        val nextHighest = if (completedNow) {
            maxOf(state.highestUnlockedLevel, puzzle.level + 1)
        } else {
            state.highestUnlockedLevel
        }
        val clearedHint = if (state.activeHint?.word == match.word) null else state.activeHint

        _uiState.update {
            it.copy(
                foundWords = updatedFound,
                activeHint = clearedHint,
                armedStartCell = null,
                isCompleted = completedNow,
                highestUnlockedLevel = nextHighest,
                foundWordTick = it.foundWordTick + 1
            )
        }

        viewModelScope.launch {
            if (completedNow) {
                val result = repository.recordLevelCompletion(
                    puzzle = puzzle,
                    elapsedSeconds = state.elapsedSeconds,
                    hintsUsed = state.hintsUsed
                )
                _uiState.update {
                    it.copy(
                        isNewBestTime = result.isNewBestTime,
                        previousBestSeconds = result.previousBestSeconds
                    )
                }
            }
            persistCurrentSession()
        }

        return true
    }

    fun useHint() {
        val state = _uiState.value
        val puzzle = state.puzzle ?: return
        if (state.isPaused || state.isCompleted) return

        val remaining = puzzle.words.filter { it.word !in state.foundWords }
        if (remaining.isEmpty()) return

        val currentHint = state.activeHint
        val nextHint: HintState = if (
            currentHint != null &&
            currentHint.word !in state.foundWords &&
            !currentHint.isFullReveal
        ) {
            val target = remaining.firstOrNull { it.word == currentHint.word } ?: remaining.first()
            HintState(
                word = target.word,
                revealedCells = target.cells,
                isFullReveal = true
            )
        } else {
            val target = remaining.random()
            HintState(
                word = target.word,
                revealedCells = listOf(target.start),
                isFullReveal = false
            )
        }

        soundEffects.playHint()

        _uiState.update {
            it.copy(
                activeHint = nextHint,
                hintsUsed = it.hintsUsed + 1,
                armedStartCell = null
            )
        }
        persistCurrentSessionAsync()
    }

    fun nextLevel() {
        val current = _uiState.value.puzzle?.level ?: _uiState.value.currentLevel
        loadLevel(level = current + 1, seedOffset = 0L)
    }

    fun replayCurrentLevel() {
        val state = _uiState.value
        val level = state.puzzle?.level ?: state.currentLevel
        loadLevel(level = level, seedOffset = state.seedOffset)
    }

    fun generateFreshBoardForCurrentLevel() {
        val state = _uiState.value
        val level = state.puzzle?.level ?: state.currentLevel
        loadLevel(level = level, seedOffset = state.seedOffset + 1L)
    }

    fun goToLevel(level: Int) {
        val safeLevel = level.coerceAtLeast(1)
        loadLevel(level = safeLevel, seedOffset = 0L)
    }

    private fun loadLevel(level: Int, seedOffset: Long) {
        val puzzle = repository.generatePuzzle(level = level, seedOffset = seedOffset)
        val nextHighest = maxOf(_uiState.value.highestUnlockedLevel, level)
        _uiState.update {
            it.copy(
                isLoading = false,
                puzzle = puzzle,
                foundWords = emptySet(),
                elapsedSeconds = 0L,
                isPaused = false,
                isCompleted = false,
                isNewBestTime = false,
                previousBestSeconds = null,
                hintsUsed = 0,
                activeHint = null,
                currentLevel = level,
                highestUnlockedLevel = nextHighest,
                seedOffset = seedOffset,
                armedStartCell = null
            )
        }
        persistCurrentSessionAsync()
    }

    fun cycleThemeMode() {
        val next = when (_uiState.value.themeMode) {
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
        }
        _uiState.update { it.copy(themeMode = next) }
        persistCurrentSessionAsync()
    }

    fun toggleSound() {
        val next = !_uiState.value.soundEnabled
        soundEffects.isSoundEnabled = next
        if (next) {
            soundEffects.playGridSelectionStep(2)
        }
        _uiState.update { it.copy(soundEnabled = next) }
        persistCurrentSessionAsync()
    }

    fun clearAllRecords() {
        viewModelScope.launch {
            repository.clearAllRecords()
        }
    }

    private fun persistCurrentSessionAsync() {
        viewModelScope.launch {
            persistCurrentSession()
        }
    }

    private suspend fun persistCurrentSession() {
        val state = _uiState.value
        if (state.isLoading) return
        repository.saveActiveSession(
            currentLevel = state.currentLevel,
            highestUnlockedLevel = state.highestUnlockedLevel,
            seedOffset = state.seedOffset,
            foundWords = state.foundWords,
            elapsedSeconds = state.elapsedSeconds,
            hintsUsed = state.hintsUsed,
            themeMode = state.themeMode,
            soundEnabled = state.soundEnabled
        )
    }

    override fun onCleared() {
        timerJob?.cancel()
        soundEffects.release()
        super.onCleared()
    }
}
