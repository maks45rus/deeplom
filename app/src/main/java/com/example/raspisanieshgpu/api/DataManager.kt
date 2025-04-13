package com.example.raspisanieshgpu.api

import android.util.Log
import com.example.raspisanieshgpu.DataBase.GroupAndTeacher
import com.example.raspisanieshgpu.DataBase.databaseobj
import com.example.raspisanieshgpu.api.RetrofitClient.apiService
import com.example.raspisanieshgpu.api.models.PairsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DataManager {

    private var db = databaseobj.database
    suspend fun fetchAndSaveBase() {
        withContext(Dispatchers.IO) {
            try {

                val responseteachers = apiService.getTeachers()
                if(!responseteachers.ok)
                    throw Exception(responseteachers.error)
                val responsegroups = apiService.getGroups()
                if(!responsegroups.ok)
                    throw Exception(responsegroups.error)
                val teachers = responseteachers.result.map { GroupAndTeacher(api_id = it.id, name = it.name, type = "TEACHER") }
                val groups = responsegroups.result.flatMap {fac -> fac.groups.map { group ->
                    GroupAndTeacher(api_id = group.id, name = group.name,type = "GROUP") } }
                db.getGroupAndTeacherDao().deleteAll()
                db.getGroupAndTeacherDao().insertAll(teachers + groups)
                Log.d("DataManager", "fetch groups and teachers succsess")
            }catch (e: Exception){
                Log.e("DataManager", "Error fetching data teachers: ${e.message}", e)
            }

        }
    }


    suspend fun fetchPairs(date: String, week: Int, id: Int, pairsfor: String): PairsResponse {
        lateinit var response: PairsResponse
        withContext(Dispatchers.IO) {
            try {
                if(pairsfor == "GROUP") response = apiService.getPairsGroup(date,week,id)
                else                    response = apiService.getPairsTeacher(date,week,id)
                Log.d("DataManager", "fetch pairs succsess")
            } catch (e: Exception) {
                Log.e("DataManager", "Error fetching data groups $date,$week,$id,$pairsfor: ${e.message}", e)
            }
        }
        return response
    }

}