package com.selimcinar.mapskotlin.view

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.room.Delete
import androidx.room.Room
import androidx.room.RoomDatabase

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.selimcinar.mapskotlin.R
import com.selimcinar.mapskotlin.databinding.ActivityMapsBinding
import com.selimcinar.mapskotlin.model.Place
import com.selimcinar.mapskotlin.roomdb.PlaceDao
import com.selimcinar.mapskotlin.roomdb.PlaceDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences
    private var trackBoolean : Boolean? = null
    private var selectedLatiude :Double? = null
    private var selectedLongitude : Double?=null
    private lateinit var db : PlaceDatabase
    private  lateinit var placeDao:PlaceDao
    val compositeDisposable = CompositeDisposable()
    var placeFromMain : Place? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        registerLauncher()

        sharedPreferences = this.getSharedPreferences("com.selimcinar.mapskotlin", MODE_PRIVATE)
        trackBoolean = false

        selectedLatiude=0.0
        selectedLongitude=0.0
        // Places veritabanı ismi
        db= Room.databaseBuilder(applicationContext,PlaceDatabase::class.java,"Places")
            //  .allowMainThreadQueries()  arka plansız işlemi yapar.
            .build()

        placeDao = db.placeDao()

        binding.placeSave.isEnabled = false
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
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

        val intent = intent
        val info = intent.getStringExtra("info")
        if (info == "new") {
            binding.placeSave.visibility = View.VISIBLE
            binding.placeDelete.visibility = View.GONE
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    trackBoolean = sharedPreferences.getBoolean("trackBoolean", false)
                    if (!trackBoolean!!) {
                        //KONUM DEĞİŞTİNDE AL
                        val userLocation = LatLng(location.latitude, location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                        sharedPreferences.edit().putBoolean("trackBoolean", true).apply()
                    }

                }

                override fun onProviderDisabled(provider: String) {
                    // Konum sağlayıcı devre dışı bırakıldığında yapılacak işlemler
                    Toast.makeText(
                        this@MapsActivity,
                        "Konum sağlayıcı devre dışı bırakıldı.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                //buraya android işletim sistemi karar verir.
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) {
                    Snackbar.make(
                        binding.root,
                        "Permission needed for location",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("Give permission") {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }.show()
                } else {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            } else {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    0f,
                    locationListener
                )
                val lastLocation =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null) {
                    val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15f))
                }
                mMap.isMyLocationEnabled = true
            }
        } else {
            mMap.clear()
            placeFromMain=intent.getSerializableExtra("selectedPlace") as? Place

            placeFromMain?.let {
                val latLng = LatLng(it.latitude,it.longitude)
                mMap.addMarker(MarkerOptions().position(latLng).title(it.name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15f))

                binding.placeText.setText(it.name)
                binding.placeSave.visibility=View.GONE
                binding.placeDelete.visibility=View.VISIBLE
            }

            }


            /*
        // Add a marker in Sydney and move the camera
        val eiffel = LatLng(48.853915, 2.2913515)
        mMap.addMarker(MarkerOptions().position(eiffel).title("Marker in Eifell"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(eiffel))

         */


        }


    private fun registerLauncher(){
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            result ->
            if (result){
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0,
                        0f,
                        locationListener
                    )
                    val  lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastLocation!=null){
                        val lastUserLocation = LatLng(lastLocation.latitude,lastLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15f))
                    }
                    mMap.isMyLocationEnabled=true
                }
            }
            else {
                Toast.makeText(this@MapsActivity,"Permission needed!",Toast.LENGTH_LONG).show()

            }
        }
    }

    override fun onMapLongClick(p0: LatLng) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(p0))

        selectedLatiude=p0.latitude
        selectedLongitude=p0.longitude
        binding.placeSave.isEnabled=true
    }
    
    fun save (view:View){

      if (selectedLatiude!=null && selectedLongitude!=null)
        {
            val place = Place(binding.placeText.text.toString(),selectedLatiude!!,selectedLongitude!!)
          compositeDisposable.add(
              placeDao.insert(place)
                  .subscribeOn(Schedulers.io()) //arkaplanda çalıştır
                  .observeOn(AndroidSchedulers.mainThread()) //main thread gözlemle
                  .subscribe(this::handleResponse) // şu fonksiyonu çalıştır
          )
        }
    }

    private  fun handleResponse(){
        val  intent = Intent(this,MapsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

    }

    fun delete(view: View){
        placeFromMain?.let {
            compositeDisposable.add(
                placeDao.delete(it)
                    .subscribeOn(Schedulers.io()) //arkaplanda çalıştır
                    .observeOn(AndroidSchedulers.mainThread()) //main thread gözlemle
                    .subscribe(this::handleResponse) // şu fonksiyonu çalıştır
            )
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}