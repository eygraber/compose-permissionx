package com.eygraber.compose.permissionx

public sealed interface PermissionStatus {
  public data object Granted : PermissionStatus

  public sealed interface NotGranted : PermissionStatus {
    public data object NotRequested : NotGranted
    public data object Denied : NotGranted
    public data object PermanentlyDenied : NotGranted
  }

  public val isGranted: Boolean get() = this == Granted
  public val isNotGranted: Boolean get() = this is NotGranted
  public val isNotRequested: Boolean get() = this == NotGranted.NotRequested
  public val isDenied: Boolean get() = this == NotGranted.Denied
  public val isPermanentlyDenied: Boolean get() = this == NotGranted.PermanentlyDenied
}
