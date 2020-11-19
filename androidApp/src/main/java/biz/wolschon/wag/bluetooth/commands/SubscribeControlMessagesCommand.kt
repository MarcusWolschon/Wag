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
import biz.wolschon.wag.bluetooth.DeviceConnection
import biz.wolschon.wag.model.*


class SubscribeControlMessagesCommand() : BLECommand() {
    @ExperimentalUnsignedTypes
    override fun execute(connection: DeviceConnection): Boolean {

        // tell BLE to expect notifications
        if (!connection.bluetoothGatt.setCharacteristicNotification(connection.controlIn, true)) {
            Log.e(TAG, "setCharacteristicNotification failed")
        }

        // tell the device to send indications(confirmed by us) or notifications(not confirmed by us)
//        connection.controlIn.descriptors.forEach { descriptor ->
//            Log.d(TAG,"descriptor: ${descriptor.uuid}") // only 1 "00002902-0000-1000-8000-00805f9b34fb"
//        }
        val descriptor = connection.controlIn.descriptors[0]
                .apply {
                    value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

                }
        return connection.bluetoothGatt.writeDescriptor(descriptor)
    }


    @ExperimentalUnsignedTypes
    companion object {
        private const val TAG = "SubscribeCtrlMsgCmd"

        fun onControlMessageCharacteristicChanged(gatt: BluetoothGatt,
                                            characteristic: BluetoothGattCharacteristic,
                                                  viewModel: DeviceDetailsViewModel) {
              //TODO

            Log.d(TAG, "onControlMessageCharacteristicChanged ${characteristic.getStringValue(0)}")

        }
    }

}
