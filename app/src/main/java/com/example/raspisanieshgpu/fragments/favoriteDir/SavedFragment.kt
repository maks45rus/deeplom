package com.example.raspisanieshgpu.fragments.favoriteDir

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.raspisanieshgpu.R
import com.example.raspisanieshgpu.databinding.FragmentSavedBinding
import com.example.raspisanieshgpu.databinding.ItemFavoriteBinding
import com.example.raspisanieshgpu.databinding.ItemGroupHeaderBinding
import com.example.raspisanieshgpu.fragments.scheduleDir.RaspisanieFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SavedFragment : Fragment() {

    private lateinit var binding: FragmentSavedBinding
    private lateinit var viewModel: SavedVM
    private val groupsList = mutableListOf<String>()
    private val teachersList = mutableListOf<String>()
    private val othersList = mutableListOf<String>()
    private lateinit var headers: List<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = SavedVM()
        binding = FragmentSavedBinding.inflate(inflater, container, false)
        headers = listOf(
            getString(R.string.groups_header),
            getString(R.string.teachers_header),
            getString(R.string.others_header)
        )

        setupExpandableListView()
        observeViewModel()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadFavorites(requireContext())
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.favoritesState.collectLatest { state ->
                when (state) {
                    is FavoriteState.Loading -> {
                        startloading()
                    }
                    is FavoriteState.Loaded -> {
                        groupsList.clear()
                        teachersList.clear()
                        othersList.clear()
                        groupsList.addAll(state.groups)
                        teachersList.addAll(state.teachers)
                        othersList.addAll(state.others)
                        (binding.expandableListView.expandableListAdapter as? BaseExpandableListAdapter)
                            ?.notifyDataSetChanged()

                        // Раскрываем все группы
                        for (i in headers.indices) {
                            binding.expandableListView.expandGroup(i)
                        }
                        successloading()
                    }
                    is FavoriteState.Error -> {
                        errorloading(Exception(state.message))
                    }
                }
            }
        }
    }


    private fun setupExpandableListView() {

        binding.expandableListView.setAdapter(object : BaseExpandableListAdapter() {
            override fun getGroupCount(): Int = headers.size
            override fun getChildrenCount(groupPosition: Int): Int = when (groupPosition) {
                0 -> groupsList.size
                1 -> teachersList.size
                2 -> othersList.size
                else -> 0
            }

            override fun getGroup(groupPosition: Int): Any = headers[groupPosition]
            override fun getChild(groupPosition: Int, childPosition: Int): Any = when (groupPosition) {
                0 -> groupsList[childPosition]
                1 -> teachersList[childPosition]
                2 -> othersList[childPosition]
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
                    2 -> othersList[childPosition]
                    else -> ""
                }
                return binding.root
            }

            override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
                return true // Все элементы можно выбирать
            }
        })

        binding.expandableListView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            val type = when (groupPosition) {
                0 -> "group"
                1 -> "teacher"
                2 -> "other"
                else -> ""
            }
            val name = when (groupPosition) {
                0 -> groupsList[childPosition]
                1 -> teachersList[childPosition]
                2 -> othersList[childPosition]
                else -> ""
            }
            openFavoriteSchedule(name, type)
            true
        }
    }

    private fun openFavoriteSchedule(name: String, type: String) {
        Log.d("test","openfromfavorite")
        val fr = RaspisanieFragment.send(name, type)
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_cont, fr)
            .addToBackStack(null)
            .commit()
    }

    private fun startloading(){
        binding.progressSchedule.visibility = View.VISIBLE
        binding.expandableListView.visibility = View.GONE
        binding.errorTextView.visibility = View.GONE
    }

    private fun successloading(){
        binding.progressSchedule.visibility = View.GONE
        binding.errorTextView.visibility = View.GONE
        binding.expandableListView.visibility = View.VISIBLE
    }

    private fun errorloading(e: Exception) {
        Log.e("RaspisanieFragment", "error: ", e)
        binding.errorTextView.text = getString(R.string.error_saved)
        binding.progressSchedule.visibility = View.GONE
        binding.errorTextView.visibility = View.VISIBLE
        binding.expandableListView.visibility = View.GONE
    }
}