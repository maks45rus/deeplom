package com.example.raspisanieshgpu

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.raspisanieshgpu.DataBase.databaseobj
import com.example.raspisanieshgpu.databinding.ActivityMainBinding
import com.example.raspisanieshgpu.api.DataManager
import com.example.raspisanieshgpu.fragments.HomeFragment
import com.example.raspisanieshgpu.fragments.SavedFragment
import com.example.raspisanieshgpu.fragments.SearchFragment
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding

    private val homeFragment = HomeFragment()
    private val savedFragment = SavedFragment()
    private val searchFragment = SearchFragment()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {

        databaseobj.initialize(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        lifecycleScope.launch {

            try {
                // Обновляем группы с сохранением избранных
                DataManager.refreshGroups()

                // Обновляем преподавателей с сохранением избранных
                DataManager.refreshTeachers()

                // Логируем успешное обновление
                Log.d("DataUpdate", "Данные успешно обновлены с сохранением избранного")


            } catch (e: Exception) {
                Log.e("DataUpdate", "Ошибка при обновлении данных: ${e.message}", e)
            }
        }

        // Обработка кликов по кнопкам

        binding.btnSearch.setOnClickListener {
            loadFragment(searchFragment)
            updateButtonState(R.id.btnSearch)
        }

        binding.btnSaved.setOnClickListener {
            loadFragment(savedFragment)
            updateButtonState(R.id.btnSaved)
        }

        if (savedInstanceState == null) {
            loadFragment(savedFragment)
            updateButtonState(R.id.btnSaved)
        }
    }


    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_cont, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun updateButtonState(selectedButtonId: Int) {
        binding.btnSearch.isSelected = selectedButtonId == R.id.btnSearch
        binding.btnSaved.isSelected = selectedButtonId == R.id.btnSaved
    }

}

