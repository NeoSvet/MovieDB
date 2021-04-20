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
import com.google.android.gms.maps.model.LatLng
import ru.neosvet.moviedb.R
import ru.neosvet.moviedb.databinding.FragmentMapsBinding
import java.io.IOException

class MapsFragment : Fragment() {
    companion object {
        @JvmStatic
        fun newInstance(country: String) =
            MapsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_COUNTRY, country)
                }
            }
    }

    private val ARG_COUNTRY = "country"
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
        arguments?.getString(ARG_COUNTRY)?.let {
            showCountry(it)
        }
    }

    private fun showCountry(country: String) {
        Thread {
            try {
                val geoCoder = Geocoder(requireContext())
                val addresses = geoCoder.getFromLocationName(country, 1)
                if (addresses.size == 0)
                    return@Thread

                val location = LatLng(
                    addresses[0].latitude,
                    addresses[0].longitude
                )
                view?.post {
                    // setMarker(location, country, R.drawable.ic_map_marker)
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(location, 5f)
                    )
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }
}