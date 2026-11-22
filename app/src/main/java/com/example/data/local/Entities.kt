package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_records")
data class LevelRecordEntity(
    @PrimaryKey val level: Int,
    val bestTimeSeconds: Long,
    val lastTimeSeconds: Long,
    val bestHintsUsed: Int,
    val timesCompleted: Int,
    val gridSize: Int,
    val wordCount: Int,
    val themeName: String,
    val completedAtMillis: Long
)

@Entity(tableName = "active_session")
data class ActiveSessionEntity(
    @PrimaryKey val id: Int = 1,
    val currentLevel: Int = 1,
    val highestUnlockedLevel: Int = 1,
    val seedOffset: Long = 0L,
    val foundWordsCsv: String = "",
    val elapsedSeconds: Long = 0L,
    val hintsUsed: Int = 0,
    val themeMode: String = "SYSTEM",
    val soundEnabled: Boolean = true,
    val updatedAtMillis: Long = System.currentTimeMillis()
)
