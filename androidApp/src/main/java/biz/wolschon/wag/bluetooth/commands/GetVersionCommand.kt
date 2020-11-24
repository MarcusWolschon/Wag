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

class GetVersionCommand(
    private val versionText: MutableLiveData<String>,
    private val success: MutableLiveData<Boolean>? = null
) : BLECommand() {

    override var expectingResult: Boolean = true

    override fun execute(deviceConnection: DeviceConnection): Boolean {
        /*Log.d(TAG, "Reading Status...")
        statusText.postValue(R.string.status_getstatus)
        return bluetoothGatt.readCharacteristic(deviceStatus);*/

        val characteristic = deviceConnection.controlOut
            .apply {
                value = "VER".toByteArray()// ask for firmware version as a test command
                // result: "VER 1.3.2"
            }
        val result =  deviceConnection.bluetoothGatt.writeCharacteristic(characteristic)
        expectingResult = result
        return result
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        val version = characteristic?.getStringValue(0)
        Log.d(TAG, "onCharacteristicChanged '$version'")

        versionText.postValue(version)
        expectingResult = false
        super.onCharacteristicChanged(gatt, characteristic)
    }

    companion object {
        private const val TAG = "GetVersionCommand"
    }
}
