package com.example.raspisanieshgpu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.raspisanieshgpu.R

class PairsAdapter(context: Context, resource: Int) :
    ArrayAdapter<String>(context, resource) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_list, parent, false)

        val itemText = getItem(position)
        val itemNum = position + 1

        val tvNum = view.findViewById<TextView>(R.id.item_num)
        val tvText = view.findViewById<TextView>(R.id.item_text)

        tvNum.text = itemNum.toString()
        tvText.text = itemText

        return view
    }
}