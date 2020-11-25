package biz.wolschon.wag.bluetooth.commands

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.util.Log
import biz.wolschon.wag.bluetooth.BLECommand
import biz.wolschon.wag.bluetooth.DeviceConnection


class SubscribeControlMessagesCommand() : BLECommand() {

    override fun execute(deviceConnection: DeviceConnection): Boolean {

        // tell BLE to expect notifications
        if (!deviceConnection.bluetoothGatt.setCharacteristicNotification(
                deviceConnection.controlIn,
                true
            )
        ) {
            Log.e(TAG, "setCharacteristicNotification failed")
        }

        // tell the device to send indications(confirmed by us) or notifications(not confirmed by us)
//        connection.controlIn.descriptors.forEach { descriptor ->
//            Log.d(TAG,"descriptor: ${descriptor.uuid}") // only 1 "00002902-0000-1000-8000-00805f9b34fb"
//        }
        val descriptor = deviceConnection.controlIn.descriptors[0]
            .apply {
                value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

            }
        return deviceConnection.bluetoothGatt.writeDescriptor(descriptor)
    }


    companion object {
        private const val TAG = "SubscribeCtrlMsgCmd"

        fun onControlMessageCharacteristicChanged(
            @Suppress("UNUSED_PARAMETER")
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            //TODO

            Log.d(TAG, "onControlMessageCharacteristicChanged ${characteristic.getStringValue(0)}")

        }
    }

}
