package com.eygraber.compose.permissionx.test

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.eygraber.compose.permissionx.PermissionStatus
import com.eygraber.compose.permissionx.rememberPermissionState
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalPermissionsApi::class)
class PermissionsTestActivity : ComponentActivity() {

  var shouldShowRequestPermissionRationale: Map<String, Boolean> = emptyMap()

  override fun shouldShowRequestPermissionRationale(permission: String) = when(permission) {
    in shouldShowRequestPermissionRationale.keys -> shouldShowRequestPermissionRationale[permission]!!
    else -> super.shouldShowRequestPermissionRationale(permission)
  }

  /**
   * Code used in `MultipleAndSinglePermissionsTest`
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      Column {
        Text("PermissionsTestActivity")

        val state = rememberPermissionState(Manifest.permission.CAMERA)
        when(state.status) {
          PermissionStatus.Granted -> {
            Text("Granted")
          }

          PermissionStatus.NotGranted.Denied -> {
            Text("Denied")
            Button(onClick = { state.launchPermissionRequest() }) {
              Text("Request")
            }
          }

          PermissionStatus.NotGranted.NotRequested -> {
            Text("No permission")
            Button(onClick = { state.launchPermissionRequest() }) {
              Text("Request")
            }
          }

          PermissionStatus.NotGranted.PermanentlyDenied -> {
            Text("PermanentlyDenied")
            Button(onClick = { state.openAppSettings() }) {
              Text("Open App Settings")
            }
          }
        }
      }
    }
  }
}
