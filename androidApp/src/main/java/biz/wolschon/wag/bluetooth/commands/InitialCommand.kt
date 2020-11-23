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

class InitialCommand(
    private val versionText: MutableLiveData<String>,
    private val ready: MutableLiveData<Boolean>
) : BLECommand() {

    override val expectingResult: Boolean = true

    override fun execute(deviceConnection: DeviceConnection): Boolean {
        /*Log.d(TAG, "Reading Status...")
        statusText.postValue(R.string.status_getstatus)
        return bluetoothGatt.readCharacteristic(deviceStatus);*/

        val characteristic = deviceConnection.controlOut
            .apply {
                value = "VER".toByteArray()// ask for firmware version as a test command
                // result: "VER 1.3.2"
            }
        return deviceConnection.bluetoothGatt.writeCharacteristic(characteristic)
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        val version = characteristic?.getStringValue(0)
        Log.d(TAG, "onCharacteristicChanged '$version'")

        versionText.postValue(version)
        super.onCharacteristicChanged(gatt, characteristic)
    }

    companion object {
        private const val TAG = "InitialCommand"
    }
}
