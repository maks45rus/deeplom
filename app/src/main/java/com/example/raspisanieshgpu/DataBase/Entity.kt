package com.example.raspisanieshgpu.DataBase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.raspisanieshgpu.api.models.Date
import com.example.raspisanieshgpu.api.models.Para

@Entity(tableName = "Teacher")
data class Teacher (
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "name")
    var name: String,
)

@Entity(tableName = "Faculty")
data class Faculty (
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "name")
    var name: String,

)

@Entity(tableName = "Group")
data class Group (
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "name")
    var name: String,
)

