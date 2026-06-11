package com.eijyo.tracker.data.repository

import com.eijyo.tracker.data.local.dao.ApplicationDao
import com.eijyo.tracker.data.local.dao.UserDao
import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for the user and their application file. The UI only ever
 * talks to repositories, never to DAOs directly.
 */
@Singleton
class ProfileRepository @Inject constructor(
    private val userDao: UserDao,
    private val applicationDao: ApplicationDao,
) {
    fun observeUser(): Flow<UserProfile?> = userDao.observe()

    fun observeApplication(): Flow<ApplicationProfile?> = applicationDao.observe()

    suspend fun getApplication(): ApplicationProfile? = applicationDao.get()

    suspend fun saveUser(user: UserProfile) =
        userDao.upsert(user.copy(updatedAt = System.currentTimeMillis()))

    suspend fun saveApplication(application: ApplicationProfile) =
        applicationDao.upsert(application.copy(updatedAt = System.currentTimeMillis()))
}
