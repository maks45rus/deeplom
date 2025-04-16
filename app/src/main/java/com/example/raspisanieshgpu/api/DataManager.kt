package com.example.raspisanieshgpu.api

import android.util.Log
import com.example.raspisanieshgpu.DataBase.Group
import com.example.raspisanieshgpu.DataBase.Teacher
import com.example.raspisanieshgpu.DataBase.databaseobj
import com.example.raspisanieshgpu.api.RetrofitClient.apiService
import com.example.raspisanieshgpu.api.models.PairsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DataManager {

    private var db = databaseobj.database
    suspend fun fetchAndSaveTeachers() {
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTeachers()
                if(!response.ok)
                    throw Exception(response.error)
                val teachers = response.result.map { Teacher(id = it.id, name = it.name) }
                db.getTeacherDao().deleteAll()
                db.getTeacherDao().insertAll(teachers)
                Log.d("DataManager", "fetch teachers succsess")
            }catch (e: Exception){
                Log.e("DataManager", "Error fetching data teachers: ${e.message}", e)
            }

        }
    }

    suspend fun fetchAndSaveGroups() {
        withContext(Dispatchers.IO) {
            try {

                val response = apiService.getGroups()
                if(!response.ok)
                    throw Exception(response.error)

                val groups = response.result.flatMap {fac -> fac.groups.map { group ->
                        Group(id = group.id, name = group.name)
                    }
                }
                db.getGroupDao().deleteAll()
                db.getGroupDao().insertAll(groups)
                Log.d("DataManager", "fetch groups succsess")
            } catch (e: Exception) {
                Log.e("DataManager", "Error fetching data groups: ${e.message}", e)
            }
        }
    }

    suspend fun refreshGroups() {
        withContext(Dispatchers.IO) {
            try {
                // 1. Загружаем группы с сервера
                val response = apiService.getGroups()
                if (!response.ok) throw Exception(response.error)

                val serverGroups = response.result.flatMap { faculty ->
                    faculty.groups.map { Group(id = it.id, name = it.name, isFavorite = false) }
                }

                // 2. Получаем текущие избранные группы из БД
                val favoriteGroups = db.getGroupDao().getFavorites()

                // 3. Удаляем все группы, кроме избранных
                db.getGroupDao().deleteNonFavorites()

                // 4. Для избранных групп обновляем ID по имени
                favoriteGroups.forEach { favorite ->
                    val serverGroup = serverGroups.find { it.name == favorite.name }
                    if (serverGroup != null) {
                        db.getGroupDao().updateIdForFavorite(favorite.name, serverGroup.id)
                    }
                }

                // 5. Добавляем все группы с сервера (IGNORE конфликты для избранных)
                db.getGroupDao().insertAll(serverGroups)

                Log.d("DataManager", "Groups refreshed (favorites preserved)")
            } catch (e: Exception) {
                Log.e("DataManager", "Error refreshing groups: ${e.message}", e)
            }
        }
    }

    suspend fun refreshTeachers() {
        withContext(Dispatchers.IO) {
            try {
                // 1. Загружаем преподавателей с сервера
                val response = apiService.getTeachers()
                if (!response.ok) throw Exception(response.error)

                val serverTeachers = response.result.map {
                    Teacher(id = it.id, name = it.name, isFavorite = false)
                }

                // 2. Получаем текущих избранных преподавателей из БД
                val favoriteTeachers = db.getTeacherDao().getFavorites()

                // 3. Удаляем всех преподавателей, кроме избранных
                db.getTeacherDao().deleteNonFavorites()

                // 4. Для избранных преподавателей обновляем ID по имени
                favoriteTeachers.forEach { favorite ->
                    val serverTeacher = serverTeachers.find { it.name == favorite.name }
                    if (serverTeacher != null) {
                        db.getTeacherDao().updateIdForFavorite(favorite.name, serverTeacher.id!!)
                    }
                }

                // 5. Добавляем всех преподавателей с сервера
                db.getTeacherDao().insertAll(serverTeachers)

                Log.d("DataManager", "Teachers refreshed (favorites preserved)")
            } catch (e: Exception) {
                Log.e("DataManager", "Error refreshing teachers: ${e.message}", e)
            }
        }
    }

    suspend fun toggleTeacherFavorite(teacherId: Int, isFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            try {
                db.getTeacherDao().setFavoriteStatus(teacherId, isFavorite)
                Log.d("DataManager", "Teacher $teacherId favorite status set to $isFavorite")
            } catch (e: Exception) {
                Log.e("DataManager", "Error setting teacher favorite status: ${e.message}", e)
            }
        }
    }

    suspend fun toggleGroupFavorite(teacherId: Int, isFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            try {
                db.getGroupDao().setFavoriteStatus(teacherId, isFavorite)
                Log.d("DataManager", "Group $teacherId favorite status set to $isFavorite")
            } catch (e: Exception) {
                Log.e("DataManager", "Error setting Group favorite status: ${e.message}", e)
            }
        }
    }

    suspend fun fetchPairs(date: String, week: Int, id: Int, pairsfor: String): PairsResponse {
        lateinit var response: PairsResponse
        withContext(Dispatchers.IO) {
            try {
                if(pairsfor == "group") response = apiService.getPairsGroup(date,week,id)
                else                    response = apiService.getPairsTeacher(date,week,id)
                Log.d("DataManager", "fetch pairs succsess")
            } catch (e: Exception) {
                Log.e("DataManager", "Error fetching data groups $date,$week,$id,$pairsfor: ${e.message}", e)
            }
        }
        return response
    }

}