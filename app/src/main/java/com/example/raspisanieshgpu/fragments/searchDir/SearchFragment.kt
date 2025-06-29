package com.example.raspisanieshgpu.fragments.searchDir

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.raspisanieshgpu.R
import com.example.raspisanieshgpu.adapter.SearchAdapter
import com.example.raspisanieshgpu.databinding.FragmentSearchBinding
import com.example.raspisanieshgpu.fragments.scheduleDir.RaspisanieFragment
import com.google.android.material.button.MaterialButton

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

        viewModel.setType("group")


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        setupButtons()
        setupSearchInput()
        observeViewModel()




    }

    private fun setupAdapter() {
        searchAdapter = SearchAdapter(requireContext()) { selectedItem ->
            openScheduleFragment(selectedItem)
        }
        binding.searchList.adapter = searchAdapter
    }

    private fun setupButtons() {

        binding.searchButton.setOnClickListener {
            openScheduleFragment(binding.searchInput.text.toString())
        }

        binding.btnGroup.setOnClickListener {
            viewModel.setType("group")
        }

        binding.btnTeacher.setOnClickListener {
            viewModel.setType("teacher")
        }


        binding.freeSearchCheckbox.setOnCheckedChangeListener {b, check ->
            if(check){
                viewModel.setType("")
                binding.searchButton.visibility = android.view.View.VISIBLE
                binding.searchList.visibility = android.view.View.GONE
            }else{
                binding.searchButton.visibility = android.view.View.GONE
                binding.searchList.visibility = android.view.View.VISIBLE
            }
        }
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
        viewModel.currentType.observe(viewLifecycleOwner) { type ->
            updateButtonSelection(type)
            viewModel.search(binding.searchInput.text?.toString()?.trim() ?: "")
        }
    }

    private fun updateButtonSelection(type: String) {

        binding.searchButton.visibility = android.view.View.GONE
        binding.searchList.visibility = android.view.View.VISIBLE

        when (type) {
            "group" -> {
                binding.freeSearchCheckbox.isChecked = false
                setButtonSelected(binding.btnGroup, true)
                setButtonSelected(binding.btnTeacher, false)
            }
            "teacher" -> {
                binding.freeSearchCheckbox.isChecked = false
                setButtonSelected(binding.btnGroup, false)
                setButtonSelected(binding.btnTeacher, true)
            }
            else -> {
                binding.searchButton.visibility = android.view.View.VISIBLE
                binding.searchList.visibility = android.view.View.GONE
                setButtonSelected(binding.btnGroup, false)
                setButtonSelected(binding.btnTeacher, false)
            }
        }
    }

    private fun setButtonSelected(button: MaterialButton, isSelected: Boolean) {
        button.isSelected = isSelected
        if (isSelected) {
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green_light))
            button.setIconTintResource(android.R.color.white)
            button.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        } else {
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            button.setIconTintResource(R.color.green_light)
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_light))
        }
    }

    private fun openScheduleFragment(name: String) {
        val fr = RaspisanieFragment.send(name, viewModel.currentType.value!!)
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
        updateButtonSelection(viewModel.currentType.value!!)
    }

    private fun errorloading(e: Exception) {
        Log.e("RaspisanieFragment", "error: ", e)
        binding.errorTextView.text = getString(R.string.error_search)
        binding.progressBar.visibility = View.GONE
        binding.errorTextView.visibility = View.VISIBLE
        binding.searchList.visibility = View.GONE
    }
}