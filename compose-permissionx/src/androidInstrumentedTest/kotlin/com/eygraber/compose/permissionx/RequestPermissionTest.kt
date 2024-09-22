package com.eygraber.compose.permissionx

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
import androidx.test.filters.FlakyTest
import androidx.test.filters.SdkSuppress
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalPermissionsApi::class)
@SdkSuppress(minSdkVersion = 23)
class RequestPermissionTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent { ComposableUnderTest() }
  }

  @Test
  fun permissionTest_grantPermission() {
    composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    grantPermissionInDialog()
    composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
  }

  @Test
  fun permissionTest_denyPermission() {
    composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    denyPermissionInDialog()
    composeTestRule.onNodeWithText("Denied").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    grantPermissionInDialog()
    composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
  }

  @Test
  fun permissionTest_doNotAskAgainPermission() {
    composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    denyPermissionInDialog()
    composeTestRule.onNodeWithText("Denied").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    doNotAskAgainPermissionInDialog()
    composeTestRule.onNodeWithText("PermanentlyDenied").assertIsDisplayed()
  }

  @SdkSuppress(minSdkVersion = 29) // Flaky below
  @FlakyTest
  @Test
  fun permissionTest_grantInTheBackground() {
    composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    denyPermissionInDialog()
    composeTestRule.onNodeWithText("Denied").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    doNotAskAgainPermissionInDialog()
    composeTestRule.onNodeWithText("PermanentlyDenied").assertIsDisplayed()

    // This simulates the user going to the Settings screen and granting the permission
    grantPermissionProgrammatically(android.Manifest.permission.CAMERA)
    simulateAppComingFromTheBackground(composeTestRule)
    composeTestRule.activityRule.scenario.onActivity {
      it.setContent { ComposableUnderTest() }
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
  }

  @Composable
  private fun ComposableUnderTest() {
    val state = rememberPermissionState(android.Manifest.permission.CAMERA)
    when(val status = state.status) {
      PermissionStatus.Granted -> {
        Text("Granted")
      }

      is PermissionStatus.NotGranted -> {
        Column {
          when(status) {
            PermissionStatus.NotGranted.Denied -> {
              Text("Denied")
              Button(onClick = { state.launchPermissionRequest() }) {
                Text("Request")
              }
            }

            PermissionStatus.NotGranted.NotRequested -> {
              Text("No permission")
              Button(onClick = { state.launchPermissionRequest() }) {
                Text("Request")
              }
            }

            PermissionStatus.NotGranted.PermanentlyDenied -> {
              Text("PermanentlyDenied")
              Button(onClick = { state.openAppSettings() }) {
                Text("Open App Settings")
              }
            }
          }
        }
      }
    }
  }
}
