plugins {
  id("com.eygraber.conventions-kotlin-multiplatform")
  id("com.eygraber.conventions-android-library")
  id("com.eygraber.conventions-compose")
  id("com.eygraber.conventions-detekt")
  id("com.eygraber.conventions-publish-maven-central")
}

android {
  namespace = "com.eygraber.compose.permissionx"

  defaultConfig {
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    // The following argument makes the Android Test Orchestrator run its
    // "pm clear" command after each test invocation. This command ensures
    // that the app's state is completely cleared between tests.
    testInstrumentationRunnerArguments["clearPackageData"] = "true"

    testInstrumentationRunnerArguments["numShards"] = "2"

    System.getenv("ANDROID_TEST_INSTRUMENTATION_NUM_SHARDS")?.let { numShards ->
      testInstrumentationRunnerArguments["numShards"] = numShards
    }

    System.getenv("ANDROID_TEST_INSTRUMENTATION_SHARD_INDEX")?.let { shardIndex ->
      testInstrumentationRunnerArguments["shardIndex"] = shardIndex
    }
  }

  lint {
    // skip vital checks for assemble tasks since CI runs lintRelease
    checkReleaseBuilds = false
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
    animationsDisabled = true
    execution = "ANDROIDX_TEST_ORCHESTRATOR"
    targetSdk = libs.versions.android.sdk.target.get().toInt()
  }

  // packaging {
  //   // Exclude license files to enable the test APK to build (has no effect on the AARs)
  //   resources {
  //     excludes += listOf("/META-INF/AL2.0", "/META-INF/LGPL2.1")
  //   }
  // }
}

kotlin {
  kmpTargets(
    target = KmpTarget.Android,
    project = project,
    webOptions = KmpTarget.WebOptions(
      isNodeEnabled = true,
      isBrowserEnabled = true,
    ),
  )

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
