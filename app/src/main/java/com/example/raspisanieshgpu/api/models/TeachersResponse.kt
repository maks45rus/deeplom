package com.example.raspisanieshgpu.api.models

data class TeachersResponse(
     val ok: Boolean,
     val error: String,
     val result: List<Teacher>
)

data class Teacher(
    val id: Int,
    val name: String,
    val url: String
)