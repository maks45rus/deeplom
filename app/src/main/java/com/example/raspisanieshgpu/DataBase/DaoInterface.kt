package com.example.raspisanieshgpu.DataBase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface GroupAndTeacherDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(nanika: GroupAndTeacher)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(nanika: List<GroupAndTeacher>)

    @Update
    suspend fun update(nanika: GroupAndTeacher)

    @Query("DELETE FROM GroupAndTeacher")
    suspend fun deleteAll()

    @Query("SELECT * FROM GroupAndTeacher")
    suspend fun getAllGroupsAndTeachers(): List<GroupAndTeacher>

    @Query("SELECT * FROM GroupAndTeacher WHERE name = :name")
    suspend fun getGroupsAndTeachersByName(name: String):GroupAndTeacher

    @Query("SELECT EXISTS(SELECT 1 FROM GroupAndTeacher WHERE name = :name COLLATE NOCASE)")
    suspend fun isNameExists(name: String): Boolean

    @Query("SELECT * FROM GroupAndTeacher WHERE type = 'TEACHER'")
    suspend fun getAllTeachers(): List<GroupAndTeacher>

    @Query("SELECT * FROM GroupAndTeacher WHERE type = 'TEACHER' AND api_id = :id")
    suspend fun getTeacherById(id: Int): GroupAndTeacher

    @Query("SELECT * FROM GroupAndTeacher WHERE type = 'TEACHER' AND name = :name")
    suspend fun getTeacherByName(name: String): GroupAndTeacher

    @Query("SELECT * FROM GroupAndTeacher WHERE type = 'GROUP'")
    suspend fun getAllGroups(): List<GroupAndTeacher>

    @Query("SELECT * FROM GroupAndTeacher WHERE type = 'GROUP' AND api_id = :id")
    suspend fun getGroupById(id: Int): GroupAndTeacher

    @Query("SELECT * FROM GroupAndTeacher WHERE type = 'GROUP' AND name = :name")
    suspend fun getGroupByName(name: String): GroupAndTeacher
}

