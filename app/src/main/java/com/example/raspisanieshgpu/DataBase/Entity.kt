package com.example.raspisanieshgpu.DataBase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

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

@Entity(tableName = "Group",
    foreignKeys = [
        ForeignKey(entity = Faculty::class,
            parentColumns = ["id"],
            childColumns = ["facultyId"],
            onDelete = ForeignKey.CASCADE)
    ])
data class Group (
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "facultyId")
    var facultyId: Int,
    @ColumnInfo(name = "inFavorites")
    var inFavorites: Boolean = false
)

@Entity(tableName = "Pairs",
    foreignKeys = [
        ForeignKey(entity = Group::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE)
    ])
data class Pairs (
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "groupId")
    var groupId: Int,
    @ColumnInfo(name = "curWeekPairs")
    var curWeekPairs: String,
    @ColumnInfo(name = "nextWeekPairs")
    var nextWeekPairs: String
)
