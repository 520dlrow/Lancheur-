package com.mc88.launcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(
    private val apps: List<AppInfo>,
    private val isHorizontal: Boolean,
    private val onClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    private var iconSize = 48

    fun setIconSize(size: Int) {
        iconSize = size
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: LinearLayout = view.findViewById(R.id.itemRoot)
        val ivAppIcon: ImageView = view.findViewById(R.id.ivAppIcon)
        val tvAppName: TextView = view.findViewById(R.id.tvAppName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.tvAppName.text = app.name
        holder.ivAppIcon.setImageDrawable(app.icon)
        
        if (isHorizontal) {
            holder.root.orientation = LinearLayout.VERTICAL
            holder.tvAppName.setPadding(0, 12, 0, 0)
            holder.root.minimumWidth = 100
        } else {
            holder.root.orientation = LinearLayout.HORIZONTAL
            holder.tvAppName.setPadding(16, 0, 0, 0)
            holder.root.minimumWidth = 0
        }
        
        val params = holder.ivAppIcon.layoutParams
        params.width = iconSize
        params.height = iconSize
        holder.ivAppIcon.layoutParams = params
        
        holder.itemView.setOnClickListener { onClick(app) }
    }

    override fun getItemCount() = apps.size
}
