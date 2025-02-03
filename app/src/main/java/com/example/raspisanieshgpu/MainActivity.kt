package com.example.raspisanieshgpu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
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

    override fun onCreate(savedInstanceState: Bundle?) {

        databaseobj.initialize(this)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        lifecycleScope.launch {
                DataManager.fetchAndSaveTeachers()
                DataManager.fetchAndSaveGroups()
        }

        // Обработка кликов по кнопкам
        binding.btnHome.setOnClickListener {
            loadFragment(homeFragment)
            updateButtonState(R.id.btnHome)
        }
        binding.btnSearch.setOnClickListener {
            loadFragment(searchFragment)
            updateButtonState(R.id.btnSearch)
        }
        binding.btnSaved.setOnClickListener {
            loadFragment(savedFragment)
            updateButtonState(R.id.btnSaved)
        }

        // Установка начального фрагмента (HomeFragment)
        if (savedInstanceState == null) {
            loadFragment(homeFragment)
            updateButtonState(R.id.btnHome)
        }
    }


    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_cont, fragment)
            .commit()
    }

    private fun updateButtonState(selectedButtonId: Int) {
        binding.btnHome.isSelected = selectedButtonId == R.id.btnHome
        binding.btnSearch.isSelected = selectedButtonId == R.id.btnSearch
        binding.btnSaved.isSelected = selectedButtonId == R.id.btnSaved
    }

}

