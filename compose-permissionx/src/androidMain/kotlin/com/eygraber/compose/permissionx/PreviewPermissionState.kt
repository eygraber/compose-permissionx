package com.eygraber.compose.permissionx

import androidx.compose.runtime.Immutable
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalPermissionsApi::class)
@Immutable
internal class PreviewPermissionState(
  override val permission: String,
  override val status: PermissionStatus,
) : PermissionState {
  override fun launchPermissionRequest() {}
  override fun openAppSettings() {}
}
