package com.example.raspisanieshgpu.api.models

data class GroupsResponse(
    val ok: Boolean,
    val error: String,
    val result: List<Faculty>
)

data class Faculty(
    val faculty: String,
    val groups: List<Group>
)

data class Group(
    val id: Int,
    val name: String
)