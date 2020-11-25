package biz.wolschon.wag.bluetooth

import android.bluetooth.*
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import biz.wolschon.wag.BuildConfig
import biz.wolschon.wag.R
import biz.wolschon.wag.bluetooth.commands.*
import java.util.*


class DeviceConnection(
    context: Context,
    private val adapter: BluetoothAdapter,
    private val ready: MutableLiveData<Boolean>,
    private val versionText: MutableLiveData<String>,
    private val batteryText: MutableLiveData<String>?,
    private val statusText: MutableLiveData<Int>,
    val device: BluetoothDevice,
    val onDisconnect: () -> Unit
) : BluetoothGattCallback() {

    internal var bluetoothGatt: BluetoothGatt
    private var workqueue: BLECommandQueue
    private var deviceService: BluetoothGattService? = null
    internal lateinit var controlOut: BluetoothGattCharacteristic
    internal lateinit var controlIn: BluetoothGattCharacteristic
    val address: String
        get() = device.address


    init {
        Log.d(TAG, "init device=${device.name} address=${device.address}")
        ready.postValue(false)
        bluetoothGatt = device.connectGatt(context, false, this, BluetoothDevice.TRANSPORT_LE)
        workqueue = BLECommandQueue(bluetoothGatt, statusText, this)
    }


    fun reconnect() {
        Log.i(TAG, "reconnecting...")
        statusText.postValue(R.string.status_reconnecting)
        if (!bluetoothGatt.connect()) {
            Log.e(TAG, "reconnect failed")
        }
    }

    fun disconnect() {
        Log.i(TAG, "disconnecting...")
        statusText.postValue(R.string.status_disconnected)
        bluetoothGatt.disconnect()
        workqueue.flush()

//        context.unregisterReceiver(pairingRequestListener)

        ready.postValue(false)
    }


    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        val bondState = device.bondState
        when (newState) {
            BluetoothProfile.STATE_CONNECTING -> {
                Log.i(TAG, "Connecting...")
            }
            BluetoothProfile.STATE_DISCONNECTING -> {
                Log.i(TAG, "Disconnecting...")
            }
            BluetoothProfile.STATE_CONNECTED -> {
                Log.i(TAG, "Connected")
                statusText.postValue(R.string.status_discovering)
                gatt.discoverServices()
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "Disconnected because we wanted to")
                } else {
                    Log.e(TAG, "Disconnected due to an error status=$status bondState=$bondState")
                }
                statusText.postValue(R.string.status_disconnected)
                gatt.close()
                ready.postValue(false)
                onDisconnect.invoke()
            }
        }
    }


    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        Log.d(TAG, "Service discovery")
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.e(TAG, "Service discovery failed")
            statusText.postValue(R.string.status_failed)
            ready.postValue(false)
            return
        }
        statusText.postValue(R.string.status_checking_services)
        val service = gatt.getService(UUID.fromString(BLEConstants.UUID_SERVICE))
        if (service == null) {
            Log.e(TAG, "Device Service missing")
            statusText.postValue(R.string.status_failed)
            ready.postValue(false)
            return
        }
        deviceService = service

        var characteristic =
            service.getCharacteristic(UUID.fromString(BLEConstants.UUID_READ_CHARACTERISTIC))
        if (characteristic == null) {
            Log.e(TAG, "Service is missing READ characteristic")
            statusText.postValue(R.string.status_failed)
            ready.postValue(false)
            return
        }
        controlIn = characteristic

        characteristic =
            service.getCharacteristic(UUID.fromString(BLEConstants.UUID_WRITE_CHARACTERISTIC))
        if (characteristic == null) {
            Log.e(TAG, "Service is missing WRITE characteristic")
            statusText.postValue(R.string.status_failed)
            ready.postValue(false)
            return
        }
        controlOut = characteristic

        // next step: get the status!
        doInitialCommand()
    }

    private fun doInitialCommand() {
        Log.d(TAG, "initial commands")
        workqueue.addCommand(SubscribeControlMessagesCommand())
        workqueue.addCommand(
            GetVersionCommand(
                versionText,
                success = ready // if this commands succeeds, we are ready
            )
        )
        if (batteryText != null) {
            workqueue.addCommand(GetBatteryCommand(batteryText))
        }
    }


    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        Log.d(TAG, "onCharacteristicRead()")
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.e(TAG, "Characteristic read failed status=" + status)
            workqueue.commandFinished()
            return
        }
        val cmd = workqueue.currentCommand
        if (cmd != null) {
            cmd.onCharacteristicRead(gatt, characteristic, status)
            if (!cmd.expectingResult) {
                workqueue.commandFinished()
            }
        } else {
            workqueue.commandFinished()
        }
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCharacteristicWrite()")
        }
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.e(TAG, "Characteristic write failed")
            workqueue.commandFinished()
            return
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "write success")
        }
        workqueue.currentCommand?.onCharacteristicWrite(gatt, characteristic, status)
        if (workqueue.currentCommand?.expectingResult != true) {
            workqueue.commandFinished()
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCharacteristicChanged()")
        }

        if (characteristic.uuid.equals(BLEConstants.UUID_READ_CHARACTERISTIC)) {
            SubscribeControlMessagesCommand.onControlMessageCharacteristicChanged(
                gatt,
                characteristic
            )
        }
//TODO: the device will report  battery-changes regularly on it's own, update batteryText
        // just for completeness sake
        val cmd = workqueue.currentCommand
        if (cmd != null) {
            cmd.onCharacteristicChanged(gatt, characteristic)
            if (!cmd.expectingResult) {
                workqueue.commandFinished()
            }
        } else {
            workqueue.commandFinished()
        }
    }

    override fun onDescriptorRead(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onDescriptorRead()")
        }
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.e(TAG, "Descriptor read failed")
            workqueue.commandFinished()
            return
        }
        workqueue.currentCommand?.onDescriptorRead(gatt, descriptor, status)
        workqueue.commandFinished()
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "onDescriptorWrite() descriptor=${descriptor?.uuid} status=0x${status.toString(16)}"
            )
        }
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.e(TAG, "Descriptor write failed")
            workqueue.commandFinished()
            return
        }
        workqueue.currentCommand?.onDescriptorWrite(gatt, descriptor, status)
        workqueue.commandFinished()
    }

    ///////////////////////////////////////////////////////////////////
    //       Actions

    fun execute(cmd: BLECommand) {
        workqueue.addCommand(cmd)
    }

    companion object {
        private const val TAG = "DeviceConnection"
    }
}
