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
import androidx.compose.ui.util.fastAll
import androidx.compose.ui.util.fastMap
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState as accompanistRememberMultiplePermissionsState

/**
 * Creates a [MultiplePermissionsState] that is remembered across compositions.
 *
 * @param permission a permission to control and observe.
 * @param otherPermissions additional permissions to control and observe.
 * @param onPermissionsResult will be called with whether or not the user granted the permissions
 *  after [MultiplePermissionsState.launchMultiplePermissionRequest] is called.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
public fun rememberMultiplePermissionsState(
  permission: String,
  vararg otherPermissions: String,
  onPermissionsResult: (Map<String, Boolean>) -> Unit = {}
): MultiplePermissionsState = rememberMultiplePermissionsState(
  permissions = listOf(permission) + otherPermissions.toList(),
  onPermissionsResult = onPermissionsResult,
)

/**
 * Creates a [MultiplePermissionsState] that is remembered across compositions.
 *
 * @param permissions the permissions to control and observe.
 * @param onPermissionsResult will be called with whether or not the user granted the permissions
 *  after [MultiplePermissionsState.launchMultiplePermissionRequest] is called.
 */
@ExperimentalPermissionsApi
@Composable
public fun rememberMultiplePermissionsState(
  permissions: List<String>,
  onPermissionsResult: (Map<String, Boolean>) -> Unit = {},
): MultiplePermissionsState = when {
  LocalInspectionMode.current -> PreviewMultiplePermissionsState(permissions)
  else -> {
    var isPermissionPostRequest by remember { mutableStateOf(false) }
    val accompanistPermissionState =
      accompanistRememberMultiplePermissionsState(permissions) { permissionsResult ->
        isPermissionPostRequest = true
        onPermissionsResult(permissionsResult)
      }

    val context = LocalContext.current

    var state: MultiplePermissionsState by remember {
      mutableStateOf(
        AccompanistMultiplePermissionsState(
          accompanist = accompanistPermissionState,
          isPermissionPostRequest = isPermissionPostRequest,
          context = context,
        )
      )
    }

    when {
      isPermissionPostRequest ->
        AccompanistMultiplePermissionsState(
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
private class PreviewMultiplePermissionsState(
  permissions: List<String>
) : MultiplePermissionsState {
  override val permissions: List<PermissionState> = permissions.fastMap(::PreviewPermissionState)
  override val isNotRequested: Boolean = true
  override val isAllPermissionsGranted: Boolean = false
  override val isAllNotGrantedPermissionsPermanentlyDenied: Boolean = false

  override fun launchMultiplePermissionRequest() {}
  override fun openAppSettings() {}
}

@OptIn(ExperimentalPermissionsApi::class)
@Immutable
private data class AccompanistMultiplePermissionsState(
  private val accompanist: com.google.accompanist.permissions.MultiplePermissionsState,
  private val isPermissionPostRequest: Boolean,
  private val context: Context,
) : MultiplePermissionsState {
  override val permissions: List<PermissionState> = accompanist.permissions.fastMap { state ->
    AccompanistPermissionState(
      accompanist = state,
      isPermissionPostRequest = isPermissionPostRequest,
      context = context,
    )
  }

  override val isNotRequested: Boolean = !isPermissionPostRequest

  override val isAllPermissionsGranted: Boolean = accompanist.allPermissionsGranted
  override val isAllNotGrantedPermissionsPermanentlyDenied: Boolean =
    permissions.fastAll { it.status.isGranted || it.status.isPermanentlyDenied }

  override fun launchMultiplePermissionRequest() {
    accompanist.launchMultiplePermissionRequest()
  }

  override fun openAppSettings() {
    context.openAppSettings(permissions.firstOrNull()?.permission)
  }
}
