package com.example.raspisanieshgpu.fragments.searchDir

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.raspisanieshgpu.R
import com.example.raspisanieshgpu.adapter.SearchAdapter
import com.example.raspisanieshgpu.databinding.FragmentSearchBinding
import com.example.raspisanieshgpu.fragments.scheduleDir.RaspisanieFragment

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var viewModel: SearchVM
    private lateinit var searchAdapter: SearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        viewModel = SearchVM(requireContext())

        setupAdapter()
        setupChips()
        setupSearchInput()
        observeViewModel()

        // Загружаем начальные данные
        viewModel.setType("group")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupChips()
        setupSearchInput()
        viewModel.setType("group")
    }

    private fun setupAdapter() {
        searchAdapter = SearchAdapter(requireContext()) { selectedItem ->
            openScheduleFragment(selectedItem)
        }
        binding.searchList.adapter = searchAdapter
    }

    private fun setupChips() {
        binding.chipGroup.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.chipTeacher.isChecked = false
                viewModel.setType("group")
            }
        }

        binding.chipTeacher.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.chipGroup.isChecked = false
                viewModel.setType("teacher")
            }
        }

        // Выбираем чип по умолчанию
        binding.chipGroup.isChecked = true
    }

    private fun setupSearchInput() {
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s?.toString()?.trim() ?: "")
            }
        })
    }

    private fun observeViewModel() {
        viewModel.searchState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SearchState.Loading -> {
                    startloading()
                }
                is SearchState.Success -> {
                    searchAdapter.clear()
                    searchAdapter.addAll(state.items)
                    searchAdapter.notifyDataSetChanged()
                    successloading()
                }
                is SearchState.Error -> {
                    errorloading(Exception(state.message))
                }
            }
        }
    }

    private fun openScheduleFragment(name: String) {
        val fr = RaspisanieFragment.send(name, viewModel.currentType)
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_cont, fr)
            .addToBackStack(null)
            .commit()
    }

    private fun startloading(){
        binding.progressBar.visibility = View.VISIBLE
        binding.searchList.visibility = View.GONE
        binding.errorTextView.visibility = View.GONE
    }

    private fun successloading(){
        binding.progressBar.visibility = View.GONE
        binding.errorTextView.visibility = View.GONE
        binding.searchList.visibility = View.VISIBLE
    }

    private fun errorloading(e: Exception) {
        Log.e("RaspisanieFragment", "error: ", e)
        binding.errorTextView.text = getString(R.string.error_schedule)
        binding.progressBar.visibility = View.GONE
        binding.errorTextView.visibility = View.VISIBLE
        binding.searchList.visibility = View.GONE
    }
}