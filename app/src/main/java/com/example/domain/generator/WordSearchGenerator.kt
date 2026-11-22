package com.example.domain.generator

import com.example.domain.model.DifficultyProfile
import com.example.domain.model.Direction
import com.example.domain.model.GridCoord
import com.example.domain.model.PlacedWord
import com.example.domain.model.WordSearchPuzzle
import kotlin.math.min
import kotlin.random.Random

object WordSearchGenerator {

    private const val COLOR_PALETTE_COUNT = 6

    fun difficultyForLevel(level: Int): DifficultyProfile {
        val safeLevel = level.coerceAtLeast(1)
        return when {
            safeLevel == 1 -> DifficultyProfile(
                level = safeLevel,
                gridSize = 6,
                wordCount = 4,
                minWordLength = 3,
                maxWordLength = 5,
                allowDiagonals = false,
                allowReverse = false,
                reverseWeight = 0f,
                decoyWeight = 0.0f,
                tierLabel = "Starter"
            )
            safeLevel == 2 -> DifficultyProfile(
                level = safeLevel,
                gridSize = 6,
                wordCount = 5,
                minWordLength = 4,
                maxWordLength = 6,
                allowDiagonals = false,
                allowReverse = false,
                reverseWeight = 0f,
                decoyWeight = 0.12f,
                tierLabel = "Starter"
            )
            safeLevel in 3..5 -> DifficultyProfile(
                level = safeLevel,
                gridSize = 7,
                wordCount = 5 + (safeLevel - 3),
                minWordLength = 4,
                maxWordLength = 6,
                allowDiagonals = true,
                allowReverse = false,
                reverseWeight = 0f,
                decoyWeight = 0.22f + (safeLevel - 3) * 0.03f,
                tierLabel = "Easy"
            )
            safeLevel in 6..10 -> DifficultyProfile(
                level = safeLevel,
                gridSize = 8,
                wordCount = 7 + (safeLevel - 6) / 2,
                minWordLength = 4,
                maxWordLength = 7,
                allowDiagonals = true,
                allowReverse = true,
                reverseWeight = 0.25f + (safeLevel - 6) * 0.03f,
                decoyWeight = 0.35f + (safeLevel - 6) * 0.03f,
                tierLabel = "Moderate"
            )
            safeLevel in 11..18 -> DifficultyProfile(
                level = safeLevel,
                gridSize = 9,
                wordCount = 9 + (safeLevel - 11) / 3,
                minWordLength = 5,
                maxWordLength = 8,
                allowDiagonals = true,
                allowReverse = true,
                reverseWeight = 0.40f + (safeLevel - 11) * 0.015f,
                decoyWeight = 0.50f + (safeLevel - 11) * 0.015f,
                tierLabel = "Challenging"
            )
            safeLevel in 19..30 -> DifficultyProfile(
                level = safeLevel,
                gridSize = 10,
                wordCount = 11 + (safeLevel - 19) / 4,
                minWordLength = 5,
                maxWordLength = 9,
                allowDiagonals = true,
                allowReverse = true,
                reverseWeight = 0.52f + (safeLevel - 19) * 0.008f,
                decoyWeight = 0.65f + (safeLevel - 19) * 0.008f,
                tierLabel = "Hard"
            )
            safeLevel in 31..50 -> DifficultyProfile(
                level = safeLevel,
                gridSize = 11,
                wordCount = 14 + (safeLevel - 31) / 7,
                minWordLength = 5,
                maxWordLength = 10,
                allowDiagonals = true,
                allowReverse = true,
                reverseWeight = 0.60f + (safeLevel - 31) * 0.004f,
                decoyWeight = 0.74f + (safeLevel - 31) * 0.004f,
                tierLabel = "Expert"
            )
            safeLevel in 51..85 -> DifficultyProfile(
                level = safeLevel,
                gridSize = 12,
                wordCount = 17 + (safeLevel - 51) / 10,
                minWordLength = 6,
                maxWordLength = 11,
                allowDiagonals = true,
                allowReverse = true,
                reverseWeight = 0.68f,
                decoyWeight = 0.84f,
                tierLabel = "Master"
            )
            safeLevel in 86..140 -> DifficultyProfile(
                level = safeLevel,
                gridSize = 13,
                wordCount = 20 + (safeLevel - 86) / 15,
                minWordLength = 6,
                maxWordLength = 12,
                allowDiagonals = true,
                allowReverse = true,
                reverseWeight = 0.74f,
                decoyWeight = 0.89f,
                tierLabel = if (safeLevel >= 100) "Grandmaster" else "Master"
            )
            else -> {
                val bonusWords = min(6, (safeLevel - 141) / 30)
                DifficultyProfile(
                    level = safeLevel,
                    gridSize = 14,
                    wordCount = 23 + bonusWords,
                    minWordLength = 6,
                    maxWordLength = 12,
                    allowDiagonals = true,
                    allowReverse = true,
                    reverseWeight = 0.78f,
                    decoyWeight = 0.94f,
                    tierLabel = "Grandmaster"
                )
            }
        }
    }

