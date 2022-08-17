package com.docter.icare.ui.main.uam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.docter.icare.data.repositories.RadarRepository

@Suppress("UNCHECKED_CAST")
class ActivityMonitoringViewModelFactory (
    private val radarRepository: RadarRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ActivityMonitoringViewModel(radarRepository) as T
}