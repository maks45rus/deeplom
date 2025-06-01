package com.example.raspisanieshgpu.fragments.searchDir

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raspisanieshgpu.Data.DataBase.MainDataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchVM(
    context: Context
) : ViewModel() {
    private val _searchState = MutableLiveData<SearchState>(SearchState.Loading)
    val searchState: LiveData<SearchState> = _searchState


    private val _currentType = MutableLiveData<String>("group")
    val currentType: LiveData<String> = _currentType
    private val db = MainDataBase.getInstance(context)

    fun setType(type: String) {
        _currentType.value = type
        loadAll()
    }

    fun search(query: String) {
        if (query.isEmpty()) {
            loadAll()
        } else {
            performSearch(query)
        }
    }

    private fun loadAll() {
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            try {
                val items = withContext(Dispatchers.IO) {
                    when (currentType.value) {
                        "group" -> db.getGroupDao().getAllGroups().map { it.name }
                        "teacher" -> db.getTeacherDao().getAllTeachers().map { it.name }
                        else -> emptyList()
                    }
                }
                _searchState.value = SearchState.Success(items)
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(e.message ?: "Error loading data")
            }
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            try {
                val items = withContext(Dispatchers.IO) {
                    when (currentType.value) {
                        "group" -> db.getGroupDao().getAllGroups()
                            .filter { it.name.contains(query, ignoreCase = true) }
                            .map { it.name }
                        "teacher" -> db.getTeacherDao().getAllTeachers()
                            .filter { it.name.contains(query, ignoreCase = true) }
                            .map { it.name }
                        else -> emptyList()
                    }
                }
                _searchState.value = SearchState.Success(items)
            } catch (e: Exception) {
                _searchState.value = SearchState.Error(e.message ?: "Search failed")
            }
        }
    }
}