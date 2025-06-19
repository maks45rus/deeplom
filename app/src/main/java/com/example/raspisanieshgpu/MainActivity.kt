package com.example.raspisanieshgpu

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.raspisanieshgpu.Data.DataManager
import com.example.raspisanieshgpu.databinding.ActivityMainBinding
import com.example.raspisanieshgpu.fragments.HomeFragment
import com.example.raspisanieshgpu.fragments.favoriteDir.SavedFragment
import com.example.raspisanieshgpu.fragments.searchDir.SearchFragment
import com.example.raspisanieshgpu.service.WorkManagerHelper
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var workManagerHelper: WorkManagerHelper

    private val homeFragment = HomeFragment()
    private val savedFragment = SavedFragment()
    private val searchFragment = SearchFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideSystemUI()
        workManagerHelper = WorkManagerHelper(this)
        workManagerHelper.setupScheduleCheckWorker()


        val pk = packageManager.getPackageInfo(packageName, 0)
        binding.versionName.text = "${pk.versionName}"

        lifecycleScope.launch {
            try {
                if(!isInternetAvailable()){
                    showNoInternetIcon()
                    throw Exception("internet error")
                }
                DataManager.refreshGroups(applicationContext)
                DataManager.refreshTeachers(applicationContext)
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


    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }



    private fun showNoInternetIcon() {
        runOnUiThread {
            binding.errorTextMain.text = getString(R.string.no_internet_connection)
            binding.internetStatusIcon.visibility = android.view.View.VISIBLE
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

