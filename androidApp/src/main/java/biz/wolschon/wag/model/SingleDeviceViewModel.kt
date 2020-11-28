package biz.wolschon.wag.model

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import biz.wolschon.wag.R
import biz.wolschon.wag.bluetooth.BLECommand
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
    val batteryPercentage = MutableLiveData<Int?>().also { it.value = null }
    val batteryIcon = Transformations.map(batteryPercentage) { percentage ->
        ResourcesCompat.getDrawable(
            context.resources,
            when {
                percentage == null -> R.drawable.ic_bluetooth_battery_unknown
                percentage <= 10 -> R.drawable.ic_bluetooth_battery_10
                percentage <= 20 -> R.drawable.ic_bluetooth_battery_20
                percentage <= 30 -> R.drawable.ic_bluetooth_battery_30
                percentage <= 40 -> R.drawable.ic_bluetooth_battery_40
                percentage <= 50 -> R.drawable.ic_bluetooth_battery_50
                percentage <= 60 -> R.drawable.ic_bluetooth_battery_60
                percentage <= 70 -> R.drawable.ic_bluetooth_battery_70
                percentage <= 80 -> R.drawable.ic_bluetooth_battery_80
                percentage <= 90 -> R.drawable.ic_bluetooth_battery_90
                percentage > 90 -> R.drawable.ic_bluetooth_battery_100
                else -> R.drawable.ic_bluetooth_battery_unknown
            },
            context.theme
        )
    }
    private val statusTextResource =
        MutableLiveData<Int>().also { it.value = R.string.status_initializing }
    val statusText = Transformations.map(statusTextResource) {
        if (it == 0) {
            context.getString(R.string.status_initializing)
        } else {
            context.getString(it)
        }
    }
    val address: String = device.address
    val name: String = device.name

    /**
     * Source: https://github.com/MasterTailer/CRUMPET/blob/master/src/BTDeviceModel.cpp#L206
     */
    val isEarGear: Boolean = device.name == BLEConstants.NAME_EARGEAR

    /**
     * Source: https://github.com/MasterTailer/CRUMPET/blob/master/src/BTDeviceModel.cpp#L206
     */
    val isDigitail: Boolean = device.name == BLEConstants.NAME_DIGITAIL
    val displayName =
        Transformations.map(versionText) { versionText -> if (name.isBlank()) "($address) $versionText" else "$name $versionText" }
    val connection = DeviceConnection(
        context = context,
        ready = ready,
        versionText = versionText,
        batteryPercentage = batteryPercentage,
        statusText = statusTextResource,
        device = device
    ) {
        listener.onConnectionLost(this)
    }

    internal fun onDeviceLost() {
        // do any cleanup
    }

    fun isCommandCompatible(cmd: BLECommand): Boolean {
        //TODO: test this
        return (cmd.isEarCommand && isEarGear) || (cmd.isTailCommand && isDigitail)
    }

    fun executeCommand(cmd: BLECommand): Boolean {
        if (!isCommandCompatible(cmd) || ready.value == false) {
            return false
        }
        connection.execute(cmd)
        return true
    }

    interface ConnectionLostListener {
        fun onConnectionLost(singleDevice: SingleDeviceViewModel)
    }

    fun disconnect() {
        connection.disconnect()
    }
}
