package com.example.raspisanieshgpu.fragments


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.raspisanieshgpu.R
import com.example.raspisanieshgpu.databinding.FragmentHomeBinding
import com.example.raspisanieshgpu.fragments.scheduleDir.RaspisanieFragment
import com.example.raspisanieshgpu.fragments.favoriteDir.SavedFragment

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val savedFragment = SavedFragment()

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
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_cont, savedFragment)
                .commit()
        } else {
            val fr = RaspisanieFragment.send(homeName.toString(), homeType.toString())
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_cont, fr)
                .commit()
        }


        return binding.root
    }

}