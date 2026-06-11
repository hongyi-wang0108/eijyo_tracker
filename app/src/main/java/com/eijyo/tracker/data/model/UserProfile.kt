package com.eijyo.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The person using the app. A single-user MVP keeps exactly one row
 * (id == [SINGLETON_ID]); the schema stays multi-row ready for later cloud sync.
 */
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: String = SINGLETON_ID,
    val nickname: String = "",
    val language: String = "zh",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
) {
    companion object {
        const val SINGLETON_ID = "local-user"
    }
}
