package com.cinavault.android

import com.cinavault.android.agent.AgentMood
import com.cinavault.android.agent.PortraitCalibration
import com.cinavault.android.agent.PortraitMath
import com.cinavault.android.agent.PortraitMesh
import com.cinavault.android.agent.PortraitPoint
import com.cinavault.android.agent.PortraitPose
import com.cinavault.android.data.AgentStatus
import com.cinavault.android.data.AgentUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PortraitMathTest {
    private val face = PortraitCalibration(
        leftEye = PortraitPoint(0.4, 0.4),
        rightEye = PortraitPoint(0.6, 0.4),
        mouth = PortraitPoint(0.5, 0.6),
    )

    @Test
    fun neutralPoseLeavesThePhotoAlone() {
        for (point in listOf(PortraitPoint(0.1, 0.1), PortraitPoint(0.5, 0.6), PortraitPoint(0.45, 0.4))) {
            val moved = PortraitMath.deform(face, point.x, point.y, PortraitPose())
            assertEquals(point.x, moved.x, 1e-9)
            assertEquals(point.y, moved.y, 1e-9)
        }
    }

    @Test
    fun openMouthDropsTheChinButNotTheEyes() {
        val open = PortraitPose(mouth = 1.0)
        val chin = PortraitMath.deform(face, 0.5, 0.66, open)
        assertTrue("chin moves down", chin.y > 0.66 + 0.01)
        val eye = PortraitMath.deform(face, 0.4, 0.4, open)
        assertEquals(0.4, eye.y, 1e-3)
    }

    @Test
    fun blinkLowersTheLidsAndBrowsLift() {
        val lid = PortraitMath.deform(face, 0.4, 0.39, PortraitPose(blink = 1.0))
        assertTrue(lid.y > 0.39)
        val brow = PortraitMath.deform(face, 0.4, 0.352, PortraitPose(brow = 1.0))
        assertTrue(brow.y < 0.352)
    }

    @Test
    fun turningMovesTheNoseMoreThanTheBackground() {
        val turn = PortraitPose(yaw = 0.2)
        val nose = PortraitMath.deform(face, 0.5, 0.5, turn)
        val corner = PortraitMath.deform(face, 0.02, 0.02, turn)
        assertTrue(nose.x - 0.5 > 0.01)
        assertEquals(0.02, corner.x, 1e-6)
    }

    @Test
    fun markersAreValidatedLikeTheDesktop() {
        assertNull(PortraitMath.calibrationError(face))
        assertNotNull(PortraitMath.calibrationError(face.copy(leftEye = PortraitPoint(0.7, 0.4))))
        assertNotNull(PortraitMath.calibrationError(face.copy(mouth = PortraitPoint(0.5, 0.42))))
        assertNotNull(PortraitMath.calibrationError(face.copy(mouth = PortraitPoint(0.5, 1.2))))
        // On a tall photo the same markers are judged in true proportions.
        val tall = PortraitCalibration(PortraitPoint(0.45, 0.2), PortraitPoint(0.55, 0.2), PortraitPoint(0.5, 0.25))
        assertNull(PortraitMath.calibrationError(tall, aspect = 1000.0 / 1500.0))
    }

    @Test
    fun blinksStayInRangeAndHappen() {
        var closed = 0
        var t = 0.0
        while (t < 30) {
            val blink = PortraitMath.blinkAt(t)
            assertTrue(blink in 0.0..1.0)
            if (blink > 0.9) closed++
            t += 0.01
        }
        assertTrue(closed > 0)
    }

    @Test
    fun moodsShapeThePose() {
        assertTrue(PortraitMath.pose(AgentMood.Listening, 1.0).brow > 0.4)
        assertTrue(PortraitMath.pose(AgentMood.Thinking, 1.0).blink >= 0.3)
        assertTrue(PortraitMath.pose(AgentMood.Error, 1.0).brow < 0)
        assertEquals(0.8, PortraitMath.pose(AgentMood.Speaking, 1.0, mouth = 0.8).mouth, 1e-9)
        val still = PortraitMath.pose(AgentMood.Speaking, 3.0, mouth = 1.0, reduceMotion = true)
        assertEquals(0.0, still.yaw, 0.0)
        assertEquals(0.0, still.mouth, 0.0)
        for (step in 0..200) {
            assertTrue(PortraitMath.speechMouth(step * 0.05) in 0.0..1.0)
        }
    }

    @Test
    fun fullBodyPhotosAreFramedAsACloseUp() {
        val body = PortraitCalibration(PortraitPoint(0.46, 0.2), PortraitPoint(0.54, 0.2), PortraitPoint(0.5, 0.25))
        val view = PortraitMath.view(body, aspect = 1000.0 / 1500.0)
        assertTrue(view.height < 0.5)
        assertTrue(view.center.y in 0.2..0.35)
        assertEquals(0.5, view.center.x, 1e-9)
    }

    @Test
    fun meshFillsTheGridAndFadesTheBackground() {
        val mesh = PortraitMesh(face, aspect = 1.0, cols = 8, rows = 8)
        assertEquals(81, mesh.vertexCount)
        val vertices = FloatArray(mesh.vertexCount * 2)
        mesh.vertices(PortraitPose(), 100f, 200f, vertices)
        assertEquals(0f, vertices[0], 1e-4f)
        assertEquals(100f, vertices[16], 1e-4f)
        assertEquals(200f, vertices[vertices.size - 1], 1e-4f)
        val colors = IntArray(mesh.vertexCount)
        mesh.colors(PortraitPose(), colors)
        val cornerAlpha = colors[0] ushr 24
        val centre = 4 * 9 + 4
        val centreAlpha = colors[centre] ushr 24
        assertTrue(cornerAlpha < 80)
        assertEquals(255, centreAlpha)
    }

    @Test
    fun agentStateExplainsWhyItCannotAnswer() {
        assertNotNull(AgentUiState().unavailableReason)
        val off = AgentStatus(enabled = false, configured = true, hasFace = false, messagesPerHour = 40)
        assertTrue(AgentUiState(status = off).unavailableReason!!.contains("turned off"))
        val noKey = off.copy(enabled = true, configured = false)
        assertTrue(AgentUiState(status = noKey).unavailableReason!!.contains("API key"))
        assertNull(AgentUiState(status = noKey.copy(configured = true)).unavailableReason)
    }

    @Test
    fun repliesSpeakAtReadingPaceAndFaceUrlsAreChecked() {
        assertEquals(1_200L, AgentUiState.speakingMillis("Hi"))
        assertEquals(9_000L, AgentUiState.speakingMillis("x".repeat(1_000)))
        assertEquals("AAAA", AgentUiState.dataUrlPayload("data:image/jpeg;base64,AAAA"))
        assertNull(AgentUiState.dataUrlPayload("https://example.com/face.jpg"))
        assertNull(AgentUiState.dataUrlPayload("data:image/jpeg;base64,"))
    }
}
