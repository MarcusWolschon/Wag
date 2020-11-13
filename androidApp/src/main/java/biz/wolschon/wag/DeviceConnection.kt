
package biz.wolschon.wag.bluetooth

import android.bluetooth.*
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import biz.wolschon.wag.BuildConfig
import biz.wolschon.wag.R
import biz.wolschon.wag.bluetooth.commands.*
import biz.wolschon.wag.model.DeviceDetailsViewModel
import java.util.*


@OptIn(ExperimentalUnsignedTypes::class)
class DeviceConnection(context: Context,
                       private val adapter: BluetoothAdapter,
                       private val ready: MutableLiveData<Boolean>,
                       private val viewModel: DeviceDetailsViewModel,
                       private val statusText: MutableLiveData<Int>,
                       device: BluetoothDevice) : BluetoothGattCallback() {

    private var bluetoothGatt: BluetoothGatt
    private var workqueue: BLECommandQueue
    private var deviceService: BluetoothGattService? = null
    internal var controlOut: BluetoothGattCharacteristic? = null
    private lateinit var controlIn: BluetoothGattCharacteristic


    init {
        Log.d(TAG, "init device=${device.name} address=${device.address}")
        ready.postValue(false)
        bluetoothGatt = device.connectGatt(context, false, this)
        workqueue = BLECommandQueue(bluetoothGatt, statusText)
/*        registerPairingRequestListener()

        when(device.bondState) {
            BluetoothDevice.BOND_BONDED -> {
                // waiting for connected-event
            }
            BluetoothDevice.BOND_NONE -> {
                registerBondListener()
                Log.d(TAG, "initiating bonding...")
                device.createBond()
            }
            BluetoothDevice.BOND_BONDING ->
                registerBondListener()
        }*/
    }
/*    val pairingRequestListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BluetoothDevice.ACTION_PAIRING_REQUEST == intent.action) {
                val dev: BluetoothDevice  = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                val type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR)

                if (dev.address != device.address) {
                    Log.d(TAG, "pairing request for different device -> ignored")
                    return
                }

                if (type == BluetoothDevice.PAIRING_VARIANT_PIN) {
                    device.setPin(Util.IntToPasskey(pinCode()))
                    abortBroadcast()
                } else {
                    Log.w(TAG, "Unexpected pairing type: $type")
                }
            }
        }
    }

    /**
     * If the bond information is wrong (e.g. it has been deleted on the peripheral) then
     * discoverServices() will cause a disconnect. <br/>
     * You need to delete the bonding information and reconnect.
     */
    fun deleteBondInformation() {
        try {
            // FFS Google, just unhide the method.
            // Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            val m = device.javaClass.getMethod("removeBond", null)
            m.invoke(device, null as Array<Any>?)
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }

    }

    private fun registerPairingRequestListener() {
        Log.d(TAG, "registering bonding-requesrt receiver")
        val filter = IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST)
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
        context.registerReceiver(pairingRequestListener, filter)

    }

    private fun registerBondListener() {
        Log.d(TAG, "registering bonding receiver")
        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)
                val previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1)

                Log.d(TAG, "Bond state changed for: " + device.address + " new state: " + bondState + " previous: " + previousBondState)

                // skip other devices
                if (device.address != bluetoothGatt.getDevice().getAddress()) {
                    return
                }

                if (bondState == BluetoothDevice.BOND_BONDED) {
                    Log.i(TAG, "new bonding state: Bonded/Paired")
                    statusText.postValue(R.string.status_discovering)
                    bluetoothGatt.discoverServices()
                    context.unregisterReceiver(this)
                } else if (bondState == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "new bonding state: Bonding...")
                } else if (bondState == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "new bonding state: No longer bonded/paired")
                    // what to do?
                }
            }
        }, filter);
    }*/

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
        when (newState) {
            BluetoothProfile.STATE_CONNECTED -> {
                Log.i(TAG, "Connected")
                //if (device.bondState == BluetoothDevice.BOND_BONDED) {
                    statusText.postValue(R.string.status_discovering)
                    gatt.discoverServices()
                /*} else {
                    Log.i(TAG, "Connected but not yet bonded -> ignored")
                }*/
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                Log.i(TAG, "Disconnected")
                statusText.postValue(R.string.status_disconnected)
                ready.postValue(false)

/*                if (deviceService == null) {
                    // This can happen if the bond information is incorrect. Delete it and reconnect.
                    deleteBondInformation()
                    reconnect()
                }*/
            }
        }
    }



    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.e(TAG, "Service discovery failed")
            statusText.postValue(R.string.status_failed)
            ready.postValue(false)
            return
        }
        statusText.postValue(R.string.status_checking_services)
        var service = gatt.getService(UUID.fromString(BLEConstants.UUID_SERVICE))
        if (service == null) {
            Log.e(TAG, "Device Service missing")
            statusText.postValue(R.string.status_failed)
            ready.postValue(false)
            return
        }
        deviceService = service

        var characteristic = service.getCharacteristic(UUID.fromString(BLEConstants.UUID_READ_CHARACTERISTIC))
        if (characteristic == null) {
            Log.e(TAG, "Service is missing READ characteristic")
            statusText.postValue(R.string.status_failed)
            ready.postValue(false)
            return
        }
        controlIn = characteristic

        characteristic = service.getCharacteristic(UUID.fromString(BLEConstants.UUID_WRITE_CHARACTERISTIC))
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
        workqueue.addCommand(
                InitialCommand(deviceStatus,
                        statusText,
                        ready,
                        arrayOf(
                            //TODO: initial commands
                        ),
                        workqueue)
        )
    }


    override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic?, status: Int) {
        Log.d(TAG, "onCharacteristicRead()")
/*        if (BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION == status ||
                BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION == status) {

    }
    else if (status == GATT_AUTH_FAIL)
            {
                // This can happen because the user ignored the pairing request notification for too long.
                // Or presumably if they put the wrong PIN in.
                disconnectGatt();
                mState = ConnectionState.FAILED;
                State.notifyChanged();
            }
            else if (status == GATT_ERROR)
            {
                // I thought this happened if the bond information was wrong, but now I'm not sure.
                disconnectGatt();
                mState = ConnectionState.FAILED;
                State.notifyChanged();
            }
    */
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.e(TAG, "Characteristic read failed status=" + status)
            workqueue.commandFinished()
            return
        }
        workqueue.currentCommand?.onCharacteristicRead(gatt, characteristic, status)
        workqueue.commandFinished()
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic?, status: Int) {
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
        workqueue.commandFinished()
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onCharacteristicChanged()")
        }

        if(characteristic.uuid.equals(BLEConstants.UUID_READ_CHARACTERISTIC)) {
            SubscribeControlMessagesCommand.onControlMessageCharacteristicChanged(gatt, characteristic, viewModel)
        }

        // just for completeness sake
        workqueue.currentCommand?.onCharacteristicChanged(gatt, characteristic)
    }

    override fun onDescriptorRead(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
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

    override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onDescriptorWrite() descriptor=${descriptor?.uuid} status=0x${status.toString(16)}")
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

    fun execute(cmd : BLECommand) {
        workqueue.addCommand(cmd)
    }

    companion object {
        private const val TAG = "DeviceConnection"
    }
}
