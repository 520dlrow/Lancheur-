package com.mc88.launcher

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mc88.launcher.databinding.ItemAppBinding

class AppAdapter(
    private val apps: List<AppInfo>,
    private val onClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    private var iconSize = 48

    fun setIconSize(size: Int) {
        iconSize = size
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.binding.tvAppName.text = app.name
        holder.binding.ivAppIcon.setImageDrawable(app.icon)
        
        val params = holder.binding.ivAppIcon.layoutParams
        params.width = iconSize * 2
        params.height = iconSize * 2
        holder.binding.ivAppIcon.layoutParams = params
        
        holder.binding.root.setOnClickListener { onClick(app) }
    }

    override fun getItemCount() = apps.size
}
