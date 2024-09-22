package com.eygraber.compose.permissionx

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import com.google.accompanist.permissions.PermissionStatus as AccompanistPermissionStatus
import com.google.accompanist.permissions.rememberPermissionState as accompanistRememberPermissionState

/**
 * Creates a [PermissionState] that is remembered across compositions.
 *
 * @param permission the permission to control and observe.
 * @param onPermissionResult will be called with whether or not the user granted the permission
 *  after [PermissionState.launchPermissionRequest] is called.
 */
@ExperimentalPermissionsApi
@Composable
public fun rememberPermissionState(
  permission: String,
  onPermissionResult: (Boolean) -> Unit = {},
): PermissionState = when {
  LocalInspectionMode.current -> PreviewPermissionState(permission)
  else -> {
    var isPermissionPostRequest by remember { mutableStateOf(false) }
    val accompanistPermissionState =
      accompanistRememberPermissionState(permission) { isGranted ->
        isPermissionPostRequest = true
        onPermissionResult(isGranted)
      }

    val context = LocalContext.current

    var state: PermissionState by remember {
      mutableStateOf(
        AccompanistPermissionState(
          accompanist = accompanistPermissionState,
          isPermissionPostRequest = isPermissionPostRequest,
          context = context,
        )
      )
    }

    when {
      isPermissionPostRequest ->
        AccompanistPermissionState(
          accompanist = accompanistPermissionState,
          isPermissionPostRequest = true,
          context = context,
        ).also {
          state = it
        }

      else -> state
    }
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Immutable
internal data class AccompanistPermissionState(
  private val accompanist: com.google.accompanist.permissions.PermissionState,
  private val isPermissionPostRequest: Boolean,
  private val context: Context,
) : PermissionState {
  override val permission: String = accompanist.permission
  override val status: PermissionStatus = when {
    isPermissionPostRequest -> when(val accompanistStatus = accompanist.status) {
      is AccompanistPermissionStatus.Denied -> when {
        accompanistStatus.shouldShowRationale -> PermissionStatus.NotGranted.Denied
        else -> PermissionStatus.NotGranted.PermanentlyDenied
      }

      AccompanistPermissionStatus.Granted -> PermissionStatus.Granted
    }

    else -> when {
      accompanist.status.isGranted -> PermissionStatus.Granted
      accompanist.status.shouldShowRationale -> PermissionStatus.NotGranted.Denied
      else -> PermissionStatus.NotGranted.NotRequested
    }
  }

  override fun launchPermissionRequest() {
    accompanist.launchPermissionRequest()
  }

  override fun openAppSettings() {
    context.openAppSettings(accompanist.permission)
  }
}
