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

        binding.btnSearch.setOnClickListener{
            val x = binding.actwList.text.toString()
            if((x == "") or (check == "")) {
                Toast.makeText(requireContext(),R.string.selectchip,Toast.LENGTH_LONG).show()
            }else{
                val fr = RaspisanieFragment.send(x,check)
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
           when(type){
               "group" -> itemlist = db.getGroupDao().getAllGroups().map { gr -> gr.name }
               "teacher" -> itemlist = db.getTeacherDao().getAllTeachers().map { tc -> tc.name }
           }
        acAdapter.clear()
        acAdapter.addAll(itemlist)
        acAdapter.notifyDataSetChanged()

        }
    }
}