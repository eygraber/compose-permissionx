import com.android.build.api.dsl.androidLibrary

plugins {
  id("com.eygraber.conventions-kotlin-multiplatform")
  id("com.eygraber.conventions-android-kmp-library")
  id("com.eygraber.conventions-compose")
  id("com.eygraber.conventions-detekt2")
  id("com.eygraber.conventions-publish-maven-central")
}

kotlin {
  kmpTargets(
    target = KmpTarget.Android,
    project = project,
    webOptions = KmpTarget.WebOptions(
      isNodeEnabled = true,
      isBrowserEnabled = true,
    ),
    androidNamespace = "com.eygraber.compose.permissionx",
  )

  androidLibrary {
    lint {
      // skip vital checks for assemble tasks since CI runs lintRelease
      checkReleaseBuilds = false
    }

    withHostTest {
      isIncludeAndroidResources = true
    }

    withDeviceTest {
      animationsDisabled = true
      execution = "ANDROIDX_TEST_ORCHESTRATOR"
      instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
      // The following argument makes the Android Test Orchestrator run its
      // "pm clear" command after each test invocation. This command ensures
      // that the app's state is completely cleared between tests.
      instrumentationRunnerArguments["clearPackageData"] = "true"

      instrumentationRunnerArguments["numShards"] = "2"

      System.getenv("ANDROID_TEST_INSTRUMENTATION_NUM_SHARDS")?.let { numShards ->
        instrumentationRunnerArguments["numShards"] = numShards
      }

      System.getenv("ANDROID_TEST_INSTRUMENTATION_SHARD_INDEX")?.let { shardIndex ->
        instrumentationRunnerArguments["shardIndex"] = shardIndex
      }

      targetSdk {
        release(
          libs.versions.android.sdk.target.get().toInt()
        )
      }
    }
  }

  sourceSets {
    androidMain.dependencies {
      implementation(libs.accompanist.permissions)
      implementation(libs.compose.runtime)
      implementation(libs.compose.ui)
    }

    androidInstrumentedTest.dependencies {
      implementation(libs.androidx.activity.compose)

      implementation(libs.androidx.test.runner)
      implementation(libs.androidx.test.rules)
      implementation(libs.androidx.test.uiAutomator)

      implementation(libs.junit)
      implementation(libs.truth)

      implementation(libs.compose.foundation)
      implementation(libs.compose.material3)
      implementation(libs.compose.ui.test.junit4)
      implementation(libs.compose.ui.test.manifest)
    }
  }
}

dependencies {
  androidTestUtil(libs.androidx.test.orchestrator)
}
