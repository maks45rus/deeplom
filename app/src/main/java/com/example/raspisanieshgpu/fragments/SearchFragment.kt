package com.example.raspisanieshgpu.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.raspisanieshgpu.DataBase.MainDataBase
import com.example.raspisanieshgpu.R
import com.example.raspisanieshgpu.databinding.FragmentSearchBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment: Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var acAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        var check = ""
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        acAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line)
        binding.actwList.setAdapter(acAdapter)

        binding.chipHolder.setOnCheckedStateChangeListener { _, checkedIds ->
           check = when(checkedIds.firstOrNull()){
                R.id.chip_group -> "group"
                R.id.chip_teacher -> "teacher"
                else -> ""
            }
            updateSpinnerAdapter(check)
        }



        binding.btnSearch.setOnClickListener {
            val x = binding.actwList.text.toString().trim()
            if (x.isEmpty() || check.isEmpty()) {
                Toast.makeText(requireContext(), R.string.selectchip, Toast.LENGTH_LONG).show()
            } else {
                checkAndOpenSchedule(x, check)
            }
        }

        return binding.root

    }

    private fun checkAndOpenSchedule(name: String, type: String) {
        viewLifecycleOwner.lifecycleScope.launch {

            try {
                var exists = false
                withContext(Dispatchers.IO) {
                    val db = MainDataBase.getInstance(requireContext())
                    exists = when (type) {
                        "group" -> db.getGroupDao().getGroupByName(name) != null
                        "teacher" -> db.getTeacherDao().getTeacherByName(name) != null
                        else -> false
                    }
                }
                if (exists) {
                    openScheduleFragment(name, type)
                } else {
                    showNotFoundError(type)
                }
            } catch (e: Exception) {
                Log.e("SearchFragment", "error", e)
            }
        }
    }

    private fun showNotFoundError(type: String) {
        val errorMsg = when (type) {
            "teacher" -> getString(R.string.teacher_not_found)
            else -> getString(R.string.group_not_found)
        }
        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
    }

    private fun openScheduleFragment(name: String, type: String) {
        val fr = RaspisanieFragment.send(name, type)
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_cont, fr)
            .addToBackStack(null)
            .commit()
    }

    private fun updateSpinnerAdapter(type: String) {
        var itemlist: List<String> = mutableListOf()

        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO){
                try {
                    val db = MainDataBase.getInstance(requireContext())
                    when(type){
                        "group" -> itemlist = db.getGroupDao().getAllGroups().map { gr -> gr.name }
                        "teacher" -> itemlist = db.getTeacherDao().getAllTeachers().map { tc -> tc.name }
                    }
                    acAdapter.clear()
                    acAdapter.addAll(itemlist)
                    acAdapter.notifyDataSetChanged()
                }catch (e: Exception){
                    Log.e("SearchFragment", "error", e)
                }
            }
        }
    }
}