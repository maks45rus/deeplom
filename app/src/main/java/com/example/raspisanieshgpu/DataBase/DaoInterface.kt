package com.example.raspisanieshgpu.DataBase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update


@Dao
interface TeacherDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(teacher: Teacher)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(teachers: List<Teacher>)

    @Query("DELETE FROM Teacher WHERE isFavorite = 0")
    suspend fun deleteNonFavorites()

    @Query("UPDATE Teacher SET id = :newId WHERE name = :name AND isFavorite = 1")
    suspend fun updateIdForFavorite(name: String, newId: Int)

    @Query("SELECT * FROM Teacher WHERE isFavorite = 1")
    suspend fun getFavorites(): List<Teacher>

    @Query("DELETE FROM Teacher")
    suspend fun deleteAll()

    @Query("SELECT * FROM Teacher")
    suspend fun getAllTeachers(): List<Teacher>

    @Query("SELECT * FROM Teacher WHERE id = :id")
    suspend fun getTeacherById(id: Int): Teacher

    @Query("SELECT * FROM Teacher WHERE name = :name")
    suspend fun getTeacherByName(name: String): Teacher

    @Query("UPDATE Teacher SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavoriteStatus(id: Int, isFavorite: Boolean)
}

@Dao
interface GroupDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(group: Group)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(groups: List<Group>)

    @Query("DELETE FROM `Group` WHERE isFavorite = 0")
    suspend fun deleteNonFavorites()

    // Обновляет ID группы по имени (для избранных)
    @Query("UPDATE `Group` SET id = :newId WHERE name = :name AND isFavorite = 1")
    suspend fun updateIdForFavorite(name: String, newId: Int)

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

    @Query("UPDATE `Group` SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavoriteStatus(id: Int, isFavorite: Boolean)
}

@Dao
interface CachedScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(schedule: CachedSchedule)

    @Query("SELECT * FROM CachedSchedule WHERE entityId = :entityId AND entityType = :entityType AND weekStartDate = :weekStartDate")
    suspend fun getSchedule(entityId: Int, entityType: String, weekStartDate: String): CachedSchedule?

    @Query("DELETE FROM CachedSchedule WHERE entityId = :entityId AND entityType = :entityType")
    suspend fun deleteForEntity(entityId: Int, entityType: String)
}


