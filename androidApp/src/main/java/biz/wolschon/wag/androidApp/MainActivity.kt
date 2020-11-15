package biz.wolschon.wag.androidApp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.ViewModelProvider
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

        setContentView(binding.root)

        //TODO: ask for permissions to enable buttons
        //TODO: add Jetpack Navigation
    }
}
