package biz.wolschon.wag.bluetooth.commands

/**
 * To be executed by a platform specific implementation,
 * commands get visited by a class implementing this interface.
 */
interface CommandExecutionVisitor {


    /**
     * @return true, if the write operation was initiated successfully
     */
    fun sendCommandString(commandString: String): Boolean

    var responseExpected: Boolean

    var commandSucceeded: Boolean

    /**
     * A [Command] *must* call this method when done.
     * e.g.
     * After a call to [sendCommandString], the default implementation of [Command.handleWriteCompleted] calls this method if no result is expected.
     * After a call to [sendCommandString] fails, it must be called manually.
     * After a the last, expected response has been retrieved in [Command.handleCommandResponse].
     */
    fun executionCompleted(completedCommand: Command)
}