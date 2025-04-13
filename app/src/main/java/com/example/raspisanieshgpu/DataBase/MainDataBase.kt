package com.example.raspisanieshgpu.DataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [GroupAndTeacher::class, Schedule::class], version = 1)
abstract class MainDataBase: RoomDatabase() {

    abstract fun getGroupAndTeacherDao(): GroupAndTeacherDao

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