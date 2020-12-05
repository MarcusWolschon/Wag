package biz.wolschon.wag.bluetooth.commands


actual class GetBatteryCommand(
    reportResult: ((Int?) -> Unit)
) : AbstractGetBatteryCommand(
    reportResult = reportResult
)