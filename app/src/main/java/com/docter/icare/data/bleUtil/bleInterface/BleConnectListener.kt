package com.docter.icare.data.bleUtil.bleInterface

import android.bluetooth.BluetoothDevice

interface BleConnectListener {
    fun onConnectSuccess(device: BluetoothDevice)
    fun onConnectFinished()
    fun onConnectFailed()
}