package com.example.ma23_android_project_2

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.ma23_android_project_2.databinding.ActivityMainBinding
import com.example.ma23_android_project_2.ui.list.ListFragment
import com.example.ma23_android_project_2.ui.map.MapFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import places


class MainActivity : AppCompatActivity(), OnMapReadyCallback{

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var listFragment: ListFragment? = null

    private var mMap : GoogleMap? = null
    var loggedIn : Boolean = false
    private val REQUEST_LOCATION = 1
    lateinit var locationProvider: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback
    private var yourLatitude: Double = 0.0
    private var yourLongitude: Double = 0.0
    private var locationUpdatesStarted = false
    private var mapReady = false
    var loggedInUser = ""
    lateinit var auth : FirebaseAuth
    val location = mutableListOf<places>()

    override fun onCreate(savedInstanceState: Bundle?) {



        getDataFromDB()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_map, R.id.nav_list, R.id.nav_login
            ), drawerLayout
        )

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_list -> {
                    Log.d("MainActivity", "Navigation item 'Lista' clicked")
                    findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.nav_list)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_login -> {
                    Log.d("MainActivity", "Navigation item 'Login' clicked")
                    if (loggedIn) {
                        auth.signOut()
                        loggedInUser = ""
                        loggedIn = false
                        updateLoginMenuItemText("Logga in")
                        getDataFromDB()
                        findNavController(R.id.nav_host_fragment_content_main).popBackStack()
                    } else {
                        findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.nav_login)
                    }
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_map -> {
                    Log.d("MainActivity", "Navigation item 'Map' clicked")
                    findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.nav_map)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                // ... other menu items
                else -> false
            }
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        auth = Firebase.auth
        if(auth.currentUser != null){
            loggedInUser = auth.currentUser?.email ?: "Unknown LOGGED in user"
            loggedIn = true
            updateLoginMenuItemText("Logga ut")

        }

        locationProvider = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.Builder(2000).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                Log.d("MapFragment", "onLocationResult called with ${locationResult.locations.size} locations - in MainActivity")
                for (location in locationResult.locations) {
                    yourLatitude = location.latitude
                    yourLongitude = location.longitude
                }
                if (!locationUpdatesStarted) {
                    locationUpdatesStarted = true
                    if (mapReady) {
                        startLocationUpdates()
                        updateMapLocation()
                    }
                }
            }

        }

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION
            )
        }

    }

    fun closeDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
    }
    fun updateLoginMenuItemText(newText: String) {
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val menu: Menu = navigationView.menu

        val loginMenuItem: MenuItem? = menu.findItem(R.id.nav_login)

        if (loginMenuItem != null) {
            loginMenuItem.title = newText
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    override fun onResume(){
        super.onResume()
        binding.drawerLayout.visibility = View.VISIBLE
        startLocationUpdates()
    }

    override fun onPause(){
        super.onPause()
        stopLocationUpdates()
    }
    fun stopLocationUpdates(){
        if(mapReady)
            locationProvider.removeLocationUpdates(locationCallback)
    }

    fun startLocationUpdates() {
        if (mapReady && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                locationProvider.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_LOCATION){
            Log.d("MapFragment", "onRequestPermissionsResult called")

            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startLocationUpdates()
            }
        }
    }

    private fun updateMapLocation() {
        Log.d("MapFragment", "updateMapLocation called - in MainAcitivity")
        if (mapReady && locationUpdatesStarted && yourLatitude != 0.0 && yourLongitude != 0.0) {
            val yourPosition = LatLng(yourLatitude, yourLongitude)
            val currentZoom = mMap?.cameraPosition?.zoom ?: 13.0f
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(yourPosition, currentZoom))
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("MapFragment", "onMapReady called in MainActivity")
        mMap = googleMap
        mapReady = true
        updateMapLocation()
    }

    fun getDataFromDB() {

        val fragment = supportFragmentManager.findFragmentById(R.id.nav_list)
        if (fragment is ListFragment) {
            listFragment = fragment
            val db = Firebase.firestore
            location.clear()

            val docRef = db.collection("places")
            docRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("Firestore", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    location.clear()

                    val navHostFragment =
                        supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
                    val fragmentList =
                        navHostFragment.childFragmentManager.findFragmentById(R.id.nav_list)

                    Log.d("ListFragment", "FragmentList: $fragmentList")

                    val fragmentTag = "list_fragment"
                    val fragment =
                        navHostFragment?.childFragmentManager?.findFragmentByTag(fragmentTag) as? ListFragment
                    val allFragments =
                        navHostFragment?.childFragmentManager?.fragments?.joinToString(", ") { it.javaClass.simpleName }
                    Log.d("ListFragment", "All Fragments: $allFragments")

                    if (fragment != null) {
                        listFragment = fragment
                        Log.d("ListFragment", "Fragment set!")
                    } else {
                        Log.d("ListFragment", "Fragment NOT set!")
                    }


                    for (document in snapshot.documents) {
                        val place = document.toObject<places>()
                        if (place != null) {
                            place.documentId = document.id
                            location.add(place)
                            Log.d("!!!", place.toString())
                        }
                    }

                }
            }

        }
    }
}