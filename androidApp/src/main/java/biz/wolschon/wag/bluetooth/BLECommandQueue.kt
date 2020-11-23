package biz.wolschon.wag.bluetooth

import androidx.lifecycle.MutableLiveData
import android.bluetooth.BluetoothGatt
import biz.wolschon.wag.R
import android.util.Log
import java.util.*
import java.util.function.Predicate

class BLECommandQueue(
    private val bluetoothGatt: BluetoothGatt,
    private val statusText: MutableLiveData<Int>,
    val connection: DeviceConnection
) {

    private val workQueue = LinkedList<BLECommand>()
    private var mCurrentCommand: BLECommand? = null
    val currentCommand: BLECommand?
        get() = mCurrentCommand

    /**
     * Add a command to the queue.
     * If no command is currently running, execute the next one.
     */
    fun addCommand(cmd: BLECommand) {
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
    fun flushAndAddCommand(cmd: BLECommand) {
        synchronized(workQueue) {
            workQueue.clear()
            workQueue.add(cmd)
        }

        attemptExecuteNext()
    }


    /**
     * Drop all other commands OFT THIS TYPE and add this one to be executed next/now.
     */
    fun flushSimilarAndAddCommand(cmd: BLECommand) {
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
     * Called by [DeviceConnection] when a callback was retrieved.
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
        if (cmd.execute(connection)) {
            Log.d(TAG, "command ${cmd.javaClass.simpleName} started")
        } else {
            Log.e(TAG, "command ${cmd.javaClass.simpleName} failed to start")
            statusText.postValue(R.string.status_failed)
            mCurrentCommand = null
            attemptExecuteNext()
        }

    }

    companion object {
        private const val TAG = "BLECmdQueue"
    }
}
