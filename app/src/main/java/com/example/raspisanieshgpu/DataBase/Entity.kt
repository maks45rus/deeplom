package com.example.raspisanieshgpu.DataBase

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
    var isFavorite: Boolean = false
)

@Entity(tableName = "Group")
data class Group (
    @PrimaryKey(autoGenerate = false)
    var id: Int,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "isFavorite", defaultValue = "0")  // 0 = false, 1 = true
    var isFavorite: Boolean = false

)

@Entity(tableName = "CachedSchedule")
data class CachedSchedule(
    @PrimaryKey
    val entityId: Int, // ID группы или преподавателя
    val entityType: String, // "group" или "teacher"
    val weekStartDate: String, // Дата начала недели в формате yyyy-MM-dd
    val scheduleData: String // JSON с данными расписания
)