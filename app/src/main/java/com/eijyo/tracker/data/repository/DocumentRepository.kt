package com.eijyo.tracker.data.repository

import com.eijyo.tracker.data.local.dao.DocumentDao
import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.DocumentItem
import com.eijyo.tracker.data.model.DocumentStatus
import com.eijyo.tracker.domain.documents.DocumentTemplateGenerator
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepository @Inject constructor(
    private val documentDao: DocumentDao,
    private val generator: DocumentTemplateGenerator,
) {
    fun observe(appId: String = ApplicationProfile.SINGLETON_ID): Flow<List<DocumentItem>> =
        documentDao.observeByApplication(appId)

    suspend fun updateStatus(itemId: String, status: DocumentStatus) =
        documentDao.updateStatus(itemId, status)

    /** Rebuilds the checklist from the profile, replacing any previous generated set. */
    suspend fun regenerate(profile: ApplicationProfile) {
        documentDao.deleteByApplication(profile.id)
        documentDao.insertAll(generator.generate(profile))
    }
}
