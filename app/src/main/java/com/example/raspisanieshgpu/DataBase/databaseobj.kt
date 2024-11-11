package com.example.raspisanieshgpu.DataBase

import android.content.Context

object databaseobj {

    lateinit var database: MainDataBase
        private set

    fun initialize(context: Context) {
        database = MainDataBase.getDb(context)
    }

}