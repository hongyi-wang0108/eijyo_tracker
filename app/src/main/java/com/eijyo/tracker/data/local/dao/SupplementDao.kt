package com.eijyo.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.eijyo.tracker.data.model.CaseRecord
import com.eijyo.tracker.data.model.ImmigrationOffice
import com.eijyo.tracker.data.model.SupplementRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplementDao {
    @Upsert
    suspend fun upsert(request: SupplementRequest)

    @Query("SELECT * FROM supplement_request WHERE applicationId = :appId ORDER BY receivedDate DESC")
    fun observeByApplication(appId: String): Flow<List<SupplementRequest>>

    @Query("DELETE FROM supplement_request WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface CaseRecordDao {
    @Upsert
    suspend fun upsertAll(records: List<CaseRecord>)

    @Query("SELECT * FROM case_record ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<CaseRecord>>

    @Query("SELECT * FROM case_record WHERE office = :office")
    suspend fun byOffice(office: ImmigrationOffice): List<CaseRecord>

    @Query("SELECT COUNT(*) FROM case_record")
    suspend fun count(): Int
}
