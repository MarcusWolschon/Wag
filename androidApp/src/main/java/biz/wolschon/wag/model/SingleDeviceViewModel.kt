package biz.wolschon.wag.model

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.MutableLiveData
import biz.wolschon.wag.R
import biz.wolschon.wag.bluetooth.DeviceConnection

/**
 * ViewModel for one of multiple, simultaneous connections to devices. (e.g. EarGear and Tail)
 */
class SingleDeviceViewModel(context: Context,
                            device: BluetoothDevice,
                            private val listener: ConnectionLostListener) { //TODO: call the listener
    val ready = MutableLiveData<Boolean>()
    val statusText = MutableLiveData<Int>().also { it.value = R.string.status_initializing }
    val connection = DeviceConnection(
                context    = context,
                adapter    = BluetoothAdapter.getDefaultAdapter(),
                ready      = ready,
                statusText = statusText,
                device     = device
            )
    val address: String = device.address
    val name: String = device.name
    val displayName: String = "$name ($address)"
    internal fun onDeviceLost() {
        // do any cleanup
    }


    interface ConnectionLostListener {
        fun onConnectionLost(singleDevice: SingleDeviceViewModel)
    }

    fun disconnect() {
        connection.disconnect()
    }
}
