package biz.wolschon.wag.bluetooth.commands

import androidx.lifecycle.asLiveData

actual class GetBatteryCommand : AbstractGetBatteryCommand() {
    val batteryPercentage by lazy { result.asLiveData() }
}