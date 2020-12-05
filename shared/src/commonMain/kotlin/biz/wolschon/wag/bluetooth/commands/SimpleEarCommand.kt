
package biz.wolschon.wag.bluetooth.commands

/**
 * A simple command only for EarGear that expects no answer.
 */
class SimpleEarCommand(
    commandString: String
) : SimpleCommand(commandString, forTail = false, forEarGear = true)