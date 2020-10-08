package com.example.cardiohelper.ui.home

import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.lifecycle.ViewModelProviders
import com.example.cardiohelper.R
import com.example.cardiohelper.database.SessionViewModel
import com.example.cardiohelper.database.StepViewModel
import com.example.cardiohelper.database.UserDB
import com.example.cardiohelper.util.Formatter
import com.example.cardiohelper.util.User
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment() {

    private lateinit var stepViewModel: StepViewModel
    private lateinit var sessionViewModel: SessionViewModel
    private lateinit var userDB: UserDB

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        userDB = UserDB.get(requireContext())

        view.stepProgressBar.secondaryProgress = User.stepGoal
        view.stepProgressBar.max = User.stepGoal

        //observe daily steps and animate the progress bar
        //animator starts the animation from the secondary progress which is set to user.stepgoal
        //after that it keeps building it till it reaches the step goal
        stepViewModel = ViewModelProviders.of(this).get(StepViewModel::class.java)
        stepViewModel.getTodayStepsLive().observe(viewLifecycleOwner, {
            val objectAnimator = ObjectAnimator.ofInt(stepProgressBar, "secondaryProgress", stepProgressBar.secondaryProgress , it)
            objectAnimator.interpolator = DecelerateInterpolator()
            objectAnimator.duration = 3000
            objectAnimator.start()
            stepProgressBar.clearAnimation()
            view.stepCountTextView.text = it.toString()
        })

        //observe total steps
        stepViewModel.getAllStepsLive().observe(viewLifecycleOwner, {
                totalStepsTextView.text = it.toString()
        })

        //observe the total distance
        sessionViewModel = ViewModelProviders.of(this).get(SessionViewModel::class.java)
        sessionViewModel.getTotalMetersLive().observe(viewLifecycleOwner, {
            it?.let {
                distanceTextView.text = Formatter.formatDistance(it)
            }
        })

        //observe the avg speed
        sessionViewModel.getAvgSpeedLive().observe(viewLifecycleOwner, {
            it?.let {
                speedTextView.text = Formatter.formatAverageSpeed(it)
            }
        })

        //observe the avg time
        sessionViewModel.getAvgTimeLive().observe(viewLifecycleOwner, {
            it?.let {
                timeTextView.text = Formatter.formatAverageDuration(it)
            }
        })

        //get the step goal from the shared prefs
        view.stepGoalTextView.text = if (User.stepGoal == 0) "${getString(R.string.progressbarGoalNone)}" else "${getString(R.string.progressbarGoal)} ${User.stepGoal}"

        return view
    }


    companion object {
        fun newInstance() = HomeFragment()
    }
}