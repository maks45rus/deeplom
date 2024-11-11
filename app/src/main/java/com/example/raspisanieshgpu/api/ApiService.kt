package com.example.raspisanieshgpu.api

import com.example.raspisanieshgpu.api.models.GroupsResponse
import com.example.raspisanieshgpu.api.models.PairsResponse
import com.example.raspisanieshgpu.api.models.TeachersResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("sch_api/?method=groups.get")
    suspend fun getGroups(): GroupsResponse

    @GET("sch_api/?method=teachers.get")
    suspend fun getTeachers(): TeachersResponse

    @GET("sch_api/?method=pairs.get")
    suspend fun getPairsGroup(
        @Query("date") date: String,
        @Query("week") week: Int,
        @Query("groupId") groupId: Int
    ): PairsResponse

    @GET("sch_api/?method=pairs.get")
    suspend fun getPairsTeacher(
        @Query("date") date: String,
        @Query("week") week: Int,
        @Query("teacherId") teacherId: Int
    ): PairsResponse

}