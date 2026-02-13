package com.eygraber.compose.permissionx

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.util.fastAll
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import com.google.accompanist.permissions.MultiplePermissionsState as AccompanistMultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState as accompanistRememberMultiplePermissionsState

/**
 * Creates a [MultiplePermissionsState] that is remembered across compositions.
 *
 * @param permission a permission to control and observe.
 * @param otherPermissions additional permissions to control and observe.
 * @param onPermissionsResult will be called with whether the user granted the permissions
 *  after [MultiplePermissionsState.launchMultiplePermissionRequest] is called.
 * @param cancellationThreshold if a permission request is made, and a denied result is received without a
 *  rationale within this duration after the first request, it is likely a cancellation and the status
 *  will remain [PermissionStatus.NotGranted.Denied]. After the first request, if the result comes back
 *  faster than this threshold, the status will be [PermissionStatus.NotGranted.PermanentlyDenied].
 * @param previewPermissionStatuses provides a [PermissionStatus] for a given permission when running
 *  in a preview.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
public fun rememberMultiplePermissionsState(
  permission: String,
  vararg otherPermissions: String,
  onPermissionsResult: (Map<String, Boolean>) -> Unit = {},
  cancellationThreshold: Duration = 135.milliseconds,
  previewPermissionStatuses: Map<String, PermissionStatus> = emptyMap(),
): MultiplePermissionsState = rememberMultiplePermissionsState(
  permissions = listOf(permission) + otherPermissions.toList(),
  onPermissionsResult = onPermissionsResult,
  cancellationThreshold = cancellationThreshold,
  previewPermissionStatuses = previewPermissionStatuses,
)

/**
 * Creates a [MultiplePermissionsState] that is remembered across compositions.
 *
 * @param permissions the permissions to control and observe.
 * @param onPermissionsResult will be called with whether the user granted the permissions
 *  after [MultiplePermissionsState.launchMultiplePermissionRequest] is called.
 * @param cancellationThreshold if a permission request is made, and a denied result is received without a
 *  rationale within this duration after the first request, it is likely a cancellation and the status
 *  will remain [PermissionStatus.NotGranted.Denied]. After the first request, if the result comes back
 *  faster than this threshold, the status will be [PermissionStatus.NotGranted.PermanentlyDenied].
 * @param previewPermissionStatuses provides a [PermissionStatus] for a given permission when running
 *  in a preview.
 */
@ExperimentalPermissionsApi
@Composable
public fun rememberMultiplePermissionsState(
  permissions: List<String>,
  onPermissionsResult: (Map<String, Boolean>) -> Unit = {},
  cancellationThreshold: Duration = 135.milliseconds,
  previewPermissionStatuses: Map<String, PermissionStatus> = emptyMap(),
): MultiplePermissionsState = when {
  LocalInspectionMode.current -> PreviewMultiplePermissionsState(
    permissions = permissions,
    permissionStatuses = previewPermissionStatuses,
  )

  else -> {
    val context = LocalContext.current

    lateinit var permissionState: AccompanistMultiplePermissionsStateWrapper

    val accompanistPermissionState =
      accompanistRememberMultiplePermissionsState(permissions) { permissionsResult ->
        // we can't check if permissionState is initialized because it is a local var
        try {
          permissionState.isNotRequested = false
          permissionState.refreshPermissionStatus()
          onPermissionsResult(permissionsResult)
        }
        catch(_: UninitializedPropertyAccessException) {
        }
      }

    permissionState = remember(accompanistPermissionState) {
      AccompanistMultiplePermissionsStateWrapper(
        accompanist = accompanistPermissionState,
        context = context,
        cancellationThreshold = cancellationThreshold,
      )
    }

    permissionState
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Immutable
private class PreviewMultiplePermissionsState(
  permissions: List<String>,
  permissionStatuses: Map<String, PermissionStatus>
) : MultiplePermissionsState {
  override val permissions: List<PermissionState> = permissions.fastMap { permission ->
    PreviewPermissionState(
      permission = permission,
      status = permissionStatuses[permission] ?: PermissionStatus.Granted,
    )
  }

  override val isNotRequested: Boolean = true
  override val isAllPermissionsGranted: Boolean = false
  override val isAllNotGrantedPermissionsPermanentlyDenied: Boolean = false

  override fun launchMultiplePermissionRequest() {}
  override fun openAppSettings() {}
}

@OptIn(ExperimentalPermissionsApi::class)
@Stable
private class AccompanistMultiplePermissionsStateWrapper(
  private val accompanist: AccompanistMultiplePermissionsState,
  private val context: Context,
  private val cancellationThreshold: Duration,
) : MultiplePermissionsState {
  override var permissions: List<AccompanistPermissionStateWrapper> by mutableStateOf(
    wrapPermissions()
  )

  override var isNotRequested: Boolean by mutableStateOf(true)

  override val isAllPermissionsGranted: Boolean get() = accompanist.allPermissionsGranted
  override val isAllNotGrantedPermissionsPermanentlyDenied: Boolean
    get() = permissions.fastAll { it.status.isGranted || it.status.isPermanentlyDenied }

  private var requestedPermissionMark: ComparableTimeMark? = null

  override fun launchMultiplePermissionRequest() {
    requestedPermissionMark = TimeSource.Monotonic.markNow()
    accompanist.launchMultiplePermissionRequest()
  }

  override fun openAppSettings() {
    val permission = permanentlyDeniedPermissions.firstOrNull()
    context.openAppSettings(permission?.permission)
  }

  fun refreshPermissionStatus() {
    permissions.fastForEach { permission ->
      permission.isPermissionPostRequest = true
      permission.requestedPermissionMark = requestedPermissionMark
      permission.refreshPermissionStatus()
    }
  }

  private fun wrapPermissions(): List<AccompanistPermissionStateWrapper> =
    accompanist.permissions.fastMap { state ->
      AccompanistPermissionStateWrapper(
        accompanist = state,
        context = context,
        cancellationThreshold = cancellationThreshold,
      )
    }
}
