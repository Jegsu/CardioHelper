package com.example.cardiohelper.ui.tracker

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.cardiohelper.R
import com.example.cardiohelper.database.StepViewModel
import kotlinx.android.synthetic.main.fragment_tracker.*

class TrackerFragment : Fragment() {

    private lateinit var stepViewModel: StepViewModel

    @SuppressLint("FragmentLiveDataObserve")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_tracker, container, false)

        stepViewModel = ViewModelProviders.of(this).get(StepViewModel::class.java)
        stepViewModel.getAllSteps().observe(this, Observer { stepCount.text = it.toString() })

        return view
    }

    companion object {
        fun newInstance() = TrackerFragment()
    }
}