package biz.wolschon.wag.bluetooth.commands

import androidx.lifecycle.MutableLiveData
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import biz.wolschon.wag.bluetooth.BLECommand
import biz.wolschon.wag.bluetooth.BLECommandQueue
import biz.wolschon.wag.R

class InitialCommand(private val statusText: MutableLiveData<Int>,
                     private val ready: MutableLiveData<Boolean>,
                     private val nextCommands: Array<BLECommand>,
                     private val workQueue : BLECommandQueue) : BLECommand() {

    override fun execute(bluetoothGatt: BluetoothGatt): Boolean {
        /*Log.d(TAG, "Reading Status...")
        statusText.postValue(R.string.status_getstatus)
        return bluetoothGatt.readCharacteristic(cameraStatus);*/
        return true
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic?, commandStatus: Int) {
        /*val status = Status.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
        Log.d(TAG, "Status = 0x" + status.toString(16))*/
        ready.postValue(true)
        //statusText.postValue(R.string.status_ready)
        nextCommands.forEach { workQueue.addCommand(it) }
    }

    companion object {
        private const val TAG = "InitialCommand"
    }
}
