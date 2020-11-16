package biz.wolschon.wag.androidApp

import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import biz.wolschon.wag.R

abstract class PermissionCheckingActivity : AppCompatActivity() {
    private var requestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null
    protected abstract fun checkPermissions()

    fun requestPermissions(requiredPermissions: Array<String>,
                                   onGranted: () -> Unit,
                                   onNotYetGranted: () -> Unit
    ) {
        // ensure we are in the correct lifecycle state

        // Register the permissions callback, which handles the user's response
        // to the system permissions dialog.
        if (requestPermissionLauncher == null) {
            requestPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
                    if (requiredPermissions.all { map[it] == true }) {
                        // all are granted
                        onGranted.invoke()
                    } else {
                        onNotYetGranted.invoke()
                        // Explain to the user that the feature is unavailable because the
                        // features requires a permission that the user has denied.

                        val missing = requiredPermissions.filter { map[it] != true }.joinToString()
                        AlertDialog.Builder(this)
                            .setMessage(
                                getString(
                                    R.string.request_permission_denied_message,
                                    missing
                                )
                            )
                            .setPositiveButton(R.string.request_permission_ok) { dlg, _ -> dlg.cancel() }
                            .create().show()

                    }
                }
        }


        val notGranted = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {
            onGranted.invoke()
            return
        }
        onNotYetGranted.invoke()

        val needAsking = notGranted.any {
            shouldShowRequestPermissionRationale(it)
        }

        if (needAsking) {
            // Explain to the user why our app requires this permission for a specific
            // feature to behave as expected.

            AlertDialog.Builder(this)
                .setMessage(
                    getString(
                        R.string.request_permission_message,
                        requiredPermissions.joinToString()
                    )
                )
                .setPositiveButton(R.string.request_permission_ok) { dlg: DialogInterface, _: Int ->
                    // We can now ask for the permission.
                    // requestPermissionLauncher gets the result of this request.

                    requestPermissionLauncher!!.launch(requiredPermissions)
                    dlg.cancel()
                }
                .setNegativeButton(R.string.request_permission_deny) { dlg: DialogInterface, _: Int ->
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied.

                    AlertDialog.Builder(this)
                        .setMessage(R.string.request_permission_denied_message)
                        .create().show()


                    dlg.cancel()
                }
                .create().show()

        } else {
            // We can directly ask for the permission.
            // requestPermissionLauncher gets the result of this request.

            requestPermissionLauncher!!.launch(requiredPermissions)
        }

    }
}