package com.cinavault.android.agent

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The agent's photo face, ported from CinaVault Premium's portraitAvatar.ts so
 * the phone head moves like the desktop one. The owner's photo is laid over a
 * grid whose points carry jaw, eyelid, brow and depth weights worked out from
 * three markers (both eyes and the mouth). Everything here is pure math.
 */
data class PortraitPoint(val x: Double, val y: Double)

data class PortraitCalibration(
    val leftEye: PortraitPoint,
    val rightEye: PortraitPoint,
    val mouth: PortraitPoint,
)

data class PortraitPose(
    val yaw: Double = 0.0,
    val pitch: Double = 0.0,
    val roll: Double = 0.0,
    val blink: Double = 0.0,
    val brow: Double = 0.0,
    val mouth: Double = 0.0,
)

enum class AgentMood { Idle, Listening, Thinking, Speaking, Error }

/** Depth, jaw, eyelid, brow, lip gap and head mask for one photo point, each 0..1. */
data class PortraitWeights(
    val depth: Double,
    val jaw: Double,
    val lid: Double,
    val brow: Double,
    val gap: Double,
    val head: Double,
)

data class FaceFrame(
    val origin: PortraitPoint,
    val across: PortraitPoint,
    val down: PortraitPoint,
    val eyeDistance: Double,
    val mouthDistance: Double,
)

data class PortraitView(val center: PortraitPoint, val height: Double)

object PortraitMath {
    const val JAW_DROP = 0.14
    const val LID_TRAVEL = 0.07
    const val BROW_LIFT = 0.045
    const val PARALLAX = 0.12

    fun faceFrame(cal: PortraitCalibration, aspect: Double = 1.0): FaceFrame {
        val ex = (cal.rightEye.x - cal.leftEye.x) * aspect
        val ey = cal.rightEye.y - cal.leftEye.y
        val eyeDistance = hypot(ex, ey)
        val across = if (eyeDistance > 0) PortraitPoint(ex / eyeDistance, ey / eyeDistance) else PortraitPoint(1.0, 0.0)
        val down = PortraitPoint(-across.y, across.x)
        val origin = PortraitPoint((cal.leftEye.x + cal.rightEye.x) / 2, (cal.leftEye.y + cal.rightEye.y) / 2)
        val mouthDistance = (cal.mouth.x - origin.x) * aspect * down.x + (cal.mouth.y - origin.y) * down.y
        return FaceFrame(origin, across, down, eyeDistance, mouthDistance)
    }

    /** Why a set of markers can't drive the face, or null when it can. Same rules as the desktop. */
    fun calibrationError(cal: PortraitCalibration, aspect: Double = 1.0): String? {
        val points = listOf(cal.leftEye, cal.rightEye, cal.mouth)
        if (points.any { !it.x.isFinite() || !it.y.isFinite() || it.x !in 0.0..1.0 || it.y !in 0.0..1.0 }) {
            return "Keep the markers on the photo."
        }
        if (cal.leftEye.x >= cal.rightEye.x) return "The left eye marker must be left of the right eye marker."
        val frame = faceFrame(cal, aspect)
        if (frame.eyeDistance < 0.04) return "Move the eye markers apart, onto each eye."
        if (frame.mouthDistance < frame.eyeDistance * 0.45) return "Move the mouth marker down onto the mouth."
        if (frame.mouthDistance > frame.eyeDistance * 2.5) return "The mouth marker is too far from the eyes."
        return null
    }

    private fun smoothstep(edge0: Double, edge1: Double, value: Double): Double {
        val t = ((value - edge0) / (edge1 - edge0)).coerceIn(0.0, 1.0)
        return t * t * (3 - 2 * t)
    }

    private fun gauss(value: Double, width: Double): Double = exp(-(value * value) / (2 * width * width))

    fun weights(cal: PortraitCalibration, u: Double, v: Double, aspect: Double = 1.0): PortraitWeights =
        weights(faceFrame(cal, aspect), u, v, aspect)

    fun weights(f: FaceFrame, u: Double, v: Double, aspect: Double): PortraitWeights {
        val d = f.eyeDistance
        val m = f.mouthDistance
        val dx = (u - f.origin.x) * aspect
        val dy = v - f.origin.y
        val a = dx * f.across.x + dy * f.across.y
        val b = dx * f.down.x + dy * f.down.y

        val ex = a / (d * 1.05)
        val ey = (b - m * 0.4) / (m * 1.55)
        val inside = 1 - (ex * ex + ey * ey)
        var depth = if (inside > 0) sqrt(inside) * smoothstep(0.0, 0.25, inside) else 0.0
        depth += 0.35 * gauss(a, d * 0.16) * gauss(b - m * 0.55, m * 0.3) * smoothstep(0.0, 0.3, inside)
        depth = min(1.0, depth)

        val t = (b - m) / m
        val jaw = smoothstep(-0.04, 0.06, t) * (1 - smoothstep(0.55, 0.85, t)) * gauss(a, d * 0.62)

        var lid = 0.0
        var brow = 0.0
        for (side in intArrayOf(-1, 1)) {
            val ea = a - side * d * 0.5
            val above = -b
            lid = max(lid, gauss(ea, d * 0.15) * smoothstep(-0.06 * d, 0.0, above) * (1 - smoothstep(0.05 * d, 0.13 * d, above)))
            brow = max(brow, gauss(ea, d * 0.26) * gauss(above - 0.24 * d, 0.1 * d))
        }

        val gap = gauss(a, d * 0.2) * gauss(t, 0.03)

        val ha = a / (d * 1.4)
        val hb = (b - m * 0.2) / (m * 2.05)
        val neck = gauss(a, d * 0.55) * smoothstep(m * 1.0, m * 1.6, b) * (1 - smoothstep(m * 2.2, m * 2.8, b))
        val head = max(1 - smoothstep(0.75, 1.05, hypot(ha, hb)), neck)
        return PortraitWeights(depth, jaw, lid, brow, gap, head)
    }

