package com.example.cardiohelper.service

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.cardiohelper.R
import com.example.cardiohelper.database.Step
import com.example.cardiohelper.database.UserDB
import com.example.cardiohelper.util.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class StepService : SensorEventListener, CoroutineIntentService() {

    private lateinit var userDB: UserDB
    private lateinit var sm: SensorManager

    private var steps = 0

    // get the date and format it
    private val date = LocalDateTime.now()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val formattedDate = date.format(formatter)

    //get today steps
    override fun onCreate() {
        sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        userDB = UserDB.get(applicationContext)
        launch { steps = userDB.stepDao().getTodaySteps(formattedDate) }
        super.onCreate()
    }

    override fun onHandleIntent(p0: Intent?) {}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        initializeStepSensor()

        // if no step goal found set a string
        // else set string and values for the progress bar
        if (User.stepGoal == 0) {
            val message = getString(R.string.enterStepGoal)
            startForeground(420, createNotification(message))
        } else {
            val message = String.format(getString(R.string.notificationStepsMessage), steps, User.stepGoal)
            startForeground(420, createNotification(message))
        }
        return START_STICKY
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    // when step detector has a new sensor event
    // call the function to add a step to the step room
    // steps have date value for daily steps
    override fun onSensorChanged(p0: SensorEvent) {
        stepDetected(Step(formattedDate))
    }

    private fun createNotification(message: String): Notification {

        return NotificationCompat.Builder(this, "StepCounter")
            .setContentText(message)
            .setProgress(User.stepGoal, steps, false)
            .setSmallIcon(R.drawable.ic_baseline_timer_24)
            .setTicker(message)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    private fun updateNotification() {
        val message = String.format(getString(R.string.notificationStepsMessage), steps, User.stepGoal)
        NotificationManagerCompat.from(this).notify(420, createNotification(message))
    }

    private fun initializeStepSensor() {
        val sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    // update the notification every time step is detected
    private fun stepDetected(step: Step) {
        steps++
        GlobalScope.launch(Dispatchers.IO) {
            userDB.stepDao().insert(step)
        }
        updateNotification()
    }
}