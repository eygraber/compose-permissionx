package com.eygraber.compose.permissionx

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

internal fun Context.openAppSettings(permission: String?) {
  if(permission == android.Manifest.permission.POST_NOTIFICATIONS) {
    if(Build.VERSION.SDK_INT >= 33) {
      findActivity().startActivity(
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
          putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
      )
    }
  }
  else {
    findActivity().startActivity(
      Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", packageName, null)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
      }
    )
  }
}

/**
 * Find the closest Activity in a given Context.
 */
private fun Context.findActivity(): Activity {
  var context = this
  while (context is ContextWrapper) {
    if (context is Activity) return context
    context = context.baseContext
  }
  throw IllegalStateException("Permissions should be called in the context of an Activity")
}
