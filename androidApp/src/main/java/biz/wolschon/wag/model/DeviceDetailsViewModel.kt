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
import androidx.lifecycle.Transformations
import biz.wolschon.wag.bluetooth.DeviceConnection
import biz.wolschon.wag.bluetooth.DeviceScanner


class DeviceDetailsViewModel(private val app: Application) :
                    AndroidViewModel(app),
                    SingleDeviceViewModel.ConnectionLostListener {

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

    private val connectedDevicesInternal = MutableLiveData<MutableList<SingleDeviceViewModel>>()
    val connectedDevices: LiveData<List<SingleDeviceViewModel>> = Transformations.map(connectedDevicesInternal) { 
        it as List<SingleDeviceViewModel>
    }

    private fun onDeviceLost(dev: BluetoothDevice) {
        val list = connectedDevicesInternal.value ?: return
        list.forEach{ singleDevice ->
            if (singleDevice.address == dev.address) {
                list.remove(singleDevice)
                connectedDevicesInternal.postValue(list)
                singleDevice.onDeviceLost()
            }
        }
    }

    override fun onConnectionLost(singleDevice: SingleDeviceViewModel) {
        val list = connectedDevicesInternal.value ?: return
         list.remove(singleDevice)
        connectedDevicesInternal.postValue(list)
    }

    fun connect(context: Context, device: BluetoothDevice) {
        val list = connectedDevicesInternal.value ?: mutableListOf<SingleDeviceViewModel>()
        list.add(SingleDeviceViewModel(context, device, this))
        connectedDevicesInternal.postValue(list)
    }

    /**
     * Execute the given command on all connected devices that are compatible.
     * @return true if executed on at least 1 device
     */
    fun executeCommand(cmd: BLECommand): Boolean {
        val list = connectedDevicesInternal.value ?: return
        var success = false
        list.forEach{ singleDevice ->
            success = singleDevice.executeCommand(cmd) || success
        }
        return success
    }

    val hasEarGears
        get() = list.any{ it.isEarGear}

    val hasTails
        get() = list.any{ it.isTail}
}
