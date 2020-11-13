
package biz.wolschon.wag.model

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import biz.wolschon.wag.R
import biz.wolschon.wag.bluetooth.DeviceConnection
import biz.wolschon.wag.bluetooth.commands.SubscribeControlMessagesCommand

@ExperimentalUnsignedTypes
class DeviceDetailsViewModel(application: Application) :  AndroidViewModel(application) {
    private val bluetoothManager  by lazy(LazyThreadSafetyMode.NONE) {
        application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
}
