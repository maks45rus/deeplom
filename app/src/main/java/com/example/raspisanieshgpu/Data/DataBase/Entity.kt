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
    val weekStartDate: String = "", // Дата начала недели в формате yyyy-MM-dd
    @ColumnInfo(name = "scheduleData", defaultValue = "")
    val scheduleData: String = ""// JSON с данными расписания
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

@Entity(tableName = "CachedSchedule")
data class CachedSchedule(
    @PrimaryKey
    val entityId: Int, // ID группы или преподавателя
    val entityType: String, // "group" или "teacher"
    val weekStartDate: String, // Дата начала недели в формате yyyy-MM-dd
    val scheduleData: String // JSON с данными расписания
)