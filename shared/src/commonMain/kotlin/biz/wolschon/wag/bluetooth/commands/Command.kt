package biz.wolschon.wag.bluetooth.commands

abstract class Command {

    open val isTailCommand: Boolean = true
    open val isEarCommand: Boolean = true

    abstract fun execute(visitor: CommandExecutionVisitor)

    open fun handleWriteCompleted(visitor: CommandExecutionVisitor) {
        if (!visitor.responseExpected) {
            visitor.executionCompleted(this)
        }
    }

    open fun handleCommandResponse(visitor: CommandExecutionVisitor, response: String) {
        if (!visitor.responseExpected) {
            visitor.executionCompleted(this)
        }
    }

}