package com.healthpocket

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.healthpocket", appContext.packageName)
    }

    @Test
    fun appNameIsCorrect() {
        val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val appName = appContext.getString(R.string.app_name)
        assertEquals("HealthPocket", appName)
    }

    @Test
    fun appResourcesAreAccessible() {
        val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        
        // Test that key string resources are accessible
        assertNotNull(appContext.getString(R.string.app_name))
        assertNotNull(appContext.getString(R.string.login))
        assertNotNull(appContext.getString(R.string.nav_home))
        assertNotNull(appContext.getString(R.string.nav_medications))
        assertNotNull(appContext.getString(R.string.nav_appointments))
        assertNotNull(appContext.getString(R.string.nav_journal))
        assertNotNull(appContext.getString(R.string.nav_profile))
    }

    @Test
    fun appPackageNameIsCorrect() {
        val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val packageName = appContext.packageName
        assertEquals("com.healthpocket", packageName)
        assertTrue(packageName.isNotEmpty())
    }

    @Test
    fun appContextIsNotNull() {
        val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
        assertNotNull("App context should not be null", appContext)
        assertNotNull("Application context should not be null", appContext.applicationContext)
    }
}