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
import androidx.lifecycle.*
import biz.wolschon.wag.bluetooth.BLECommand
import biz.wolschon.wag.bluetooth.DeviceScanner
import biz.wolschon.wag.bluetooth.commands.SimpleEarCommand
import biz.wolschon.wag.bluetooth.commands.SimpleTailCommand


class DeviceListViewModel(private val app: Application) :
    AndroidViewModel(app),
    SingleDeviceViewModel.ConnectionLostListener {

    private val bluetoothManager by lazy(LazyThreadSafetyMode.NONE) {
        app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    val isBluetoothSupported
        get() = app.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)


    private var bluetoothBroadcastReceiver: BroadcastReceiver? = null

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
        bluetoothBroadcastReceiver = mReceiver
        app.registerReceiver(mReceiver, filter)

        mutable
    }

    override fun onCleared() {
        bluetoothBroadcastReceiver?.let {
            app.unregisterReceiver(bluetoothBroadcastReceiver)
            bluetoothBroadcastReceiver = null
        }
    }

    ///////////////////////////////////////////////////////
    //               SCANNING
    ///////////////////////////////////////////////////////

    /**
     * All devices currently announcing.
     */
    private val allDevices = MutableLiveData<List<BluetoothDevice>>()

    /**
     * Devices that we can connect to.
     */
    val unconnectedDevices by lazy {
        MediatorLiveData<List<BluetoothDevice>>().also { mediator ->
            fun update(
                allDevices: List<BluetoothDevice>,
                connectedDevices: List<SingleDeviceViewModel>?
            ) {
                val connected = connectedDevices ?: listOf()
                mediator.postValue(allDevices.filterNot { dev -> connected.any { it.address == dev.address } })
            }

            mediator.addSource(allDevices) { all ->
                update(
                    all ?: listOf(),
                    connectedDevices.value
                )
            }
            mediator.addSource(connectedDevicesInternal) { con ->
                update(
                    allDevices.value ?: listOf(), con
                )
            }
        }
    }

    /**
     * Internal, mutable version of [isScanning]
     */
    private val isScanningInternal = MutableLiveData<Boolean>()

    /**
     * Are we currently scanning for devices?
     */
    val isScanning: LiveData<Boolean> = isScanningInternal

    private val scanner by lazy {
        DeviceScanner(
            bluetoothManager.adapter,
            devices = allDevices,
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
    /**
     * Internal, mutable version of [connectedDevices]
     */
    private val connectedDevicesInternal = MutableLiveData<MutableList<SingleDeviceViewModel>>()

    /**
     * Everything we are actively connected to.
     */
    val connectedDevices: LiveData<List<SingleDeviceViewModel>> =
        Transformations.map(connectedDevicesInternal) {
            it as List<SingleDeviceViewModel>
        }

    private fun onDeviceLost(dev: BluetoothDevice) {
        val list = connectedDevicesInternal.value ?: return
        list.forEach { singleDevice ->
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

    fun connect(context: Context, device: BluetoothDevice?) {
        if (device == null) {
            return
        }
        val list = connectedDevicesInternal.value ?: mutableListOf()
        list.add(SingleDeviceViewModel(context, device, this))
        connectedDevicesInternal.postValue(list)
    }

    /**
     * Execute the given command on all connected devices that are compatible.
     * @return true if executed on at least 1 device
     */
    fun executeSimpleEarCommand(cmd: String): Boolean =
        executeCommand(SimpleEarCommand(cmd))

    /**
     * Execute the given command on all connected devices that are compatible.
     * @return true if executed on at least 1 device
     */
    fun executeSimpleTailCommand(cmd: String): Boolean =
        executeCommand(SimpleTailCommand(cmd))

    /**
     * Execute the given command on all connected devices that are compatible.
     * @return true if executed on at least 1 device
     */
    fun executeCommand(cmd: BLECommand): Boolean {
        val list = connectedDevicesInternal.value ?: return false
        var success = false
        list.forEach { singleDevice ->
            success = singleDevice.executeCommand(cmd) || success
        }
        return success
    }

    val hasEarGears = Transformations.map(connectedDevicesInternal) { list ->
        list.any { it.isEarGear }
    }

    val hasDigitails = Transformations.map(connectedDevicesInternal) { list ->
        list.any { it.isDigitail }
    }
}
