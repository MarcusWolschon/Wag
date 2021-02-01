package biz.wolschon.wag.bluetooth.commands

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class GetCommand<T>(private val cmd: String): Command() {
    /**
     * Mutable, private state.
     */
    internal val internalResult = MutableStateFlow<T?>(null)

    /**
     * Immutable, public state containing the last result of running this command.
     */
    val result = internalResult as StateFlow<T?>

    override fun execute(visitor: CommandExecutionVisitor) {
        if (visitor.sendCommandString(cmd)) {
            visitor.responseExpected = true
        } else {
            visitor.commandSucceeded = false
            visitor.responseExpected = false
            visitor.executionCompleted(this)
        }
    }
}