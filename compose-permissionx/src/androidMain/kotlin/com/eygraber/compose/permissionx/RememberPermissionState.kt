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
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import com.google.accompanist.permissions.PermissionState as AccompanistPermissionState
import com.google.accompanist.permissions.PermissionStatus as AccompanistPermissionStatus
import com.google.accompanist.permissions.rememberPermissionState as accompanistRememberPermissionState

/**
 * Creates a [PermissionState] that is remembered across compositions.
 *
 * @param permission the permission to control and observe.
 * @param cancellationThreshold if a permission request is made, and a denied result is received without a
 *  rationale within this duration after the first request, it is likely a cancellation and the status
 *  will remain [PermissionStatus.NotGranted.Denied]. After the first request, if the result comes back
 *  faster than this threshold, the status will be [PermissionStatus.NotGranted.PermanentlyDenied].
 * @param previewPermissionStatus provides a [PermissionStatus] when running in a preview.
 * @param onPermissionResult will be called with whether the user granted the permission
 *  after [PermissionState.launchPermissionRequest] is called.
 */
@ExperimentalPermissionsApi
@Composable
public fun rememberPermissionState(
  permission: String,
  cancellationThreshold: Duration = 135.milliseconds,
  previewPermissionStatus: PermissionStatus = PermissionStatus.Granted,
  onPermissionResult: (Boolean) -> Unit = {},
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
        catch(_: UninitializedPropertyAccessException) {
        }
      }

    permissionState = remember(accompanistPermissionState) {
      AccompanistPermissionStateWrapper(
        accompanist = accompanistPermissionState,
        context = context,
        cancellationThreshold = cancellationThreshold,
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
  private val cancellationThreshold: Duration,
) : PermissionState {
  var isPreviousStatusNotRequested by mutableStateOf(true)
  var isPermissionPostRequest by mutableStateOf(false)

  override var status: PermissionStatus by mutableStateOf(getPermissionStatus())

  override val permission: String = accompanist.permission

  internal var requestedPermissionMark: ComparableTimeMark? = null

  private fun getPermissionStatus() = when {
    isPermissionPostRequest -> when(val accompanistStatus = accompanist.status) {
      is AccompanistPermissionStatus.Denied -> when {
        accompanistStatus.shouldShowRationale -> PermissionStatus.NotGranted.Denied
        else -> when {
          // First request always goes to Denied to handle cancellation vs permanent denial ambiguity
          isPreviousStatusNotRequested -> PermissionStatus.NotGranted.Denied

          // Subsequent requests use timing heuristic to distinguish cancellation from permanent denial
          else -> {
            val elapsed = requestedPermissionMark?.let { TimeSource.Monotonic.markNow() - it }
            if(elapsed != null && elapsed < cancellationThreshold) {
              // Fast response suggests system didn't show dialog (true permanent denial)
              PermissionStatus.NotGranted.PermanentlyDenied
            }
            else {
              // Slow response suggests user saw and dismissed dialog (cancellation or soft denial)
              PermissionStatus.NotGranted.Denied
            }
          }
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
    requestedPermissionMark = TimeSource.Monotonic.markNow()
    accompanist.launchPermissionRequest()
  }

  override fun openAppSettings() {
    context.openAppSettings(accompanist.permission)
  }
}
