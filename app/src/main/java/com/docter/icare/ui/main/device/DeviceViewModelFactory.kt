package com.docter.icare.ui.main.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.docter.icare.data.repositories.DeviceRepository
import com.docter.icare.data.repositories.RadarRepository

@Suppress("UNCHECKED_CAST")
class DeviceViewModelFactory(
    private val deviceRepository: DeviceRepository,
    private val radarRepository: RadarRepository
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = DeviceViewModel(deviceRepository, radarRepository) as T
}