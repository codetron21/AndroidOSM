package com.codetron.androidosm

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.codetron.androidosm.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.RoadManager
import java.util.concurrent.atomic.AtomicReference

class MainActivity : AppCompatActivity() {

    //110.3442,-7.402,110.387,-7.3682
    //pos 1 kemuning: -7.393947, 110.372771
    //tujuan gunung: -7.3898378,110.3644134

    private lateinit var binding: ActivityMainBinding
    private lateinit var mapController: IMapController

    private val roadOverlay = AtomicReference(Polyline())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        Configuration.getInstance().load(application, getPreferences(MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName

        setContentView(binding.root)

        binding.run {
            mapController = osm.controller

            osm.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
            osm.isVerticalMapRepetitionEnabled = false
            osm.isHorizontalMapRepetitionEnabled = false
            osm.minZoomLevel = 15.5
            osm.boundingBox.set(-7.3682, 110.387, -7.402, 110.3442)
            osm.setScrollableAreaLimitLatitude(-7.3682, -7.402, 0)
            osm.setScrollableAreaLimitLongitude(110.3442, 110.387, 0)

            mapController.setZoom(15.5)

            val centerPoint = GeoPoint(-7.3898378, 110.3644134)
            mapController.setCenter(centerPoint)

            val compassOverlay = CompassOverlay(
                this@MainActivity,
                InternalCompassOrientationProvider(this@MainActivity),
                osm
            )
            compassOverlay.enableCompass()
            osm.overlays.add(compassOverlay)

            val startPoint = GeoPoint(-7.393947, 110.372771)
            val startMarker = Marker(osm)
            startMarker.isDraggable = false
            startMarker.setInfoWindow(null)
            startMarker.setTextIcon("Pos 1 Kemuning")
            startMarker.position = startPoint
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            osm.overlays.add(startMarker)

            val targetPoint = GeoPoint(-7.3898378, 110.3644134)
            val targetMarker = Marker(osm)
            targetMarker.isDraggable = false
            targetMarker.setInfoWindow(null)
            targetMarker.setTextIcon("Puncak")
            targetMarker.position = targetPoint
            targetMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            osm.overlays.add(targetMarker)

            val line = Polyline(osm)
            line.addPoint(startPoint)
            line.addPoint(targetPoint)
            osm.overlays.add(line)

            //drawPolyLine(startPoint, targetPoint)

            osm.invalidate()
        }
    }

    private fun drawPolyLine(point1: GeoPoint?, point2: GeoPoint?) = with(binding){
        if (point1 == null || point2 == null) return@with

        lifecycleScope.launch(Dispatchers.Main) {
            osm.overlays.removeIf { it is Polyline }
            withContext(Dispatchers.IO) {
                val roadManager: RoadManager =
                    OSRMRoadManager(this@MainActivity, packageName)
                val road = roadManager.getRoad(arrayListOf(point1, point2))
                roadOverlay.set(RoadManager.buildRoadOverlay(road, Color.BLUE, 8f))
            }
            osm.overlays.add(roadOverlay.get())
            osm.invalidate()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.osm.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.osm.onPause()
    }

}