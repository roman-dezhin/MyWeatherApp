package com.github.romandezhin.myweatherapp.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.github.romandezhin.myweatherapp.R
import com.github.romandezhin.myweatherapp.databinding.FragmentMapBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap
    private var marker: Marker? = null
    private lateinit var mapViewModel: MapViewModel
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                enableMyLocation()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mapViewModel = ViewModelProvider(this).get(MapViewModel::class.java)
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        val mapViewBundle = savedInstanceState?.getBundle(MAPVIEW_BUNDLE_KEY)
        mapView = binding.mapView
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY) ?: Bundle().also {
            outState.putBundle(MAPVIEW_BUNDLE_KEY, it)
        }
        mapView.onSaveInstanceState(mapViewBundle)
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap ?: return
        enableMyLocation()
        map.setOnMapClickListener {
            marker?.remove()
            marker = map.addMarker(MarkerOptions().position(it))
        }
        map.setOnMarkerClickListener {
            if (it == marker) {
                findNavController().navigate(R.id.navigation_today)
            }
            false
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (!::map.isInitialized) return
        val permission = Manifest.permission.ACCESS_COARSE_LOCATION
        when {
            ContextCompat.checkSelfPermission(requireActivity(), permission)
                    == PackageManager.PERMISSION_GRANTED -> {
                map.isMyLocationEnabled = true
                moveMyLocationButton()
            }
            shouldShowRequestPermissionRationale(permission) -> askUserForOpeningAppSettings()
            else -> requestPermissionLauncher.launch(permission)
        }
    }

    private fun moveMyLocationButton() {
        if (mapView.findViewById<View>(Integer.parseInt("1")) != null) {
            val locationButton =
                (mapView.findViewById<View>("1".toInt()).parent as View).findViewById<View>("2".toInt())
            val rlp = locationButton.layoutParams as RelativeLayout.LayoutParams
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
            rlp.setMargins(0, 0, MY_LOCATION_BUTTON_MARGIN, MY_LOCATION_BUTTON_MARGIN)
        }
    }

    private fun askUserForOpeningAppSettings() {
        val appSettingsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireContext().packageName, null)
        )
        if (activity?.packageManager?.resolveActivity(
                appSettingsIntent,
                PackageManager.MATCH_DEFAULT_ONLY
            ) == null
        ) {
            Toast.makeText(requireContext(), R.string.permission_denied_forever, Toast.LENGTH_SHORT)
                .show()
        } else {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.permission_denied)
                .setMessage(R.string.permission_denied_forever_message)
                .setPositiveButton(R.string.open) { _, _ ->
                    startActivity(appSettingsIntent)
                }
                .setNegativeButton(R.string.no_thanks) { _, _ ->
                }
                .create()
                .show()
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
        private const val MY_LOCATION_BUTTON_MARGIN = 32
    }
}