    /** Where one photo point lands (in photo coordinates) for a pose. */
    fun deform(f: FaceFrame, w: PortraitWeights, u: Double, v: Double, pose: PortraitPose, aspect: Double): PortraitPoint {
        val down = w.jaw * pose.mouth * JAW_DROP * f.mouthDistance +
            w.lid * pose.blink * LID_TRAVEL * f.eyeDistance -
            w.brow * pose.brow * BROW_LIFT * f.eyeDistance
        var x = (u - f.origin.x) * aspect + f.down.x * down
        var y = v - f.origin.y + f.down.y * down
        x += sin(pose.yaw) * w.depth * PARALLAX
        y += sin(pose.pitch) * w.depth * PARALLAX
        val c = cos(pose.roll)
        val s = sin(pose.roll)
        return PortraitPoint((x * c - y * s) / aspect + f.origin.x, x * s + y * c + f.origin.y)
    }

    fun deform(cal: PortraitCalibration, u: Double, v: Double, pose: PortraitPose, aspect: Double = 1.0): PortraitPoint {
        val f = faceFrame(cal, aspect)
        return deform(f, weights(f, u, v, aspect), u, v, pose, aspect)
    }

    /** Deterministic blinking: a quick blink every few seconds, sometimes twice. */
    /** Power-of-two decode sample size that brings the photo's longer side down to about [maxSide]. */
    fun decodeSampleSize(width: Int, height: Int, maxSide: Int): Int {
        var sample = 1
        while (maxOf(width, height) / (sample * 2) >= maxSide) sample *= 2
        return sample
    }

    fun blinkAt(seconds: Double): Double {
        val period = 3.6
        val cycle = floor(seconds / period)
        val hash = abs(sin(cycle * 12.9898) * 43758.5453) % 1
        val local = seconds - cycle * period - hash * 1.4
        fun one(at: Double): Double {
            val p = (local - at) / 0.16
            return if (p < 0 || p > 1) 0.0 else 1 - abs(p * 2 - 1)
        }
        return max(one(0.0), if (hash > 0.7) one(0.3) else 0.0)
    }

    fun pose(mood: AgentMood, seconds: Double, mouth: Double = 0.0, reduceMotion: Boolean = false): PortraitPose {
        if (reduceMotion) {
            val brow = when (mood) {
                AgentMood.Listening -> 0.5
                AgentMood.Error -> -0.6
                else -> 0.0
            }
            return PortraitPose(brow = brow)
        }
        val swayYaw = sin(seconds * 0.5) * 0.05
        val swayPitch = sin(seconds * 0.37) * 0.025
        val blink = blinkAt(seconds)
        return when (mood) {
            AgentMood.Listening -> PortraitPose(swayYaw, swayPitch, 0.05, blink, 0.55, 0.0)
            AgentMood.Thinking -> PortraitPose(
                yaw = sin(seconds * 1.1) * 0.08,
                pitch = -0.06 + sin(seconds * 0.8) * 0.015,
                roll = sin(seconds * 0.6) * 0.02,
                blink = max(0.3, blink),
                brow = 0.3,
            )
            AgentMood.Speaking -> PortraitPose(
                yaw = swayYaw,
                pitch = swayPitch + sin(seconds * 5) * 0.025 * mouth,
                blink = blink,
                brow = 0.15 + mouth * 0.25,
                mouth = mouth.coerceIn(0.0, 1.0),
            )
            AgentMood.Error -> PortraitPose(sin(seconds * 3) * 0.04, 0.03, 0.0, blink, -0.7, 0.0)
            AgentMood.Idle -> PortraitPose(swayYaw, swayPitch, 0.0, blink, 0.0, 0.0)
        }
    }

    /** Frames the face so a full-body or off-centre photo still shows a close-up. */
    fun view(cal: PortraitCalibration, aspect: Double = 1.0): PortraitView {
        val f = faceFrame(cal, aspect)
        val m = max(f.mouthDistance, 1e-3)
        val offset = m * 0.75
        return PortraitView(
            center = PortraitPoint(f.origin.x + f.down.x * offset / aspect, f.origin.y + f.down.y * offset),
            height = min(1.5, m * 5.4),
        )
    }

    /**
     * A synthetic speech envelope for text the phone shows rather than speaks:
     * syllable-rate jaw movement that settles between words.
     */
    fun speechMouth(seconds: Double): Double {
        val syllable = (sin(seconds * 13.0) + 1) / 2
        val phrase = (sin(seconds * 2.3) + 1) / 2
        return (0.25 + 0.75 * syllable) * (0.45 + 0.55 * phrase)
    }
}
