package com.example.ma23_android_project_2.ui.map

import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.ma23_android_project_2.MainActivity
import com.example.ma23_android_project_2.R
import com.example.ma23_android_project_2.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null

    private val binding get() = _binding!!
    private val REQUEST_LOCATION = 1
    lateinit var locationProvider: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback
    private var yourLatitude: Double = 0.0
    private var yourLongitude: Double = 0.0
    private var locationUpdatesStarted = false
    private var mapReady = false
    lateinit var auth : FirebaseAuth
    private var mMap: GoogleMap? = null
    private var loggedIn: Boolean = true
    lateinit var mainActivity : MainActivity


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mapViewModel =
            ViewModelProvider(this).get(MapViewModel::class.java)

        Log.d("MapFragment", "onCreateView called")
        mainActivity = requireActivity() as MainActivity




        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textMap
        mapViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        locationProvider = LocationServices.getFusedLocationProviderClient(mainActivity)
        locationRequest = LocationRequest.Builder(2000).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                Log.d("MapFragment", "onLocationResult called with ${locationResult.locations.size} locations - in MapFragment")
                for (location in locationResult.locations) {
                    yourLatitude = location.latitude
                    yourLongitude = location.longitude
                }
                if (!locationUpdatesStarted) {
                    locationUpdatesStarted = true
                    if (mapReady) {
                        mainActivity.startLocationUpdates()
                        if (yourLatitude != 0.0 && yourLongitude != 0.0) {
                            val yourPosition = LatLng(yourLatitude, yourLongitude)

                            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(yourPosition, 13.0f))
                        }
                    }
                }
            }
        }

        checkLocationPermission()

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapView) as? SupportMapFragment

        if(mapFragment == null)
            return root


        mapFragment.getMapAsync { googleMap ->
            if (googleMap == null) {
                Log.e("MapFragment", "GoogleMap is null. Map initialization failed.")
            } else {
                Log.d("MapFragment", "Map initialized successfully.")
                googleMap.setOnMapClickListener { latLng ->
                    if (loggedIn)
                        onMapClick(latLng)
                }
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("MapFragment", "onMapReady called in MapFragment")

        mMap = googleMap
        mapReady = true
        if (locationUpdatesStarted) {
            mainActivity.startLocationUpdates()
            if (yourLatitude != 0.0 && yourLongitude != 0.0) {
                val yourPosition = LatLng(yourLatitude, yourLongitude)
                mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(yourPosition, 13.0f))
            }
        }

        mMap?.setOnMapClickListener { latLng ->
            if (loggedIn)
                onMapClick(latLng)
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                mainActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                mainActivity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION
            )
        }
    }



    private fun onMapClick(latLng: LatLng) {
        Log.d("MapFragment", "onMapClick called in MapFragment")
        val zoomLevel = mMap?.cameraPosition?.zoom ?: 0.0f
        val cameraPosition = mMap?.cameraPosition
        Log.d("MapFragment", "Camera Position: $cameraPosition, Zoom level: $zoomLevel")

        if (zoomLevel >= 0.0f) {
            Log.d("MapFragment", "Adding marker at $latLng")
            mMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Where you clicked!")
                    .snippet("By your fingertips!")
            )
        } else {
            Toast.makeText(mainActivity, "Zoom in, too inaccurate", Toast.LENGTH_SHORT).show()
        }
    }



}