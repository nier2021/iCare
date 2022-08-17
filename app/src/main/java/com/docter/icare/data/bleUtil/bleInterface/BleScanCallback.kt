package com.docter.icare.data.bleUtil.bleInterface

import com.docter.icare.data.entities.view.item.DeviceScanItemEntity

interface BleScanCallback {
    fun onDeviceFound(entity: DeviceScanItemEntity)
    fun onScanFinish()
}