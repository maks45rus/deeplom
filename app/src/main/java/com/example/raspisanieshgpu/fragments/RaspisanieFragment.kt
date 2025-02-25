package com.example.raspisanieshgpu.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.raspisanieshgpu.DataBase.databaseobj
import com.example.raspisanieshgpu.R
import com.example.raspisanieshgpu.adapter.PairsAdapter
import com.example.raspisanieshgpu.api.DataManager
import com.example.raspisanieshgpu.api.models.PairsResponse
import com.example.raspisanieshgpu.databinding.FragmentRaspisanieBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RaspisanieFragment : Fragment() {

    private lateinit var binding: FragmentRaspisanieBinding
    private lateinit var rasisanieAdapter: ArrayAdapter<String>
    private lateinit var sharedPreferences: SharedPreferences
    private var rasp: MutableList<String> = mutableListOf("-","-","-","-","-","-","-","-","-","-")

    companion object{
        private const val NAME_SEARCH = "430б"
        private const val PAIRS_FOR = "group"
        fun send(nsearch: String, pfor: String): RaspisanieFragment {
            val fr = RaspisanieFragment()
            val args = Bundle()
            args.putString(NAME_SEARCH, nsearch)
            args.putString(PAIRS_FOR, pfor)
            fr.arguments = args
            return fr
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentRaspisanieBinding.inflate(inflater, container, false)
        rasisanieAdapter = PairsAdapter(requireContext(), R.layout.item_list)
        binding.raspisanieList.adapter = rasisanieAdapter

        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)

        val namesearch = arguments?.getString(NAME_SEARCH).toString()
        val pairsfor = arguments?.getString(PAIRS_FOR).toString()
        var idsearch = 0

        val db = databaseobj.database




        var homeName = sharedPreferences.getString("home_name", null)
        binding.btnSethome.setImageResource(
            when (homeName) {
              namesearch -> R.drawable.baseline_home_selected
              else -> R.drawable.baseline_home_unselected
            })

        binding.pairsFor.text = when (pairsfor) {
            "teacher" -> {formatName(namesearch) // ФИО в формат Фамилия И. О.
            }
            else -> namesearch.uppercase()
        }

        val currentDate = LocalDate.now()    // Текущая дата
        var selectedDate = currentDate
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")


        binding.btnSethome.setOnClickListener {
            sharedPreferences.edit {
                putString("home_type", pairsfor) // Тип: "group" или "teacher"
                putString("home_name", namesearch) // Название группы или преподавателя
            }
            Toast.makeText(requireContext(), "Домашняя группа/преподаватель сохранена", Toast.LENGTH_SHORT).show()
            homeName = sharedPreferences.getString("home_name", null)
            binding.btnSethome.setImageResource(
                when (homeName) {
                    namesearch -> R.drawable.baseline_home_selected
                    else -> R.drawable.baseline_home_unselected
                }
            )
        }


        binding.textDate.setOnClickListener {
            showDatePicker { selectedDate ->
                if (selectedDate != null) {
                    // Обновляем текстовое поле с датой
                    binding.textDate.text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                    // Загружаем расписание для выбранной даты
                    loadScheduleForDay(selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), idsearch, pairsfor)
                } else {
                    // Пользователь отменил выбор даты
                    Toast.makeText(requireContext(), "Выбор даты отменен", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnPrev.setOnClickListener {
            selectedDate = selectedDate.minusDays(1) // Переключение на предыдущий день
            loadScheduleForDay(selectedDate.format(format), idsearch, pairsfor)
        }

        binding.btnNext.setOnClickListener {
            selectedDate = selectedDate.plusDays(1) // Переключение на следующий день
            loadScheduleForDay(selectedDate.format(format), idsearch, pairsfor)
        }


        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                try {
                    idsearch = when (pairsfor) {
                        "group" -> db.getGroupDao().getGroupByName(namesearch).id!!
                        "teacher" -> db.getTeacherDao().getTeacherByName(namesearch).id!!
                        else -> 0
                    }
                    Log.d("RaspisanieFragment", "ID search: $idsearch")

                    // Загрузка расписания для текущего дня
                    loadScheduleForDay(selectedDate.format(format), idsearch, pairsfor)

                } catch (e: Exception) {
                    rasisanieAdapter.addAll(rasp)
                    rasisanieAdapter.notifyDataSetChanged()
                    Log.e("RaspisanieFragment", ":err add: ${e.message}", e)
                }
            }
        }

        return binding.root
    }



    private fun loadScheduleForDay(date: String, idsearch: Int, pairsfor: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                try {
                    binding.textDate.text = date // Обновляем текст даты
                    val newrasp = DataManager.fetchPairs(date, 0, idsearch, pairsfor)
                    if (newrasp.result.isNotEmpty()) {
                        updateRaspisanie(newrasp,date)
                    } else {
                        Toast.makeText(requireContext(), R.string.scheduleerror, Toast.LENGTH_LONG).show()
                        for (i in rasp.indices) {
                            rasp[i] = "-"
                        }
                        rasisanieAdapter.clear()
                        rasisanieAdapter.addAll(rasp)
                        rasisanieAdapter.notifyDataSetChanged()
                    }
                } catch (e: Exception) {
                    Log.e("RaspisanieFragment", "Error loading schedule: ${e.message}", e)
                    when (e) {
                        is java.net.UnknownHostException -> {
                            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Toast.makeText(requireContext(), "Failed to load schedule: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateRaspisanie(allpairs: PairsResponse, date: String) {
        if(!allpairs.ok){
            Log.e("RaspisanieFragment", "no raspisania")
            return
        }

        for (i in rasp.indices) {
            rasp[i] = "-"
        }

        val days = allpairs.result

        for (day in days) {
            if (day.date == date) {
                day.pairs.forEach { para ->
                    rasp[para.num - 1] = para.text
                }
                break
            }
        }

        rasisanieAdapter.clear()
        rasisanieAdapter.addAll(rasp)
        rasisanieAdapter.notifyDataSetChanged()
    }

    private fun formatName(fullName: String): String {
        val parts = fullName.split(" ")
        if (parts.size < 2) return fullName
        return "${parts[0]} ${parts.subList(1, parts.size).joinToString(" ") { it.take(1) + "." }}"
    }

    private fun showDatePicker(onDateSelected: (LocalDate?) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Создаем DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                // Создаем объект LocalDate из выбранной даты
                val selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                onDateSelected(selectedDate) // Вызываем колбэк с выбранным значением
            },
            year,
            month,
            day
        )

        // Устанавливаем обработчик отмены диалога
        datePickerDialog.setOnCancelListener {
            // Если диалог был закрыт без выбора даты, вызываем колбэк с null
            onDateSelected(null)
        }

        // Показываем диалог
        datePickerDialog.show()
    }

}