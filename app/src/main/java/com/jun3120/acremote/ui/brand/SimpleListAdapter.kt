package com.jun3120.acremote.ui.brand

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jun3120.acremote.R

class SimpleListAdapter(
    private val items: List<Pair<Any, String>>,
    private val onClick: (Any) -> Unit
) : RecyclerView.Adapter<SimpleListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_simple_text, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (item, name) = items[position]
        holder.textView.text = name
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.tv_item_text)
    }
}
