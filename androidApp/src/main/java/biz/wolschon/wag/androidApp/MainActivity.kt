package biz.wolschon.wag.androidApp

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import biz.wolschon.wag.R
import biz.wolschon.wag.databinding.ActivityMainBinding
import biz.wolschon.wag.model.DeviceListViewModel
import biz.wolschon.wag.model.SingleDeviceListAdapter

class MainActivity : PermissionCheckingActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this).get(DeviceListViewModel::class.java)
    }

    private val permissionsGranted = MutableLiveData<Boolean>().also { it.value = false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.permissionsGranted = permissionsGranted

        // device we can connect to
        binding.connectDeviceSpinner.adapter =
            ArrayAdapter<BluetoothDevice>(this, R.layout.device_spinner_entry).also { adapter ->
                viewModel.unconnectedDevices.observe(this) { list ->
                    adapter.clear()
                    adapter.addAll(list)
                }
            }

        // device we are connected to
        with(binding.singleDeviceList) {
            layoutManager = LinearLayoutManager(context)
            adapter = SingleDeviceListAdapter(liveList = viewModel.connectedDevices, viewLifecycleOwner = this@MainActivity/*in fragment use: viewLifecycleOwner*/)
        }

        //TODO: add Jetpack Navigation

        setContentView(binding.root)


        //ask for permissions to enable buttons
        checkPermissions()
    }

    override fun onResume() {
        super.onResume()

        // check for hardware support

        if (!viewModel.isBluetoothSupported) {
            AlertDialog.Builder(this)
                .setMessage(R.string.error_no_bluetooth)
                .setPositiveButton(R.string.request_permission_ok) { dlg, _ -> dlg.cancel() }
                .create().show()
            permissionsGranted.postValue(false)
        } else if (viewModel.bluetoothEnabled.value == false) {
            AlertDialog.Builder(this)
                .setMessage(R.string.error_disabled_bluetooth)
                .setPositiveButton(R.string.request_permission_ok) { dlg, _ -> dlg.cancel() }
                .create().show()
            permissionsGranted.postValue(false)
        }

        checkPermissions()

    }

    override fun checkPermissions() {
        requestPermissions(
            requiredPermissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH
            ),
            onGranted = {
                permissionsGranted.postValue(true)
            },
            onNotYetGranted = {
                permissionsGranted.postValue(false)
            }
        )
    }

}
