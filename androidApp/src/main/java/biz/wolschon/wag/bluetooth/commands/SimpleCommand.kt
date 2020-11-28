package biz.wolschon.wag.bluetooth.commands

import android.util.Log
import biz.wolschon.wag.bluetooth.BLECommand
import biz.wolschon.wag.bluetooth.DeviceConnection

/**
 * A simple command that expects no answer.
 */
open class SimpleCommand(
    private val commandString: String,
    forTail: Boolean,
    forEarGear: Boolean
) : BLECommand() {

    override val isTailCommand: Boolean = forTail
    override val isEarCommand: Boolean = forEarGear

    override fun execute(deviceConnection: DeviceConnection): Boolean {
        Log.d(TAG, "performing simple command $commandString")
        val characteristic = deviceConnection.controlOut
        if (characteristic == null) {
            Log.e(TAG, "Can't execute SimpleCommand($commandString) because the controlOut characteristic is null")
            return false
        }
        characteristic.value = commandString.toByteArray()
        return deviceConnection.bluetoothGatt.writeCharacteristic(characteristic)
    }

    companion object {
        private const val TAG = "SimpleCommand"
    }
}
