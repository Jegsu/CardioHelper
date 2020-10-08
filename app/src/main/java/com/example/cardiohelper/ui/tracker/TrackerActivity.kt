package com.example.cardiohelper.ui.tracker

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.widget.Chronometer
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat
import com.example.cardiohelper.R
import com.example.cardiohelper.database.Session
import com.example.cardiohelper.database.UserDB
import com.example.cardiohelper.util.Formatter
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.utils.PolylineUtils
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import kotlinx.android.synthetic.main.activity_tracker.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TrackerActivity : AppCompatActivity(), LocationEngineCallback<LocationEngineResult>, PermissionsListener {

    companion object {
        private const val INTERVAL_IN_MILLISECONDS: Long = 1000
        private const val MAX_WAIT_TIME: Long = 5000
        private const val KEY_TIME_PAUSED = "TimePaused"
        private const val KEY_BASE = "TimeBase"
        private const val KEY_STATE = "ChronometerState"
    }

    internal enum class ChronometerState {
        Running, Paused, Stopped
    }

    private lateinit var userDB: UserDB
    private lateinit var mapboxMap: MapboxMap
    private lateinit var permissionManager: PermissionsManager
    private lateinit var locationEngine: LocationEngine
    private lateinit var geoJsonSource: GeoJsonSource
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var chronometerView: Chronometer

    private var timeWhenPaused: Long = 0
    private var timeBase: Long = 0
    private var startTime: Long = 0
    private var elapsedTime: Long = 0

    private val geoPoints: MutableList<Point> = ArrayList()
    private val locationCallback = this
    private val isRunning: Boolean
        get() = ChronometerState.values()[sharedPreferences.getInt(KEY_STATE, ChronometerState.Stopped.ordinal)] == ChronometerState.Running

    private val isPaused: Boolean
        get() = ChronometerState.values()[sharedPreferences.getInt(KEY_STATE, ChronometerState.Stopped.ordinal)] == ChronometerState.Paused

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker)

        //set the back button and title
        supportActionBar?.let {
            it.title = "Chronometer"
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        mapboxView.onCreate(savedInstanceState)

        userDB = UserDB.get(this)

        geoJsonSource = GeoJsonSource("lineSource")

        chronometerView = findViewById(R.id.chronometerView)

        sharedPreferences = this.getSharedPreferences("Chronometer", Context.MODE_PRIVATE)


        mapboxView.getMapAsync { map ->
            mapboxMap = map
            mapboxMap.setMinZoomPreference(14.0)
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                initLocationComponent(it)
                it.addSource(geoJsonSource)
            }
        }

        startButton.setOnClickListener {
            //check if timer is running
            if (isRunning) {
                pauseChronometer()
            } else {
                startChronometer()
            }
        }

        stopButton.setOnClickListener {
                startButton.setIconResource(R.drawable.ic_baseline_play_arrow_24)
                stopChronometer()
        }
    }

    private fun startChronometer() {
        // check for location permissions and if location is enabled
        if (PermissionsManager.areLocationPermissionsGranted(this) && isLocationEnabled(this) ) {
            storeState(ChronometerState.Running)
            saveBase()
            startStateChronometer()
        } else {
            storeState(ChronometerState.Stopped)
            showAlert()
        }
    }

    private fun pauseChronometer() {
        storeState(ChronometerState.Paused)
        saveTimeWhenPaused()
        pauseStateChronometer()
    }

    private fun stopChronometer() {
        // only allow user to click the stop button if the timer is running or paused
        if (isRunning || isPaused) {
            startTime = System.currentTimeMillis()
            elapsedTime = SystemClock.elapsedRealtime() - chronometerView.base
            storeState(ChronometerState.Stopped)

            //encode the points and store the tracker session
            val encodedPoints = PolylineUtils.encode(geoPoints, 5)
            storeSession(
                Session(
                    startTime,
                    startTime + elapsedTime,
                    encodedPoints,
                    calculateDistance(),
                    calculateSpeed(calculateDistance(), elapsedTime)
                )
            )

            //stop the location updates and clear timer state
            locationEngine.removeLocationUpdates(locationCallback)
            chronometerView.stop()
            clearState()
        }
    }

    private fun storeSession(session: Session) {
        GlobalScope.launch(Dispatchers.IO) {
            //insert the session
            userDB.sessionDao().insert(session)

            //clear the geopoints for next session
            geoPoints.clear()
        }
    }

    //store timer state
    private fun storeState(state: ChronometerState) {
        sharedPreferences.edit().putInt(KEY_STATE, state.ordinal).apply()
    }

    private fun startStateChronometer() {
        startButton.setIconResource(R.drawable.ic_baseline_pause_24)
        timeBase = sharedPreferences.getLong(KEY_BASE, SystemClock.elapsedRealtime())
        timeWhenPaused = sharedPreferences.getLong(KEY_TIME_PAUSED, 0)
        chronometerView.base = timeBase + timeWhenPaused
        chronometerView.start()

        //start the location again just in case and draw the path
        initLocationEngine()
        mapboxMap.style?.let { initLinePath(it) }
    }

    private fun pauseStateChronometer() {
        startButton.setIconResource(R.drawable.ic_baseline_play_arrow_24)
        timeWhenPaused = sharedPreferences.getLong(KEY_TIME_PAUSED, chronometerView.base - SystemClock.elapsedRealtime())
        chronometerView.base = SystemClock.elapsedRealtime() + timeWhenPaused
        chronometerView.stop()

        //?? remove layer to enable it again when timer starts
        mapboxMap.style?.removeLayer("lineLayer")
    }

    private fun clearState() {
        storeState(ChronometerState.Stopped)

        // set values back to default
        timeWhenPaused = 0
        startTime = 0
        elapsedTime = 0
        distanceTextView.text = "0 m"
        speedTextView.text = "0 km/h"
        chronometerView.text = "00:00"
        chronometerView.base = SystemClock.elapsedRealtime()
        sharedPreferences.edit()
            .remove(KEY_BASE)
            .remove(KEY_TIME_PAUSED)
            .apply()
        mapboxMap.style?.removeLayer("lineLayer")
    }

    // save timer base
    private fun saveBase() {
        sharedPreferences.edit()
            .putLong(KEY_BASE, SystemClock.elapsedRealtime())
            .apply()
    }

    private fun saveTimeWhenPaused() {
        sharedPreferences.edit()
            .putLong(KEY_TIME_PAUSED, chronometerView.base - SystemClock.elapsedRealtime())
            .apply()
    }

    //init mapbox location component
    @SuppressLint("MissingPermission")
    private fun initLocationComponent(loadedMapStyle: Style) {

        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            val locationComponentOptions = LocationComponentOptions.builder(this)
                .build()

            val locationComponent = mapboxMap.locationComponent

            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(this, loadedMapStyle)
                    .locationComponentOptions(locationComponentOptions)
                    .build()
            )

            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING_GPS
            locationComponent.renderMode = RenderMode.NORMAL

        } else {
            permissionManager = PermissionsManager(this)
            permissionManager.requestLocationPermissions(this)
        }
    }

    // init line that follows the current location
    private fun initLinePath(style: Style) {
        style.addLayer(
            LineLayer("lineLayer", "lineSource")
                .withProperties(
                    PropertyFactory.lineColor(Color.parseColor("#FFAB91")),
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                    PropertyFactory.lineWidth(5f)
                ))
    }


    @SuppressLint("MissingPermission")
    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this)

        val request = LocationEngineRequest.Builder(INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(MAX_WAIT_TIME)
            .build()

        //Request location updates
        locationEngine.requestLocationUpdates(request, locationCallback, mainLooper)
        locationEngine.getLastLocation(locationCallback)
    }

    //calculate average speed from the distance calculated by turf and elapsed time
    private fun calculateSpeed(meters: Double, millis: Long): Double {
        val seconds = millis / 1000
        val meterPerSecond = meters / seconds
        return meterPerSecond * 3.6
    }

    //calculate distance using mapbox turf
    private fun calculateDistance(): Double {
        val distance = TurfMeasurement.length(geoPoints, TurfConstants.UNIT_METERS)
        if (distance <= 0) {
            return 0.0
        }
        return distance
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage("Location services are required to start the timer.")
        builder.setPositiveButton("OK", null)
        val dialog = builder.create()
        dialog.show()
    }

    //check if location service is enabled
    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }


    override fun onPermissionResult(granted: Boolean){
        if (granted) {
            mapboxMap.getStyle() {
                initLocationComponent(it)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // location engine result
    // add points to the geoPoints array
    // geojson draws line from the points
    override fun onSuccess(result: LocationEngineResult?) {
        for (location in result!!.locations) {
            val point = Point.fromLngLat(location.longitude, location.latitude)

            geoPoints.add(point)

            geoJsonSource.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(geoPoints)))

            speedTextView.text = Formatter.formatSpeed(location.speed)
            distanceTextView.text = Formatter.formatDistance(calculateDistance())
        }
    }

    //mapbox overrides
    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {}

    override fun onFailure(exception: Exception) {}

    //mapbox lifecycle declarations
    @SuppressWarnings("MissingPermission")
    override fun onStart() {
        super.onStart()
        mapboxView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapboxView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapboxView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapboxView.onStop()
    }

    override fun onDestroy() {
        //save the session if activity is killed
        stopChronometer()
        super.onDestroy()
        mapboxView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapboxView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapboxView.onSaveInstanceState(outState)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}