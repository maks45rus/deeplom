package com.example.raspisanieshgpu.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.example.raspisanieshgpu.DataBase.databaseobj
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.raspisanieshgpu.R
import com.example.raspisanieshgpu.databinding.FragmentSavedBinding
import kotlinx.coroutines.launch

class SavedFragment: Fragment() {

    private lateinit var binding: FragmentSavedBinding
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSavedBinding.inflate(inflater, container, false)

        // Настройка списка
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1)
        binding.savedList.adapter = adapter

        // Загрузка данных
        loadFavorites()

        // Обработчик клика по элементу списка
        binding.savedList.setOnItemClickListener { _, _, position, _ ->
            val item = adapter.getItem(position) ?: return@setOnItemClickListener
            openFavoriteSchedule(item)
        }

        return binding.root
    }

    private fun loadFavorites() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Получаем избранные группы и преподавателей
            val favoriteGroups = databaseobj.database.getGroupDao().getFavorites()
            val favoriteTeachers = databaseobj.database.getTeacherDao().getFavorites()

            // Объединяем и сортируем
            val allFavorites = (favoriteGroups.map { it.name to "group" } +
                    favoriteTeachers.map { it.name to "teacher" })
                .sortedBy { it.first }

            // Обновляем адаптер
            adapter.clear()
            adapter.addAll(allFavorites.map { "${it.first} (${if (it.second == "group") "Группа" else "Преподаватель"})" })
            adapter.notifyDataSetChanged()
        }
    }

    private fun openFavoriteSchedule(fullItemName: String) {
        val regex = """(.+)\s\((Группа|Преподаватель)\)""".toRegex()
        val matchResult = regex.find(fullItemName) ?: return

        val name = matchResult.groupValues[1]
        val type = when(matchResult.groupValues[2]) {
            "Группа" -> "group"
            "Преподаватель" -> "teacher"
            else -> return
        }

        val fr = RaspisanieFragment.send(name, type)
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_cont, fr)
            .addToBackStack(null)
            .commit()
    }


}