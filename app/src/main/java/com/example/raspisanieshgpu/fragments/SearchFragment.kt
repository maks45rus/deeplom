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

        binding = FragmentSearchBinding.inflate(inflater, container, false)

        acAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line)
        binding.actwList.setAdapter(acAdapter)

        val db = databaseobj.database
        updateSpinnerAdapter()


        binding.btnSearch.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val searchname = binding.actwList.text.toString()
                    if ((searchname == "")) {
                        Toast.makeText(requireContext(), R.string.selectchip, Toast.LENGTH_LONG)
                            .show()
                    } else {

                        val checkdb = db.getGroupAndTeacherDao().isNameExists(name = searchname)
                        if (checkdb) {
                            val type = db.getGroupAndTeacherDao().getGroupsAndTeachersByName(searchname).type
                            val fr = RaspisanieFragment.send(searchname, type.toString())
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.main_cont, fr)
                                .commit()
                        }
                    }
                }catch (e: Exception){
                    Log.e("SeacrchFragment", ":error search: ${e.message}", e)
                }
            }
        }

        return binding.root

    }

    private fun updateSpinnerAdapter() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val db = databaseobj.database
                var itemlist: List<String> = mutableListOf()
                itemlist =
                    db.getGroupAndTeacherDao().getAllGroupsAndTeachers().map { item -> item.name }
                acAdapter.clear()
                acAdapter.addAll(itemlist)
                acAdapter.notifyDataSetChanged()
            }catch (e: Exception){
                Log.e("SeacrchFragment", ":error spinneradapter: ${e.message}", e)
            }
        }
    }
}