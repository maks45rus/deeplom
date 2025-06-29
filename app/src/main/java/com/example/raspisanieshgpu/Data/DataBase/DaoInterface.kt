package com.example.raspisanieshgpu.Data.DataBase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface TeacherDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(teacher: Teacher)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(teachers: List<Teacher>)

    @Query("SELECT * FROM Teacher WHERE isFavorite = 1")
    suspend fun getFavorites(): List<Teacher>

    @Query("DELETE FROM Teacher")
    suspend fun deleteAll()

    @Query("SELECT * FROM Teacher")
    suspend fun getAllTeachers(): List<Teacher>

    @Query("SELECT * FROM Teacher WHERE name = :name")
    suspend fun getTeacherByName(name: String): Teacher

    @Query("SELECT scheduleData FROM Teacher WHERE name = :name")
    suspend fun getScheduleData(name: String): String

    @Query("SELECT weekStartDate FROM Teacher WHERE name = :name")
    suspend fun getWeekStartDate(name: String): String

    @Query("UPDATE Teacher SET isFavorite = :isFavorite WHERE name = :name")
    suspend fun setFavoriteStatus(name: String, isFavorite: Boolean)

    @Query("UPDATE Teacher SET scheduleData = :scheduleData WHERE name = :name")
    suspend fun setScheduleData(name: String, scheduleData: String)

    @Query("UPDATE Teacher SET weekStartDate = :weekStartDate WHERE name = :name")
    suspend fun setWeekStartDate(name: String, weekStartDate: String)
}

@Dao
interface GroupDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(groups: List<Group>)

    // Получает избранные группы
    @Query("SELECT * FROM `Group` WHERE isFavorite = 1")
    suspend fun getFavorites(): List<Group>

    @Query("DELETE FROM `Group`")
    suspend fun deleteAll()

    @Query("SELECT * FROM `Group`")
    suspend fun getAllGroups(): List<Group>

    @Query("SELECT * FROM `Group` WHERE id = :id")
    suspend fun getGroupById(id: Int): Group

    @Query("SELECT * FROM `Group` WHERE name = :name")
    suspend fun getGroupByName(name: String): Group

    @Query("SELECT * FROM `Group` WHERE name = :name")
    suspend fun searchGroups(name: String): List<Group>

    @Query("SELECT scheduleData FROM `Group` WHERE name = :name")
    suspend fun getScheduleData(name: String): String


    @Query("UPDATE `Group` SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavoriteStatus(id: Int, isFavorite: Boolean)

    @Query("UPDATE `Group` SET scheduleData = :scheduleData WHERE name = :name")
    suspend fun setScheduleData(name: String, scheduleData: String)

    @Query("UPDATE `Group` SET weekStartDate = :weekStartDate WHERE name = :name")
    suspend fun setWeekStartDate(name: String, weekStartDate: String)

}

@Dao
interface SavedOtherDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(savedOther: SavedOther)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(savedOther: List<SavedOther>)

    @Query("DELETE FROM SavedOther")
    suspend fun deleteAll()

    @Query("DELETE FROM SavedOther WHERE name = :name")
    suspend fun deleteSavedOther(name: String)

    @Query("SELECT * FROM SavedOther")
    suspend fun getAllSavedOther(): List<SavedOther>

    @Query("SELECT * FROM SavedOther WHERE name = :name")
    suspend fun getSavedOther(name: String): SavedOther

    @Query("SELECT scheduleData FROM SavedOther WHERE name = :name")
    suspend fun getScheduleData(name: String): String

    @Query("SELECT weekStartDate FROM SavedOther WHERE name = :name")
    suspend fun getWeekStartDate(name: String): String

    @Query("UPDATE SavedOther SET scheduleData = :scheduleData WHERE name = :name")
    suspend fun setScheduleData(name: String, scheduleData: String)

    @Query("UPDATE SavedOther SET weekStartDate = :weekStartDate WHERE name = :name")
    suspend fun setWeekStartDate(name: String, weekStartDate: String)
}




