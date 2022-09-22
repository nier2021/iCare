package com.docter.icare.data.bleUtil.bleInterface

interface BleDataReceiveListener {
    fun onAirReceive(data: ByteArray){}
    fun onRadarData(data: ByteArray){}
}