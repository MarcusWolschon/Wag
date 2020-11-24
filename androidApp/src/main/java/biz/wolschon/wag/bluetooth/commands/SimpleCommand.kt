package biz.wolschon.wag.bluetooth.commands

import androidx.lifecycle.MutableLiveData
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.util.Log
import biz.wolschon.wag.bluetooth.BLECommand
import biz.wolschon.wag.bluetooth.BLECommandQueue
import biz.wolschon.wag.R
import biz.wolschon.wag.bluetooth.DeviceConnection

/**
 * A simple command that expects no answer.
 */
class SimpleCommand(
    private val commandString: String,
    forTail: Boolean,
    forEarGear: Boolean
) : BLECommand() {
,

    override val isTailCommand: Boolean = forTail
    override val isEarCommand: Boolean = forEarGear

    override fun execute(deviceConnection: DeviceConnection): Boolean {
        val characteristic = deviceConnection.controlOut
            .apply { value = commandString.toByteArray() }
        return deviceConnection.bluetoothGatt.writeCharacteristic(characteristic)
    }

    companion object {
        private const val TAG = "SimpleCommand($commandString)"
    }
}
