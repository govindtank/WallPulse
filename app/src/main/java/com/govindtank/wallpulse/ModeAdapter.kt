package com.govindtank.wallpulse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.govindtank.wallpulse.WallpaperPreviewView

class ModeAdapter(
    private val modes: List<ModeItem>,
    private val onClick: (ModeItem) -> Unit
) : RecyclerView.Adapter<ModeAdapter.ModeViewHolder>() {

    inner class ModeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.tvTitle)
        private val desc = itemView.findViewById<TextView>(R.id.tvDesc)
        private val preview = itemView.findViewById<WallpaperPreviewView>(R.id.preview)

        fun bind(mode: ModeItem) {
            title.text = mode.title
            desc.text = mode.description
            preview.setMode(mode.key)
            itemView.setOnClickListener { onClick(mode) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mode, parent, false)
        return ModeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModeViewHolder, position: Int) {
        holder.bind(modes[position])
    }

    override fun getItemCount(): Int = modes.size
}
