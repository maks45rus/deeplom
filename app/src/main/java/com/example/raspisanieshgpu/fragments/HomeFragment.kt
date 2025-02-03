package com.example.raspisanieshgpu.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.raspisanieshgpu.R
import com.example.raspisanieshgpu.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Проверяем, есть ли сохраненный фрагмент в Back Stack
        val fragmentManager = parentFragmentManager
        val savedFragment = fragmentManager.findFragmentByTag("lastRasp")

        if (savedFragment != null) {
            // Если фрагмент найден, отображаем его
            fragmentManager.beginTransaction()
                .replace(R.id.main_cont, savedFragment, "lastRasp")
                .commit()
        } else {
            // Иначе отображаем стандартный контент для HomeFragment
            binding.homeText.text = "последнее расписание"
        }

        return binding.root
    }
}