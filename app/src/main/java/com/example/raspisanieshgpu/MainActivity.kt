package com.example.raspisanieshgpu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
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

        binding.BtnMenu.setOnClickListener {
            binding.mainLayout.openDrawer(GravityCompat.START)
        }

        binding.botNav.setNavigationItemSelectedListener  { menuItem ->
            when (menuItem.itemId) {
                R.id.HomeFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_cont, homeFragment)
                        .commit()

                }
                R.id.SearchFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_cont, searchFragment)
                        .commit()

                }
                R.id.SavedFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_cont, savedFragment)
                        .commit()

                }

            }

            binding.mainLayout.closeDrawers()
            true
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.main_cont, savedFragment)
            .commit()


    }






}

