
package biz.wolschon.wag.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback

abstract class BLECommand : BluetoothGattCallback() {

    /**
     * @return false if unsuccessful and no callback is to be expected
     */
    abstract fun execute(bluetoothGatt: BluetoothGatt) : Boolean
}
