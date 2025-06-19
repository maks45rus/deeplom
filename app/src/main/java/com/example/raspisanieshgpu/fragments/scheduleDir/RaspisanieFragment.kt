package com.example.raspisanieshgpu.fragments.scheduleDir

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
    private lateinit var currentDate: LocalDate
    private var isLoading = false
    private var available: Boolean = true
    private var isFavorite = false
    private var namesearch = "430б"
    private var pairsfor = "group"
    private var rasp: MutableList<String> = mutableListOf("-", "-", "-", "-", "-", "-", "-", "-", "-", "-")
    private lateinit var currentWeekStart: LocalDate
    private lateinit var currentWeekSchedule: List<Date>
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
        viewModel = RaspisanieVM(requireContext())
        binding = FragmentRaspisanieBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)

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
        viewModel.loadScheduleForWeek(currentDate, namesearch, pairsfor, requireContext())
    }

    private fun initArguments() {

        namesearch = arguments?.getString(NAME_SEARCH).toString()
        pairsfor = arguments?.getString(PAIRS_FOR).toString()
        viewModel.checkFavoriteStatus(namesearch, pairsfor)
        updateHomeButton(isHomeItem(namesearch,pairsfor))
        currentDate = LocalDate.now().let { date ->
            if (date.dayOfWeek == DayOfWeek.SUNDAY) date.plusDays(1) else date
        }
        selectedDate = currentDate

        currentWeekStart = getWeekStartDate(selectedDate)
        currentWeekSchedule = emptyList()
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

        binding.btnSethome.setOnClickListener {
            addHome()
        }

        binding.textDate.setOnClickListener {
            showDatePicker { selDate ->
                selDate?.let {
                    selectedDate = it
                    changedate(selectedDate)
                    viewModel.loadScheduleForWeek(selectedDate, namesearch, pairsfor, requireContext())
                }
            }
        }

        binding.btnPrev.setOnClickListener {
            currentWeekStart = currentWeekStart.minusWeeks(1)
            selectedDate = currentWeekStart
            viewModel.loadScheduleForWeek(selectedDate, namesearch, pairsfor, requireContext())
            updateButtonState("Mon")
            changedate(selectedDate)
        }

        binding.btnNext.setOnClickListener {
            currentWeekStart = currentWeekStart.plusWeeks(1)
            selectedDate = currentWeekStart
            viewModel.loadScheduleForWeek(selectedDate, namesearch, pairsfor, requireContext())
            updateButtonState("Mon")
            changedate(selectedDate)
        }

        binding.date1.setOnClickListener {
            if(isLoading)return@setOnClickListener
            selectedDate = currentWeekStart
            updateRaspisanie(currentWeekSchedule, selectedDate)
            updateButtonState("Mon")
            changedate(selectedDate)
        }

        binding.date2.setOnClickListener {
            if(isLoading)return@setOnClickListener
            selectedDate = currentWeekStart.plusDays(1)
            updateRaspisanie(currentWeekSchedule, selectedDate)
            updateButtonState("Tue")
            changedate(selectedDate)
        }

        binding.date3.setOnClickListener {
            if(isLoading)return@setOnClickListener
            selectedDate = currentWeekStart.plusDays(2)
            updateRaspisanie(currentWeekSchedule, selectedDate)
            updateButtonState("Wed")
            changedate(selectedDate)
        }

        binding.date4.setOnClickListener {
            if(isLoading)return@setOnClickListener
            selectedDate = currentWeekStart.plusDays(3)
            updateRaspisanie(currentWeekSchedule, selectedDate)
            updateButtonState("Thu")
            changedate(selectedDate)
        }

        binding.date5.setOnClickListener {
            if(isLoading)return@setOnClickListener
            selectedDate = currentWeekStart.plusDays(4)
            updateRaspisanie(currentWeekSchedule, selectedDate)
            updateButtonState("Fri")
            changedate(selectedDate)
        }

        binding.date6.setOnClickListener {
            if(isLoading)return@setOnClickListener
            selectedDate = currentWeekStart.plusDays(5)
            updateRaspisanie(currentWeekSchedule, selectedDate)
            updateButtonState("Sat")
            changedate(selectedDate)
        }
    }

    private fun observeViewModel() {
        viewModel.scheduleState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RaspisanieState.Loading -> {
                    isLoading = true
                    startloading()
                }
                is RaspisanieState.Success -> {
                    currentWeekStart = getWeekStartDate(selectedDate)
                    currentWeekSchedule = state.schedule ?: emptyList()
                    available = state.available
                    updateRaspisanie(state.schedule, selectedDate)
                    isLoading = false
                }
                is RaspisanieState.Error -> {
                    currentWeekSchedule = emptyList()
                    available = false
                    errorloading(Exception(state.message))
                    isLoading = false
                }
            }
        }
        viewModel.favoriteState.observe(viewLifecycleOwner) { favorite ->
            isFavorite = favorite
            Log.d("FavoriteStatus", "Favorite status updated: $isFavorite")
            if(isFavorite)loadInitialData()
            updateFavoriteButton(favorite)
        }
    }




    private fun changedate(date: LocalDate) {
        binding.textDate.text = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

    }

    private fun getWeekStartDate(date: LocalDate): LocalDate {
        date.dayOfWeek
        return date.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    private fun updateRaspisanie(days: List<Date>?, date: LocalDate) {
        try {
            if(!available) {
                errorloading(Exception("not available"))
                return
            }
            // Очищаем только 5 пар
            rasp = MutableList(5) { "-" }

            if (!days.isNullOrEmpty()) {
                for (day in days) {
                    if (day.date == date.format(format)) {
                        day.pairs.forEach { para ->
                            if (para.num - 1 < 5) {
                                rasp[para.num - 1] = para.text
                            }
                        }
                        break
                    }
                }
            }

            rasisanieAdapter.clear()
            rasisanieAdapter.addAll(rasp)
            rasisanieAdapter.notifyDataSetChanged()
            successloading()
        } catch (e: Exception) {
            errorloading(e)
        }
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
            if (fav){
                viewModel.loadScheduleForWeek(selectedDate, namesearch, pairsfor, requireContext())
                R.drawable.baseline_star_24_selected
            }
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
        updateHomeButton(isHomeItem(namesearch,pairsfor))
    }

    private fun updateHomeButton(isHome: Boolean) {
        binding.btnSethome.setImageResource(
            if (isHome) R.drawable.baseline_home_selected
            else R.drawable.baseline_home_unselected
        )
    }

    fun isHomeItem(name: String, type: String): Boolean {
        val homeName = sharedPreferences.getString("home_name", "")
        val homeType = sharedPreferences.getString("home_type", "")
        return name == homeName && type == homeType
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
        binding.errorTextView.text = getString(R.string.schedule_not_available)
        binding.progressSchedule.visibility = View.GONE
        binding.errorTextView.visibility = View.VISIBLE
        binding.raspisanieList.visibility = View.GONE
    }
}