package com.example.raspisanieshgpu.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
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
import java.time.temporal.TemporalAdjusters

class RaspisanieFragment : Fragment() {

    private lateinit var binding: FragmentRaspisanieBinding
    private lateinit var rasisanieAdapter: ArrayAdapter<String>
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


        val namesearch = arguments?.getString(NAME_SEARCH).toString()
        val pairsfor = arguments?.getString(PAIRS_FOR).toString()
        var idsearch = 0

        val db = databaseobj.database


        binding.pairsFor.text = when (pairsfor) {
            "teacher" -> {formatName(namesearch) // ФИО в формат Фамилия И. О.
            }
            else -> namesearch
        }

        var currentDay = LocalDate.now() // Текущая дата
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        binding.btnPrev.setOnClickListener {
            currentDay = currentDay.minusDays(1) // Переключение на предыдущий день
            loadScheduleForDay(currentDay.format(format), idsearch, pairsfor)
        }

        binding.btnNext.setOnClickListener {
            currentDay = currentDay.plusDays(1) // Переключение на следующий день
            loadScheduleForDay(currentDay.format(format), idsearch, pairsfor)
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
                    loadScheduleForDay(currentDay.format(format), idsearch, pairsfor)

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
                    val newrasp = DataManager.fetchPairs(date, 1, idsearch, pairsfor)
                    if (newrasp.result.isNotEmpty()) {
                        updateRaspisanie(newrasp,date)
                        binding.textDate.text = date // Обновляем текст даты
                    } else {
                        Toast.makeText(requireContext(), "No schedule for this day", Toast.LENGTH_LONG).show()
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
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")

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


}