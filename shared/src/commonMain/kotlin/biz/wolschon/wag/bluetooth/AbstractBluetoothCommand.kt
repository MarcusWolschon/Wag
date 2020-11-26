package biz.wolschon.wag.bluetooth

expect class AbstractBluetoothCommand() {
    fun sendCommand(cmd: String): Boolean
    open fun onResultReceived(result: String)
}
