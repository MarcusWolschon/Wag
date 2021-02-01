package biz.wolschon.wag.bluetooth

import androidx.lifecycle.MutableLiveData
import android.bluetooth.BluetoothGattCharacteristic
//import biz.wolschon.wag.R
import android.util.Log
import biz.wolschon.wag.android.R
import biz.wolschon.wag.bluetooth.commands.Command
import biz.wolschon.wag.bluetooth.commands.CommandExecutionVisitor
import biz.wolschon.wag.logging.logDebug
import java.util.*

class BLECommandQueue(
    private val statusText: MutableLiveData<Int>,
    private val connection: DeviceConnection
): CommandExecutionVisitor {

    private val workQueue = LinkedList<Command>()
    private var mCurrentCommand: Command? = null

    @Suppress("unused")
    val currentCommand: Command?
        get() = mCurrentCommand

    /**
     * Add a command to the queue.
     * If no command is currently running, execute the next one.
     */
    fun addCommand(cmd: Command) {
        synchronized(workQueue) {
            workQueue.add(cmd)
        }

        attemptExecuteNext()
    }

    fun flush() {
        workQueue.clear()
        mCurrentCommand = null
    }

    /**
     * Drop all other commands and add this one to be executed next/now.
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate")
    fun flushAndAddCommand(cmd: Command) {
        synchronized(workQueue) {
            workQueue.clear()
            workQueue.add(cmd)
        }

        attemptExecuteNext()
    }


    /**
     * Drop all other commands OFT THIS TYPE and add this one to be executed next/now.
     */
    @Suppress("unused")
    fun flushSimilarAndAddCommand(cmd: Command) {
        synchronized(workQueue) {
            val iter = workQueue.iterator()
            while (iter.hasNext()) {
                val next = iter.next()
                if (next.javaClass == cmd.javaClass) {
                    iter.remove()
                }
            }
            workQueue.add(cmd)
        }

        attemptExecuteNext()
    }

    /**
     * Called by [executionCompleted], when a callback was retrieved.
     * Attempt to execute the next command in the queue
     */
    fun commandFinished() {
        Log.d(TAG, "command ${mCurrentCommand?.javaClass?.simpleName} finished")
        mCurrentCommand = null
        attemptExecuteNext()
    }

    private fun attemptExecuteNext() {
        if (mCurrentCommand != null) {
            Log.d(TAG, "last command ${mCurrentCommand?.javaClass?.simpleName} still in progress")
            // we are still executing a command
            return
        }
        synchronized(workQueue) {
            mCurrentCommand = workQueue.pollFirst()
        }

        val cmd = mCurrentCommand
        if (cmd == null) {
            Log.d(TAG, "no next command to start")
            return
        }
        responseExpected = false // sensible default values
        commandSucceeded = true // sensible default valuesS
        cmd.execute(this)
        Log.d(TAG, "command ${cmd.javaClass.simpleName} started")

    }

    companion object {
        private const val TAG = "BLECmdQueue"
    }

    /**
     * @return true, if the write operation was initiated successfully
     */
    override fun sendCommandString(commandString: String): Boolean {
        val characteristic = connection.controlOut
        if (characteristic == null) {
            logDebug(TAG, "Can't execute because the controlOut characteristic is null")
            return false
        }
        characteristic.value = commandString.toByteArray()
        return connection.bluetoothGatt.writeCharacteristic(characteristic)
    }

    override var responseExpected: Boolean = false

    override var commandSucceeded: Boolean = true

    override fun executionCompleted(completedCommand: Command) {
        Log.d(TAG, "executionCompleted commandSucceeded=$commandSucceeded")
        if (!commandSucceeded && currentCommand == completedCommand) {
            statusText.postValue(R.string.status_failed)
        }
        commandFinished()
    }

    fun onCharacteristicChanged(characteristic: BluetoothGattCharacteristic) {
        val result = characteristic.getStringValue(0)
        val cmd = currentCommand
        if (cmd == null) {
            attemptExecuteNext()
        } else {
            cmd.handleCommandResponse(this, result)
        }
    }

    fun onCharacteristicWrite() {
        val cmd = currentCommand
        if (cmd == null) {
            attemptExecuteNext()
        } else {
            cmd.handleWriteCompleted(this)
        }
    }
}
