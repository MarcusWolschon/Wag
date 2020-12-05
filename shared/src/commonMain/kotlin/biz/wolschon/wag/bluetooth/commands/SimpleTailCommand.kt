
package biz.wolschon.wag.bluetooth.commands

/**
 * A simple command only for Digitail that expects no answer.
 */
class SimpleTailCommand(
    commandString: String
) : SimpleCommand(commandString, forTail = true, forEarGear = false)
