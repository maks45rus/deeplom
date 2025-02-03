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

        var namesearch = arguments?.getString(NAME_SEARCH).toString()
        val pairsfor = arguments?.getString(PAIRS_FOR).toString()


        val db = databaseobj.database

        if(pairsfor == "teacher"){
            val parts = namesearch.split(" ")
            var formatedname = parts[0] + " "
            for (i in 1 until parts.size) {
                if (parts[i].isNotEmpty()) {
                    formatedname += ("${parts[i][0]}. ") // Добавляем первую букву с точкой
                }
            }
            namesearch = formatedname
        }

        binding.textDate.text = namesearch

       viewLifecycleOwner.lifecycleScope.launch {
           withContext(Dispatchers.Main) {
               try {

                   val idsearch = when(pairsfor) {
                       "group" -> db.getGroupDao().getGroupByName(namesearch).id!!
                       "teacher" -> db.getTeacherDao().getTeacherByName(namesearch).id!!
                       else -> 0
                   }
                   val now = LocalDate.now()
                   val monday = now.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                   val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                   val startweek = monday.format(format)

                   val newrasp = DataManager.fetchPairs(startweek, 1, idsearch, pairsfor)

                   if (newrasp.result.isNotEmpty()) {
                       updateRaspisanie(newrasp)

                   } else {
                       rasisanieAdapter.addAll(rasp)
                       rasisanieAdapter.notifyDataSetChanged()
                       Toast.makeText(requireContext(), "pairs not found", Toast.LENGTH_LONG)
                           .show()
                   }

               } catch (e: Exception) {
                   rasisanieAdapter.addAll(rasp)
                   rasisanieAdapter.notifyDataSetChanged()
                   Log.e("RaspisanieFragment", ":err add: ${e.message}", e)
               }
           }
       }

        return binding.root
    }

    private fun updateRaspisanie(allpairs: PairsResponse) {
        if(!allpairs.ok){
            Log.e("RaspisanieFragment", "no raspisania")
            return
        }

        val days = allpairs.result
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val curdate = LocalDate.now().format(format)

        for (day in days) {
            if (day.date == curdate) {
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

}