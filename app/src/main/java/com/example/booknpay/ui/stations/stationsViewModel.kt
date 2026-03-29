package com.example.booknpay.ui.stations

import androidx.lifecycle.ViewModel
import com.example.booknpay.data.model.Station
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class StationViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _stations = MutableStateFlow<List<Station>>(emptyList())
    val stations: StateFlow<List<Station>> = _stations.asStateFlow()

    init {
        fetchStations()
    }

    private fun fetchStations() {
        firestore.collection("stations")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val stationList = snapshot?.toObjects(Station::class.java) ?: emptyList()
                _stations.value = stationList
            }
    }
}