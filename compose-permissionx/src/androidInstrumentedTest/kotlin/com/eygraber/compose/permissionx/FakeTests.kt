package com.eygraber.compose.permissionx

import androidx.test.filters.SdkSuppress
import org.junit.Test

/**
 * Fake tests to avoid the "No tests found error" when running in Build.VERSION.SDK_INT < 23
 */
class FakeTests {
  @SdkSuppress(maxSdkVersion = 22)
  @Test
  fun fakeTestToAvoidNoTestsFoundErrorInAPI22AndBelow() = Unit

  // More Fake tests to help with sharding: https://github.com/android/android-test/issues/973
  @Test
  fun fake1() = Unit

  @Test
  fun fake2() = Unit

  @Test
  fun fake3() = Unit

  @Test
  fun fake4() = Unit

  @Test
  fun fake5() = Unit

  @Test
  fun fake6() = Unit

  @Test
  fun fake7() = Unit

  @Test
  fun fake8() = Unit

  @Test
  fun fake9() = Unit
}
