package com.example.raspisanieshgpu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.raspisanieshgpu.R

class SearchAdapter(
    context: Context,
    private val onItemClick: (String) -> Unit
) : ArrayAdapter<String>(context, R.layout.item_search_result) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_search_result, parent, false)

        getItem(position)?.let { item ->
            view.findViewById<TextView>(R.id.item_text).text = item
            view.setOnClickListener {
                onItemClick(item)
            }
        }

        return view
    }
}