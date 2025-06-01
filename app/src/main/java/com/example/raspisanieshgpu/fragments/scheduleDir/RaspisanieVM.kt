package com.example.raspisanieshgpu.fragments.scheduleDir

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raspisanieshgpu.Data.DataBase.MainDataBase
import com.example.raspisanieshgpu.Data.DataManager
import com.example.raspisanieshgpu.R
import com.example.raspisanieshgpu.api.models.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RaspisanieVM(
    private val context: Context
) : ViewModel() {
    private val _scheduleState = MutableLiveData<RaspisanieState>()
    val scheduleState: LiveData<RaspisanieState> = _scheduleState

    private val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private var currentWeekStart: LocalDate? = null
    private var currentWeekSchedule: List<Date>? = null
    private val _favoriteState = MutableLiveData<Boolean>()
    val favoriteState: LiveData<Boolean> = _favoriteState

    fun loadScheduleForWeek(
        date: LocalDate,
        name: String,
        type: String,
        context: Context,
        isFavorite: Boolean
    ) {
        _scheduleState.value = RaspisanieState.Loading

        viewModelScope.launch {
            try {
                currentWeekStart = getWeekStartDate(date)
                val newrasp = DataManager.fetchPairs(
                    currentWeekStart!!.format(format),
                    1,
                    name,
                    type,
                    context
                )

                if (!newrasp.ok) {
                    throw Exception(context.getString(R.string.no_api_connection))
                }
                if (!newrasp.result.available) {
                    throw Exception(context.getString(R.string.schedule_not_available))
                }

                currentWeekSchedule = newrasp.result.days

                if (isFavorite) {
                    if (!DataManager.saveCachedSchedule(type, name, newrasp, context)) {
                        Log.e("RaspisanieVM", "Schedule not saved")
                    }
                }

                _scheduleState.value = RaspisanieState.Success(currentWeekSchedule!!)
            } catch (e: Exception) {
                if (isFavorite) {
                    try {
                        val cachedSchedule = DataManager.loadCachedSchedule(type, name, context)
                        if (!cachedSchedule.ok || !cachedSchedule.result.available) throw Exception(
                            e
                        )

                        currentWeekSchedule = cachedSchedule.result.days

                        _scheduleState.value = RaspisanieState.Success(currentWeekSchedule!!)
                    } catch (e: Exception) {
                        _scheduleState.value = RaspisanieState.Error(e.message ?: "Unknown error")
                    }
                } else {
                    _scheduleState.value = RaspisanieState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun checkFavoriteStatus(name: String, type: String) {
        viewModelScope.launch {
            _favoriteState.value = isFavoriteItem(name, type)
        }
    }

    fun toggleFavorite(name: String, type: String) {
        viewModelScope.launch {
            val newState = DataManager.toggleFavorite(type, name, context)
            _favoriteState.value = newState
        }
    }

    private suspend fun isFavoriteItem(name: String, type: String): Boolean {
        val db = MainDataBase.getInstance(context)
        return withContext(Dispatchers.IO) {
            try {
                when (type) {
                    "group" -> db.getGroupDao().getGroupByName(name).isFavorite
                    "teacher" -> db.getTeacherDao().getTeacherByName(name).isFavorite
                    else -> false
                }
            } catch (e: Exception) {
                Log.e("RaspisanieVM", "Error checking favorite status", e)
                false
            }
        }
    }

    private fun getWeekStartDate(date: LocalDate): LocalDate {
        return date.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
}