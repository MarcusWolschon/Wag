package biz.wolschon.wag.bluetooth.commands

import biz.wolschon.wag.logging.logDebug

expect class GetVersionCommand

abstract class AbstractGetVersionCommand(
    private val reportResult: ((String) -> Unit),
    private val onSuccess: (() -> Unit)? = null
) : Command() {


    override fun execute(visitor: CommandExecutionVisitor) {
        if (visitor.sendCommandString("VER"))  {
            visitor.responseExpected = true
        } else {
            visitor.commandSucceeded = false
            visitor.responseExpected = false
            visitor.executionCompleted(this)
        }
    }

    override fun handleCommandResponse(visitor: CommandExecutionVisitor, response: String) {
        logDebug(TAG, "onCharacteristicChanged '$response'")
        reportResult.invoke(response)
        visitor.commandSucceeded = true
        visitor.responseExpected = false
        onSuccess?.invoke()
        super.handleCommandResponse(visitor, response)
    }

    companion object {
        private const val TAG = "GetVersionCommand"
    }
}
