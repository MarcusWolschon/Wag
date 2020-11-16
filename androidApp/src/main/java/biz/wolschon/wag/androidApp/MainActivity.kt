package biz.wolschon.wag.androidApp

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import biz.wolschon.wag.R
import biz.wolschon.wag.databinding.ActivityMainBinding
import biz.wolschon.wag.model.DeviceDetailsViewModel

class MainActivity : PermissionCheckingActivity() {

    val viewModel by lazy {
        ViewModelProvider(this).get(DeviceDetailsViewModel::class.java)
    }

    private val permissionsGranted = MutableLiveData<Boolean>().also { it.value = false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.permissionsGranted = permissionsGranted

        binding.earDeviceSpinner.adapter =
            ArrayAdapter<BluetoothDevice>(this, R.layout.device_list_entry).also { adapter ->
                viewModel.devices.observe(this) { list ->
                    adapter.clear()
                    adapter.addAll(list)
                }
            }
        binding.tailDeviceSpinner.adapter =
            ArrayAdapter<BluetoothDevice>(this, R.layout.device_list_entry).also { adapter ->
                viewModel.devices.observe(this) { list ->
                    adapter.clear()
                    adapter.addAll(list)
                }
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
