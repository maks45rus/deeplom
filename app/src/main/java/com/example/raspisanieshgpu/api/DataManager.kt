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
                val teachers = response.result!!.map { Teacher(id = it.id, name = it.name) }
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

                val groups = response.result!!.flatMap {fac -> fac.groups.map { group ->
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

    suspend fun refreshGroups() {  // если ид не меняется
        withContext(Dispatchers.IO) {
            try {
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
                throw e
            }
        }
    }

    suspend fun refreshTeachers() {
        withContext(Dispatchers.IO) {
            try {
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

    suspend fun addFavorite(pairsfor: String, namesearch: String){
        withContext(Dispatchers.IO) {
            try{
                var isFavorite = false
                when (pairsfor) {
                    "group" -> {
                        val group = databaseobj.database.getGroupDao()
                            .getGroupByName(namesearch)
                        isFavorite = group.isFavorite
                        databaseobj.database.getGroupDao()
                            .setFavoriteStatus(group.id, !isFavorite)
                    }
                    "teacher" -> {
                        val teacher = databaseobj.database.getTeacherDao()
                            .getTeacherByName(namesearch)
                        isFavorite = teacher.isFavorite
                        databaseobj.database.getTeacherDao()
                            .setFavoriteStatus(teacher.id, !isFavorite)
                    }
                }
            }finally{
            }
        }
    }

    fun isApiAvailable(): Boolean {
        return true
    }

    suspend fun fetchPairs(date: String, week: Int, id: Int, pairsfor: String): PairsResponse {
        return try {
            withContext(Dispatchers.IO) {
                val response = if (pairsfor == "group") {
                    apiService.getPairsGroup(date, week, id)
                } else {
                    apiService.getPairsTeacher(date, week, id)
                }

                Log.d("DataManager", "Fetch pairs success for $pairsfor $id")
                response // Возвращаем успешный ответ
            }
        } catch (e: Exception) {
            Log.e("DataManager", "Error fetching data for $pairsfor $id: ${e.message}", e)

            // Возвращаем корректный PairsResponse с флагом ошибки
            PairsResponse(
                ok = false,
                result = emptyList(), // Пустой список как значение по умолчанию
                error = e.message ?: "Unknown network error"
            )
        }
    }

}