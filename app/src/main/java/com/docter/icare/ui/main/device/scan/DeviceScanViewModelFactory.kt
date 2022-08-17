package com.docter.icare.ui.main.device.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.docter.icare.data.repositories.DeviceRepository

@Suppress("UNCHECKED_CAST")
class DeviceScanViewModelFactory(
    private val deviceRepository: DeviceRepository
) : ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T = DeviceScanViewModel(deviceRepository) as T
}