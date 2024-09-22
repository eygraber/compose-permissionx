package com.eygraber.compose.permissionx

import androidx.compose.runtime.Immutable
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@OptIn(ExperimentalPermissionsApi::class)
@Immutable
internal class PreviewPermissionState(
  override val permission: String
) : PermissionState {
  override val status: PermissionStatus = PermissionStatus.NotGranted.NotRequested
  override fun launchPermissionRequest() {}
  override fun openAppSettings() {}
}
