package com.example.raspisanieshgpu.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.raspisanieshgpu.R
import com.example.raspisanieshgpu.databinding.ActivityMainBinding
import com.example.raspisanieshgpu.fragments.HomeFragment
import com.example.raspisanieshgpu.fragments.favoriteDir.SavedFragment
import com.example.raspisanieshgpu.fragments.searchDir.SearchFragment
import com.example.raspisanieshgpu.service.WorkManagerHelper
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var workManagerHelper: WorkManagerHelper
    private val viewModel: MainActivityVM by viewModels()

    private val homeFragment = HomeFragment()
    private val savedFragment = SavedFragment()
    private val searchFragment = SearchFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        workManagerHelper = WorkManagerHelper(this)
        workManagerHelper.setupScheduleCheckWorker()

        // Инициализируем мониторинг сети
        viewModel.setupNetworkMonitor(this)



        setupVersionInfo()
        setupObservers()
        setupButtonListeners()

        if (savedInstanceState == null) {
            loadFragment(homeFragment)
            updateButtonState(R.id.btnSaved)
        }
    }

    private fun setupVersionInfo() {
        val pk = packageManager.getPackageInfo(packageName, 0)
        binding.versionName.text = "v${pk.versionName}"
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.internetStatus.collect { isAvailable ->
                if (!isAvailable) {
                    showNoInternetIcon()
                } else {
                    hideNoInternetIcon()
                }
            }
        }
    }

    private fun setupButtonListeners() {
        binding.btnSearch.setOnClickListener {
            loadFragment(searchFragment)
            updateButtonState(R.id.btnSearch)
        }

        binding.btnSaved.setOnClickListener {
            loadFragment(savedFragment)
            updateButtonState(R.id.btnSaved)
        }
    }

    private fun showNoInternetIcon() {
        binding.errorTextMain.text = getString(R.string.no_internet_connection)
        binding.internetStatusIcon.visibility = android.view.View.VISIBLE
    }

    private fun hideNoInternetIcon() {
        binding.errorTextMain.text = ""
        binding.internetStatusIcon.visibility = android.view.View.GONE
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

