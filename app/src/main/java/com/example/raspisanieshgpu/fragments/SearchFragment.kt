package com.example.raspisanieshgpu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.raspisanieshgpu.DataBase.databaseobj
import com.example.raspisanieshgpu.R
import com.example.raspisanieshgpu.databinding.FragmentSearchBinding
import kotlinx.coroutines.launch

class SearchFragment: Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var spinnerAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        var check = ""
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        spinnerAdapter = ArrayAdapter(requireContext(), R.layout.item_list, R.id.item_text)
        binding.spinList.adapter = spinnerAdapter

        binding.chipHolder.setOnCheckedStateChangeListener { _, checkedIds ->
            when(checkedIds.firstOrNull()){
                R.id.chip_group -> {
                    check = "group"
                    updateSpinnerAdapter(check)
                }
                R.id.chip_teacher -> {
                    check = "teacher"
                    updateSpinnerAdapter(check)
                }
                else -> check = ""
            }
        }

        binding.btnSearch.setOnClickListener{
            if(binding.chipHolder.checkedChipIds.isEmpty()) {
                Toast.makeText(requireContext(),R.string.selectchip,Toast.LENGTH_LONG).show()
            }else{
                val fr = RaspisanieFragment.send(binding.spinList.selectedItem.toString(),check)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_cont, fr)
                    .commit()
            }
        }

        return binding.root

    }

    private fun updateSpinnerAdapter(type: String) {
        val db = databaseobj.database
        var itemlist: List<String> = mutableListOf()
        viewLifecycleOwner.lifecycleScope.launch {
            if (type == "group") {
                itemlist = db.getGroupDao().getAllGroups().map { gr -> gr.name }
            }
            if(type == "teacher"){
                itemlist = db.getTeacherDao().getAllTeachers().map { tc -> tc.name }
            }
        spinnerAdapter.clear()
        spinnerAdapter.addAll(itemlist)
        spinnerAdapter.notifyDataSetChanged()

        }
    }
}