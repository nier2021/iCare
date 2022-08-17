package com.docter.icare.data.bleUtil.bleInterface

interface BleDataReceiveListener {
    fun onReceive(data: ByteArray)
}