# Compose PermissionX

[![Download](https://img.shields.io/maven-central/v/com.eygraber.permissionx/compose-permissionx/0.0.1)](https://search.maven.org/artifact/com.eygraber.permissionx/compose-permissionx)

### Gradle

```kotlin
repositories {
  mavenCentral()
}

implementation("com.eygraber.permissionx:compose-permissionx:0.3.0")
```

Snapshots can be found [here](https://central.sonatype.org/publish/publish-portal-snapshots/#consuming-via-gradle).

### Motivation

Have you ever struggled with providing a good UX to your users when it comes time to ask them for permissions?
Do you get the sense that Android makes it really difficult to do this?

If so, you're on to something. Requesting permission in Android is not as straightforward as it should be. With the
best of intentions around user safety, a monster was created. Unfortunately, the complicated nature of requesting
permission from users usually results in a lot of frustration and corner cutting that leads to a poor UX, and a loss
of trust from our users.

Through very careful state management, it is possible to get it right, but it comes with a lot of frustration.
[Accompanist Permissions](https://google.github.io/accompanist/permissions/) goes a long way to solving that
frustration, and that is why Compose PermissionX is built on top of it.

However, there are a couple of issues that Accompanist won't (or can't solve):

1. `rememberPermissionState` breaks Compose Preview

    - [An issue](https://github.com/google/accompanist/issues/1498) exists, but was closed,
[delegating the responsibility](https://issuetracker.google.com/issues/267227895) for the fix to the
`findActivity` API. While that might be the correct place to fix this issue, devs have had to deal with this for years,
with no movement towards a fix.

    - Compose PermissionX solves this by simply making sure `findActivity` isn't called while in a preview.

2. The default state of the permission before a request is made
3. Detecting whether a permission was permanently denied

Both of these issues have the same underlying cause, namely the Android permissions framework doesn't want you to
have this information (allegedly for user safety, and so that it isn't simple to send users to the App Settings
to grant the permission). It looks unlikely that this will ever
change<sup>[1](https://github.com/google/accompanist/issues/1066)</sup>
<sup>[2](https://github.com/google/accompanist/issues/1300)</sup>
<sup>[3](https://github.com/google/accompanist/pull/990)</sup>, and that's why Compose PermissionX is here.

### Handling Canceled Permission Requests

One particularly tricky scenario occurs when a permission request is canceled. This can happen when:
- The user taps outside the permission dialog
- The system quickly denies the request for some reason
- The permission was previously permanently denied in a prior app session

In these cases, Android returns a denied result without showing the rationale flag (`shouldShowRationale = false`).
This makes it **impossible to distinguish** between:
1. A genuine permanent denial
2. A canceled request

Compose PermissionX solves this with a hybrid approach:

#### First Request: Always Denied

The first request will always transition to `Denied`, even if the underlying system status indicates permanent denial.
This gives the consumer a chance to show rationale and request again:

```
NotRequested → Denied
```

#### Subsequent Requests: Timing Heuristic

After the first request, Compose PermissionX uses a **timing threshold** to distinguish between cancellations and
permanent denials:

- If the result comes back **faster than 135ms** (configurable via `cancellationThreshold`), the system likely
didn't show a dialog (true permanent denial) → `PermanentlyDenied`
- If the result takes **longer than 135ms**, the user likely saw and dismissed the dialog (cancellation) → `Denied`

```
Denied → (fast result) → PermanentlyDenied
Denied → (slow result) → Denied (can request again)
```

This threshold is configurable:

```kotlin
val permissionState = rememberPermissionState(
  permission = android.Manifest.permission.CAMERA,
  cancellationThreshold = 200.milliseconds, // custom threshold
)
```

The default of 135ms was chosen because it's slightly longer than typical system response times for pre-denied
permissions, while being short enough that user interactions (even quick taps) will exceed it.

### Usage

Most of the API mirrors Accompanist Permissions.

For single permission requests:

```kotlin
@Composable
fun FeatureThatRequiresCameraPermission() {
  val cameraPermissionState = rememberPermissionState(
    android.Manifest.permission.CAMERA
  )

  when(val status = cameraPermissionState.status) {
    PermissionStatus.Granted -> FeatureWithGrantedCameraPermission()
    PermissionStatus.NotGranted -> {
      val message = when(status) {
        PermissionStatus.NotGranted.NotRequested ->
          "Camera permission required for this feature to be available. Please grant the permission."
        
        PermissionStatus.NotGranted.Denied ->
          "The camera is important for this app. Please grant the permission."
        
        PermissionStatus.NotGranted.PermanentlyDenied ->
          "This feature can't be used without granting the camera permission. Please grant it in the app's settings."
      }
      
      val buttonLabel = when(status) {
        PermissionStatus.NotGranted.PermanentlyDenied -> "Open App Settings"
        else -> "Request Permission"
      }
      
      Column {
        Text(message)

        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
          onClick = {
            // alternatively call cameraPermissionState.launchPermissionRequestOrAppSettings()
            when(status) {
              PermissionStatus.NotGranted.PermanentlyDenied -> cameraPermissionState.openAppSettings()
              else -> cameraPermissionState.launchPermissionRequest()
            }
          }
        ) {
          Text(buttonLabel)
        }
      }
    }
  }
}
```

For multiple permission requests:

```kotlin
@Composable
fun FeatureThatRequiresCameraAndRecordAudioPermission() {
  val multiplePermissionsState = rememberMultiplePermissionsState(
    android.Manifest.permission.CAMERA,
    android.Manifest.permission.RECORD_AUDIO,
  )

  when {
    multiplePermissionsState.isAllPermissionsGranted -> FeatureWithGrantedCameraAndRecordAudioPermission()
    else -> {
      Text(multiplePermissionsState.getTextForDeniedPermissions())

      Spacer(modifier = Modifier.height(8.dp))

      val buttonLabel = when {
        multiplePermissionsState.isAllNotGrantedPermissionsPermanentlyDenied -> "Open App Settings"
        else -> "Request Permissions"
      }
      
      Button(
        onClick = {
          // alternatively call multiplePermissionsState.launchMultiplePermissionRequestOrAppSettings()
          when {
            multiplePermissionsState.isAllNotGrantedPermissionsPermanentlyDenied ->
              multiplePermissionsState.openAppSettings()
            
            else ->
              multiplePermissionsState.launchMultiplePermissionRequest()
          }   
        }
      ) {
        Text(buttonLabel)
      }
    }
  }
}

private fun MultiplePermissionsState.getTextForDeniedPermissions() = buildString {
  if(isNotRequested) {
    append(
      "Camera and record audio permissions are required for this feature to be available. Please grant the permissions."
    )
  }
  else {
    val isCameraPermanentlyDenied = isPermanentlyDenied(android.Manifest.permission.CAMERA)
    val isRecordAudioPermanentlyDenied = isPermanentlyDenied(android.Manifest.permission.RECORD_AUDIO)

    if(isCameraPermanentlyDenied && isRecordAudioPermanentlyDenied){
      append("This feature can't be used without granting the camera and record audio permissions. ")
      append("Please grant them in the app's settings.")
    }
    else {
      val isCameraDenied = isDenied(android.Manifest.permission.CAMERA)
      val isRecordAudioDenied = isDenied(android.Manifest.permission.RECORD_AUDIO)

      append("The ")

      if(isCameraDenied && isRecordAudioDenied) append("camera and record audio permissions are ")
      else if(isCameraDenied) append("camera permission is ")
      else if(isRecordAudioDenied) append("record audio permission is ")

      append("important for this app. Please grant the ")
      if(isCameraDenied && isRecordAudioDenied) append("permissions.")
      else append("permission.")
    }
  }
}
```

### KMP

Currently, this library just supports Android. If there is ever a need to solve a platform specific issue on other
platforms it can get added here. It is not a goal of Compose PermissionX to be a general use case multiplatform
permission solution.
