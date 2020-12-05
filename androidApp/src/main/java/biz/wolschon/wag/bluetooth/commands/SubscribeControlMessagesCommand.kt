package biz.wolschon.wag.bluetooth.commands

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.util.Log
import biz.wolschon.wag.bluetooth.DeviceConnection


class SubscribeControlMessagesCommand(private val deviceConnection: DeviceConnection) : Command() {

    override fun execute(visitor: CommandExecutionVisitor) {
        visitor.responseExpected = false
        visitor.commandSucceeded = false

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
        val cIn = deviceConnection.controlIn
        if (cIn == null) {
            Log.e(
                TAG,
                "Can't execute SubscribeControlMessagesCommand because the controlIn characteristic is null"
            )
            visitor.executionCompleted(this)
            return
        }
        val descriptor = cIn.descriptors[0]
            .apply {
                value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

            }
        visitor.commandSucceeded = deviceConnection.bluetoothGatt.writeDescriptor(descriptor)
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
