package com.raaveinm.arcadia

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.raaveinm.arcadia.ui.handlers.PermissionHandler
import com.raaveinm.arcadia.ui.screens.PermissionDeniedScreen
import com.raaveinm.arcadia.ui.screens.StartArScreen
import com.raaveinm.arcadia.ui.theme.ArcadiaTheme

class MainActivity : ComponentActivity() {

    private lateinit var permissionHandler: PermissionHandler
    private var permissionGranted by mutableStateOf(false)
    private var isPermanentlyDenied by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        permissionHandler = PermissionHandler(this) { results ->
            val granted = results[Manifest.permission.CAMERA] == true
            permissionGranted = granted

            if (!granted) {
                val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                )
                isPermanentlyDenied = !showRationale
            }
        }

        setContent {
            ArcadiaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (!permissionGranted) {
                        PermissionDeniedScreen(
                            isPermanentlyDenied = isPermanentlyDenied,
                            onRequestPermission = {
                                permissionHandler.requestPermissions(
                                    this@MainActivity,
                                    listOf(Manifest.permission.CAMERA)
                                )
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        StartArScreen()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val currentStatus = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        permissionGranted = currentStatus
        if (currentStatus) {
            isPermanentlyDenied = false
        }
    }

    override fun onStart() {
        super.onStart()
        if (!permissionGranted) {
            permissionHandler.requestPermissions(
                this,
                listOf(Manifest.permission.CAMERA)
            )
        }
    }
}