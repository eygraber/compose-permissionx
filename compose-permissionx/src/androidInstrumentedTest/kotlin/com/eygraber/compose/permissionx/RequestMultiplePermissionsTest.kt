package com.eygraber.compose.permissionx

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.filters.SdkSuppress
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalPermissionsApi::class)
@SdkSuppress(minSdkVersion = 27) // Flaky on 26
class RequestMultiplePermissionsTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent { ComposableUnderTest() }
  }

  @Test
  fun permissionTest_grantPermissions() {
    composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    grantPermissionInDialog() // Grant first permission
    grantPermissionInDialog() // Grant second permission
    composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
  }

  @Test
  fun permissionTest_denyOnePermission() {
    composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    grantPermissionInDialog() // Grant first permission
    denyPermissionInDialog() // Deny second permission
    composeTestRule.onNodeWithText("Denied").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    grantPermissionInDialog() // Grant second permission
    if(Build.VERSION.SDK_INT == 23) { // API 23 shows all permissions again
      grantPermissionInDialog()
    }

    composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
  }

  @Test
  fun permissionTest_doNotAskAgainPermission() {
    composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    grantPermissionInDialog() // Grant first permission
    denyPermissionInDialog() // Deny second permission
    composeTestRule.onNodeWithText("Denied").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()

    if(Build.VERSION.SDK_INT == 23) { // API 23 shows all permissions again
      grantPermissionInDialog()
    }
    doNotAskAgainPermissionInDialog() // Do not ask again second permission

    composeTestRule.onNodeWithText("PermanentlyDenied").assertIsDisplayed()
    composeTestRule.onNodeWithText("Open App Settings").assertIsDisplayed()
  }

  @Test
  fun permissionTest_grantInTheBackground() {
    composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    grantPermissionInDialog() // Grant first permission
    denyPermissionInDialog() // Deny second permission
    composeTestRule.onNodeWithText("Denied").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()

    if(Build.VERSION.SDK_INT == 23) { // API 23 shows all permissions again
      grantPermissionInDialog()
    }
    doNotAskAgainPermissionInDialog() // Do not ask again second permission
    composeTestRule.onNodeWithText("PermanentlyDenied").assertIsDisplayed()
    composeTestRule.onNodeWithText("Open App Settings").assertIsDisplayed()

    // This simulates the user going to the Settings screen and granting both permissions.
    // This is cheating, I know, but the order in which the system request the permissions
    // is unpredictable. Therefore, we need to grant both to make this test deterministic.
    grantPermissionProgrammatically("android.permission.CAMERA")
    grantPermissionProgrammatically("android.permission.ACCESS_FINE_LOCATION")
    simulateAppComingFromTheBackground(composeTestRule)
    composeTestRule.activityRule.scenario.onActivity {
      it.setContent { ComposableUnderTest() }
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
  }

  @Composable
  private fun ComposableUnderTest() {
    val state = rememberMultiplePermissionsState(
      listOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.CAMERA
      )
    )
    if(state.isAllPermissionsGranted) {
      Text("Granted")
    }
    else {
      Column {
        val textToShow = when {
          state.isAllNotGrantedPermissionsPermanentlyDenied -> "PermanentlyDenied"
          state.isNotRequested -> "No permission"
          else -> "Denied"
        }

        val buttonLabel = when {
          state.isAllNotGrantedPermissionsPermanentlyDenied -> "Open App Settings"
          else -> "Request"
        }

        Text(textToShow)
        Button(onClick = { state.launchMultiplePermissionRequestOrAppSettings() }) {
          Text(buttonLabel)
        }
      }
    }
  }
}
