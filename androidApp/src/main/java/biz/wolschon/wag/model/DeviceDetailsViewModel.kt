@file:Suppress("MemberVisibilityCanBePrivate")

package biz.wolschon.wag.model

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import biz.wolschon.wag.bluetooth.DeviceConnection
import biz.wolschon.wag.bluetooth.DeviceScanner


class DeviceDetailsViewModel(private val app: Application) : AndroidViewModel(app) {

    private val bluetoothManager by lazy(LazyThreadSafetyMode.NONE) {
        app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    val isBluetoothSupported
        get() = app.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)



    val bluetoothEnabled by lazy {
        val mutable = MutableLiveData<Boolean>()
        mutable.value = bluetoothManager.adapter.isEnabled

        val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    val state = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR
                    )
                    when (state) {
                        BluetoothAdapter.STATE_OFF -> mutable.postValue(false)
                        BluetoothAdapter.STATE_TURNING_OFF -> mutable.postValue(false)
                        BluetoothAdapter.STATE_ON -> mutable.postValue(true)
                        BluetoothAdapter.STATE_TURNING_ON -> mutable.postValue(false)
                    }
                }
            }
        }

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        app.registerReceiver(mReceiver, filter)
        //TODO: take care of unregistering

        mutable
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

    fun toggleScanning() {
        if (isScanning.value != true) {
            startScanning()
        } else {
            stopScanning()
        }
    }

    fun startScanning() {
        if (isScanning.value != true) {
            scanner.scan()
        }
    }

    fun stopScanning() {
        scanner.stop()
    }

    ///////////////////////////////////////////////////////
    //               CONNECTIONS
    ///////////////////////////////////////////////////////

    val selectedEarDevice = MutableLiveData<DeviceConnection>()
    val selectedTailDevice = MutableLiveData<DeviceConnection>()
    private fun onDeviceLost(dev: BluetoothDevice) {
        selectedEarDevice.value?.device?.let { earDevice ->
            if (earDevice.address == dev.address)
                selectedEarDevice.postValue(null)
        }

        selectedTailDevice.value?.device?.let { tailDevice ->
            if (tailDevice.address == dev.address)
                selectedTailDevice.postValue(null)
        }
    }

    fun toggleEarConnection(context: Context, device: BluetoothDevice) {
        val current = selectedEarDevice.value
        if (current == null) {
            selectedEarDevice.postValue(DeviceConnection(
                context = context,
                adapter = BluetoothAdapter.getDefaultAdapter(),
                MutableLiveData<Boolean>(),
                this,
                MutableLiveData<Int>(),
                device
            ))
        } else {
            current.disconnect()
            selectedEarDevice.postValue(null)
        }
    }

    fun toggleTailConnection(context: Context, device: BluetoothDevice) {
        val current = selectedTailDevice.value
        if (current == null) {
            selectedTailDevice.postValue(DeviceConnection(
                context = context,
                adapter = BluetoothAdapter.getDefaultAdapter(),
                MutableLiveData<Boolean>(),
                this,
                MutableLiveData<Int>(),
                device
            ))
        } else {
            current.disconnect()
            selectedTailDevice.postValue(null)
        }
    }

}
