package com.example.raspisanieshgpu.api.models

data class PairsResponse(
    val ok: Boolean,
    val error: String,
    val result: List<Date>
)

data class Date(
    val date: String,
    val pairs: List<Para>
)

data class Para(
    val num: Int,
    val text: String
)