    fun generate(level: Int, seedOffset: Long = 0L): WordSearchPuzzle {
        val profile = difficultyForLevel(level)
        val seed = (level.toLong() * 104729L) xor (seedOffset * 7919L) xor 0x5EED1337L
        val rng = Random(seed)

        val themes = WordDictionary.themes
        val themeIndex = ((level - 1) + (seedOffset % themes.size).toInt()).let {
            ((it % themes.size) + themes.size) % themes.size
        }
        val primaryTheme = themes[themeIndex]

        val fittingPrimary = primaryTheme.words
            .filter { it.length in profile.minWordLength..profile.maxWordLength && it.length <= profile.gridSize }
            .shuffled(rng)

        val fittingSecondary = WordDictionary.allWords
            .filter {
                it.length in profile.minWordLength..profile.maxWordLength &&
                    it.length <= profile.gridSize &&
                    it !in fittingPrimary
            }
            .shuffled(rng)

        val candidatePool = (fittingPrimary + fittingSecondary).distinct()

        // Sort candidate slice longest-first for dense packing
        val selectedBatch = candidatePool.take(profile.wordCount * 3)
            .sortedByDescending { it.length }

        val allowedDirections = buildAllowedDirections(profile)

        val size = profile.gridSize
        val board = Array(size) { CharArray(size) { '\u0000' } }
        val placedWords = mutableListOf<PlacedWord>()

        for (word in selectedBatch) {
            if (placedWords.size >= profile.wordCount) break
            if (placedWords.any { it.word.contains(word) || word.contains(it.word) }) continue

            val placement = findBestPlacement(
                board = board,
                word = word,
                allowedDirections = allowedDirections,
                reverseWeight = profile.reverseWeight,
                diagonalWeight = if (profile.level >= 15) 3 else 2,
                rng = rng,
                colorIndex = placedWords.size % COLOR_PALETTE_COUNT
            )
            if (placement != null) {
                for (i in word.indices) {
                    val c = placement.cells[i]
                    board[c.row][c.col] = word[i]
                }
                placedWords.add(placement)
            }
        }

        // Inject deceptive false-start prefixes at moderate-to-grandmaster levels
        if (profile.decoyWeight >= 0.30f && placedWords.isNotEmpty()) {
            injectFalseStartDecoys(
                board = board,
                placedWords = placedWords,
                allowedDirections = allowedDirections,
                decoyWeight = profile.decoyWeight,
                level = profile.level,
                rng = rng
            )
        }

        // Fill remaining empty cells with weighted decoys
        fillEmptyCells(
            board = board,
            placedWords = placedWords,
            decoyWeight = profile.decoyWeight,
            rng = rng
        )

        val immutableGrid = board.map { row -> row.toList() }
        val displayWords = placedWords.sortedBy { it.word }

        return WordSearchPuzzle(
            level = profile.level,
            seed = seed,
            themeName = primaryTheme.name,
            gridSize = size,
            grid = immutableGrid,
            words = displayWords,
            difficulty = profile
        )
    }

    private fun buildAllowedDirections(profile: DifficultyProfile): List<Direction> {
        return Direction.entries.filter { dir ->
            val diagonalOk = profile.allowDiagonals || !dir.isDiagonal
            val reverseOk = profile.allowReverse || !dir.isReverse
            diagonalOk && reverseOk
        }
    }

    private data class CandidatePlacement(
        val cells: List<GridCoord>,
        val score: Int
    )

    private fun findBestPlacement(
        board: Array<CharArray>,
        word: String,
        allowedDirections: List<Direction>,
        reverseWeight: Float,
        diagonalWeight: Int,
        rng: Random,
        colorIndex: Int
    ): PlacedWord? {
        val size = board.size
        val candidates = mutableListOf<CandidatePlacement>()

        for (dir in allowedDirections) {
            val dirBonus = if (dir.isReverse) {
                if (rng.nextFloat() < reverseWeight) 3 else -2
            } else {
                1
            }

            for (r in 0 until size) {
                for (c in 0 until size) {
                    val endR = r + dir.dRow * (word.length - 1)
                    val endC = c + dir.dCol * (word.length - 1)
                    if (endR !in 0 until size || endC !in 0 until size) continue

                    var valid = true
                    var intersections = 0
                    val coords = ArrayList<GridCoord>(word.length)

                    for (i in word.indices) {
                        val nr = r + dir.dRow * i
                        val nc = c + dir.dCol * i
                        val existing = board[nr][nc]
                        if (existing != '\u0000' && existing != word[i]) {
                            valid = false
                            break
                        }
                        if (existing == word[i]) {
                            intersections++
                        }
                        coords.add(GridCoord(nr, nc))
                    }

                    if (valid) {
                        val diagonalBonus = if (dir.isDiagonal) diagonalWeight else 0
                        val score = intersections * 12 + dirBonus + diagonalBonus + rng.nextInt(4)
                        candidates.add(CandidatePlacement(coords, score))
                    }
                }
            }
        }

        val best = candidates.maxByOrNull { it.score } ?: return null
        return PlacedWord(
            word = word,
            start = best.cells.first(),
            end = best.cells.last(),
            cells = best.cells,
            colorIndex = colorIndex
        )
    }

