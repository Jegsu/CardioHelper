package com.example.cardiohelper.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.cardiohelper.R
import com.example.cardiohelper.ui.util.User
import kotlinx.android.synthetic.main.fragment_settings.view.*

class SettingsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        sharedPreferences = requireContext().getSharedPreferences("User", Context.MODE_PRIVATE)

        view.nameEditText.hint = if (User.name.isEmpty()) getString(R.string.enterName) else User.name
        view.stepEditText.hint = if (User.stepGoal == 0) getString(R.string.enterStepGoal) else User.stepGoal.toString()

        //save edit text to shared prefs if edit text loses focus
        //hides the keyboard if loses focus
        view.nameEditText.setOnFocusChangeListener { view: View, b: Boolean ->
            val name = view.nameEditText.text.toString()
            if (name.isNotEmpty()) {
                sharedPreferences.edit().putString(USER_KEY_NAME, name).apply()
                User.name = name
                view.nameEditText.text.clear()
                view.nameEditText.hint = User.name
                view.hideKeyboard()
                Toast.makeText(requireContext(), getString(R.string.nameSaved), Toast.LENGTH_SHORT).show()
            } else {
                view.hideKeyboard()
            }
        }

        view.stepEditText.setOnFocusChangeListener { view: View, b: Boolean ->
            val steps = view.stepEditText.text.toString()
            if (steps.isNotEmpty()) {
                sharedPreferences.edit().putInt(USER_KEY_DAILY_STEP_GOAL, steps.toInt()).apply()
                User.stepGoal = steps.toInt()
                view.stepEditText.text.clear()
                view.stepEditText.hint = User.stepGoal.toString()
                view.hideKeyboard()
                Toast.makeText(requireContext(), getString(R.string.dailyStepsSaved), Toast.LENGTH_SHORT).show()
            } else {
                view.hideKeyboard()
            }
        }

        return view
    }

    private fun View.hideKeyboard() {
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    }

    companion object {
        const val USER_KEY_NAME = "Name"
        const val USER_KEY_DAILY_STEP_GOAL = "dailyStepGoal"

        fun newInstance() = SettingsFragment()
    }
}

