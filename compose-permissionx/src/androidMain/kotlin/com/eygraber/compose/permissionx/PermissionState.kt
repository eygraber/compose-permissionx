package com.eygraber.compose.permissionx

import androidx.compose.runtime.Stable
import com.google.accompanist.permissions.ExperimentalPermissionsApi

/**
 * A state object that can be hoisted to control and observe [permission] status changes.
 *
 * In most cases, this will be created via [rememberPermissionState].
 */
@ExperimentalPermissionsApi
@Stable
public interface PermissionState {

  /**
   * The permission to control and observe.
   */
  public val permission: String

  /**
   * [permission]'s status
   */
  public val status: PermissionStatus

  /**
   * Request the [permission] to the user.
   *
   * This should always be triggered from non-composable scope, for example, from a side effect
   * or a non-composable callback. Otherwise, this will result in an IllegalStateException.
   *
   * This triggers a system dialog that asks the user to grant or revoke the permission.
   * Note that this dialog might not appear on the screen if the user doesn't want to be asked
   * again or has denied the permission multiple times.
   * This behavior varies depending on the Android level API.
   */
  public fun launchPermissionRequest()

  /**
   * Open the app settings page.
   *
   * If the [permission] is [android.Manifest.permission.POST_NOTIFICATIONS] then
   * the notification settings will be opened. Otherwise, the app's settings will be opened.
   *
   * This should always be triggered from non-composable scope, for example, from a side effect
   * or a non-composable callback. Otherwise, this will result in an IllegalStateException.
   */
  public fun openAppSettings()
}

/**
 * Calls [PermissionState.launchPermissionRequest] or [PermissionState.openAppSettings]
 * depending on the state of [PermissionState.status].
 *
 * This should always be triggered from non-composable scope, for example, from a side effect
 * or a non-composable callback. Otherwise, this will result in an IllegalStateException.
 */
@ExperimentalPermissionsApi
public fun PermissionState.launchPermissionRequestOrAppSettings() {
  when(status) {
    PermissionStatus.NotGranted.PermanentlyDenied -> openAppSettings()
    PermissionStatus.Granted,
    PermissionStatus.NotGranted.Denied,
    PermissionStatus.NotGranted.NotRequested
    -> launchPermissionRequest()
  }
}
