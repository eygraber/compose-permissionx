package com.eygraber.compose.permissionx.test

import androidx.activity.ComponentActivity

class EmptyPermissionsTestActivity : ComponentActivity() {

  var shouldShowRequestPermissionRationale: Map<String, Boolean> = emptyMap()

  override fun shouldShowRequestPermissionRationale(permission: String) = when(permission) {
    in shouldShowRequestPermissionRationale.keys -> shouldShowRequestPermissionRationale[permission]!!
    else -> super.shouldShowRequestPermissionRationale(permission)
  }
}
