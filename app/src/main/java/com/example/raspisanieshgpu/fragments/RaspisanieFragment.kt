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
import com.example.raspisanieshgpu.DataBase.CachedSchedule
import com.example.raspisanieshgpu.DataBase.databaseobj
import com.example.raspisanieshgpu.R
import com.example.raspisanieshgpu.adapter.PairsAdapter
import com.example.raspisanieshgpu.api.DataManager
import com.example.raspisanieshgpu.api.models.PairsResponse
import com.example.raspisanieshgpu.databinding.FragmentRaspisanieBinding
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RaspisanieFragment : Fragment() {

    private lateinit var binding: FragmentRaspisanieBinding
    private lateinit var rasisanieAdapter: ArrayAdapter<String>
    private lateinit var sharedPreferences: SharedPreferences
    private var isFavorite = false
    private var namesearch = ""
    private var pairsfor = ""
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
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)

        val db = databaseobj.database
        namesearch = arguments?.getString(NAME_SEARCH).toString()
        pairsfor = arguments?.getString(PAIRS_FOR).toString()
        var idsearch = 0

        rasisanieAdapter = PairsAdapter(requireContext())
        binding.raspisanieList.apply {
            adapter = rasisanieAdapter
        }

        binding.pairsFor.text = when (pairsfor) { // надпись для кого пара
            "teacher" -> formatName(namesearch)
            else -> namesearch.uppercase()
        }

        val homeName = sharedPreferences.getString("home_name", null)
        binding.btnSethome.setImageResource(
            when (homeName) {
                namesearch -> R.drawable.baseline_home_selected
                else -> R.drawable.baseline_home_unselected
            }
        )

        var selectedDate = LocalDate.now()
        if(selectedDate.dayOfWeek == DayOfWeek.SUNDAY){
            selectedDate = selectedDate.plusDays(1)
        }
        currentWeekStart = getWeekStartDate(selectedDate)
        changedate(selectedDate)
        updateButtonState(getDayOfWeekString(selectedDate.dayOfWeek).toString())


        viewLifecycleOwner.lifecycleScope.launch {  // в избранном ли
            withContext(Dispatchers.Main) {
                try {
                    idsearch = when (pairsfor) {
                        "group" -> {
                            isFavorite = databaseobj.database.getGroupDao().getGroupByName(namesearch).isFavorite
                            db.getGroupDao().getGroupByName(namesearch).id!!
                        }
                        "teacher" -> {
                            isFavorite = databaseobj.database.getTeacherDao().getTeacherByName(namesearch).isFavorite
                            db.getTeacherDao().getTeacherByName(namesearch).id!!
                        }
                        else -> 0
                    }
                    updateFavoriteButton()
                } catch (e: Exception) {
                    Log.e("RaspisanieFragment", "error: ${e.message}", e)
                }
                loadScheduleForWeek(selectedDate, idsearch)
            }
        }


        // Обработчик клика по кнопке избранного
        binding.btnFavorite.setOnClickListener {
            addFavorite()
        }

        binding.btnSethome.setOnClickListener {
            addHome()
        }

        binding.textDate.setOnClickListener {   // переключение даты по календарю
            showDatePicker { selDate ->
                if (selDate != null) {
                    selectedDate = selDate
                    changedate(selectedDate) // Обновляем текст даты
                    loadScheduleForWeek(selectedDate, idsearch)
                }
            }
        }

        binding.btnPrev.setOnClickListener {   // Пролистывание на неделю назад
            selectedDate = currentWeekStart!!.minusDays(2)
            loadScheduleForWeek(selectedDate, idsearch)
            updateButtonState("Sat")
            changedate(selectedDate) // Обновляем текст даты
        }

        binding.btnNext.setOnClickListener {   // Пролистывание на неделю вперед
            selectedDate = currentWeekStart!!.plusDays(7)
            loadScheduleForWeek(selectedDate, idsearch)
            updateButtonState("Mon")
            changedate(selectedDate) // Обновляем текст даты
        }


        binding.date1.setOnClickListener {
            selectedDate = currentWeekStart!! // Понедельник
            updateRaspisanie(currentWeekSchedule!!, selectedDate)
            updateButtonState("Mon") // Обновляем состояние кнопки
            changedate(selectedDate)
        }

        binding.date2.setOnClickListener {
            selectedDate = currentWeekStart!!.plusDays(1) // Вторник
            updateRaspisanie(currentWeekSchedule!!, selectedDate)
            updateButtonState("Tue") // Обновляем состояние кнопки
            changedate(selectedDate)
        }

        binding.date3.setOnClickListener {
            selectedDate = currentWeekStart!!.plusDays(2) // Среда
            updateRaspisanie(currentWeekSchedule!!, selectedDate)
            updateButtonState("Wed") // Обновляем состояние кнопки
            changedate(selectedDate)
        }

        binding.date4.setOnClickListener {
            selectedDate = currentWeekStart!!.plusDays(3) // Четверг
            updateRaspisanie(currentWeekSchedule!!, selectedDate)
            updateButtonState("Thu") // Обновляем состояние кнопки
            changedate(selectedDate)
        }

        binding.date5.setOnClickListener {
            selectedDate = currentWeekStart!!.plusDays(4) // Пятница
            updateRaspisanie(currentWeekSchedule!!, selectedDate)
            updateButtonState("Fri") // Обновляем состояние кнопки
            changedate(selectedDate)
        }

        binding.date6.setOnClickListener {
            selectedDate = currentWeekStart!!.plusDays(5) // Суббота
            updateRaspisanie(currentWeekSchedule!!, selectedDate)
            updateButtonState("Sat") // Обновляем состояние кнопки
            changedate(selectedDate)
        }

        return binding.root
    }



    private fun loadScheduleForWeek(date: LocalDate, idsearch: Int) {

        // Показываем индикатор загрузки
        startloading()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                currentWeekStart = getWeekStartDate(date)
                val newrasp = DataManager.fetchPairs(currentWeekStart!!.format(format),
                    1, idsearch, pairsfor)

                if (newrasp.ok) {
                    currentWeekSchedule = newrasp
                    updateRaspisanie(currentWeekSchedule!!, date)

                    if (isFavoriteItem()) {
                        val scheduleJson = convertScheduleToJson(newrasp)
                        val cachedSchedule = CachedSchedule(
                            entityId = idsearch,
                            entityType = pairsfor,
                            weekStartDate = currentWeekStart!!.format(format),
                            scheduleData = scheduleJson
                        )
                        databaseobj.database.getCachedScheduleDao().insert(cachedSchedule)
                    }
                } else {
                    Log.e("RaspisanieFragment", newrasp.error.toString())
                    cahchedSchedule(idsearch, currentWeekStart!!)
                }
            } catch (e: Exception) {
                Log.e("RaspisanieFragment", "Network error", e)
                cahchedSchedule(idsearch, currentWeekStart!!)
            } finally {
                // Всегда скрываем индикатор загрузки
                binding.progressSchedule.visibility = View.GONE
            }
        }
    }

    private suspend fun cahchedSchedule(id: Int, weekStart: LocalDate) {
        // Пробуем загрузить из кэша
        viewLifecycleOwner.lifecycleScope.launch {
        try {
            val cached = databaseobj.database.getCachedScheduleDao()
                .getSchedule(id, pairsfor, weekStart.format(format))

            if (cached != null) {

                val cachedSchedule = parseCachedSchedule(cached.scheduleData)
                currentWeekSchedule = cachedSchedule
                updateRaspisanie(cachedSchedule, weekStart)
                Toast.makeText(
                    requireContext(),
                    "Используются кэшированные данные",
                    Toast.LENGTH_SHORT
                ).show()
            } else showError(getString(R.string.error_schedule))
        } catch (e: Exception) {
            showError(getString(R.string.error_schedule))
        }
            }
    }

    private fun showError(e: String) {
        Log.e("RaspisanieFragment", e)
        binding.errorTextView.text = getString(R.string.error_schedule)
        binding.progressSchedule.visibility = View.GONE
        binding.errorTextView.visibility = View.VISIBLE
        binding.raspisanieList.visibility = View.GONE
    }

    private suspend fun isFavoriteItem(): Boolean {
        return when (pairsfor) {
            "group" -> databaseobj.database.getGroupDao().getGroupByName(namesearch).isFavorite ?: false
            "teacher" -> databaseobj.database.getTeacherDao().getTeacherByName(namesearch).isFavorite ?: false
            else -> false
        }
    }
    private fun convertScheduleToJson(schedule: PairsResponse): String {
        return Gson().toJson(schedule)
    }
    private fun parseCachedSchedule(json: String): PairsResponse {
        return Gson().fromJson(json, PairsResponse::class.java)
    }

    private fun changedate(date: LocalDate) {
        binding.textDate.text = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

    }

    private fun getWeekStartDate(date: LocalDate): LocalDate {
        return date.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
    }

    private fun updateRaspisanie(allpairs: PairsResponse, date: LocalDate) {
        if (!allpairs.ok) {
            Log.e("RaspisanieFragment", "no raspisania")
            return
        }

        // Очищаем только 5 пар
        rasp = MutableList(5) { "-" }

        val days = allpairs.result

        for (day in days!!) {
            if (day.date == date.format(format)) {
                day.pairs.forEach { para ->
                    // Убедимся, что номер пары не превышает 5
                    if (para.num - 1 < 5) {
                        rasp[para.num - 1] = para.text
                    }
                }
                break
            }
        }

        rasisanieAdapter.clear()
        rasisanieAdapter.addAll(rasp)
        rasisanieAdapter.notifyDataSetChanged()
        showSchedule()
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

    private fun updateButtonState(selectedDay: String) {
        // Список дней недели и соответствующих TextView
        val daysMap = mapOf(
            "Mon" to binding.date1,
            "Tue" to binding.date2,
            "Wed" to binding.date3,
            "Thu" to binding.date4,
            "Fri" to binding.date5,
            "Sat" to binding.date6
        )

        // Обновляем состояние для каждого TextView
        daysMap.forEach { (day, textView) ->
            textView.isSelected = day == selectedDay
        }
    }

    private fun updateFavoriteButton() {
        binding.btnFavorite.setImageResource(
            if (isFavorite) R.drawable.baseline_star_24_selected
            else R.drawable.baseline_star_24_unselected
        )
    }

    private fun getDayOfWeekString(dayOfWeek: DayOfWeek): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY -> "Mon"
            DayOfWeek.TUESDAY -> "Tue"
            DayOfWeek.WEDNESDAY -> "Wed"
            DayOfWeek.THURSDAY -> "Thu"
            DayOfWeek.FRIDAY -> "Fri"
            DayOfWeek.SATURDAY -> "Sat"
            else -> "Mon" // По умолчанию (например, для воскресенья)
        }
    }


    private fun addFavorite(){
        viewLifecycleOwner.lifecycleScope.launch {
            try{
                when (pairsfor) {
                    "group" -> {
                        val group = databaseobj.database.getGroupDao()
                            .getGroupByName(namesearch)
                        databaseobj.database.getGroupDao()
                            .setFavoriteStatus(group.id, !isFavorite)
                    }

                    "teacher" -> {
                        val teacher = databaseobj.database.getTeacherDao().getTeacherByName(namesearch)
                        databaseobj.database.getTeacherDao()
                            .setFavoriteStatus(teacher.id, !isFavorite)
                    }
                }
            }finally{
                isFavorite = !isFavorite
                updateFavoriteButton()
                if (isFavorite && sharedPreferences.getString("home_name", null) == null) {
                    addHome()
                }
            }
        }
    }


    private fun addHome() {
        sharedPreferences.edit {
            putString("home_type", pairsfor)
            putString("home_name", namesearch)
        }
        Toast.makeText(requireContext(), "Домашняя группа/преподаватель сохранена", Toast.LENGTH_SHORT).show()
        val homeName = sharedPreferences.getString("home_name", null)
        binding.btnSethome.setImageResource(
            when (homeName) {
                namesearch -> R.drawable.baseline_home_selected
                else -> R.drawable.baseline_home_unselected
            }
        )
    }

    private fun startloading(){
        binding.progressSchedule.visibility = View.VISIBLE
        binding.raspisanieList.visibility = View.GONE
        binding.errorTextView.visibility = View.GONE
    }

    private fun showSchedule(){
        binding.raspisanieList.visibility = View.GONE
        binding.errorTextView.visibility = View.GONE
        binding.raspisanieList.visibility = View.VISIBLE
    }
}