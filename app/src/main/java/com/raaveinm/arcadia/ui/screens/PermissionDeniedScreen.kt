package com.raaveinm.arcadia.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.raaveinm.arcadia.R
import com.raaveinm.arcadia.ui.canvas.AmbientRandom

@Composable
fun PermissionDeniedScreen(
    isPermanentlyDenied: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AmbientRandom(Modifier
            .fillMaxSize()
            .zIndex(-1f))
        Box(
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = .24f))
                .clickable {
                    if (isPermanentlyDenied) {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        context.startActivity(intent)
                    } else {
                        onRequestPermission()
                    }
                }
        ) {
            Text(
                text = if (isPermanentlyDenied) {
                    stringResource(R.string.permanent_denial)
                } else {
                    stringResource(R.string.permission_denied)
                },
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}