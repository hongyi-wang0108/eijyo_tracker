package com.eijyo.tracker.data.repository

import com.eijyo.tracker.data.local.dao.SupplementDao
import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.SupplementRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupplementRepository @Inject constructor(
    private val supplementDao: SupplementDao,
) {
    fun observeByApplication(appId: String = ApplicationProfile.SINGLETON_ID): Flow<List<SupplementRequest>> =
        supplementDao.observeByApplication(appId)

    suspend fun save(request: SupplementRequest) = supplementDao.upsert(request)

    suspend fun delete(id: String) = supplementDao.delete(id)
}