    /**
     * Plants short 2-3 letter false-start prefixes of target words into empty cells
     * so high-level boards require sharper scanning instead of just checking first letters.
     */
    private fun injectFalseStartDecoys(
        board: Array<CharArray>,
        placedWords: List<PlacedWord>,
        allowedDirections: List<Direction>,
        decoyWeight: Float,
        level: Int,
        rng: Random
    ) {
        val size = board.size
        val targetWordSet = placedWords.map { it.word }.toSet()
        val decoyBudget = ((level / 4) + (decoyWeight * 10).toInt()).coerceIn(2, 24)
        var planted = 0
        var attempts = 0

        while (planted < decoyBudget && attempts < decoyBudget * 8) {
            attempts++
            val sourceWord = placedWords[rng.nextInt(placedWords.size)].word
            if (sourceWord.length < 4) continue
            val prefixLen = if (sourceWord.length >= 6 && rng.nextFloat() < decoyWeight) 3 else 2
            val prefix = sourceWord.substring(0, prefixLen)
            val dir = allowedDirections[rng.nextInt(allowedDirections.size)]
            val r = rng.nextInt(size)
            val c = rng.nextInt(size)
            val endR = r + dir.dRow * (prefixLen - 1)
            val endC = c + dir.dCol * (prefixLen - 1)
            if (endR !in 0 until size || endC !in 0 until size) continue

            var fits = true
            for (i in 0 until prefixLen) {
                val nr = r + dir.dRow * i
                val nc = c + dir.dCol * i
                val cur = board[nr][nc]
                if (cur != '\u0000' && cur != prefix[i]) {
                    fits = false
                    break
                }
            }
            if (!fits) continue

            // Ensure planting this prefix doesn't accidentally spell a full target word
            var createsFullWord = false
            val written = mutableListOf<GridCoord>()
            for (i in 0 until prefixLen) {
                val nr = r + dir.dRow * i
                val nc = c + dir.dCol * i
                if (board[nr][nc] == '\u0000') {
                    if (createsAccidentalWord(board, nr, nc, prefix[i], targetWordSet)) {
                        createsFullWord = true
                        break
                    }
                    board[nr][nc] = prefix[i]
                    written.add(GridCoord(nr, nc))
                }
            }
            if (createsFullWord) {
                for (coord in written) {
                    board[coord.row][coord.col] = '\u0000'
                }
            } else if (written.isNotEmpty()) {
                planted++
            }
        }
    }

    private fun fillEmptyCells(
        board: Array<CharArray>,
        placedWords: List<PlacedWord>,
        decoyWeight: Float,
        rng: Random
    ) {
        val size = board.size
        val placedLetters = placedWords.flatMap { it.word.toList() }.ifEmpty { ('A'..'Z').toList() }
        val alphabet = ('A'..'Z').toList()
        val targetWordSet = placedWords.map { it.word }.toSet()

        for (r in 0 until size) {
            for (c in 0 until size) {
                if (board[r][c] == '\u0000') {
                    var chosen = pickFillerChar(placedLetters, alphabet, decoyWeight, rng)
                    var attempts = 0
                    while (attempts < 6 && createsAccidentalWord(board, r, c, chosen, targetWordSet)) {
                        chosen = alphabet[rng.nextInt(alphabet.size)]
                        attempts++
                    }
                    board[r][c] = chosen
                }
            }
        }
    }

    private fun pickFillerChar(
        placedLetters: List<Char>,
        alphabet: List<Char>,
        decoyWeight: Float,
        rng: Random
    ): Char {
        return if (rng.nextFloat() < decoyWeight) {
            placedLetters[rng.nextInt(placedLetters.size)]
        } else {
            alphabet[rng.nextInt(alphabet.size)]
        }
    }

    private fun createsAccidentalWord(
        board: Array<CharArray>,
        row: Int,
        col: Int,
        candidate: Char,
        targetWords: Set<String>
    ): Boolean {
        val size = board.size
        val maxLen = targetWords.maxOfOrNull { it.length } ?: return false
        board[row][col] = candidate

        for (dir in Direction.entries) {
            val sb = StringBuilder()
            for (step in -(maxLen - 1)..(maxLen - 1)) {
                val nr = row + dir.dRow * step
                val nc = col + dir.dCol * step
                if (nr in 0 until size && nc in 0 until size) {
                    val ch = board[nr][nc]
                    if (ch != '\u0000') sb.append(ch) else sb.append(' ')
                }
            }
            val line = sb.toString()
            if (targetWords.any { line.contains(it) }) {
                board[row][col] = '\u0000'
                return true
            }
        }
        board[row][col] = '\u0000'
        return false
    }
}
