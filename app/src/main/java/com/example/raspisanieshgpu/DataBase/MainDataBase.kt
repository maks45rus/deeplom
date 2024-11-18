package com.example.raspisanieshgpu.DataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Faculty::class, Group::class, Teacher::class, Pairs::class], version = 1)
abstract class MainDataBase: RoomDatabase() {

    abstract fun getTeacherDao(): TeacherDao
    abstract fun getFacultyDao(): FacultyDao
    abstract fun getGroupDao(): GroupDao

    companion object{
        fun getDb(context: Context): MainDataBase{
            return Room.databaseBuilder(
                context.applicationContext,
                MainDataBase::class.java,
                "RaspisanieDB"
            ).build()
        }
    }

}