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
import com.example.raspisanieshgpu.api.models.AvailableSchedule
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
    private var scheduleWeekStart: LocalDate? = null
    private var currentWeekSchedule: AvailableSchedule? = null
    private val _favoriteState = MutableLiveData<Boolean>()
    val favoriteState: LiveData<Boolean> = _favoriteState
    private val currentWeekStart = LocalDate.now().let { date ->
        if (date.dayOfWeek == DayOfWeek.SUNDAY) date.plusDays(1) else date
    }

    fun loadScheduleForWeek(
        date: LocalDate,
        name: String,
        type: String,
        context: Context,
    ) {
        _scheduleState.value = RaspisanieState.Loading

        viewModelScope.launch {
            val isFavorite = isFavoriteItem(name,type)
            try {
               scheduleWeekStart = getWeekStartDate(date)
                val weekStart = scheduleWeekStart!!.format(format)
                val newrasp = DataManager.fetchPairs(
                    weekStart,
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

                currentWeekSchedule = newrasp.result

                if (isFavorite && currentWeekStart == scheduleWeekStart) {
                    if (!DataManager.saveCachedSchedule(type, name, newrasp,weekStart, context)) {
                        Log.e("RaspisanieVM", "Schedule not saved")
                    }else{
                        Log.e("RaspisanieVM", "Schedule saved")
                    }
                }

                _scheduleState.value = RaspisanieState.Success(
                    currentWeekSchedule!!.available,
                    currentWeekSchedule!!.days)
            } catch (e: Exception) {
                if (isFavorite && currentWeekStart == scheduleWeekStart) {
                    try {
                        val cachedSchedule = DataManager.loadCachedSchedule(type, name, context)
                        if (!cachedSchedule.ok || !cachedSchedule.result.available) throw Exception(e)

                        currentWeekSchedule = cachedSchedule.result

                        _scheduleState.value = RaspisanieState.Success(
                            currentWeekSchedule!!.available,
                            currentWeekSchedule!!.days)
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
            try {
                val isFav = isFavoriteItem(name, type)
                Log.d("FavoriteCheck", "Checking favorite status for $name ($type): $isFav")
                _favoriteState.value = isFav
            } catch (e: Exception) {
                Log.e("FavoriteCheck", "Error checking favorite status", e)
                _favoriteState.value = false
            }
        }
    }

    fun toggleFavorite(name: String, type: String) {
        viewModelScope.launch {
            val newState = DataManager.toggleFavorite(type, name, context)
            _favoriteState.value = newState
        }
    }

    private suspend fun isFavoriteItem(name: String, type: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val db = MainDataBase.getInstance(context)
                when (type) {
                    "group" -> db.getGroupDao().getGroupByName(name).isFavorite
                    "teacher" -> db.getTeacherDao().getTeacherByName(name).isFavorite
                    else -> false
                }
            } catch (e: Exception) {
                Log.e("FavoriteCheck", "Database error", e)
                false
            }
        }
    }

    private fun getWeekStartDate(date: LocalDate): LocalDate {
        return date.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }
}