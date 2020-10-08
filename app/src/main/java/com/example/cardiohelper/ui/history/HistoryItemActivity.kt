package com.example.cardiohelper.ui.history

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.example.cardiohelper.R
import com.example.cardiohelper.database.Session
import com.example.cardiohelper.database.SessionViewModel
import com.mapbox.geojson.Feature
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.utils.PolylineUtils
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_tracker.*
import kotlinx.coroutines.*

class HistoryItemActivity : AppCompatActivity() {

    private lateinit var mapboxMap: MapboxMap
    private lateinit var geoJsonSource: GeoJsonSource
    private lateinit var sessionViewModel: SessionViewModel
    private var session : Session? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_item)
        mapboxView.onCreate(savedInstanceState)

        //setup the back button and title
        supportActionBar?.let {
            it.title = "Map"
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        // get the session id from the intent
        val id = intent.getIntExtra("SessionId",-1)


        sessionViewModel = ViewModelProviders.of(this).get(SessionViewModel::class.java)

        geoJsonSource = GeoJsonSource("lineSource")
        mapboxView.getMapAsync { map ->
            mapboxMap = map
            mapboxMap.setMinZoomPreference(14.0)
            mapboxMap.setStyle(Style.MAPBOX_STREETS) {
                it.addSource(geoJsonSource)
                initLinePath(it)
            }
        }

        // fetch the session with the id got from intent
        // needs delay or crashes
        GlobalScope.launch(Dispatchers.Main){
            session = withContext(Dispatchers.IO) {
                delay(500)
                sessionViewModel.getSessionFromId(id)
            }
            drawLineFromGeoPoints()
        }
    }

    // init the line path
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

    // draw the line from session geopoints
    private fun drawLineFromGeoPoints() {
        //decode the Points from Session and find the first Point
        val sessionGeoPoints = PolylineUtils.decode(session?.geoPoints!!, 5)
        val firstGeoPoint = sessionGeoPoints.first()
        val position = CameraPosition.Builder()
            .target(LatLng(firstGeoPoint.latitude(), firstGeoPoint.longitude()))
            .build()

        //position camera to the first found Point
        //draw the line from geopoints
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 10)
        geoJsonSource.setGeoJson(Feature.fromGeometry(LineString.fromLngLats(sessionGeoPoints)))
    }

    //mapbox lifecycle declarations
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