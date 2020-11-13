package biz.wolschon.wag.bluetooth.commands

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT16
import android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8
import android.bluetooth.BluetoothGattDescriptor
import android.util.Log
import androidx.lifecycle.MutableLiveData
import biz.wolschon.wag.BuildConfig
import biz.wolschon.wag.bluetooth.BLECommand
//import biz.wolschon.wag.model.*


class SubscribeControlMessagesCommand(private val controlIn: BluetoothGattCharacteristic) : BLECommand() {
    @ExperimentalUnsignedTypes
    override fun execute(bluetoothGatt: BluetoothGatt): Boolean {
        if (!bluetoothGatt.setCharacteristicNotification(controlIn, true)) {
            Log.e(TAG, "setCharacteristicNotification failed")
        }
        val descriptor = controlIn.descriptors[0]
                .apply {
                    value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE

                }
        return bluetoothGatt.writeDescriptor(descriptor)
    }


    @ExperimentalUnsignedTypes
    companion object {
        private const val TAG = "SubscribeCtrlMsgCmd"

        fun onControlMessageCharacteristicChanged(gatt: BluetoothGatt,
                                            characteristic: BluetoothGattCharacteristic,
                                                  viewModel: DeviceDetailsViewModel) {
              //TODO
        }
    }

}
