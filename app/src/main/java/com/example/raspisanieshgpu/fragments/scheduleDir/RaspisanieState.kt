package com.example.raspisanieshgpu.fragments.scheduleDir

import com.example.raspisanieshgpu.api.models.Date

sealed class RaspisanieState {
    object Loading : RaspisanieState()
    data class Success(val available: Boolean, val schedule: List<Date>?) : RaspisanieState()
    data class Error(val message: String) : RaspisanieState()
}