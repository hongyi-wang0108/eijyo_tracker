package com.eijyo.tracker.di

import android.content.Context
import androidx.room.Room
import com.eijyo.tracker.data.local.EijyoDatabase
import com.eijyo.tracker.data.local.dao.ApplicationDao
import com.eijyo.tracker.data.local.dao.CaseRecordDao
import com.eijyo.tracker.data.local.dao.DocumentDao
import com.eijyo.tracker.data.local.dao.PredictionDao
import com.eijyo.tracker.data.local.dao.RiskDao
import com.eijyo.tracker.data.local.dao.SupplementDao
import com.eijyo.tracker.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides the Room database and its DAOs to the Hilt graph. Repositories and engines
 * use constructor injection (@Inject) and need no explicit @Provides here.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EijyoDatabase =
        Room.databaseBuilder(context, EijyoDatabase::class.java, EijyoDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideUserDao(db: EijyoDatabase): UserDao = db.userDao()
    @Provides fun provideApplicationDao(db: EijyoDatabase): ApplicationDao = db.applicationDao()
    @Provides fun providePredictionDao(db: EijyoDatabase): PredictionDao = db.predictionDao()
    @Provides fun provideDocumentDao(db: EijyoDatabase): DocumentDao = db.documentDao()
    @Provides fun provideSupplementDao(db: EijyoDatabase): SupplementDao = db.supplementDao()
    @Provides fun provideCaseRecordDao(db: EijyoDatabase): CaseRecordDao = db.caseRecordDao()
    @Provides fun provideRiskDao(db: EijyoDatabase): RiskDao = db.riskDao()
}
