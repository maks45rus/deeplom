package com.example.raspisanieshgpu.Data

import android.content.Context
import android.util.Log
import com.example.raspisanieshgpu.Data.DataBase.Group
import com.example.raspisanieshgpu.Data.DataBase.MainDataBase
import com.example.raspisanieshgpu.Data.DataBase.Teacher
import com.example.raspisanieshgpu.R
import com.example.raspisanieshgpu.api.RetrofitClient.apiService
import com.example.raspisanieshgpu.api.models.AvailableSchedule
import com.example.raspisanieshgpu.api.models.Date
import com.example.raspisanieshgpu.api.models.PairsResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DataManager {



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
        val id: Int
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
    suspend fun saveCachedSchedule(type: String, name: String, schedule: PairsResponse, weekStart: String, context: Context):Boolean{

        return withContext(Dispatchers.IO) {
            try {
                if(!schedule.ok) throw Exception((schedule.error).toString())
                if(!schedule.result.available) throw Exception((R.string.schedule_not_available).toString())
                val db = MainDataBase.getInstance(context)
                if(type == "group"){
                    db.getGroupDao().setWeekStartDate(name, weekStart)
                    db.getGroupDao().setScheduleData(name, Gson().toJson(schedule))
                }
                else{
                    db.getTeacherDao().setWeekStartDate(name, weekStart)
                    db.getTeacherDao().setScheduleData(name,Gson().toJson(schedule))
                }
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

                ret
            }
        }
    }

}