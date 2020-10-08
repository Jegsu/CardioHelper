package com.example.cardiohelper.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.cardiohelper.R
import com.example.cardiohelper.database.Session
import com.example.cardiohelper.ui.util.Formatter
import kotlinx.android.synthetic.main.history_recycler_list.view.*

class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class HistoryRecyclerAdapter(private var session: List<Session>) : RecyclerView.Adapter<HistoryViewHolder>() {

    private var cellClickListener: (Session) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.history_recycler_list, parent, false) as ConstraintLayout
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val sessionPos = session[position]
        holder.itemView.sessionDate.text = Formatter.formatDate(sessionPos.startTime)
        holder.itemView.sessionDistance.text = Formatter.formatDistance(sessionPos.distance)
        holder.itemView.sessionAverageSpeed.text = Formatter.formatAverageSpeed(sessionPos.speed)
        holder.itemView.sessionDuration.text = Formatter.formatDuration(sessionPos.startTime, sessionPos.endTime)
        holder.itemView.setOnClickListener{ cellClickListener(sessionPos) }
    }

    override fun getItemCount(): Int {
        return session.size
    }

    fun update(list: List<Session>) {
            session = list
            notifyDataSetChanged()
    }

    fun setCellClickListener(clickListener: (Session) -> Unit) {
        cellClickListener = clickListener
    }

}