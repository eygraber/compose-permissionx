package com.eygraber.compose.permissionx

import androidx.compose.runtime.Stable
import com.google.accompanist.permissions.ExperimentalPermissionsApi

/**
 * A state object that can be hoisted to control and observe multiple [permissions] status changes.
 *
 * In most cases, this will be created via [rememberMultiplePermissionsState].
 */
@ExperimentalPermissionsApi
@Stable
public interface MultiplePermissionsState {

  /**
   * List of all permissions to request.
   */
  public val permissions: List<PermissionState>

  /**
   * When `true`, the user hasn't requested [permissions] yet.
   */
  public val isNotRequested: Boolean

  /**
   * When `true`, the user has granted all [permissions].
   */
  public val isAllPermissionsGranted: Boolean

  /**
   * When `true`, the user has permanently denied all [permissions] that haven't been granted.
   */
  public val isAllNotGrantedPermissionsPermanentlyDenied: Boolean

  /**
   * Request the [permissions] to the user.
   *
   * This should always be triggered from non-composable scope, for example, from a side-effect
   * or a non-composable callback. Otherwise, this will result in an IllegalStateException.
   *
   * This triggers a system dialog that asks the user to grant or revoke the permission.
   * Note that this dialog might not appear on the screen if the user doesn't want to be asked
   * again or has denied the permission multiple times.
   * This behavior varies depending on the Android level API.
   */
  public fun launchMultiplePermissionRequest()

  /**
   * Open the app settings page.
   *
   * If the first request permission in [permissions] is [android.Manifest.permission.POST_NOTIFICATIONS] then
   * the notification settings will be opened. Otherwise the app's settings will be opened.
   *
   * This should always be triggered from non-composable scope, for example, from a side-effect
   * or a non-composable callback. Otherwise, this will result in an IllegalStateException.
   */
  public fun openAppSettings()
}

/**
 * Calls [MultiplePermissionsState.openAppSettings] when
 * [MultiplePermissionsState.isAllNotGrantedPermissionsPermanentlyDenied] is `true`; otherwise calls
 * [MultiplePermissionsState.launchMultiplePermissionRequest].
 *
 * This should always be triggered from non-composable scope, for example, from a side-effect
 * or a non-composable callback. Otherwise, this will result in an IllegalStateException.
 */
@ExperimentalPermissionsApi
public fun MultiplePermissionsState.launchMultiplePermissionRequestOrAppSettings() {
  when {
    isAllNotGrantedPermissionsPermanentlyDenied -> openAppSettings()
    else -> launchMultiplePermissionRequest()
  }
}

/**
 * List of permissions granted by the user.
 */
@ExperimentalPermissionsApi
public inline val MultiplePermissionsState.grantedPermissions: List<PermissionState>
  get() = permissions.filter { it.status.isGranted }

/**
 * List of permissions not granted by the user.
 */
@ExperimentalPermissionsApi
public inline val MultiplePermissionsState.notGrantedPermissions: List<PermissionState>
  get() = permissions.filter { it.status.isNotGranted }

/**
 * List of permissions denied by the user.
 */
@ExperimentalPermissionsApi
public inline val MultiplePermissionsState.deniedPermissions: List<PermissionState>
  get() = permissions.filter { it.status.isDenied }

/**
 * List of permissions permanently denied by the user.
 */
@ExperimentalPermissionsApi
public inline val MultiplePermissionsState.permanentlyDeniedPermissions: List<PermissionState>
  get() = permissions.filter { it.status.isPermanentlyDenied }

/**
 * Returns `true` if [permission] was granted, otherwise `false`.
 *
 * If [permission] wasn't requested a [IllegalArgumentException] will be thrown.
 */
@ExperimentalPermissionsApi
public fun MultiplePermissionsState.isGranted(permission: String): Boolean =
  requireNotNull(permissions.find { it.permission == permission }) {
    "$permission is not present in the list of requested permissions"
  }.status.isGranted

/**
 * Returns `true` if [permission] was not granted, otherwise `false`.
 *
 * If [permission] wasn't requested a [IllegalArgumentException] will be thrown.
 */
@ExperimentalPermissionsApi
public fun MultiplePermissionsState.isNotGranted(permission: String): Boolean =
  requireNotNull(permissions.find { it.permission == permission }) {
    "$permission is not present in the list of requested permissions"
  }.status.isNotGranted

/**
 * Returns `true` if [permission] was denied, otherwise `false`.
 *
 * If [permission] wasn't requested a [IllegalArgumentException] will be thrown.
 */
@ExperimentalPermissionsApi
public fun MultiplePermissionsState.isDenied(permission: String): Boolean =
  requireNotNull(permissions.find { it.permission == permission }) {
    "$permission is not present in the list of requested permissions"
  }.status.isDenied

/**
 * Returns `true` if [permission] was permanently denied, otherwise `false`.
 *
 * If [permission] wasn't requested a [IllegalArgumentException] will be thrown.
 */
@ExperimentalPermissionsApi
public fun MultiplePermissionsState.isPermanentlyDenied(permission: String): Boolean =
  requireNotNull(permissions.find { it.permission == permission }) {
    "$permission is not present in the list of requested permissions"
  }.status.isPermanentlyDenied
