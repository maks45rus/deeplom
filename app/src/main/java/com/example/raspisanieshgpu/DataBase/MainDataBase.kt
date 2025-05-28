package com.example.raspisanieshgpu.DataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(
    entities = [Teacher::class, Group::class, CachedSchedule::class],
    version = 1,
    exportSchema = false // Отключаем экспорт схемы, если не используете миграции
)
abstract class MainDataBase : RoomDatabase() {

    abstract fun getGroupDao(): GroupDao
    abstract fun getTeacherDao(): TeacherDao
    abstract fun getCachedScheduleDao(): CachedScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: MainDataBase? = null

        fun getInstance(context: Context): MainDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context)
                INSTANCE = instance
                instance
            }
        }

        private fun buildDatabase(context: Context): MainDataBase {
            return Room.databaseBuilder(
                context.applicationContext,
                MainDataBase::class.java,
                "RaspisanieDB"
            )
                // Убираем allowMainThreadQueries - это антипаттерн
                .fallbackToDestructiveMigration() // Разрешаем разрушительную миграцию
                .build()
        }
    }
}