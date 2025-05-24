package com.example.raspisanieshgpu

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter


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
                if(!isInternetAvailable()){
                    showNoInternetIcon()
                    throw Exception("internet error")
                }
                if(!isApiAvailable()){
                    showNoApiIcon()
                    throw Exception("API error")
                }
                DataManager.refreshGroups()
                DataManager.refreshTeachers()
                Log.d("DataUpdate", "Данные успешно обновлены с сохранением избранного")

            } catch (e: Exception) {
                Log.e("DataUpdateMainActivity","error:",e)




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

    private suspend fun isApiAvailable(): Boolean{
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val day = LocalDate.now()
        val rr = DataManager.fetchPairs(day.format(format), 0, 1, "group").ok
        Log.d("DataUpdate", "API ${rr}:")
        return rr

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

