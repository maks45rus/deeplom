package com.example.raspisanieshgpu.api

import android.content.Context
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.raspisanieshgpu.DataBase.Group
import com.example.raspisanieshgpu.DataBase.MainDataBase
import com.example.raspisanieshgpu.DataBase.Teacher
import com.example.raspisanieshgpu.R
import com.example.raspisanieshgpu.api.RetrofitClient.apiService
import com.example.raspisanieshgpu.api.models.AvailableSchedule
import com.example.raspisanieshgpu.api.models.Date
import com.example.raspisanieshgpu.api.models.PairsResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object DataManager {


   /* если ид на сервере меняется придется так suspend fun refreshGroups() {
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
 } */

    suspend fun refreshGroups(context: Context) {  // если ид не меняется
        withContext(Dispatchers.IO) {
            try {
                val db = MainDataBase.getInstance(context)
                // 1. Получаем текущие группы из базы (для проверки существующих)
                val currentGroups = db.getGroupDao().getAllGroups()
                val currentGroupIds = currentGroups.map { it.id }.toSet()

                // 3. Загружаем свежие данные с сервера
                val response = apiService.getGroups()
                if (!response.ok) throw Exception(response.error)

                // 4. Фильтруем и обрабатываем новые группы
                val newGroups = response.result!!.flatMap { faculty ->
                    faculty.groups.map { serverGroup ->
                        Group(
                            id = serverGroup.id,
                            name = serverGroup.name,
                        )
                    }.filterNot { currentGroupIds.contains(it.id) }
                }

                // 5. Добавляем только новые группы
                if (newGroups.isNotEmpty()) {
                    db.getGroupDao().insertAll(newGroups)
                }

                Log.d("DataManager", "Added ${newGroups.size} new groups")
            } catch (e: Exception) {
                Log.e("DataManager", "Error refreshing groups: ${e.message}", e)
            }
        }
    }

    suspend fun refreshTeachers(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val db = MainDataBase.getInstance(context)
                val currentTeachers = db.getTeacherDao().getAllTeachers()
                val currentTeacherIds = currentTeachers.map { it.id }.toSet()

                val response = apiService.getTeachers()
                if (!response.ok) throw Exception(response.error)

                val newTeachers = response.result!!.map { serverTeacher ->
                    Teacher(
                        id = serverTeacher.id,
                        name = serverTeacher.name,
                    )
                }.filterNot { currentTeacherIds.contains(it.id) }

                if (newTeachers.isNotEmpty()) {
                    db.getTeacherDao().insertAll(newTeachers)
                }

                Log.d("DataManager", "Added ${newTeachers.size} new teachers")
            } catch (e: Exception) {
                Log.e("DataManager", "Error refreshing teachers: ${e.message}", e)
                throw e
            }
        }
    }

    suspend fun toggleFavorite(entityType: String, entityName: String, context: Context): Boolean {
        return try {
            when (entityType) {
                "group" -> handleGroupFavorite(entityName,context)
                "teacher" -> handleTeacherFavorite(entityName,context)
                else -> false
            }
        } catch (e: Exception) {
            Log.e("Favorite", "Error toggling favorite status for $entityType: $entityName", e)
            false
        }
    }

    private suspend fun handleGroupFavorite(groupName: String, context: Context): Boolean {
        try {
            val db = MainDataBase.getInstance(context)
            val groupDao = db.getGroupDao()
            val group = groupDao.getGroupByName(groupName)
            val fav = group.isFavorite
            groupDao.setFavoriteStatus(group.id, !fav)
            Log.d("Favorite", "Group ${group.name} favorite status toggled to ${!group.isFavorite}")
            return !group.isFavorite
        } catch (e: Exception) {
            Log.w("Favorite", "Group not found: $groupName")
            return false
        }
    }

    private suspend fun handleTeacherFavorite(teacherName: String, context: Context): Boolean {
        try {
            var isFavorite = false
            withContext(Dispatchers.IO){
                val db = MainDataBase.getInstance(context)
                val teacher = db.getTeacherDao().getTeacherByName(teacherName)
                val fav = teacher.isFavorite
                db.getTeacherDao().setFavoriteStatus(teacher.name, !fav)
                Log.d("Favorite", "Teacher ${teacher.name} favorite status toggled to ${!teacher.isFavorite}")
                isFavorite = !teacher.isFavorite
            }
            return isFavorite
        } catch (e: Exception) {
            Log.w("Favorite", "Teacher not found: $teacherName")
            return false
        }
    }


    fun isApiAvailable(): Boolean {
        return true
    }





    suspend fun fetchPairs(date: String, week: Int, name: String, pairsfor: String, context: Context): PairsResponse {
        var response: PairsResponse
        withContext(Dispatchers.IO) {
            try {
                val db = MainDataBase.getInstance(context)
                val id: Int
                response = if (pairsfor == "group") {
                    id = db.getGroupDao().getGroupByName(name).id
                    apiService.getPairsGroup(date, week, id)
                } else {
                    id = db.getTeacherDao().getTeacherByName(name).id
                    apiService.getPairsTeacher(date, week, id)
                }
                Log.d("DataManager", "Fetch pairs success for $pairsfor $name")


            } catch (e: Exception) {
                Log.e("DataManager", "Error fetching data for $pairsfor $name: ${e.message}", e)

                // Возвращаем корректный PairsResponse с флагом ошибки
                response = PairsResponse(
                    ok = false,
                    result = AvailableSchedule(
                        available = false
                    ), // Пустой список как значение по умолчанию
                    error = e.message ?: "Unknown network error"
                )
            }
        }
        return response
    }
    suspend fun saveCachedSchedule(type: String, name: String, schedule: PairsResponse, context: Context):Boolean{

        return withContext(Dispatchers.IO) {
            try {
                if(!schedule.ok) throw Exception((schedule.error).toString())
                if(!schedule.result.available) throw Exception((R.string.schedule_not_available).toString())
                val db = MainDataBase.getInstance(context)
                if(type == "group") db.getGroupDao().setScheduleData(name, Gson().toJson(schedule))
                else db.getTeacherDao().setScheduleData(name,Gson().toJson(schedule))
                Log.d("DataManager", "Schedule for ${name} saved")
                true
            }catch (e: Exception){
                Log.e("DataManager", "Schedule for ${name} not saved: ", e)
                false
            }
        }
    }

    suspend fun loadCachedSchedule(type: String, name: String, context: Context): PairsResponse {
        var ret = PairsResponse(
            ok = false,
            result = AvailableSchedule(
                available = false
            ), // Пустой список как значение по умолчанию
            error = "Unknown network error"
        )

            val json: String
            return withContext(Dispatchers.IO) {
            try {
                val db = MainDataBase.getInstance(context)
                json = if (type == "group") {
                    db.getGroupDao().getScheduleData(name)
                } else {
                    db.getTeacherDao().getScheduleData(name)
                }
                if(json == "") throw Exception("no cached schedule")
                ret = Gson().fromJson(json, PairsResponse::class.java)
                Log.d("DataManager", "Schedule for ${name} loaded")
                ret
            }catch (e: Exception){
                Log.e("DataManager", "Schedule for ${name} not loaded: ", e)

                ret = PairsResponse(
                    ok = false,
                    result = AvailableSchedule(
                        available = false
                    ), // Пустой список как значение по умолчанию
                    error = e.message ?: "Unknown network error"
                )
                ret
            }
        }
    }

}