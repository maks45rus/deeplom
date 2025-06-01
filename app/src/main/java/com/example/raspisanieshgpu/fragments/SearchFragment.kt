package com.example.raspisanieshgpu.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.raspisanieshgpu.Data.DataBase.MainDataBase
import com.example.raspisanieshgpu.R
import com.example.raspisanieshgpu.adapter.SearchAdapter
import com.example.raspisanieshgpu.databinding.FragmentSearchBinding
import com.example.raspisanieshgpu.fragments.Raspisanie.RaspisanieFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var searchAdapter: SearchAdapter
    private var currentType: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchAdapter = SearchAdapter(requireContext()) { selectedItem ->
            openScheduleFragment(selectedItem, currentType)
        }
        binding.searchList.adapter = searchAdapter

        setupChips()
        setupSearchInput()
        loadInitialData()
    }

    private fun setupChips() {
        // Обработчики для каждого Chip
        binding.chipGroup.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.chipTeacher.isChecked = false
                currentType = "group"
                loadAllGroups()
            }
        }

        binding.chipTeacher.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.chipGroup.isChecked = false
                currentType = "teacher"
                loadAllTeachers()
            }
        }

        // По умолчанию выбираем первую вкладку
        binding.chipGroup.isChecked = true
    }

    private fun setupSearchInput() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (currentType.isEmpty()) return

                val query = s?.toString()?.trim() ?: ""
                if (query.isEmpty()) {
                    when (currentType) {
                        "group" -> loadAllGroups()
                        "teacher" -> loadAllTeachers()
                    }
                } else {
                    performSearch(query)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadInitialData() {
        // Можно загрузить начальные данные, если нужно
    }

    private fun loadAllGroups() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val groups = withContext(Dispatchers.IO) {
                    MainDataBase.getInstance(requireContext())
                        .getGroupDao()
                        .getAllGroups()
                        .map { it.name }
                }

                searchAdapter.clear()
                searchAdapter.addAll(groups)
                searchAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), R.string.group_not_found, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadAllTeachers() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val teachers = withContext(Dispatchers.IO) {
                    MainDataBase.getInstance(requireContext())
                        .getTeacherDao()
                        .getAllTeachers()
                        .map { it.name }
                }

                searchAdapter.clear()
                searchAdapter.addAll(teachers)
                searchAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), R.string.teacher_not_found, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performSearch(query: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val results = when (currentType) {
                    "group" -> {
                        withContext(Dispatchers.IO) {
                            MainDataBase.getInstance(requireContext())
                                .getGroupDao()
                                .getAllGroups()
                                .filter { it.name.contains(query, ignoreCase = true) }
                                .map { it.name }
                        }
                    }
                    "teacher" -> {
                        withContext(Dispatchers.IO) {
                            MainDataBase.getInstance(requireContext())
                                .getTeacherDao()
                                .getAllTeachers()
                                .filter { it.name.contains(query, ignoreCase = true) }
                                .map { it.name }
                        }
                    }
                    else -> emptyList()
                }

                searchAdapter.clear()
                searchAdapter.addAll(results)
                searchAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), R.string.error_schedule, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openScheduleFragment(name: String, type: String) {
        val fr = RaspisanieFragment.send(name, type)
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_cont, fr)
            .addToBackStack(null)
            .commit()
    }
}