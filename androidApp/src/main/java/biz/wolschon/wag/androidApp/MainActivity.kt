package biz.wolschon.wag.androidApp

import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import biz.wolschon.wag.R
import biz.wolschon.wag.databinding.ActivityMainBinding
import biz.wolschon.wag.model.DeviceDetailsViewModel

class MainActivity : AppCompatActivity() {

    val viewModel by lazy {
        ViewModelProvider(this).get(DeviceDetailsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.earDeviceSpinner.adapter = ArrayAdapter<BluetoothDevice>(this, R.layout.device_list_entry).also { adapter ->
            viewModel.devices.observe(this) {list ->
                adapter.clear()
                adapter.addAll(list)
            }
        }
        binding.tailDeviceSpinner.adapter = ArrayAdapter<BluetoothDevice>(this, R.layout.device_list_entry).also { adapter ->
            viewModel.devices.observe(this) {list ->
                adapter.clear()
                adapter.addAll(list)
            }
        }


        setContentView(binding.root)

        //TODO: ask for permissions to enable buttons
        //TODO: add Jetpack Navigation
    }

}
