package com.example.raspisanieshgpu.fragments.favoriteDir

sealed class FavoriteState {

    object Loading : FavoriteState()
    data class Loaded(
        val groups: List<String>,
        val teachers: List<String>
    ) : FavoriteState()
    data class Error(val message: String) : FavoriteState()

}