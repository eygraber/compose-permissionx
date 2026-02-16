package com.eygraber.compose.permissionx

import com.eygraber.compose.permissionx.PermissionStatus.Granted
import com.eygraber.compose.permissionx.PermissionStatus.NotGranted
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

public sealed interface PermissionStatus {
  public data object Granted : PermissionStatus

  public sealed interface NotGranted : PermissionStatus {
    public data object NotRequested : NotGranted
    public data object Denied : NotGranted
    public data object PermanentlyDenied : NotGranted
  }
}

@OptIn(ExperimentalContracts::class)
public fun PermissionStatus.isGranted(): Boolean {
  contract {
    returns(true) implies (this@isGranted is Granted)
  }

  return this == Granted
}

@OptIn(ExperimentalContracts::class)
public fun PermissionStatus.isNotGranted(): Boolean {
  contract {
    returns(true) implies (this@isNotGranted is NotGranted)
  }

  return this is NotGranted
}

@OptIn(ExperimentalContracts::class)
public fun PermissionStatus.isNotRequested(): Boolean {
  contract {
    returns(true) implies (this@isNotRequested is NotGranted.NotRequested)
  }

  return this == NotGranted.NotRequested
}

@OptIn(ExperimentalContracts::class)
public fun PermissionStatus.isDenied(): Boolean {
  contract {
    returns(true) implies (this@isDenied is NotGranted.Denied)
  }

  return this == NotGranted.Denied
}

@OptIn(ExperimentalContracts::class)
public fun PermissionStatus.isPermanentlyDenied(): Boolean {
  contract {
    returns(true) implies (this@isPermanentlyDenied is NotGranted.PermanentlyDenied)
  }
  return this == NotGranted.PermanentlyDenied
}
