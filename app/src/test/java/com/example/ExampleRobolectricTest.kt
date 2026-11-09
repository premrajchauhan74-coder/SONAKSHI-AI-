package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.PlaceholderAiRepository
import com.example.data.local.AssistantConfig
import com.example.data.local.PhoneActionType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name from context matches Sonakshi AI`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Sonakshi AI", appName)
    }

    @Test
    fun `placeholder ai repository parses phone call and alarm commands`() = runTest {
        val repo = PlaceholderAiRepository()
        val config = AssistantConfig(assistantName = "Sonakshi")

        val callReply = repo.generateAssistantResponse("Call 9876543210", config)
        assertEquals(PhoneActionType.DIAL_PHONE, callReply.detectedAction)
        assertEquals("9876543210", callReply.actionTarget)
        assertTrue(callReply.isPlaceholderSample)

        val alarmReply = repo.generateAssistantResponse("Set alarm for 6:30 am", config)
        assertEquals(PhoneActionType.SET_ALARM, alarmReply.detectedAction)
        assertEquals("6:30", alarmReply.actionTarget)
    }
}
