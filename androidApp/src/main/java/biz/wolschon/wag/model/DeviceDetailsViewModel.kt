package biz.wolschon.wag.model

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import biz.wolschon.wag.bluetooth.DeviceScanner

class DeviceDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val devicesInternal = MutableLiveData<List<BluetoothDevice>>()
    val devices : LiveData<List<BluetoothDevice>> = devicesInternal
    val selectedDevice = MutableLiveData<BluetoothDevice>()
    private val isScanningInternal = MutableLiveData<Boolean>()
    val isScanning : LiveData<Boolean> = isScanningInternal
    private val scanner by lazy {
        DeviceScanner(
            bluetoothManager.adapter,
            devices = devicesInternal,
            selectedDevice = selectedDevice,
            isScanning = isScanningInternal
        )
    }
    fun startScanning() {
        scanner.scan()
    }


    private val bluetoothManager by lazy(LazyThreadSafetyMode.NONE) {
        application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
}
