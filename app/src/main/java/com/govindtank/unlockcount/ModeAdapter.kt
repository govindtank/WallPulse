package com.govindtank.unlockcount

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class ModeItem(val key: String, val title: String, val description: String, val color: Int)

class ModeAdapter(
    private val modes: List<ModeItem>,
    private val onClick: (ModeItem) -> Unit
) : RecyclerView.Adapter<ModeAdapter.ModeViewHolder>() {

    inner class ModeViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_mode, parent, false)
    ) {
        private val title: TextView = itemView.findViewById(R.id.tvTitle)
        private val desc: TextView = itemView.findViewById(R.id.tvDesc)
        private val preview: WallpaperPreviewView = itemView.findViewById(R.id.preview)

        fun bind(item: ModeItem) {
            title.text = item.title
            desc.text = item.description
            preview.setMode(item.key)
            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModeViewHolder {
        return ModeViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ModeViewHolder, position: Int) {
        holder.bind(modes[position])
    }

    override fun getItemCount(): Int = modes.size
}
