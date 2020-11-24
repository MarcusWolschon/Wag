package biz.wolschon.wag.model

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import biz.wolschon.wag.R
import biz.wolschon.wag.bluetooth.DeviceConnection

/**
 * ViewModel for one of multiple, simultaneous connections to devices. (e.g. EarGear and Tail)
 */
class SingleDeviceViewModel(
    context: Context,
    device: BluetoothDevice,
    private val listener: ConnectionLostListener
) {
    val ready = MutableLiveData<Boolean>()
    val versionText = MutableLiveData<String>().also { it.value = "" }
    val batteryText = MutableLiveData<String>().also { it.value = "" }
    private val statusTextResource = MutableLiveData<Int>().also { it.value = R.string.status_initializing }
    val statusText = Transformations.map(statusTextResource) {
        if (it == 0) {
            context.getString(R.string.status_initializing)
        } else {
            context.getString(it)
        }
    }
    val connection = DeviceConnection(
        context = context,
        adapter = BluetoothAdapter.getDefaultAdapter(),
        ready = ready,
        versionText = versionText,
        batteryText = batteryText,
        statusText = statusTextResource,
        device = device,
        onDisconnect = {
            listener.onConnectionLost(this)
        }
    )
    val address: String = device.address
    val name: String = device.name
    val displayName = Transformations.map(versionText) { versionText -> if (name.isBlank()) "($address) $versionText" else "$name $versionText" }
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
