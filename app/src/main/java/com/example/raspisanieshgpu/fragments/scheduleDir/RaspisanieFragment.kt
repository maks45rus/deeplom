package com.example.raspisanieshgpu.fragments.scheduleDir

import android.app.DatePickerDialog
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
import com.example.raspisanieshgpu.R
import com.example.raspisanieshgpu.adapter.PairsAdapter
import com.example.raspisanieshgpu.api.models.Date
import com.example.raspisanieshgpu.databinding.FragmentRaspisanieBinding
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RaspisanieFragment : Fragment() {

    private lateinit var viewModel: RaspisanieVM
    private lateinit var binding: FragmentRaspisanieBinding
    private lateinit var rasisanieAdapter: ArrayAdapter<String>
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var selectedDate: LocalDate
    private var isFavorite = false
    private var namesearch = "430б"
    private var pairsfor = "group"
    private var rasp: MutableList<String> = mutableListOf("-", "-", "-", "-", "-", "-", "-", "-", "-", "-")
    private var currentWeekStart: LocalDate? = null // старт недели для выбранного дня
    private var currentWeekSchedule: List<Date>? = null // Кэш расписания для текущей недели
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
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initArguments()
        setupAdapter()
        setupUI()
        setupClickListeners()
        observeViewModel()
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModel.checkFavoriteStatus(namesearch, pairsfor)
        viewModel.loadScheduleForWeek(selectedDate, namesearch, pairsfor, requireContext(),isFavorite)
    }

    private fun initArguments() {
        namesearch = arguments?.getString("NAME_SEARCH") ?: "430б"
        pairsfor = arguments?.getString("PAIRS_FOR") ?: "group"
        selectedDate = LocalDate.now().let { date ->
            if (date.dayOfWeek == DayOfWeek.SUNDAY) date.plusDays(1) else date
        }
    }

    private fun setupAdapter() {
        rasisanieAdapter = PairsAdapter(requireContext())
        binding.raspisanieList.adapter = rasisanieAdapter
    }

    private fun setupUI() {
        binding.pairsFor.text = when (pairsfor) {
            "teacher" -> formatName(namesearch)
            else -> namesearch.uppercase()
        }

        currentWeekStart = getWeekStartDate(selectedDate)
        changedate(selectedDate)
        updateButtonState(getDayOfWeekString(selectedDate.dayOfWeek))
    }

    private fun setupClickListeners() {
        binding.btnFavorite.setOnClickListener {
            viewModel.toggleFavorite(namesearch, pairsfor)
        }

        binding.btnFavorite.setOnClickListener {
            viewModel.toggleFavorite(namesearch,pairsfor)
        }

        binding.btnSethome.setOnClickListener {
            addHome()
        }

        binding.textDate.setOnClickListener {   // переключение даты по календарю
            showDatePicker { selDate ->
                if (selDate != null) {
                    selectedDate = selDate
                    changedate(selectedDate) // Обновляем текст даты
                    viewModel.loadScheduleForWeek(selectedDate, namesearch, pairsfor, requireContext(), isFavorite)
                }
            }
        }

        binding.btnPrev.setOnClickListener {   // Пролистывание на неделю назад
            selectedDate = currentWeekStart!!.minusDays(2)
            viewModel.loadScheduleForWeek(selectedDate, namesearch, pairsfor, requireContext(), isFavorite)
            updateButtonState("Sat")
            changedate(selectedDate) // Обновляем текст даты
        }

        binding.btnNext.setOnClickListener {   // Пролистывание на неделю вперед
            selectedDate = currentWeekStart!!.plusDays(7)
            viewModel.loadScheduleForWeek(selectedDate, namesearch, pairsfor, requireContext(), isFavorite)
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

        // Остальные слушатели...
    }

    private fun observeViewModel() {
        viewModel.scheduleState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RaspisanieState.Loading -> startloading()
                is RaspisanieState.Success -> {
                    updateRaspisanie(state.schedule,selectedDate)
                    currentWeekSchedule = state.schedule
                }
                is RaspisanieState.Error -> errorloading(Exception(state.message))
            }
        }
        viewModel.favoriteState.observe(viewLifecycleOwner) { _isFavorite ->
            isFavorite = _isFavorite
            updateFavoriteButton(_isFavorite)
        }
    }




    private fun changedate(date: LocalDate) {
        binding.textDate.text = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

    }

    private fun getWeekStartDate(date: LocalDate): LocalDate {
        return date.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    private fun updateRaspisanie(days: List<Date>, date: LocalDate) {

        // Очищаем только 5 пар
        rasp = MutableList(5) { "-" }

        for (day in days) {
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
        successloading()
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

    private fun updateFavoriteButton(fav: Boolean) {
        binding.btnFavorite.setImageResource(
            if (fav) R.drawable.baseline_star_24_selected
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


    private fun addHome() {
        sharedPreferences.edit {
            putString("home_type", pairsfor)
            putString("home_name", namesearch)
        }
        Toast.makeText(
            requireContext(),
            "Домашняя группа/преподаватель сохранена",
            Toast.LENGTH_SHORT
        ).show()
        binding.btnSethome.setImageResource(R.drawable.baseline_home_selected)
    }

    private fun startloading(){
        binding.progressSchedule.visibility = View.VISIBLE
        binding.raspisanieList.visibility = View.GONE
        binding.errorTextView.visibility = View.GONE
    }

    private fun successloading(){
        binding.progressSchedule.visibility = View.GONE
        binding.errorTextView.visibility = View.GONE
        binding.raspisanieList.visibility = View.VISIBLE
    }

    private fun errorloading(e: Exception) {
        Log.e("RaspisanieFragment", "error: ", e)
        binding.errorTextView.text = getString(R.string.error_schedule)
        binding.progressSchedule.visibility = View.GONE
        binding.errorTextView.visibility = View.VISIBLE
        binding.raspisanieList.visibility = View.GONE
    }
}