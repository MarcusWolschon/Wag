package biz.wolschon.wag.bluetooth.commands

import biz.wolschon.wag.logging.logDebug

class GetBatteryCommand(

) : GetCommand<Int>("BATT") {

    override val isTailCommand: Boolean = false
    override val isEarCommand: Boolean = true

    override fun handleCommandResponse(visitor: CommandExecutionVisitor, response: String) {
        logDebug(TAG, "onCharacteristicChanged '$response'")
        internalResult.tryEmit(response.toIntOrNull()) // this never fails for a StateFlow
        visitor.commandSucceeded = true
        visitor.responseExpected = false
        super.handleCommandResponse(visitor, response)
    }

    companion object {
        private const val TAG = "GetBatteryCommand"
    }
}
