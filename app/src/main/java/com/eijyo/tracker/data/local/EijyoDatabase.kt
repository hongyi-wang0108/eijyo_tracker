package com.eijyo.tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.eijyo.tracker.data.local.dao.ApplicationDao
import com.eijyo.tracker.data.local.dao.CaseRecordDao
import com.eijyo.tracker.data.local.dao.DocumentDao
import com.eijyo.tracker.data.local.dao.PredictionDao
import com.eijyo.tracker.data.local.dao.RiskDao
import com.eijyo.tracker.data.local.dao.SupplementDao
import com.eijyo.tracker.data.local.dao.UserDao
import com.eijyo.tracker.data.model.ApplicationProfile
import com.eijyo.tracker.data.model.CaseRecord
import com.eijyo.tracker.data.model.DocumentItem
import com.eijyo.tracker.data.model.Prediction
import com.eijyo.tracker.data.model.RiskAssessment
import com.eijyo.tracker.data.model.SupplementRequest
import com.eijyo.tracker.data.model.UserProfile

@Database(
    entities = [
        UserProfile::class,
        ApplicationProfile::class,
        Prediction::class,
        DocumentItem::class,
        SupplementRequest::class,
        CaseRecord::class,
        RiskAssessment::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class EijyoDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun applicationDao(): ApplicationDao
    abstract fun predictionDao(): PredictionDao
    abstract fun documentDao(): DocumentDao
    abstract fun supplementDao(): SupplementDao
    abstract fun caseRecordDao(): CaseRecordDao
    abstract fun riskDao(): RiskDao

    companion object {
        const val NAME = "eijyo.db"
    }
}
