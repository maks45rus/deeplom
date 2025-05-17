package com.example.raspisanieshgpu.DataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Teacher::class, Group::class, CachedSchedule::class], version = 1)
abstract class MainDataBase: RoomDatabase() {

    abstract fun getGroupDao(): GroupDao
    abstract fun getTeacherDao(): TeacherDao
    abstract fun getCachedScheduleDao(): CachedScheduleDao

    companion object {
        fun getDb(context: Context): MainDataBase {
            // Удаляем файл БД перед созданием (для разработки)
            //context.deleteDatabase("RaspisanieDB")

            return Room.databaseBuilder(
                context.applicationContext,
                MainDataBase::class.java,
                "RaspisanieDB"
            )
                .allowMainThreadQueries()
                .build()
        }
    }

}