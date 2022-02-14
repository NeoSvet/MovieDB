package ru.neosvet.moviedb.view

import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ru.neosvet.moviedb.R
import java.io.IOException

class MapsFragment : Fragment() {
    companion object {
        private const val ARG_QUERY = "query"

        @JvmStatic
        fun newInstance(query: String) =
            MapsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_QUERY, query)
                }
            }
    }

    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(onMapReady)
    }

    private val onMapReady = OnMapReadyCallback { googleMap ->
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        arguments?.getString(ARG_QUERY)?.let {
            showPlace(it)
        }
    }

    private fun showPlace(place: String) {
        Thread {
            try {
                val geoCoder = Geocoder(requireContext())
                val addresses = geoCoder.getFromLocationName(place, 1)
                if (addresses.size == 0)
                    return@Thread

                val location = LatLng(
                    addresses[0].latitude,
                    addresses[0].longitude
                )
                view?.post {
                    addMarker(location, place)
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(location, 5f)
                    )
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun addMarker(location: LatLng, title: String): Marker? {
        return map.addMarker(
            MarkerOptions()
                .position(location)
                .title(title)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
        )
    }
}