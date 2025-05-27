package com.example.raspisanieshgpu

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
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
import com.example.raspisanieshgpu.service.WorkManagerHelper
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var workManagerHelper: WorkManagerHelper

    private val homeFragment = HomeFragment()
    private val savedFragment = SavedFragment()
    private val searchFragment = SearchFragment()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {

        databaseobj.initialize(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        workManagerHelper = WorkManagerHelper(this)
        workManagerHelper.setupScheduleCheckWorker()

        lifecycleScope.launch {
            try {
                if(!isInternetAvailable()){
                    showNoInternetIcon()
                    throw Exception("internet error")
                }
                if(!DataManager.isApiAvailable()){
                    showNoApiIcon()
                    throw Exception("API error")
                }
                DataManager.refreshGroups()
                DataManager.refreshTeachers()
                Log.d("DataUpdateMainActivity", "database updated")

            } catch (e: Exception) {
                Log.e("DataUpdateMainActivity","error:",e)
            }
        }

        binding.btnSearch.setOnClickListener {
            loadFragment(searchFragment)
            updateButtonState(R.id.btnSearch)
        }

        binding.btnSaved.setOnClickListener {
            loadFragment(savedFragment)
            updateButtonState(R.id.btnSaved)
        }

        if (savedInstanceState == null) {
            loadFragment(homeFragment)
            updateButtonState(R.id.btnSaved)
        }
    }


    private fun showNoInternetIcon() {
        runOnUiThread {
            binding.errorTextMain.text = getString(R.string.no_internet_connection)
            binding.internetStatusIcon.visibility = android.view.View.VISIBLE
        }
    }

    private fun showNoApiIcon() {
        runOnUiThread {
            binding.errorTextMain.text = getString(R.string.no_api_connection)
            binding.internetStatusIcon.visibility = android.view.View.VISIBLE
        }
    }

    private fun hideNoInternetIcon() {
        runOnUiThread {

            binding.internetStatusIcon.visibility = android.view.View.GONE
        }
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
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

