package com.eygraber.compose.permissionx

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import com.google.accompanist.permissions.PermissionState as AccompanistPermissionState
import com.google.accompanist.permissions.PermissionStatus as AccompanistPermissionStatus
import com.google.accompanist.permissions.rememberPermissionState as accompanistRememberPermissionState

/**
 * Creates a [PermissionState] that is remembered across compositions.
 *
 * @param permission the permission to control and observe.
 * @param onPermissionResult will be called with whether the user granted the permission
 *  after [PermissionState.launchPermissionRequest] is called.
 * @param previewPermissionStatus provides a [PermissionStatus] when running in a preview.
 */
@ExperimentalPermissionsApi
@Composable
public fun rememberPermissionState(
  permission: String,
  onPermissionResult: (Boolean) -> Unit = {},
  previewPermissionStatus: PermissionStatus = PermissionStatus.Granted,
): PermissionState = when {
  LocalInspectionMode.current -> PreviewPermissionState(
    permission = permission,
    status = previewPermissionStatus
  )

  else -> {
    val context = LocalContext.current

    lateinit var permissionState: AccompanistPermissionStateWrapper

    val accompanistPermissionState =
      accompanistRememberPermissionState(permission) { isGranted ->
        // we can't check if permissionState is initialized because it is a local var
        try {
          permissionState.isPermissionPostRequest = true
          permissionState.refreshPermissionStatus()
          onPermissionResult(isGranted)
        }
        catch(_: UninitializedPropertyAccessException) {}
      }

    permissionState = remember(accompanistPermissionState) {
      AccompanistPermissionStateWrapper(
        accompanist = accompanistPermissionState,
        context = context,
      )
    }

    permissionState
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Stable
internal class AccompanistPermissionStateWrapper(
  private val accompanist: AccompanistPermissionState,
  private val context: Context,
) : PermissionState {
  var isPreviousStatusNotRequested by mutableStateOf(true)
  var isPermissionPostRequest by mutableStateOf(false)

  override var status: PermissionStatus by mutableStateOf(getPermissionStatus())

  override val permission: String = accompanist.permission

  private fun getPermissionStatus() = when {
    isPermissionPostRequest -> when(val accompanistStatus = accompanist.status) {
      is AccompanistPermissionStatus.Denied -> when {
        accompanistStatus.shouldShowRationale -> PermissionStatus.NotGranted.Denied
        else -> when {
          // we can't go from NotRequested->PermanentlyDenied since there's
          // no way to determine if the permission was already PermanentlyDenied
          // or if the permission request was canceled.
          // isPreviousStatusNotRequested forces an extra step by inserting a
          // Denied status even though it should be PermanentlyDenied
          // since this will allow us to handle the case where the permission
          // request was canceled
          isPreviousStatusNotRequested -> PermissionStatus.NotGranted.Denied
          else -> PermissionStatus.NotGranted.PermanentlyDenied
        }
      }

      AccompanistPermissionStatus.Granted -> PermissionStatus.Granted
    }.also {
      isPreviousStatusNotRequested = false
    }

    else -> when {
      accompanist.status.isGranted -> PermissionStatus.Granted
      accompanist.status.shouldShowRationale -> PermissionStatus.NotGranted.Denied
      else -> PermissionStatus.NotGranted.NotRequested
    }
  }

  internal fun refreshPermissionStatus() {
    status = getPermissionStatus()
  }

  override fun launchPermissionRequest() {
    accompanist.launchPermissionRequest()
  }

  override fun openAppSettings() {
    context.openAppSettings(accompanist.permission)
  }
}
