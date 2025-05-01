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
                // 1. Сохраняем имена всех текущих фаворитов
                val favoriteGroupNames = db.getGroupDao().getFavorites().map { it.name }

                // 2. Загружаем свежие данные с сервера
                val response = apiService.getGroups()
                if (!response.ok) throw Exception(response.error)

                val serverGroups = response.result.flatMap { faculty ->
                    faculty.groups.map { Group(id = it.id, name = it.name, isFavorite = false) }
                }

                // 3. Полностью очищаем базу
                db.getGroupDao().deleteAll()

                // 4. Добавляем все новые группы
                db.getGroupDao().insertAll(serverGroups)

                // 5. Восстанавливаем статус фаворитов
                favoriteGroupNames.forEach { name ->
                    val group = db.getGroupDao().getGroupByName(name)
                    if (group != null) {
                        db.getGroupDao().setFavoriteStatus(group.id, true)
                    }
                }

                Log.d("DataManager", "Groups refreshed with favorites restored")
            } catch (e: Exception) {
                Log.e("DataManager", "Error refreshing groups: ${e.message}", e)
                throw e // Пробрасываем исключение для обработки выше
            }
        }
    }

    suspend fun refreshTeachers() {
        withContext(Dispatchers.IO) {
            try {
                // 1. Сохраняем имена всех текущих фаворитов
                val favoriteTeacherNames = db.getTeacherDao().getFavorites().map { it.name }

                // 2. Загружаем свежие данные с сервера
                val response = apiService.getTeachers()
                if (!response.ok) throw Exception(response.error)

                val serverTeachers = response.result.map {
                    Teacher(id = it.id, name = it.name, isFavorite = false)
                }

                // 3. Полностью очищаем базу
                db.getTeacherDao().deleteAll()

                // 4. Добавляем всех преподавателей
                db.getTeacherDao().insertAll(serverTeachers)

                // 5. Восстанавливаем статус фаворитов
                favoriteTeacherNames.forEach { name ->
                    val teacher = db.getTeacherDao().getTeacherByName(name)
                    if (teacher != null) {
                        db.getTeacherDao().setFavoriteStatus(teacher.id, true)
                    }
                }

                Log.d("DataManager", "Teachers refreshed with favorites restored")
            } catch (e: Exception) {
                Log.e("DataManager", "Error refreshing teachers: ${e.message}", e)
                throw e
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