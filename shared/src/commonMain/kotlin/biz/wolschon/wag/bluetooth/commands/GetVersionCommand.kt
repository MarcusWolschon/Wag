package biz.wolschon.wag.bluetooth.commands

import biz.wolschon.wag.logging.logDebug

expect class GetVersionCommand

abstract class AbstractGetVersionCommand(
    private val onSuccess: (() -> Unit)? = null
) : GetCommand<String>("VER") {

    override fun handleCommandResponse(visitor: CommandExecutionVisitor, response: String) {
        logDebug(TAG, "onCharacteristicChanged '$response'")
        internalResult.tryEmit(response) // this never fails for a StateFlow
        visitor.commandSucceeded = true
        visitor.responseExpected = false
        onSuccess?.invoke()
        super.handleCommandResponse(visitor, response)
    }

    companion object {
        private const val TAG = "GetVersionCommand"
    }
}
