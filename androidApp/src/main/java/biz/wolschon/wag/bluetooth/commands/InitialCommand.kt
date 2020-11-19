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
    private val statusText: MutableLiveData<Int>,
    private val ready: MutableLiveData<Boolean>
) : BLECommand() {

    override val expectingResult: Boolean = true

    override fun execute(connection: DeviceConnection): Boolean {
        /*Log.d(TAG, "Reading Status...")
        statusText.postValue(R.string.status_getstatus)
        return bluetoothGatt.readCharacteristic(deviceStatus);*/

        val characteristic = connection.controlOut
            .apply {
                value = "VER".toByteArray()// ask for firmware version as a test command
            }
        return connection.bluetoothGatt.writeCharacteristic(characteristic)
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        Log.d(TAG, "onCharacteristicChanged ${characteristic?.getStringValue(0)}")
        super.onCharacteristicChanged(gatt, characteristic)
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic?,
        commandStatus: Int
    ) {

        Log.d(TAG, "onCharacteristicRead ${characteristic?.getStringValue(0)}")
        /*val status = Status.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
        Log.d(TAG, "Status = 0x" + status.toString(16))*/
        ready.postValue(true)
        //statusText.postValue(R.string.status_ready)
    }

    companion object {
        private const val TAG = "InitialCommand"
    }
}
