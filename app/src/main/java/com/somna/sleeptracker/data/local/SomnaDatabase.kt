package com.somna.sleeptracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.somna.sleeptracker.data.local.dao.TelemetryDao
import com.somna.sleeptracker.data.local.entity.CircadianProfileEntity
import com.somna.sleeptracker.data.local.entity.EventLogEntity
import com.somna.sleeptracker.data.local.entity.SleepSessionEntity
import com.somna.sleeptracker.data.local.entity.SystemEventType
import net.sqlcipher.database.SupportFactory
import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromSystemEventType(value: SystemEventType): String = value.name

    @TypeConverter
    fun toSystemEventType(value: String): SystemEventType = SystemEventType.valueOf(value)
}

@Database(
    entities = [
        EventLogEntity::class,
        SleepSessionEntity::class,
        CircadianProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SomnaDatabase : RoomDatabase() {
    abstract fun telemetryDao(): TelemetryDao

    companion object {
        private const val DB_NAME = "somna_sleep.db"

        fun create(context: Context, passphrase: ByteArray): SomnaDatabase {
            val factory = SupportFactory(passphrase)
            return Room.databaseBuilder(
                context.applicationContext,
                SomnaDatabase::class.java,
                DB_NAME
            )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
