package biz.wolschon.wag.bluetooth.commands

actual class GetVersionCommand(
    onSuccess: (() -> Unit)? = null
) : AbstractGetVersionCommand(
    onSuccess = onSuccess
)