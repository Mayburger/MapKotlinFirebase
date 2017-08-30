package com.nacoda.mapkotlinfirebase

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.support.v4.app.FragmentActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.FirebaseDatabase
import com.nacoda.mapkotlinfirebase.Retrofit.GPSTracker
import com.nacoda.mapkotlinfirebase.Retrofit.InitRetrofit
import com.nacoda.mapkotlinfirebase.Retrofit.Lokasi
import com.nacoda.mapkotlinfirebase.Retrofit.ResponseJSON
import com.project.dzakdzak.mapskotlinfirebase.DirectionMapsV2
import kotlinx.android.synthetic.main.content_maps.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.GoogleMap


class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    var awal: LatLng? = null
    var akhir: LatLng? = null
    var gps: GPSTracker? = null

    var lat: Double? = null
    var lon: Double? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        gps = GPSTracker(this@MapsActivity)

        if (gps!!.canGetLocation) {
            lat = gps!!.getLatitude()
            lon = gps!!.getLongitude()


        } else {
            gps!!.showSettingGps()
        }


        tvFrom.setOnClickListener {


            getLocation(1)
        }

        tvTo.setOnClickListener {

            getLocation(2)
        }

        var permission = (android.Manifest.permission.ACCESS_COARSE_LOCATION)
        ActivityCompat.requestPermissions(this@MapsActivity, arrayOf(permission), 2)


    }

    fun getLocation(code: Int) {
        val typeFilter = AutocompleteFilter.Builder()
                .setTypeFilter(Place.TYPE_COUNTRY).setCountry("ID")
                .build()
        var intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                .setFilter(typeFilter)
                .build(this@MapsActivity)
        startActivityForResult(intent, code)
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


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.checkSelfPermission(this@MapsActivity,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this@MapsActivity,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }

        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var criteria = Criteria()

        var location = locationManager.getLastKnownLocation(locationManager
                .getBestProvider(criteria, false))
        var latitude = location.latitude
        var longitude = location.longitude

        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(latitude, longitude)
        mMap!!.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 17.toFloat()))


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == 1 && resultCode != null) {

                //get data return
                var place = PlaceAutocomplete.getPlace(this, data)
                var lat = place.latLng.latitude
                var long = place.latLng.longitude

                //include latlong
                awal = LatLng(lat, long)

                mMap!!.clear()

                tvFrom.setText(place.address.toString())


                //add marker
                mMap!!.addMarker(MarkerOptions().position(awal!!)
                        .title(place.address.toString())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))
                if (tvTo.text.toString().isNotEmpty()) {
                    mMap!!.addMarker(MarkerOptions().position(akhir!!)
                            .title(place.address.toString())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))

                }
                //set camera zoom
                var location = CameraUpdateFactory.newLatLngZoom(awal, 15.toFloat())
                mMap!!.animateCamera(location)

            } else if (requestCode == 2 && resultCode != null) {

                //get data return
                var place = PlaceAutocomplete.getPlace(this, data)
                var lat = place.latLng.latitude
                var long = place.latLng.longitude

                //include latlong
                akhir = LatLng(lat, long)

                mMap!!.clear()

                tvTo.setText(place.address.toString())
                //add marker
                ActionRoute()
                mMap!!.addMarker(MarkerOptions().position(akhir!!)
                        .title(place.address.toString())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                if (tvFrom.text.toString().isNotEmpty()) {
                    mMap!!.addMarker(MarkerOptions().position(awal!!)
                            .title(place.address.toString())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))

                    var lokasi = Lokasi(
                            tvFrom.text.toString(),
                            tvTo.text.toString(),
                            tvDistance.text.toString(),
                            tvFare.text.toString(),
                            tvTime.text.toString(),
                            lat.toString(),
                            lon.toString()
                    )

                    var database = FirebaseDatabase.getInstance().getReference()
                    var key = database.push().key
                    database.child(key).setValue(lokasi)

                }
                //set camera zoom
                var builder = LatLngBounds.Builder()
                builder.include(awal)
                builder.include(akhir)
                var bounds = builder.build()

                val cu = CameraUpdateFactory.newLatLngBounds(bounds, 70)
                mMap!!.animateCamera(cu, object : GoogleMap.CancelableCallback {
                    override fun onCancel() {}
                    override fun onFinish() {
                        val zout = CameraUpdateFactory.zoomBy(-1.0f)
                        mMap!!.animateCamera(zout)
                    }
                })

//                tvTime.setText("" + akhir)

                val uri = "geo: " + lat + "," + long +
                        "?q=" + lat + "," + long
                startActivity(Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)))

//                var location = CameraUpdateFactory.newLatLngZoom(akhir, 15.toFloat())
//                mMap!!.animateCamera(location)


            } else if (resultCode == 0) {
                Toast.makeText(applicationContext, "Belum Pilih Lokasi Bro", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {

        }
    }

//    private fun convertCoordinateToName(lat: Double, lon: Double): String {
//
//        var nameLocation: String? = null
//        try {
//
//
//            var geoCoder = Geocoder(this@MapsActivity, Locale.getDefault())
//            var insertCoor: List<Address> = geoCoder.getFromLocation(lat,lon,1)
//
//            if (insertCoor.size > 0) {
//                nameLocation = insertCoor.get(0).getAddressLine(0)
//            }
//
//            return nameLocation!!
//        } catch (e : Exception){
//            Toast.makeText(applicationContext, "hey", Toast.LENGTH_LONG).show()
//        } catch (nul : KotlinNullPointerException){
//            Toast.makeText(applicationContext, "null", Toast.LENGTH_LONG).show()
//        }
//
//        return nameLocation!!
//
//    }

    private fun ActionRoute() {

        //get init retrofit
        var api = InitRetrofit().getInitInstance()

        //request to server base end point
        var call = api.request_route(tvFrom.text.toString(),
                tvTo.text.toString(), "walking")

        //walking,transit,driving,bycicyle

        //get response
        call.enqueue(object : Callback<ResponseJSON> {
            override fun onResponse(call: Call<ResponseJSON>?, response: Response<ResponseJSON>?) {

                Log.d("response : ", response?.message())
                if (response != null) {
                    if (response.isSuccessful) {
                        layoutTravelDescription.visibility = View.VISIBLE
                        //get json array route
                        var route = response.body()?.routes
                        //get object overview polyline
                        var overview = route?.get(0)?.overviewPolyline
                        //get string json point
                        var point = overview?.points

                        var direction = DirectionMapsV2(this@MapsActivity)
                        direction.gambarRoute(mMap!!, point!!)

                        var legs = route?.get(0)?.legs

                        var distance = legs?.get(0)?.distance
                        tvDistance.setText("Distance (" + distance?.text.toString() + ")")

//                        var duration = legs?.get(0)?.duration
//                        tvTime.setText(duration?.text.toString())

                        var dist = Math.ceil(distance?.value?.toDouble()!! / 1000)
                        tvFare.setText("Rp. " + (dist * 5000).toString())

                    }
                    //                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

                }
            }

            override fun onFailure(call: Call<ResponseJSON>?, t: Throwable?) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })


        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 2) {
            mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
            mMap!!.isMyLocationEnabled = true
            mMap!!.uiSettings.isCompassEnabled = true
            mMap!!.uiSettings.setAllGesturesEnabled(true)
            mMap!!.isMyLocationEnabled = true
            mMap!!.uiSettings.isMyLocationButtonEnabled = true
            mMap!!.isTrafficEnabled = true
        }
    }
}
