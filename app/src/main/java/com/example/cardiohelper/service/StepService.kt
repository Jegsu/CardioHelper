package com.example.cardiohelper.service

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.example.cardiohelper.database.Step
import com.example.cardiohelper.database.UserDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class StepService : SensorEventListener, CoroutineIntentService() {

    private lateinit var userDB: UserDB
    private lateinit var sm: SensorManager
    private lateinit var sensor: Sensor

    // get the date and format it
    private val date = LocalDateTime.now()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val formattedDate = date.format(formatter)

    override fun onCreate() {
        sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        userDB = UserDB.get(applicationContext)
        super.onCreate()
    }

    override fun onHandleIntent(p0: Intent?) {}

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        initializeStepSensor()
        return START_STICKY
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onSensorChanged(p0: SensorEvent) {
        stepDetected(Step(formattedDate))
    }

    private fun stepDetected(step: Step) {
        GlobalScope.launch(Dispatchers.IO) {
            userDB.stepDao().insert(step)
        }
    }

    private fun initializeStepSensor() {
        var sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        if (sensor == null) {
            Toast.makeText(this, "No Step Sensor !", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Step sensor found !", Toast.LENGTH_SHORT).show()
            sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }
}