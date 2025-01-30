package com.example.raspisanieshgpu.api

import com.example.raspisanieshgpu.api.models.GroupsResponse
import com.example.raspisanieshgpu.api.models.PairsResponse
import com.example.raspisanieshgpu.api.models.TeachersResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("index.php?method=groups.get")
    suspend fun getGroups(): GroupsResponse

    @GET("index.php?method=teachers.get")
    suspend fun getTeachers(): TeachersResponse

    @GET("index.php?method=pairs.get")
    suspend fun getPairsGroup(
        @Query("date") date: String,
        @Query("week") week: Int,
        @Query("groupId") groupId: Int
    ): PairsResponse

    @GET("index.php?method=pairs.get")
    suspend fun getPairsTeacher(
        @Query("date") date: String,
        @Query("week") week: Int,
        @Query("teacherId") teacherId: Int
    ): PairsResponse

}