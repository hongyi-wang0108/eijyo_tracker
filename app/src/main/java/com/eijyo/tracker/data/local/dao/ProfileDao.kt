package com.eijyo.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Upsert
    suspend fun upsert(user: UserProfile)

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    fun observe(id: String = UserProfile.SINGLETON_ID): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    suspend fun get(id: String = UserProfile.SINGLETON_ID): UserProfile?
}

@Dao
interface ApplicationDao {
    @Upsert
    suspend fun upsert(application: ApplicationProfile)

    @Query("SELECT * FROM application_profile WHERE id = :id LIMIT 1")
    fun observe(id: String = ApplicationProfile.SINGLETON_ID): Flow<ApplicationProfile?>

    @Query("SELECT * FROM application_profile WHERE id = :id LIMIT 1")
    suspend fun get(id: String = ApplicationProfile.SINGLETON_ID): ApplicationProfile?
}
