package com.eygraber.compose.permissionx

import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.filters.FlakyTest
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.eygraber.compose.permissionx.test.PermissionsTestActivity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalPermissionsApi::class)
@FlakyTest(detail = "https://github.com/google/accompanist/issues/490")
@SdkSuppress(minSdkVersion = 23)
class MultipleAndSinglePermissionsTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private val instrumentation by lazy {
    InstrumentationRegistry.getInstrumentation()
  }

  private val uiDevice by lazy {
    UiDevice.getInstance(instrumentation)
  }

  @Test
  fun singlePermission_granted() {
    composeTestRule.setContent {
      ComposableUnderTest(listOf(android.Manifest.permission.CAMERA))
    }

    composeTestRule.onNodeWithText("MultipleAndSinglePermissionsTest").assertIsDisplayed()
    composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    grantPermissionInDialog()
    composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
    composeTestRule.onNodeWithText("Navigate").performClick()
    instrumentation.waitForIdleSync()
    composeTestRule.onNodeWithText("PermissionsTestActivity").assertIsDisplayed()
    composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
  }

  @Test
  fun singlePermission_deniedAndGrantedInSecondActivity() {
    composeTestRule.setContent {
      ComposableUnderTest(listOf(android.Manifest.permission.CAMERA))
    }

    composeTestRule.onNodeWithText("MultipleAndSinglePermissionsTest").assertIsDisplayed()
    composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    denyPermissionInDialog()
    composeTestRule.onNodeWithText("Denied").assertIsDisplayed()
    composeTestRule.onNodeWithText("Navigate").performClick()
    instrumentation.waitForIdleSync()
    composeTestRule.onNodeWithText("PermissionsTestActivity").assertIsDisplayed()
    composeTestRule.onNodeWithText("Denied").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    grantPermissionInDialog()
    composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
    uiDevice.pressBack()
    instrumentation.waitForIdleSync()
    composeTestRule.onNodeWithText("MultipleAndSinglePermissionsTest").assertIsDisplayed()
    composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
  }

  @Test
  fun singlePermission_deniedAndGrantedInFirstActivity() {
    composeTestRule.setContent {
      ComposableUnderTest(listOf(android.Manifest.permission.CAMERA))
    }

    composeTestRule.onNodeWithText("MultipleAndSinglePermissionsTest").assertIsDisplayed()
    composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    denyPermissionInDialog()
    composeTestRule.onNodeWithText("Denied").assertIsDisplayed()
    composeTestRule.onNodeWithText("Navigate").performClick()
    instrumentation.waitForIdleSync()
    composeTestRule.onNodeWithText("PermissionsTestActivity").assertIsDisplayed()
    composeTestRule.onNodeWithText("Denied").assertIsDisplayed()
    uiDevice.pressBack()
    instrumentation.waitForIdleSync()
    composeTestRule.onNodeWithText("MultipleAndSinglePermissionsTest").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    grantPermissionInDialog()
    composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
    composeTestRule.onNodeWithText("Navigate").performClick()
    instrumentation.waitForIdleSync()
    composeTestRule.onNodeWithText("PermissionsTestActivity").assertIsDisplayed()
    composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
  }

  @Test
  fun singlePermission_deniedAndGrantedInFirstActivity_singlePermission() {
    composeTestRule.setContent {
      ComposableUnderTest(
        listOf(android.Manifest.permission.CAMERA),
        requestSinglePermission = true
      )
    }

    composeTestRule.onNodeWithText("MultipleAndSinglePermissionsTest").assertIsDisplayed()
    composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    denyPermissionInDialog()
    composeTestRule.onNodeWithText("Denied").assertIsDisplayed()
    composeTestRule.onNodeWithText("Navigate").performClick()
    instrumentation.waitForIdleSync()
    composeTestRule.onNodeWithText("PermissionsTestActivity").assertIsDisplayed()
    composeTestRule.onNodeWithText("Denied").assertIsDisplayed()
    uiDevice.pressBack()
    instrumentation.waitForIdleSync()
    composeTestRule.onNodeWithText("MultipleAndSinglePermissionsTest").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    grantPermissionInDialog()
    composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
    composeTestRule.onNodeWithText("Navigate").performClick()
    instrumentation.waitForIdleSync()
    composeTestRule.onNodeWithText("PermissionsTestActivity").assertIsDisplayed()
    composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
  }

  @Test
  fun multiplePermissions_granted() {
    composeTestRule.setContent {
      ComposableUnderTest(
        listOf(
          android.Manifest.permission.ACCESS_FINE_LOCATION,
          android.Manifest.permission.CAMERA
        )
      )
    }

    composeTestRule.onNodeWithText("MultipleAndSinglePermissionsTest").assertIsDisplayed()
    composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    grantPermissionInDialog() // Grant first permission
    grantPermissionInDialog() // Grant second permission
    composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
    composeTestRule.onNodeWithText("Navigate").performClick()
    instrumentation.waitForIdleSync()
    composeTestRule.onNodeWithText("PermissionsTestActivity").assertIsDisplayed()
    composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
  }

  @Test
  fun multiplePermissions_denied() {
    composeTestRule.setContent {
      ComposableUnderTest(
        listOf(
          android.Manifest.permission.ACCESS_FINE_LOCATION,
          android.Manifest.permission.CAMERA
        )
      )
    }

    composeTestRule.onNodeWithText("MultipleAndSinglePermissionsTest").assertIsDisplayed()
    composeTestRule.onNodeWithText("No permission").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    denyPermissionInDialog() // Deny first permission
    denyPermissionInDialog() // Deny second permission
    composeTestRule.onNodeWithText("Denied").assertIsDisplayed()
    composeTestRule.onNodeWithText("Navigate").performClick()
    instrumentation.waitForIdleSync()
    composeTestRule.onNodeWithText("PermissionsTestActivity").assertIsDisplayed()
    composeTestRule.onNodeWithText("Denied").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    grantPermissionInDialog() // Grant the permission
    composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
    uiDevice.pressBack()
    instrumentation.waitForIdleSync()
    composeTestRule.onNodeWithText("MultipleAndSinglePermissionsTest").assertIsDisplayed()
    composeTestRule.onNodeWithText("Request").performClick()
    grantPermissionInDialog() // only one permission to grant now
    if(Build.VERSION.SDK_INT == 23) { // API 23 shows all permissions again
      grantPermissionInDialog()
    }

    composeTestRule.onNodeWithText("Granted").assertIsDisplayed()
  }

  @Composable
  private fun ComposableUnderTest(
    permissions: List<String>,
    requestSinglePermission: Boolean = false
  ) {
    val state = rememberMultiplePermissionsState(permissions)
    Column {
      Text("MultipleAndSinglePermissionsTest")
      Spacer(Modifier.height(16.dp))
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
          Button(
            onClick = {
              when {
                requestSinglePermission && state.deniedPermissions.size == 1 ->
                  state.deniedPermissions[0].launchPermissionRequest()

                else -> state.launchMultiplePermissionRequest()
              }
            }
          ) {
            Text(buttonLabel)
          }
        }
      }
      Spacer(Modifier.height(16.dp))
      Button(
        onClick = {
          composeTestRule.activity.startActivity(
            Intent(composeTestRule.activity, PermissionsTestActivity::class.java)
          )
        }
      ) {
        Text("Navigate")
      }
    }
  }
}
