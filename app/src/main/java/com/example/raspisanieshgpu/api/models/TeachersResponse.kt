package com.example.raspisanieshgpu.api.models

data class TeachersResponse(
     val ok: Boolean,
     val error: String? = null,
     val result: List<Teacher>? = null
)

data class Teacher(
    val id: Int,
    val name: String,
    val url: String
)