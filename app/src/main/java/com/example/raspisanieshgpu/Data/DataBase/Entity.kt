package com.example.raspisanieshgpu.Data.DataBase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Teacher")
data class Teacher (
    @PrimaryKey(autoGenerate = false)
    var id: Int,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "isFavorite", defaultValue = "0")  // 0 = false, 1 = true
    var isFavorite: Boolean = false,
    @ColumnInfo(name = "weekStartDate", defaultValue = "")
    val weekStartDate: String = "",
    @ColumnInfo(name = "scheduleData", defaultValue = "")
    val scheduleData: String = ""
)


@Entity(tableName = "Group")
data class Group (
    @PrimaryKey(autoGenerate = false)
    var id: Int,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "isFavorite", defaultValue = "0")  // 0 = false, 1 = true
    var isFavorite: Boolean = false,
    @ColumnInfo(name = "weekStartDate", defaultValue = "")
    val weekStartDate: String = "", // Дата начала недели в формате yyyy-MM-dd
    @ColumnInfo(name = "scheduleData", defaultValue = "")
    val scheduleData: String = ""// JSON с данными расписания

)
