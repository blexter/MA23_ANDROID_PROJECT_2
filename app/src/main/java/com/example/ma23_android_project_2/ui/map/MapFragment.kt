package com.example.ma23_android_project_2.ui.map

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.ma23_android_project_2.R
import com.example.ma23_android_project_2.databinding.FragmentMapBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: FragmentMapBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var locationProvider: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var yourLatitude: Double = 0.0
    private var yourLongitude: Double = 0.0
    private var locationUpdatesStarted = false
    private var mapReady = false
    private val REQUEST_LOCATION = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        locationProvider = LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest.Builder(2000).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    yourLatitude = location.latitude
                    yourLongitude = location.longitude
                }
                if (!locationUpdatesStarted) {
                    locationUpdatesStarted = true
                    if (mapReady) {
                        startLocationUpdates()
                        if (yourLatitude != 0.0 && yourLongitude != 0.0) {
                            val yourPosition = LatLng(yourLatitude, yourLongitude)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(yourPosition, 13.0f))
                        }
                    }
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION
            )
        }

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this@MapFragment)
    }


    override fun onResume(){
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause(){
        super.onPause()
        stopLocationUpdates()
    }
    fun stopLocationUpdates(){
        locationProvider.removeLocationUpdates(locationCallback)
    }

    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationProvider.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_LOCATION){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startLocationUpdates()
            }
        }
    }






    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("MapsActivity", "onMapReady called")
        mMap = googleMap
        mapReady = true

        val db = Firebase.firestore
        val placesCollection = db.collection("places")

        placesCollection.get()
            .addOnSuccessListener { documents ->
                Log.d("Firestore", "Successfully retrieved ${documents.size()} documents")
                for (document in documents) {
                    val positionLatStr = document.getString("positionLat")
                    val positionLonStr = document.getString("positionLon")

                    if (positionLatStr != null && positionLonStr != null) {
                        if (positionLatStr != "" && positionLonStr != "") {
                            try {
                                val positionLat = positionLatStr.toDouble()

                                val positionLon = positionLonStr.toDouble()

                                val placeLatLng = LatLng(positionLat, positionLon)
                                val title = document.getString("name") ?: "Default Title"
                                val snippet = document.getString("otherInfo") ?: "Default Snippet"

                                Log.d("Firestore", "Adding marker for $title at $placeLatLng")
                                onMapClick(placeLatLng, title, snippet)
                            } catch (ex : Exception){
                                Log.d("Firestore", "Adding marker failed")

                            }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents: ${exception.message}", exception)
            }

        if (locationUpdatesStarted) {
            startLocationUpdates()
            if (yourLatitude != 0.0 && yourLongitude != 0.0) {
                val yourPosition = LatLng(yourLatitude, yourLongitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(yourPosition))
            }
        }

    }

    private fun onMapClick(latLng: LatLng, title : String? = null, snippet : String? = null) {

        val defaultTitle = title ?: "Where you clicked!"
        val defaultSnippet = snippet ?: "By your fingertips!"

        var click = mMap.addMarker(
            MarkerOptions()
                .position(LatLng(latLng.latitude, latLng.longitude))
                .title(defaultTitle)
                .snippet(defaultSnippet)
        )
    }
}
