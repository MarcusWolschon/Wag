package biz.wolschon.wag.bluetooth.commands

import androidx.lifecycle.MutableLiveData
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import biz.wolschon.wag.bluetooth.BLECommand
import biz.wolschon.wag.bluetooth.DeviceConnection

class GetBatteryCommand(
    private val batteryPercentage: MutableLiveData<Int?>,
    private val success: MutableLiveData<Boolean>? = null
) : BLECommand() {

    override var expectingResult: Boolean = true
    override val isTailCommand: Boolean = false
    override val isEarCommand: Boolean = true

    override fun execute(deviceConnection: DeviceConnection): Boolean {
        val characteristic = deviceConnection.controlOut
        if (characteristic == null) {
            Log.e(
                TAG,
                "Can't execute GetBatteryCommand because the controlOut characteristic is null"
            )
            expectingResult = false
            return true
        }
        characteristic.value = "BATT".toByteArray()
        val result =  deviceConnection.bluetoothGatt.writeCharacteristic(characteristic)
        expectingResult = result
        return result
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        val battery = characteristic?.getStringValue(0)
        Log.d(TAG, "onCharacteristicChanged '$battery'")
        // result for EarGear: 92
        batteryPercentage.postValue(battery?.toIntOrNull())
        success?.postValue(true)
        expectingResult = false
        super.onCharacteristicChanged(gatt, characteristic)
    }

    companion object {
        private const val TAG = "GetBatteryCommand"
    }
}
