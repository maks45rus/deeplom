package com.example.raspisanieshgpu.DataBase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "GroupAndTeacher")
data class GroupAndTeacher (
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "api_id")
    var api_id: Int? = null,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "type")
    var type: String,
)

@Entity(tableName = "Favorite")
data class Schedule (
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "last_update")
    var last_update: String,
    @ColumnInfo(name = "curWeekPairs")
    var curWeekPairs: String,
)
