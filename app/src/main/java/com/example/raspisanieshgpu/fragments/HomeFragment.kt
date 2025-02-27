package com.example.raspisanieshgpu.fragments


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.raspisanieshgpu.R
import com.example.raspisanieshgpu.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)

        val homeName = sharedPreferences.getString("home_name", null)
        val homeType = sharedPreferences.getString("home_type", null)
        if (homeName==null || homeType==null) {
            binding.homeText.text = "Задайте стартовое расписание"
        } else {
            val fr = RaspisanieFragment.send(homeName.toString(), homeType.toString())
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_cont, fr)
                .commit()
        }


        return binding.root
    }
}