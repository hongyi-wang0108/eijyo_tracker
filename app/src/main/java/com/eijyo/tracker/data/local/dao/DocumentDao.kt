package com.eijyo.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.eijyo.tracker.data.model.DocumentItem
import com.eijyo.tracker.data.model.DocumentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DocumentItem>)

    @Upsert
    suspend fun upsert(item: DocumentItem)

    @Query("SELECT * FROM document_item WHERE applicationId = :appId ORDER BY sortOrder ASC")
    fun observeByApplication(appId: String): Flow<List<DocumentItem>>

    @Query("UPDATE document_item SET status = :status WHERE id = :itemId")
    suspend fun updateStatus(itemId: String, status: DocumentStatus)

    @Query("DELETE FROM document_item WHERE applicationId = :appId")
    suspend fun deleteByApplication(appId: String)

    @Query("SELECT COUNT(*) FROM document_item WHERE applicationId = :appId")
    suspend fun countAll(appId: String): Int
}
