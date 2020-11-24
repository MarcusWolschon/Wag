
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

/**
 * A simple command only for EarGear that expects no answer.
 */
class SimpleEarCommand(
    commandString: String,
    forTail: Boolean,
    forEarGear: Boolean
) : SimpleCommand(commandString, forTail = false, forEarGear = true)