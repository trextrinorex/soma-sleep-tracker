package com.somna.sleeptracker.di

import android.content.Context
import com.somna.sleeptracker.data.local.SomnaDatabase
import com.somna.sleeptracker.data.local.dao.TelemetryDao
import com.somna.sleeptracker.data.repository.SleepRepositoryImpl
import com.somna.sleeptracker.domain.engine.SleepInferenceEngine
import com.somna.sleeptracker.domain.repository.SleepRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SomnaDatabase {
        // In production use a secure key derivation; for now a fixed passphrase for demo.
        // Real apps should derive from Android Keystore / user-specific secret.
        val passphrase = "somna_secure_passphrase_v1".toByteArray(Charsets.UTF_8)
        return SomnaDatabase.create(context, passphrase)
    }

    @Provides
    @Singleton
    fun provideTelemetryDao(db: SomnaDatabase): TelemetryDao = db.telemetryDao()

    @Provides
    @Singleton
    fun provideSleepRepository(
        dao: TelemetryDao,
        engine: SleepInferenceEngine
    ): SleepRepository = SleepRepositoryImpl(dao, engine)
}
