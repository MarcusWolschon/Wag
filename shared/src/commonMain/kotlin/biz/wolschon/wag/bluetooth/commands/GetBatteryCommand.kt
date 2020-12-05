package biz.wolschon.wag.bluetooth.commands

import biz.wolschon.wag.logging.logDebug

expect class GetBatteryCommand

abstract class AbstractGetBatteryCommand(
    private val reportResult: ((Int?) -> Unit),
) : Command() {
    override val isTailCommand: Boolean = false
    override val isEarCommand: Boolean = true

    override fun execute(visitor: CommandExecutionVisitor) {
        if (visitor.sendCommandString("BATT")) {
            visitor.responseExpected = true
        } else {
            visitor.commandSucceeded = false
            visitor.responseExpected = false
            visitor.executionCompleted(this)
        }
    }

    override fun handleCommandResponse(visitor: CommandExecutionVisitor, response: String) {
        logDebug(TAG, "onCharacteristicChanged '$response'")
        reportResult.invoke(response.toIntOrNull())
        visitor.commandSucceeded = true
        visitor.responseExpected = false
        super.handleCommandResponse(visitor, response)
    }

    companion object {
        private const val TAG = "GetBatteryCommand"
    }
}
