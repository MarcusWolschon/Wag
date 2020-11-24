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

class GetBatteryCommand(
    private val batteryText: MutableLiveData<String>,
    private val success: MutableLiveData<Boolean>? = null
) : BLECommand() {

    override var expectingResult: Boolean = true
    override val isTailCommand: Boolean = false
    override val isEarCommand: Boolean = true

    override fun execute(deviceConnection: DeviceConnection): Boolean {
        val characteristic = deviceConnection.controlOut
            .apply {
                value = "BATT".toByteArray()
            }
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
//TODO: interpret batteryText into a percentage value
        batteryText.postValue(battery)
        success?.postValue(true)
        expectingResult = false
        super.onCharacteristicChanged(gatt, characteristic)
    }

    companion object {
        private const val TAG = "GetBatteryCommand"
    }
}
