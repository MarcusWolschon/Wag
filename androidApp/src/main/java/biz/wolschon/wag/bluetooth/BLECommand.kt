
package biz.wolschon.wag.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback

abstract class BLECommand : BluetoothGattCallback() {

    open val expectingResult: Boolean = false
    open val isTailCommand: Boolean = true
    open val isEarCommand: Boolean = true

    /**
     * @return false if unsuccessful and no callback is to be expected
     */
    abstract fun execute(deviceConnection: DeviceConnection) : Boolean
}
