
package biz.wolschon.wag.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback

abstract class BLECommand : BluetoothGattCallback() {

    open val expectingResult: Boolean = false

    /**
     * @return false if unsuccessful and no callback is to be expected
     */
    abstract fun execute(deviceConnection: DeviceConnection) : Boolean
}
