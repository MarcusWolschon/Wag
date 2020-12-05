package biz.wolschon.wag.bluetooth.commands

actual class GetVersionCommand(
    reportResult: ((String?) -> Unit),
    onSuccess: (() -> Unit)? = null
) : AbstractGetVersionCommand(
    reportResult = reportResult,
    onSuccess = onSuccess
)