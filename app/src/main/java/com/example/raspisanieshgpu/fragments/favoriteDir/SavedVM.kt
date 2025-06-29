package com.example.raspisanieshgpu.fragments.favoriteDir

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raspisanieshgpu.Data.DataBase.MainDataBase
import kotlinx.coroutines.launch

class SavedVM(
) : ViewModel() {
    private val _favoritesState = MutableStateFlow<FavoriteState>(FavoriteState.Loading)
    val favoritesState: StateFlow<FavoriteState> = _favoritesState

    fun loadFavorites(context: Context) {
        viewModelScope.launch {
            try {
                val db = MainDataBase.getInstance(context)
                val groups = db.getGroupDao().getFavorites().map { it.name }
                val teachers = db.getTeacherDao().getFavorites().map { it.name }
                val others = db.getSavedOtherDao().getAllSavedOther().map { it.name }
                _favoritesState.value = FavoriteState.Loaded(groups, teachers, others)
            } catch (e: Exception) {
                _favoritesState.value = FavoriteState.Error(e.message ?: "Error loading favorites")
            }
        }
    }
}