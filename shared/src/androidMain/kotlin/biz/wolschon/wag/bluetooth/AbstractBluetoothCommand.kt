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

actual abstract class AbstractBluetoothCommand() : BLECommand() {

    fun sendCommand(cmd: String): Boolean {
        val characteristic = deviceConnection.controlOut
            .apply {
                value = cmd.toByteArray()
            }
        val result =  deviceConnection.bluetoothGatt.writeCharacteristic(characteristic)
        expectingResult = result
        return result
    }

    open fun onResultReceived(result: String) {
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        val result = characteristic?.getStringValue(0)
        Log.d(TAG, "onCharacteristicChanged '$result'")
        onResultReceived(result)
        super.onCharacteristicChanged(gatt, characteristic)
    }

    companion object {
        private const val TAG = "AbstractBluetoothCommand"
    }
}
