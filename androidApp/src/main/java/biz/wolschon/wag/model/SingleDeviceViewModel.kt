package biz.wolschon.wag.model

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Transformations
import biz.wolschon.wag.R
import biz.wolschon.wag.bluetooth.DeviceConnection
import biz.wolschon.wag.bluetooth.commands.Command

/**
 * ViewModel for one of multiple, simultaneous connections to devices. (e.g. EarGear and Tail)
 */
@Suppress("MemberVisibilityCanBePrivate")
class SingleDeviceViewModel(
    context: Context,
    device: BluetoothDevice,
    private val listener: ConnectionLostListener
) {
    val connection = DeviceConnection(
        context = context,
        device = device
    ) {
        listener.onConnectionLost(this)
    }

    val ready = connection.ready

    val versionText = connection.versionText

    val batteryPercentage = connection.batteryPercentage

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
    private val statusTextResource = connection.statusText
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


    internal fun onDeviceLost() {
        // do any cleanup
    }

    fun isCommandCompatible(cmd: Command): Boolean {
        return (cmd.isEarCommand && isEarGear) || (cmd.isTailCommand && isDigitail)
    }

    fun executeCommand(cmd: Command): Boolean {
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
