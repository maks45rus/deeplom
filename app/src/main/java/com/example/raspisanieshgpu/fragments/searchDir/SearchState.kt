package com.example.raspisanieshgpu.fragments.searchDir

sealed class SearchState {
    object Loading : SearchState()
    data class Success(val items: List<String>) : SearchState()
    data class Error(val message: String) : SearchState()
}