package ru.neosvet.moviedb.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.VisibleRegionUtils
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.*
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider
import ru.neosvet.moviedb.R

class MapsFragment : Fragment(), Session.SearchListener {
    companion object {
        private var isNotInit = true
        private val ARG_QUERY = "query"

        @JvmStatic
        fun newInstance(query: String) =
            MapsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_QUERY, query)
                }
            }
    }

    private lateinit var map: MapView
    private lateinit var searchSession: Session

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (isNotInit) {
            MapKitFactory.setApiKey(getString(R.string.yandex_maps_key))
            MapKitFactory.initialize(requireContext())
            SearchFactory.initialize(requireContext())
            isNotInit = false
        }
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        map = view.findViewById(R.id.map)
        arguments?.getString(ARG_QUERY)?.let {
            showPlace(it)
        }
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        map.onStart()
    }

    override fun onStop() {
        searchSession.cancel()
        map.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    private fun showPlace(place: String) {
        val searchManager: SearchManager =
            SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
        searchSession = searchManager.submit(
            place,
            VisibleRegionUtils.toPolygon(map.getMap().getVisibleRegion()),
            SearchOptions(),
            this
        )
    }

    override fun onSearchResponse(response: Response) {
        if (response.collection.children.size == 0)
            return
        response.collection.children[0].obj?.geometry?.get(0)?.getPoint()?.let {
            addMarker(it)

            map.getMap().move(
                CameraPosition(it, 5.0f, 0.0f, 0.0f),
                Animation(Animation.Type.SMOOTH, 0f),
                null
            )
        }
    }

    override fun onSearchError(error: Error) {
    }

    private fun addMarker(point: Point) {
        val mapObjects: MapObjectCollection = map.getMap().getMapObjects()
        mapObjects.clear()
        mapObjects.addPlacemark(
            point,
            ImageProvider.fromResource(requireContext(), R.drawable.marker),
            IconStyle()
        )
    }
}