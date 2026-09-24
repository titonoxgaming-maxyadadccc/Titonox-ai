package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.api.AIResponseParser
import com.example.ai.api.SecureApiStorage
import com.example.ai.tools.RegisteredTools
import com.example.floating.OrbController
import com.example.floating.OrbDimension
import com.example.floating.OrbIntentRouter
import com.example.floating.OrbThemeType
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TitonoxOrbAndApiTest {

    private lateinit var context: Context
    private lateinit var controller: OrbController
    private lateinit var router: OrbIntentRouter

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        controller = OrbController.getInstance(context)
        router = OrbIntentRouter(context, controller)
    }

    @Test
    fun testVoiceOrbRotationCommandsInHindiAndEnglish() {
        controller.resetRotation()
        assertEquals(0f, controller.rotationY.value, 0.01f)

        // 1. "Orb ko right ghumao"
        val rightResult = router.handleCommand("Orb ko right ghumao")
        assertTrue("Hindi right rotation should be handled", rightResult.handled)
        assertTrue("RotationY should increase", controller.rotationY.value > 0f)

        // 2. "Orb ko left ghumao"
        val prevY = controller.rotationY.value
        val leftResult = router.handleCommand("Orb ko left ghumao")
        assertTrue("Hindi left rotation should be handled", leftResult.handled)
        assertTrue("RotationY should decrease", controller.rotationY.value < prevY)

        // 3. "Orb ko upar ghumao"
        val upResult = router.handleCommand("Orb ko upar ghumao")
        assertTrue("Hindi up rotation should be handled", upResult.handled)

        // 4. "Orb ko neeche ghumao"
        val downResult = router.handleCommand("Orb ko neeche ghumao")
        assertTrue("Hindi down rotation should be handled", downResult.handled)

        // 5. English: "Rotate the orb"
        val engRotateResult = router.handleCommand("Rotate the orb")
        assertTrue("English rotate should be handled", engRotateResult.handled)

        // 6. "Reset orb" / "Orb reset karo"
        val resetResult = router.handleCommand("Orb reset karo")
        assertTrue("Reset should be handled", resetResult.handled)
        assertEquals(0f, controller.rotationX.value, 0.01f)
        assertEquals(0f, controller.rotationY.value, 0.01f)
    }

    @Test
    fun testVoiceOrbSelectionAndModeSwitching() {
        // "3D orb lagao"
        val mode3d = router.handleCommand("3D orb lagao")
        assertTrue("Mode 3D switch should be handled", mode3d.handled)
        assertEquals(OrbDimension.THREE_D, controller.activeDimension)

        // "2D orb lagao"
        val mode2d = router.handleCommand("2D orb lagao")
        assertTrue("Mode 2D switch should be handled", mode2d.handled)
        assertEquals(OrbDimension.TWO_D, controller.activeDimension)

        // "Next orb" and "Pichhla orb"
        val nextResult = router.handleCommand("Next orb")
        assertTrue("Next orb should be handled", nextResult.handled)

        val prevResult = router.handleCommand("Pichhla orb")
        assertTrue("Previous orb should be handled", prevResult.handled)

        // Name match: "Plasma sphere lagao"
        val nameSelect = router.handleCommand("Plasma sphere lagao")
        assertTrue("Named selection should be handled", nameSelect.handled)
        assertEquals(OrbThemeType.PLASMA_SPHERE, controller.activeTheme)
    }

    @Test
    fun testAIResponseParserStructuredToolExtraction() {
        val aiOutputWithTool = """
            Sure sir, rotating the orb 45 degrees to the right.
            ```json
            {
              "tool": "orb.rotate",
              "parameters": {
                "axis": "Y",
                "amount": 45
              }
            }
            ```
        """.trimIndent()

        val parsed = AIResponseParser.parse(aiOutputWithTool)
        assertEquals(1, parsed.toolCalls.size)
        val tool = parsed.toolCalls[0]
        assertEquals("orb.rotate", tool.toolName)
        assertEquals("Y", tool.parameters.getString("axis"))
        assertEquals(45, tool.parameters.getInt("amount"))
        assertFalse("JSON should be cleaned from spoken text", parsed.userFacingText.contains("```json"))
    }

    @Test
    fun testRegisteredToolsSecurityRegistry() {
        assertTrue(RegisteredTools.isRegistered("orb.rotate"))
        assertTrue(RegisteredTools.isRegistered("orb.select"))
        assertTrue(RegisteredTools.isRegistered("orb.customize"))
        assertTrue(RegisteredTools.isRegistered("orb.reset"))
        assertFalse(RegisteredTools.isRegistered("execute_arbitrary_shell_command"))

        val validParams = JSONObject().put("axis", "Y").put("amount", 90)
        assertTrue(RegisteredTools.validateParameters("orb.rotate", validParams))

        val invalidParams = JSONObject().put("invalid", 123)
        assertFalse(RegisteredTools.validateParameters("orb.rotate", invalidParams))
    }

    @Test
    fun testSecureApiStorageDefaults() {
        val storage = SecureApiStorage.getInstance(context)
        val config = storage.loadConfig()
        assertNotNull(config)
        assertEquals("gemini-2.0-flash", config.model)
        assertTrue(config.streamingEnabled)
    }
}
