package com.example.raspisanieshgpu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.raspisanieshgpu.R
import kotlin.math.min

class PairsAdapter(context: Context) :
    ArrayAdapter<String>(context, R.layout.item_list) {

    // Времена пар
    private val pairTimes = listOf(
        "8:00 - 9:30",
        "9:40 - 11:10",
        "11:20 - 12:50",
        "13:20 - 14:50",
        "15:00 - 16:30"
    )

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_list, parent, false)

        val itemText = getItem(position)
        val tvTime = view.findViewById<TextView>(R.id.item_time)
        val tvText = view.findViewById<TextView>(R.id.item_text)

        // Устанавливаем время для текущей пары
        tvTime.text = pairTimes.getOrNull(position) ?: "-"
        tvText.text = itemText ?: "-"

        // Стилизация для пустых пар
        if (itemText.isNullOrEmpty() || itemText == "-") {
            tvText.setTextColor(ContextCompat.getColor(context, R.color.gray))
            tvText.text = context.getString(R.string.no_class)
        } else {
            tvText.setTextColor(ContextCompat.getColor(context, R.color.black))
        }

        return view
    }

    override fun getCount(): Int {
        return min(super.getCount(), pairTimes.size)
    }
}