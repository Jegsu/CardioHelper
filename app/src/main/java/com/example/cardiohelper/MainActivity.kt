package com.example.cardiohelper


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.cardiohelper.service.StepService
import com.example.cardiohelper.ui.settings.SettingsFragment.Companion.USER_KEY_DAILY_STEP_GOAL
import com.example.cardiohelper.ui.settings.SettingsFragment.Companion.USER_KEY_NAME
import com.example.cardiohelper.ui.tracker.TrackerActivity
import com.example.cardiohelper.util.User
import com.mapbox.mapboxsdk.Mapbox

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //get the mapbox account public key
        Mapbox.getInstance(this, getString(R.string.mapbox_token))

        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        sharedPreferences = this.getSharedPreferences("User", Context.MODE_PRIVATE)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_history, R.id.nav_settings
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //check if values in shared prefs exist
        //if not change to settings fragment
        if (checkIfUserExist()) {
            getUserSettings()
        } else {
            navController.navigate(R.id.nav_settings)
            Toast.makeText(this,"Please enter your details to continue", Toast.LENGTH_SHORT).show()
        }

        createNotificationChannel()

        //start the foreground step service or give a toast
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR)) {
            ContextCompat.startForegroundService(applicationContext, Intent(this, StepService::class.java))

        } else {
            Toast.makeText(this,"No step sensor found!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkIfUserExist(): Boolean {
        return sharedPreferences.contains(USER_KEY_NAME) && sharedPreferences.contains(USER_KEY_DAILY_STEP_GOAL)
    }

    private fun getUserSettings() {
        val name = sharedPreferences.getString(USER_KEY_NAME, null)
        val dailyStepGoal = sharedPreferences.getInt(USER_KEY_DAILY_STEP_GOAL, 0)

        User.name = name!!
        User.stepGoal = dailyStepGoal
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("StepCounter", getString(R.string.notificationChannelName), NotificationManager.IMPORTANCE_LOW)
            channel.description = getString(R.string.notificationChannelDescription)
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    // setup the tracker button on appbar
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_tracker -> {
            val trackerIntent = Intent(this, TrackerActivity::class.java)
            startActivity(trackerIntent)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}