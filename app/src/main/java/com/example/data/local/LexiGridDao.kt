package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LexiGridDao {

    @Query("SELECT * FROM level_records ORDER BY bestTimeSeconds ASC, bestHintsUsed ASC, level ASC")
    fun observeRecordsByFastestTime(): Flow<List<LevelRecordEntity>>

    @Query("SELECT * FROM level_records ORDER BY level DESC")
    fun observeRecordsByLevel(): Flow<List<LevelRecordEntity>>

    @Query("SELECT * FROM level_records WHERE level = :level LIMIT 1")
    suspend fun getRecordForLevel(level: Int): LevelRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecord(record: LevelRecordEntity)

    @Query("DELETE FROM level_records")
    suspend fun clearAllRecords()

    @Query("SELECT * FROM active_session WHERE id = 1 LIMIT 1")
    fun observeActiveSession(): Flow<ActiveSessionEntity?>

    @Query("SELECT * FROM active_session WHERE id = 1 LIMIT 1")
    suspend fun getActiveSession(): ActiveSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertActiveSession(session: ActiveSessionEntity)
}
