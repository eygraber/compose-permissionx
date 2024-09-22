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
class MultiplePermissionsStateTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<EmptyPermissionsTestActivity>()

  @get:Rule
  val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
    "android.permission.CAMERA",
    "android.permission.ACCESS_FINE_LOCATION"
  )

  @Test
  fun permissionState_hasPermission() {
    composeTestRule.setContent {
      val state = rememberMultiplePermissionsState(
        listOf(
          android.Manifest.permission.ACCESS_FINE_LOCATION,
          android.Manifest.permission.CAMERA
        )
      )

      assertThat(state.isAllPermissionsGranted).isTrue()
    }
  }

  @Test
  fun permissionTest_shouldShowRationale() {
    composeTestRule.activity.shouldShowRequestPermissionRationale = mapOf(
      android.Manifest.permission.WRITE_EXTERNAL_STORAGE to true
    )

    composeTestRule.setContent {
      val state = rememberMultiplePermissionsState(
        listOf(
          android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
          android.Manifest.permission.ACCESS_FINE_LOCATION,
          android.Manifest.permission.CAMERA
        )
      )

      assertThat(state.isAllPermissionsGranted).isFalse()
      assertThat(state.isAllNotGrantedPermissionsPermanentlyDenied).isFalse()
      assertThat(state.permissions).hasSize(3)
      assertThat(state.deniedPermissions).hasSize(1)
      assertThat(state.deniedPermissions[0].permission)
        .isEqualTo("android.permission.WRITE_EXTERNAL_STORAGE")
    }
  }
}
