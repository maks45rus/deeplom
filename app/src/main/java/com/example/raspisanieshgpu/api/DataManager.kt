package com.example.raspisanieshgpu.api

import android.util.Log
import com.example.raspisanieshgpu.DataBase.Faculty
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
                val faculties = response.result.map {Faculty(name = it.faculty)}
                db.getFacultyDao().insertAll(faculties)

                val groups = response.result.flatMap {fac -> fac.groups.map { group ->
                        Group(id = group.id, name = group.name, facultyId = db.getFacultyDao().getFacultyByName(fac.faculty).id!!)
                    }
                }
                db.getGroupDao().insertAll(groups)
            } catch (e: Exception) {
                Log.e("DataManager", "Error fetching data groups: ${e.message}", e)
            }
        }
    }

    suspend fun fetchPairs(date: String, week: Int, id: Int, pairsfor: String): PairsResponse {
        lateinit var response: PairsResponse
        withContext(Dispatchers.IO) {
            try {
                if(pairsfor == "group") response = apiService.getPairsGroup(date,week,id)
                else                    response = apiService.getPairsTeacher(date,week,id)
            } catch (e: Exception) {
                Log.e("DataManager", "Error fetching data groups $date,$week,$id,$pairsfor: ${e.message}", e)
            }
        }
        return response
    }

}