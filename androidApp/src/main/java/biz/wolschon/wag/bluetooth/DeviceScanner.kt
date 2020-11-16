
package biz.wolschon.wag.bluetooth

import androidx.lifecycle.MutableLiveData
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DeviceScanner(
        private val mBluetoothAdapter: BluetoothAdapter,
        private val devices: MutableLiveData<List<BluetoothDevice>>,
        private val onDeviceLost: (BluetoothDevice) -> Unit,
                    private val isScanning: MutableLiveData<Boolean>) {

    fun stop() {
        mBluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
        isScanning.postValue(false)
    }

    private class Callback(val parent: DeviceScanner) : ScanCallback() {

        override fun onBatchScanResults(results: List<ScanResult>) {
            Log.i(TAG, "scan batch result callback received")
            for (result in results) {
                onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, result)
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.i(TAG, "scan result callback received")
            if (result != null) {
                val mDevices = parent.devices.value?.toMutableList() ?: mutableListOf()
                val device = result.device

                if (callbackType == ScanSettings.CALLBACK_TYPE_MATCH_LOST) {
                    Log.i(TAG, "scan result: lost ${mDevices.size} devices")
                    if (mDevices.removeAll { it.address == device.address }) {

                        // notify UI about update
                        parent.devices.postValue(mDevices)
                    }

                    // notify view model that a connection may be lost
                    parent.onDeviceLost.invoke(device)
                } else if (device != null) {
                    Log.i(TAG, "scan result: found ${mDevices.size} devices up to now")
                    if (!contains(mDevices, device)) {
                        mDevices.add(device)

                        // notify UI about update
                        parent.devices.postValue(mDevices)
                    }
                }

            }
            super.onScanResult(callbackType, result)
        }


        private fun contains(mDevices: MutableList<BluetoothDevice>, result: BluetoothDevice) =
                mDevices.find { it.address == result.address } != null
    }

    private val scanCallback by lazy { Callback(this) }

    fun scan() {
        Log.i(TAG, "starting scan for device with service ${BLEConstants.UUID_SERVICE}...")

        // only compatible devices
        val filter1 = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid.fromString(BLEConstants.UUID_SERVICE))
                .build()
        val builder = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//    invalid callback type - 5             .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES + ScanSettings.CALLBACK_TYPE_MATCH_LOST)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setLegacy(false)
        }
        val settings = builder
                .build()

        isScanning.postValue(true)
        val callback = scanCallback
        GlobalScope.launch {
            delay(SCAN_TIMEOUT)
            try {
                Log.i(TAG, "scan timeout of $SCAN_TIMEOUT ms reached")
                mBluetoothAdapter.bluetoothLeScanner.stopScan(callback)
            } catch (x: Exception) {
                Log.e(TAG, "Can not stop scan", x)
            }
            isScanning.postValue(false)
        }



        mBluetoothAdapter.bluetoothLeScanner?.flushPendingScanResults(callback)
        mBluetoothAdapter.bluetoothLeScanner?.startScan(MutableList<ScanFilter>(1) {filter1},
                settings,
                callback)

    }


    companion object {
        private const val TAG = "DeviceScanner"
        private const val SCAN_TIMEOUT = 15000L
    }
}
