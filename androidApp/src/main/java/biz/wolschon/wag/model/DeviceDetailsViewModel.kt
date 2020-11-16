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

    private val bluetoothManager by lazy(LazyThreadSafetyMode.NONE) {
        application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    ///////////////////////////////////////////////////////
    //               SCANNING
    ///////////////////////////////////////////////////////

    private val devicesInternal = MutableLiveData<List<BluetoothDevice>>()
    val devices : LiveData<List<BluetoothDevice>> = devicesInternal
    private val isScanningInternal = MutableLiveData<Boolean>()
    val isScanning : LiveData<Boolean> = isScanningInternal
    private val scanner by lazy {
        DeviceScanner(
            bluetoothManager.adapter,
            devices = devicesInternal,
            onDeviceLost = this::onDeviceLost,
            isScanning = isScanningInternal
        )
    }

    fun startScanning() {
        if (isScanning.value != true) {
            scanner.scan()
        }
    }

    fun sopScanning() {
        scanner.stop()
    }

    ///////////////////////////////////////////////////////
    //               CONNECTIONS
    ///////////////////////////////////////////////////////

    val selectedEarDevice = MutableLiveData<BluetoothDevice>()
    val selectedTailDevice = MutableLiveData<BluetoothDevice>()
    private fun onDeviceLost(dev: BluetoothDevice) {
        selectedEarDevice.value?.let { ear ->
            if (ear.address == dev.address)
                selectedEarDevice.postValue(null)
        }

        selectedTailDevice.value?.let { tail ->
            if (tail.address == dev.address)
                selectedTailDevice.postValue(null)
        }
    }

}
