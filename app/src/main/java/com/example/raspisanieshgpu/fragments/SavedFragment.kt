package com.example.raspisanieshgpu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.raspisanieshgpu.Data.DataBase.MainDataBase
import com.example.raspisanieshgpu.R
import com.example.raspisanieshgpu.databinding.FragmentSavedBinding
import com.example.raspisanieshgpu.databinding.ItemFavoriteBinding
import com.example.raspisanieshgpu.databinding.ItemGroupHeaderBinding
import com.example.raspisanieshgpu.fragments.Raspisanie.RaspisanieFragment
import kotlinx.coroutines.launch

class SavedFragment : Fragment() {

    private lateinit var binding: FragmentSavedBinding
    private val groupsList = mutableListOf<String>()
    private val teachersList = mutableListOf<String>()
    private lateinit var headers: List<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSavedBinding.inflate(inflater, container, false)
        headers = listOf(
            getString(R.string.groups_header),
            getString(R.string.teachers_header)
        )
        setupExpandableListView()
        loadFavorites()
        return binding.root
    }

    private fun setupExpandableListView() {

        binding.expandableListView.setAdapter(object : BaseExpandableListAdapter() {
            override fun getGroupCount(): Int = headers.size
            override fun getChildrenCount(groupPosition: Int): Int = when (groupPosition) {
                0 -> groupsList.size
                1 -> teachersList.size
                else -> 0
            }

            override fun getGroup(groupPosition: Int): Any = headers[groupPosition]
            override fun getChild(groupPosition: Int, childPosition: Int): Any = when (groupPosition) {
                0 -> groupsList[childPosition]
                1 -> teachersList[childPosition]
                else -> ""
            }

            override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()
            override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()
            override fun hasStableIds(): Boolean = true

            override fun getGroupView(
                groupPosition: Int,
                isExpanded: Boolean,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ItemGroupHeaderBinding.inflate(inflater, parent, false)
                binding.headerText.text = headers[groupPosition]
                return binding.root
            }

            override fun getChildView(
                groupPosition: Int,
                childPosition: Int,
                isLastChild: Boolean,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ItemFavoriteBinding.inflate(inflater, parent, false)
                binding.itemText.text = when (groupPosition) {
                    0 -> groupsList[childPosition]
                    1 -> teachersList[childPosition]
                    else -> ""
                }
                return binding.root
            }

            override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
                return true // Все элементы можно выбирать
            }
        })

        binding.expandableListView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            val type = if (groupPosition == 0) "group" else "teacher"
            val name = when (groupPosition) {
                0 -> groupsList[childPosition]
                1 -> teachersList[childPosition]
                else -> ""
            }
            openFavoriteSchedule(name, type)
            true
        }
    }

    private fun loadFavorites() {
        viewLifecycleOwner.lifecycleScope.launch {
            groupsList.clear()
            teachersList.clear()
            val db = MainDataBase.getInstance(requireContext())
            groupsList.addAll(db.getGroupDao().getFavorites().map { it.name })
            teachersList.addAll(db.getTeacherDao().getFavorites().map { it.name })

            (binding.expandableListView.expandableListAdapter as BaseExpandableListAdapter).notifyDataSetChanged()

            // Раскрываем все группы по умолчанию
            for (i in headers.indices) {
                binding.expandableListView.expandGroup(i)
            }
        }
    }

    private fun openFavoriteSchedule(name: String, type: String) {
        val fr = RaspisanieFragment.send(name, type)
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_cont, fr)
            .addToBackStack(null)
            .commit()
    }
}