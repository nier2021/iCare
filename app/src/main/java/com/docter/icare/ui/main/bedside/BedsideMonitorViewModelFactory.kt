package com.docter.icare.ui.main.bedside

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.docter.icare.data.repositories.DeviceRepository
import com.docter.icare.data.repositories.RadarRepository

@Suppress("UNCHECKED_CAST")
class BedsideMonitorViewModelFactory (
    private val radarRepository: RadarRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = BedsideMonitorViewModel(radarRepository) as T
}