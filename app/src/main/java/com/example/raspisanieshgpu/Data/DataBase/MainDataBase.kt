package com.example.raspisanieshgpu.Data.DataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(
    entities = [Teacher::class, Group::class],
    version = 1,
    exportSchema = false
)
abstract class MainDataBase : RoomDatabase() {

    abstract fun getGroupDao(): GroupDao
    abstract fun getTeacherDao(): TeacherDao

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
            )   .fallbackToDestructiveMigration()
                .build()
        }
    }
}