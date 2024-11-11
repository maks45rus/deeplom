package com.example.raspisanieshgpu.fragments

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
import com.example.raspisanieshgpu.api.DataManager
import com.example.raspisanieshgpu.api.models.PairsResponse
import com.example.raspisanieshgpu.databinding.FragmentRaspisanieBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
        rasisanieAdapter = ArrayAdapter(requireContext(), R.layout.item_list, R.id.item_text)
        binding.raspisanieList.adapter = rasisanieAdapter

        val namesearch = arguments?.getString(NAME_SEARCH).toString()
        val pairsfor = arguments?.getString(PAIRS_FOR).toString()
        val db = databaseobj.database

        binding.textDate.text = namesearch

       viewLifecycleOwner.lifecycleScope.launch {
           withContext(Dispatchers.Main) {
               try {
                   var idsearch = 0
                   if (pairsfor == "group")
                       idsearch = db.getGroupDao().getGroupByName(namesearch).id!!
                   if (pairsfor == "teacher")
                       idsearch = db.getTeacherDao().getTeacherByName(namesearch).id!!

                   val currentDate = LocalDate.now()
                   val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                   val date = currentDate.format(format)
                   val newrasp = DataManager.fetchPairs(date, 1, idsearch, pairsfor)

                   if (newrasp.result.isNotEmpty()) {
                       updateRaspisanie(newrasp)
                   } else {
                       rasisanieAdapter.addAll(rasp)
                       rasisanieAdapter.notifyDataSetChanged()
                       Toast.makeText(requireContext(), "расписание не найдено", Toast.LENGTH_LONG)
                           .show()
                   }

               } catch (e: Exception) {
                   rasisanieAdapter.addAll(rasp)
                   rasisanieAdapter.notifyDataSetChanged()
                   Log.e("RaspisanieFragment", ":zzz ${e.message}", e)
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
        allpairs.result[0].pairs.forEach { para ->
            rasp[para.num - 1] = para.text
        }

        rasisanieAdapter.clear()
        rasisanieAdapter.addAll(rasp)
        rasisanieAdapter.notifyDataSetChanged()
    }


}