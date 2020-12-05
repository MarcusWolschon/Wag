package biz.wolschon.wag.logging

import android.util.Log

actual fun logDebug (tag: String, message: String) {
    Log.d(tag, message)
}