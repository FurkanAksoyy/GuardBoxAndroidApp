package com.flare.guardbox.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flare.guardbox.R
import com.flare.guardbox.model.ActivityLog
import java.text.SimpleDateFormat
import java.util.*

class ActivityLogAdapter(private val activityLogs: List<ActivityLog>) :
    RecyclerView.Adapter<ActivityLogAdapter.ActivityLogViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityLogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_activity_log, parent, false)
        return ActivityLogViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityLogViewHolder, position: Int) {
        val log = activityLogs[position]

        holder.tvMessage.text = log.message
        holder.tvTimestamp.text = dateFormat.format(Date(log.timestamp))

        // Set icon based on activity type
        when (log.activityType) {
            "PACKAGE_DELIVERED" -> holder.ivIcon.setImageResource(R.drawable.ic_package)
            "BOX_OPENED", "BOX_UNLOCKED" -> holder.ivIcon.setImageResource(R.drawable.ic_unlock)
            "BOX_LOCKED" -> holder.ivIcon.setImageResource(R.drawable.ic_lock)
            "TAMPERING_DETECTED" -> holder.ivIcon.setImageResource(R.drawable.ic_warning)
            else -> holder.ivIcon.setImageResource(R.drawable.ic_info)
        }
    }

    override fun getItemCount(): Int = activityLogs.size

    class ActivityLogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)
    }
}