package com.example.raspisanieshgpu.DataBase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TeacherDao {
    @Insert
    suspend fun insert(teacher: Teacher)

    @Insert
    suspend fun insertAll(teachers: List<Teacher>)

    @Update
    suspend fun update(teacher: Teacher)

    @Query("DELETE FROM Teacher")
    suspend fun deleteAll()

    @Query("SELECT * FROM Teacher")
    suspend fun getAllTeachers(): List<Teacher>

    @Query("SELECT * FROM Teacher WHERE id = :id")
    suspend fun getTeacherById(id: Int): Teacher

    @Query("SELECT * FROM Teacher WHERE name = :name")
    suspend fun getTeacherByName(name: String): Teacher
}

@Dao
interface FacultyDao {
    @Insert
    suspend fun insert(faculty: Faculty)

    @Insert
    suspend fun insertAll(faculties: List<Faculty>)

    @Update
    suspend fun update(faculty: Faculty)

    @Query("DELETE FROM Faculty")
    suspend fun deleteAll()

    @Query("SELECT * FROM Faculty")
    suspend fun getAllFaculties(): List<Faculty>

    @Query("SELECT * FROM Faculty WHERE id = :id")
    suspend fun getFacultyById(id: Int): Faculty

}

@Dao
interface GroupDao {
    @Insert
    suspend fun insert(group: Group)

    @Insert
    suspend fun insertAll(groups: List<Group>)

    @Update
    suspend fun update(group: Group)

    @Query("DELETE FROM `Group`")
    suspend fun deleteAll()

    @Query("SELECT * FROM `Group`")
    suspend fun getAllGroups(): List<Group>

    @Query("SELECT * FROM `Group` WHERE id = :id")
    suspend fun getGroupById(id: Int): Group

    @Query("SELECT * FROM `Group` WHERE name = :name")
    suspend fun getGroupByName(name: String): Group

}