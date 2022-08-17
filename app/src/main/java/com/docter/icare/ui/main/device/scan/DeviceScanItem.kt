package com.docter.icare.ui.main.device.scan

import android.util.Log
import android.view.View
import com.docter.icare.R
import com.docter.icare.data.entities.view.item.DeviceScanItemEntity
import com.docter.icare.databinding.ItemDeviceScanBinding
import com.xwray.groupie.viewbinding.BindableItem

class DeviceScanItem(
    private val deviceScanItemEntity:DeviceScanItemEntity,
    private val itemClickListener: ItemClickListener
): BindableItem<ItemDeviceScanBinding>() {

    override fun getLayout(): Int = R.layout.item_device_scan

    override fun initializeViewBinding(v: View): ItemDeviceScanBinding = ItemDeviceScanBinding.bind(v)

    override fun bind(viewBinding: ItemDeviceScanBinding, position: Int) {
        Log.i("DeviceScanItem","deviceScanItemEntity=>$deviceScanItemEntity")
        with(viewBinding){
            entity = deviceScanItemEntity
            this.position = position
            click = View.OnClickListener {
                itemClickListener.onClick(deviceScanItemEntity)
            }
        }
    }

    fun interface ItemClickListener {
        fun onClick(data: DeviceScanItemEntity)
    }
}