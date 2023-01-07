package com.example.mymaps

import android.app.Activity
import android.content.ClipDescription
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.mymaps.databinding.ActivityCreateMapBinding
import com.example.mymaps.models.Place
import com.example.mymaps.models.UserMap
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar

class CreateMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityCreateMapBinding
    private var markers:MutableList<Marker> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = intent.getStringExtra(EXTRA_MAP_TITLE)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mapFragment.view?.let {
            Snackbar.make(it,"Long press to add a marker!", Snackbar.LENGTH_INDEFINITE)
                .setAction("OK",{})
                .setActionTextColor(ContextCompat.getColor(this, android.R.color.white))
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_create_map, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        Check if item selected is 'save' menu icon
        if(item.itemId == R.id.miSave){
            if (markers.isEmpty()){
                Toast.makeText(this,"There must be at-least one marker on the map", Toast.LENGTH_SHORT).show()
                return true
            }
            val places = markers.map { marker-> Place(marker.title!!,marker.snippet!!, marker.position.latitude, marker.position.longitude) }
            val userMap = intent.getStringExtra(EXTRA_MAP_TITLE)?.let { UserMap(it,places) }
            val data = Intent()
            data.putExtra(EXTRA_USER_MAP,userMap)
            setResult(Activity.RESULT_OK,data)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnInfoWindowClickListener {markerToDelete->
            markers.remove(markerToDelete)
            markerToDelete.remove()
        }

        mMap.setOnMapLongClickListener {latLng->
            showAlertDialog(latLng)

        }
//        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun showAlertDialog(latLng: LatLng) {
        val placeFormView = LayoutInflater.from(this).inflate(R.layout.dialog_create_place, null)
        val dialog =
            AlertDialog.Builder(this)
                .setTitle("Create a Market")
                .setView(placeFormView)
                .setNegativeButton("Cancel",null)
                .setPositiveButton("OK",null)
                .show()

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val title = placeFormView.findViewById<EditText>(R.id.etTitle).text.toString()
            val description = placeFormView.findViewById<EditText>(R.id.etDescription).text.toString()

            if(title.trim().isEmpty() || description.trim().isEmpty()){
                Toast.makeText(this,"Both Fields must be filled",Toast.LENGTH_SHORT)
                return@setOnClickListener
            }
            val marker = mMap.addMarker(MarkerOptions().position(latLng).title(title).snippet(description))
            if (marker != null) {
                markers.add(marker)
            }
            dialog.dismiss()

        }


    }
}