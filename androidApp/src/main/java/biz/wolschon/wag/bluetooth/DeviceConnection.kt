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
    private val ready: MutableLiveData<Boolean>,
    private val versionText: MutableLiveData<String>,
    private val batteryPercentage: MutableLiveData<Int?>?,
    private val statusText: MutableLiveData<Int>,
    val device: BluetoothDevice,
    val onDisconnect: () -> Unit
) : BluetoothGattCallback() {

    internal var bluetoothGatt: BluetoothGatt
    private var workqueue: BLECommandQueue
    private var deviceService: BluetoothGattService? = null
    internal var controlOut: BluetoothGattCharacteristic? = null
    internal var controlIn: BluetoothGattCharacteristic? = null


    init {
        Log.d(TAG, "init device=${device.name} address=${device.address}")
        ready.postValue(false)
        bluetoothGatt = device.connectGatt(context, false, this, BluetoothDevice.TRANSPORT_LE)
        workqueue = BLECommandQueue(statusText, this)
    }


    @Suppress("unused")
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
        val service = gatt.getService(UUID.fromString(BLEConstants.getServiceUUID(device.name)))
        if (service == null) {
            Log.e(TAG, "Device Service missing")
            statusText.postValue(R.string.status_failed)
            ready.postValue(false)
            return
        }
        deviceService = service

        var characteristic =
            service.getCharacteristic(UUID.fromString(BLEConstants.getReadCharacteristicUUID(device.name)))
        if (characteristic == null) {
            Log.e(TAG, "Service is missing READ characteristic")
            statusText.postValue(R.string.status_failed)
            ready.postValue(false)
            return
        } else {
            Log.d(TAG, "Service has READ characteristic")
        }
        controlIn = characteristic

        characteristic =
            service.getCharacteristic(UUID.fromString(BLEConstants.getWriteCharacteristicUUID(device.name)))
        if (characteristic == null) {
            Log.e(TAG, "Service is missing WRITE characteristic")
            statusText.postValue(R.string.status_failed)
            ready.postValue(false)
            return
        } else {
            Log.d(TAG, "Service has WRITE characteristic")
        }
        controlOut = characteristic

        // next step: get the status!
        doInitialCommand()
    }

    private fun doInitialCommand() {
        Log.d(TAG, "initial commands")
        workqueue.addCommand(SubscribeControlMessagesCommand(this))
        workqueue.addCommand(
            GetVersionCommand(
                versionText,
                onSuccess = {
                    // if this commands succeeds, we are ready
                    ready.postValue(true)
                    statusText.postValue(R.string.status_ready)
                }
            )
        )
        if (batteryPercentage != null) {
            workqueue.addCommand(GetBatteryCommand(batteryPercentage))
        }
    }


    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        Log.d(TAG, "onCharacteristicRead()")
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.e(TAG, "Characteristic read failed status=" + status)
            workqueue.commandFinished()
            return
        }
        workqueue.onCharacteristicChanged(characteristic)
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
        workqueue.onCharacteristicWrite()
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCharacteristicChanged()")
        }

        if (characteristic.uuid.equals(BLEConstants.getReadCharacteristicUUID(device.name))) {
            SubscribeControlMessagesCommand.onControlMessageCharacteristicChanged(
                gatt,
                characteristic
            )
        }
//TODO: the device will report  battery-changes regularly on it's own, update batteryText

        workqueue.onCharacteristicChanged(characteristic)
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
        workqueue.onCharacteristicWrite()
    }

    ///////////////////////////////////////////////////////////////////
    //       Actions

    fun execute(cmd: Command) {
        workqueue.addCommand(cmd)
    }

    companion object {
        private const val TAG = "DeviceConnection"
    }
}
