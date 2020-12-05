package biz.wolschon.wag.bluetooth.commands

import biz.wolschon.wag.logging.*

/**
 * A simple command that expects no answer.
 */
open class SimpleCommand(
    private val commandString: String,
    forTail: Boolean,
    forEarGear: Boolean
) : Command() {

    override val isTailCommand: Boolean = forTail
    override val isEarCommand: Boolean = forEarGear

    override fun execute(visitor: CommandExecutionVisitor) {
        logDebug(TAG, "performing simple command $commandString")
        visitor.responseExpected = false
        visitor.commandSucceeded = visitor.sendCommandString(commandString)
    }

    companion object {
        private const val TAG = "SimpleCommand"
    }
}
