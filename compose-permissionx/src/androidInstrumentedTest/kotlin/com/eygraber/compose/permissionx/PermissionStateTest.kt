package com.eygraber.compose.permissionx

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.filters.SdkSuppress
import androidx.test.rule.GrantPermissionRule
import com.eygraber.compose.permissionx.test.EmptyPermissionsTestActivity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

/**
 * Simple tests that prove the data comes from the right place
 */
@OptIn(ExperimentalPermissionsApi::class)
@SdkSuppress(minSdkVersion = 23)
class PermissionStateTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<EmptyPermissionsTestActivity>()

  @get:Rule
  val permissionRule: GrantPermissionRule =
    GrantPermissionRule.grant("android.permission.CAMERA")

  @Test
  fun permissionState_hasPermission() {
    composeTestRule.setContent {
      val state = rememberPermissionState(android.Manifest.permission.CAMERA)
      assertThat(state.status.isGranted).isTrue()
    }
  }

  @Test
  fun permissionTest_shouldShowRationale() {
    val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
    composeTestRule.activity.shouldShowRequestPermissionRationale = mapOf(
      permission to true
    )

    composeTestRule.setContent {
      val state = rememberPermissionState(permission)

      assertThat(state.status.isDenied).isTrue()
    }
  }
}
