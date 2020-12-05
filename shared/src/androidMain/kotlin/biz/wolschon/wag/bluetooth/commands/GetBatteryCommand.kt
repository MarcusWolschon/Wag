package biz.wolschon.wag.bluetooth.commands

import androidx.lifecycle.MutableLiveData

actual class GetBatteryCommand(
    private val batteryPercentage: MutableLiveData<Int?>
) : AbstractGetBatteryCommand(
    reportResult = {battery -> batteryPercentage.postValue(battery)}
)