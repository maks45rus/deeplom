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
    private var rasp: MutableList<String> = mutableListOf("-", "-", "-", "-", "-", "-", "-", "-", "-", "-")
    private var currentWeekStart: LocalDate? = null // старт недели для выбранного дня
    private var currentWeekSchedule: PairsResponse? = null // Кэш расписания для текущей недели
    private val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    companion object {
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
            }
        )

        binding.pairsFor.text = when (pairsfor) {
            "teacher" -> formatName(namesearch)
            else -> namesearch.uppercase()
        }

        val currentDate = LocalDate.now()
        var selectedDate = currentDate
        currentWeekStart = getWeekStartDate(selectedDate)
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        binding.textDate.text = selectedDate.format(format)

        binding.btnSethome.setOnClickListener {
            sharedPreferences.edit {
                putString("home_type", pairsfor)
                putString("home_name", namesearch)
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

        binding.textDate.setOnClickListener {   // переключение даты по календарю
            showDatePicker { selDate ->
                if (selDate != null) {
                    selectedDate = selDate
                    binding.textDate.text = selectedDate.format(format) // Обновляем текст даты
                    loadScheduleForWeek(selectedDate, idsearch, pairsfor)
                }
            }
        }

        binding.btnPrev.setOnClickListener {   // Пролистывание на день вперед
            selectedDate = selectedDate.minusDays(1)
            loadScheduleForWeek(selectedDate, idsearch, pairsfor)
            binding.textDate.text = selectedDate.format(format) // Обновляем текст даты
        }

        binding.btnNext.setOnClickListener {   // Пролистывание на день назад
            selectedDate = selectedDate.plusDays(1)
            loadScheduleForWeek(selectedDate, idsearch, pairsfor)
            binding.textDate.text = selectedDate.format(format) // Обновляем текст даты
        }

        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                try {
                    idsearch = when (pairsfor) {
                        "group" -> db.getGroupDao().getGroupByName(namesearch).id!!
                        "teacher" -> db.getTeacherDao().getTeacherByName(namesearch).id!!
                        else -> 0
                    }
                } catch (e: Exception) {
                    Log.e("RaspisanieFragment", ":error getgr gett: ${e.message}", e)
                }
                loadScheduleForWeek(selectedDate, idsearch, pairsfor)
            }
        }

        return binding.root
    }

    /**
     * Загрузка расписания для всей недели.
     */
    private fun loadScheduleForWeek(date: LocalDate, idsearch: Int, pairsfor: String) {
        Log.d("RaspisanieFragment", (getWeekStartDate(date) == currentWeekStart &&
                currentWeekSchedule != null).toString())
        if (getWeekStartDate(date) == currentWeekStart &&
            currentWeekSchedule != null
        ) {
            // Если данные для этой недели уже загружены, используем кэш
            updateRaspisanie(currentWeekSchedule!!, date)
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                try {
                    currentWeekStart = getWeekStartDate(date)
                    val newrasp = DataManager.fetchPairs(currentWeekStart!!.format(format), 1, idsearch, pairsfor) // Запрос на неделю (week = 1)
                    if (newrasp.ok) {
                        currentWeekSchedule = newrasp // Сохраняем данные в кэш
                        updateRaspisanie(currentWeekSchedule!!, date)
                    } else {
                        Toast.makeText(requireContext(), R.string.scheduleerror, Toast.LENGTH_LONG).show()
                        for (i in rasp.indices) {
                            rasp[i] = "-"
                        }
                        rasisanieAdapter.clear()
                        rasisanieAdapter.addAll(rasp)
                        rasisanieAdapter.notifyDataSetChanged()
                        throw Exception(newrasp.error ?: "Неизвестная ошибка")
                    }
                } catch (e: Exception) {
                    Log.e("RaspisanieFragment", "Error loading schedule: ${e.message}", e)
                }
            }
        }
    }

    /**
     * Возвращает дату понедельника для заданной даты.
     */
    private fun getWeekStartDate(date: LocalDate): LocalDate {
        return date.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
    }

    /**
     * Обновление расписания для конкретного дня.
     */
    private fun updateRaspisanie(allpairs: PairsResponse, date: LocalDate) {
        if (!allpairs.ok) {
            Log.e("RaspisanieFragment", "no raspisania")
            return
        }

        for (i in rasp.indices) {
            rasp[i] = "-"
        }

        val days = allpairs.result

        for (day in days) {
            if (day.date == date.format(format)) {
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

    /**
     * Возвращает дату воскресенья для заданной даты.
     */
    private fun getWeekEndDate(date: LocalDate): LocalDate {
        return date.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))
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

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                onDateSelected(selectedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.setOnCancelListener {
            onDateSelected(null)
        }

        datePickerDialog.show()
    }

}