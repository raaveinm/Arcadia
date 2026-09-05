package com.raaveinm.arcadia.ui.handlers

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class PermissionHandler(
    caller: ActivityResultCaller,
    private val onResult: (Map<String, Boolean>) -> Unit
) {
    private val launcher: ActivityResultLauncher<Array<String>> =
        caller.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            onResult(permissions)
        }

    fun requestPermissions(context: Context, permissions: List<String>) {
        val deniedPermissions = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED
        }

        if (deniedPermissions.isEmpty()) {
            onResult(permissions.associateWith { true })
        } else {
            launcher.launch(deniedPermissions.toTypedArray())
        }
    }
}