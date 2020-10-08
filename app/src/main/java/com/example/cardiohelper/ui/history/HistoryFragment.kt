package com.example.cardiohelper.ui.history

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cardiohelper.R
import com.example.cardiohelper.adapter.HistoryRecyclerAdapter
import com.example.cardiohelper.database.SessionViewModel
import kotlinx.android.synthetic.main.fragment_history.view.*

class HistoryFragment : Fragment() {

    private lateinit var historyRecyclerAdapter: HistoryRecyclerAdapter
    private lateinit var sessionViewModel: SessionViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_history, container, false)

        historyRecyclerAdapter = HistoryRecyclerAdapter(mutableListOf())
        view.recyclerView.adapter = historyRecyclerAdapter
        view.recyclerView.layoutManager = LinearLayoutManager(activity)
        view.recyclerView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        // get the id from the clicked cell and intent to activity
        historyRecyclerAdapter.setCellClickListener {
            val intent = Intent(activity, HistoryItemActivity::class.java)
            intent.putExtra("SessionId", it.id)
            startActivity(intent)
        }

        // update recyclerview every time new session is added
        sessionViewModel = ViewModelProviders.of(this).get(SessionViewModel::class.java)
        sessionViewModel.getAllSessions().observe(viewLifecycleOwner, { historyRecyclerAdapter.update(it.reversed()) })

        return view
    }

    companion object {
        fun newInstance() = HistoryFragment()
    }
}