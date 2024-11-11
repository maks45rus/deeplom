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
                val Response = apiService.getTeachers()
                if(!Response.ok)
                    throw Exception(Response.error)
                val teachers = Response.result.map { Teacher(id = it.id, name = it.name) }
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

                val Response = apiService.getGroups()
                if(!Response.ok)
                    throw Exception(Response.error)
                val groups = Response.result.flatMap {
                    it.groups.map { group -> Group(id = group.id, name = group.name) } }
                db.getGroupDao().deleteAll()
                db.getGroupDao().insertAll(groups)
            } catch (e: Exception) {
                Log.e("DataManager", "Error fetching data groups: ${e.message}", e)
            }
        }
    }

    suspend fun fetchPairs(date: String, week: Int, id: Int, pairsfor: String): PairsResponse {
        lateinit var Response: PairsResponse
        withContext(Dispatchers.IO) {
            try {
                if(pairsfor == "group") Response = apiService.getPairsGroup(date,week,id)
                else                    Response = apiService.getPairsTeacher(date,week,id)
            } catch (e: Exception) {
                Log.e("DataManager", "Error fetching data groups $date,$week,$id,$pairsfor: ${e.message}", e)
            }
        }
        return Response
    }

}